<?php
header("Content-Type: text/html;charset=UTF-8");

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

class SolrQuery2 extends SolrQuery {

    public function logIP($ipaddress) {
        $this->addParam('clientip', $ipaddress);
    }

}

function get_client_ip() {
    $ipaddress = '';
    if (isset($_SERVER['HTTP_CLIENT_IP']))
        $ipaddress = $_SERVER['HTTP_CLIENT_IP'];
    else if (isset($_SERVER['HTTP_X_FORWARDED_FOR']))
        $ipaddress = $_SERVER['HTTP_X_FORWARDED_FOR'];
    else if (isset($_SERVER['HTTP_X_FORWARDED']))
        $ipaddress = $_SERVER['HTTP_X_FORWARDED'];
    else if (isset($_SERVER['HTTP_FORWARDED_FOR']))
        $ipaddress = $_SERVER['HTTP_FORWARDED_FOR'];
    else if (isset($_SERVER['HTTP_FORWARDED']))
        $ipaddress = $_SERVER['HTTP_FORWARDED'];
    else if (isset($_SERVER['REMOTE_ADDR']))
        $ipaddress = $_SERVER['REMOTE_ADDR'];
    else
        $ipaddress = 'UNKNOWN';
    return preg_replace('/[0-9]+\z/', '0', $ipaddress);
}

$start = (array_key_exists('start', $_GET) ? $_GET['start'] : 0);
$suchTerm = (array_key_exists('suchbegriff', $_GET) ? $_GET['suchbegriff'] : '*');
if (($suchTerm == '' || $suchTerm == '*') && $start == 0) {
    //$suchTerm  = '*';
    header("Location: index.php");
    die();
}
$facetImtype = (array_key_exists('imtype', $_GET) ? $_GET['imtype'] : null);
$facetDiscipline = (array_key_exists('discipline', $_GET) ? $_GET['discipline'] : null);
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
$query = new SolrQuery2();
$query->logIP(get_client_ip());
$query->setQuery($suchTerm);

// results per page can be changed by user
$results_per_page = !empty($_COOKIE['results_per_page']) ? intval($_COOKIE['results_per_page']) : 12;
$query->setRows($results_per_page);

$query->setStart($start);


$query->set('spellcheck', 'true');
$query->setFacet(true);

$query->addFacetField('{!ex=imt,dsc}imtype')->setFacetMinCount(2);
if ($facetImtype != null) {
    $query->addFilterQuery('{!tag=imt}imtype:"' . $facetImtype . '"');
}
$query->addFacetField('{!ex=dsc,imt}discipline')->setFacetMinCount(2);
if ($facetDiscipline != null) {
    $query->addFilterQuery('{!tag=dsc}discipline:"' . $facetDiscipline . '"');
}
$query_response = $client->query($query);
$response = $query_response->getResponse();
$nrOfResults = $response->response->numFound;



if (array_key_exists("collation", $response->spellcheck->collations)) {
    $correct = $response->spellcheck->collations["collation"];
    $correct = trim($correct, " ()\t\n\r\0\x0B");
} else {
    $correct = "";
}
$suggest = array();

if (strpos($suchTerm, ':') == false) {
    foreach ($response->spellcheck->suggestions as $term => $infarray) {
        $alts = $infarray['suggestion'];
        foreach ($alts as $alt) {
            array_push($suggest, $alt);
        }
    }
}

// Get synonyms for local MySQL DB
try {
    $servername = $ini['mysqlServer'];
    $username = $ini['mysqlUser'];
    $password = $ini['mysqlPassword'];
    $dbname = $ini['mysqlDB'];


    // Create connection
    @$conn = new mysqli($servername, $username, $password, $dbname);


    if (!$conn->connect_error) {
        //mysql_query("set names 'utf8'");
        //mysql_set_charset('utf8', $conn);
        $conn->set_charset("utf8");
        $termlist = preg_split("/[\s,]+/", $suchTerm);
        for ($i = 0; $i < count($termlist); $i++) {
            if (strpos($termlist[$i], ':') == false) {
                $sql = "SELECT suggestion FROM synonyms WHERE term = '" . $termlist[$i] . "'";
                $sqlresult = $conn->query($sql);

                if ($sqlresult->num_rows > 0) {
                    while ($row = $sqlresult->fetch_assoc()) {
                        array_push($suggest, $row['suggestion']);
                    }
                }
                $sqlresult->free();
            }
        }
    }

    @$conn->close();
} catch (Exception $e) {
    ;
}
?>

