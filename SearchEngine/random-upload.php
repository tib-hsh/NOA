<?php include ("navbar.php"); ?>

<?php
/**
This page shows the user random images. The user can then upload this images to Wikimedia Commons if an account is authorized or else mark the images to be uploaded.
*/

include 'OAuthFunctions.php';

$res = doApiQuery(array(
    'format' => 'json',
    'action' => 'query',
    'meta' => 'userinfo',
        ), $ch, "");

//Check if OAuth user is connected
$wikiUsername = NULL;
if (isset($res->query->userinfo)) {
    $wikiUsername = $res->query->userinfo->name;
}

$suchTerm = "*";
$discipline = (array_key_exists('discipline', $_GET) ? $_GET['discipline'] : null);
$oauthVerifier = (array_key_exists('oauth_verifier', $_GET) ? $_GET['oauth_verifier'] : null);
$oauthToken = (array_key_exists('oauth_token', $_GET) ? $_GET['oauth_token'] : null);

//http://php.net/manual/en/solrclient.construct.php
$options = array
    (
    'hostname' => $ini['solrHost'],
    'port' => $ini['solrPort'],
    'path' => $ini['solrPath']
);

$client = new SolrClient($options);
$client->setServlet(SolrClient::SEARCH_SERVLET_TYPE, "noaselect");


// http://php.net/manual/en/class.solrquery.php
$query = new SolrQuery();

$query->setQuery($suchTerm);
$query->setRows(30);
$query->setStart(0);

$query->addFilterQuery('NOT licensetype:"unclassified" AND NOT licensetype:"frontiers" AND NOT licensetype:"cc-by-2.0"');
$query->addFilterQuery('imtype:"Photo" OR imtype:"Drawing" OR imtype:"Scan"');
$query->addFilterQuery('NOT discipline:""');
//$query->addFilterQuery('copyrightflag:"false"');
$query->addFilterQuery('caption:[* TO *] AND title:[* TO *] AND author:[* TO *]');

$query->addFacetField('{!ex=dsc,imt}discipline')->setFacetMinCount(2);
if ($discipline != null) {
    $query->addFilterQuery('{!tag=dsc}discipline:"' . $discipline . '"');
}

$query->setFacet(true);

$randString = mt_rand();
$query->addSortField('random_' . $randString, 1);

//get 30 random images
$query_response = $client->query($query);
$response = $query_response->getResponse();

$servername = $ini['mysqlServer'];
$username = $ini['mysqlUser'];
$password = $ini['mysqlPassword'];
$dbname = $ini['mysqlDB'];

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
$minViewCount = PHP_INT_MAX;

//Don't show images which contain these words
$filterList = ["study area", "experiment", "(a)", "framework", "proposed", "test", " our ", "simulation", " b "];

//choose a image which has not been viewed yet, or the image with the lowest view count
for ($i = 0; $i < count($response->response->docs); $i++) {
    $tempDocument = $response->response->docs[$i];

    $filter = false;
    foreach ($filterList as $keyword) {
        if (stripos($tempDocument->caption, $keyword) !== false) {
            $filter = true;
        }
    }
    if ($filter) {
        continue;
    }
    //filter out medical images unless this discipline was chosen by the user
    if (endsWith($tempDocument->url, ".pdf") || $tempDocument->caption == null || (in_array("Medicine", $tempDocument->discipline) && $discipline != "Medicine")) {
        continue;
    }
    $sql = "SELECT VIEWS, UPLOADED FROM `viewedimages` WHERE HASH =\"" . hash("md5", $tempDocument->url) . "\"";
    $result = $conn->query($sql);
    if (mysqli_fetch_assoc($conn->query($sql)) === null) {
        $document = $tempDocument;
        break;
    } else if (mysqli_fetch_assoc($conn->query($sql))['UPLOADED'] == 1) {
        continue;
    } else {
        $viewCount = mysqli_fetch_assoc($conn->query($sql))['VIEWS'];
        if ($viewCount < $minViewCount) {
            $document = $tempDocument;
            $minViewCount = $viewCount;
        }
    }
}
$conn->close();

if ($document === null) {
    echo "Something went wrong";
}
?>

<script src="js/uploadtool.js" type="text/javascript"></script>