<?php include ("navbar.php"); ?>

<div class="main">
    <div class="container">
        <!-- filter-bar start -->
        <?php include ("filter.php"); ?>
        <!-- filter-bar end -->

        <?php
        if ($nrOfResults == 0) {
            ?>
            <div class="misspelled">No results!
                <?php
                if ($correct != "") {
                    ?>
                    Did you mean
                    <form action="search.php" method="get">
                        <input type="hidden" value="<?php print_r($correct) ?>" name="suchbegriff">
                        <input type ="hidden" name="start" value="0">
                        <button type="submit" class="noa-button search-suggest"><?php print_r($correct) ?>?</button>
                    </form>
                </div>
                <?php
            }
            ?>
            <!-- misspelled end -->

        <?php } else { ?>		

            <!-- results start
            
            solution for selectable view may not be optimal, but: 
            both views are always there, one view is in each case hidden.
            "view-list" and "view-gallery".
            
            you can't just change the css class to change the view, 
            because each view has a different html-structure.
            
            for further information see also settings.js
            
            please take note: each view needs his own feedback.php to make sure 
            the right feedback-modal is shown and to read the right textbox for sending the feedback-mail. 
            
            -->

            <!-- suggestions for similar terms -->
            <?php
            if ($nrOfResults != 0 && count($suggest) > 0) {
                ?>
                <div class="suggest-alternative">
                    <label class="alternatives">Alternative search terms:</label>
                    <?php
                    for ($i = 0; $i < count($suggest); $i++) {
                        ?>
                        <form action="search.php" method="get" style="float:left;">
                            <input type="hidden" value="<?php print_r($suggest[$i]) ?>" name="suchbegriff">
                            <input type ="hidden" name="start" value="0">
                            <button type="submit" class="term-suggest"><?php print_r($suggest[$i]) ?></button>
                        </form>
                        <?php
                    }
                    ?>	
                </div>
                <?php
            }
            ?>	


            <!-- Category information for category search-->
            <?php
            $suchmuster = '/^cat:\"?([^\"]+)\"?$/';
            if (preg_match($suchmuster, $suchTerm, $treffer) == 1) {
                $wpcategory = $treffer[1];
                ?>
                <div class="categoryinfo" id='wikitext'> </div>
                <script src="js/jquery-3.2.1.min.js"></script>
                <script src="js/wikipedia.js"></script>
                <script type="text/javascript">
                    var placeholder = document.getElementById('wikitext')
                    wikipedia('<?php print_r($wpcategory) ?>', placeholder);
                </script>
                <?php
            }
            ?>

            <!-- list view start -->
            <div class="loading-screen">
                <div class="spinner">
                    <div class="double-bounce1"></div>
                    <div class="double-bounce2"></div>
                </div>
                <p class="loading-caption">Fetching results! This might take a few seconds.</p>
            </div>

            <?php include ("view-list.php"); ?>
            <!-- list view end -->

            <!-- gallery view start -->
        </div><!-- .container closes for wider view -->

        <?php include ("view-gallery.php"); ?>

        <div class="container"><!-- container opens again for narrower view -->
            <!-- gallery view end -->

            <!-- results end -->


            <!-- browse start -->
            <?php include ("browse.php"); ?>
            <!-- browse end -->

            <?php
        }
        ?>

    </div><!-- .container closes -->
</div><!-- .main closes -->

</div><!-- #wrapper closes -->

<dialog id="redirectDialog">
    <form method="dialog">
        <p><label>
                To upload images to Wikimedia Commons you need to authorize the NOA Upload tool to upload in your name. 
                <br>Do you wish to continue with the authorization?
            </label></p>
        <menu>
            <button class="noa-button" id="confirmButton" value="confirm">Confirm</button>
            <button class="noa-button" value="cancel">Cancel</button>
        </menu>
    </form>
</dialog>
<dialog id="reloadDialog">
    <form method="dialog">
        <p><label>
                To continue with the upload you need to reload the page.
            </label></p>
        <menu>
            <button class="noa-button" value="confirm">Confirm</button>      
        </menu>
    </form>
</dialog>

<?php include ("footer.php"); ?>