<div class="main">
    <div class="container">  
        <br>
        <button class="noa-button" type="button" data-toggle="collapse" data-target="#disciplines" aria-expanded="false" aria-controls="bydiscipline">
            <span class="glyphicon glyphicon-filter"></span> Filter by discipline
        </button>
        <div class="collapse" id="disciplines" style="right:unset; top:185px">
            <button class="toggle-close" type="button" data-toggle="collapse" data-target="#disciplines" aria-controls="bydiscipline">
                <span class="glyphicon glyphicon-remove"></span> Close
            </button>
            <div class="filter-cat">
                <form class="disciplines" method=get action="random-upload.php">
                    <?php
                    if ($discipline == null) {
                        ?>
                        <button type="submit" class="linkselected">All disciplines</button>
                        <?php
                    } else {
                        ?>
                        <button type="submit">All disciplines</button>
                        <?php
                    }
                    ?>
                </form>

                <?php
                $facet_data = $response->facet_counts->facet_fields['discipline'];
                $max = 100;
                foreach ($facet_data as $dsc => $dsccount) {
                    ?>

                    <form method=get action="random-upload.php">
                        <input type="hidden" name="discipline" value="<?php print_r($dsc); ?>">

                        <?php
                        if ($discipline == $dsc) {
                            ?>

                            <button type="submit" class="linkselected"><?php print_r($dsc . " (" . $dsccount . ")"); ?></button>

                            <?php
                        } else {
                            ?>

                            <button type="submit"><?php print_r($dsc . " (" . $dsccount . ")"); ?></button>

                            <?php
                        }
                        ?>
                    </form>

                    <?php
                    $max--;
                    if ($max < 0) {
                        break;
                    }
                }
                ?>
            </div>
        </div>
        <?php if ($wikiUsername != NULL) { ?>        
            <center><font size="-1">You are logged in as <b><?php print_r($wikiUsername); ?></b>.<br> You can revoke access in your Wikimedia account or click <a href="upload.php"><font color="blue"><u>here</u></font></a> to authenticate a different account.</font><br><br></center>
        <?php } else { ?>   
            <center><font size="-1">You are using the NOA Upload Tool without a Wikimedia Commons account.<br> You can click <a href="upload.php"><font color="blue"><u>here</u></font></a> to authenticate an account.</font><br><br>
            <?php } ?>   
            <div style="float: left; min-width:300px"><a target="_blank" href="<?php print_r($document->url); ?>">
                    <img class="imageToUpload" style="padding-right:30px" alt="No preview available" src="loadimg.php?src=<?php print_r(urlencode($document->tiburl)); ?>"/>
                </a></div>
            <div><table style="margin-bottom:30px;min-width:400px;max-width:600px">
                    <tr>
                        <td class="table-res col-md-2 col-sm-2 col-xs-1" style="padding-bottom:10px;width:10%;padding-right:10px;"><b>Caption:</b></td>
                        <td class="table-res col-md-10 col-sm-10 col-xs-2 cite-caption" style="padding-bottom:10px;">
                            <?php print_r($document->caption); ?>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding-bottom:10px;"><b>Paper:</b></td>
                        <td class='cite-paper'>
                            <?php print_r($document->title); ?> (
                            <a target="_blank" href="https://doi.org/<?php print_r($document->doi); ?>">
                                <font color="blue"><u>doi.org/<?php print_r($document->doi); ?></u></font>
                                <span class="glyphicon glyphicon-new-window"/>
                            </a>
                            )
                        </td>
                    </tr>
                </table><div>
                    <?php if ($wikiUsername != NULL) { ?>        

                        <?php if (property_exists($document, "wmcat")) { ?>      

                            <fieldset style="padding-bottom:30px" class="checkboxgroup">
                                <label><b>Choose suitable categories (optional):</b></label><br>                                     
                                <?php
                                foreach ($document->wmcat as $value) {
                                    ?>
                                    <div id="ck-button">
                                        <label style="font-weight:500"><input id="<?php print_r($value); ?>" type="checkbox" hidden><span><?php print_r($value); ?></span></label>
                                    </div>
                                <?php }
                                ?>

                            </fieldset>
                        <?php } ?>
                        <div class="metadata-buttons">
                            <button id="uploadButton" type="button" class="noa-button">		
                                Upload to Wikimedia
                            </button>
                            <script>
                                var btn = document.getElementById("uploadButton");
                                btn.onclick = function () {
                                    uploadtool('uploadConfirmation', '<?php echo json_encode($document); ?>', '', null);
                                }
                            </script>
                        <?php } else { ?>   
                            <div class="metadata-buttons">
                                <button id="uploadButton" type="button" class="noa-button">		
                                    Mark for upload
                                </button>
                                <script>
                                    var btn = document.getElementById("uploadButton");
                                    btn.onclick = function () {
                                        uploadtool('markForUpload', '<?php echo json_encode($document); ?>', '', null);
                                    }
                                </script>
                            <?php } ?>  
                            <button id="nextButton" type="button" class="noa-button" >		
                                Next image
                            </button>
                            <script>
                                var btn = document.getElementById("nextButton");
                                btn.onclick = function () {
                                    uploadtool('next', '<?php echo json_encode($document); ?>', '', '<?php print_r($discipline); ?>');
                                }
                            </script>
                            <?php if ($wikiUsername != NULL) { ?>        
                                <div id="success" style="padding-top:10px;display:none"><font size="+1">Success! You can view your upload <a id="linkToImage" target="_blank" href="" ><font color="blue"><u>here</u></font></a>.</font></div>
                            <?php } else { ?>   
                                <div id="success" style="padding-top:10px;display:none"><font size="+1">Thank you! The image was marked for upload.</font></div>
                            <?php } ?>
                        </div>
                    </div>
                    <div id="loading" class="loading">
                        <center><img style="margin-top:10%" src="img/spinner.gif"/></center>
                    </div>
                </div>
            </div>
    </div>
</div><!-- #wrapper closes -->
<?php include ("footer.php"); ?>
