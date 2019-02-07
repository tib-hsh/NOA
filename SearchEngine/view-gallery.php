<!--Gallery view included in search.php-->

<div class="view-gallery results col-md-12 col-sm-12 col-xs-12">
    <script src="js/uploadtool.js" type="text/javascript"></script>
    <?php
    $counter = 0;
    foreach ($response->response->docs as $document) {
        $uploadButton = true;
        // Only show upload button if document has all necessary fields
        if ($document->caption == NULL || $document->year == NULL || $document->month == NULL || $document->day == NULL || $document->author == NULL || $document->title == NULL || $document->journal == NULL || $document->doi == NULL || $document->license == NULL || $document->licensetype == NULL || $document->copyrightflag == "true" || $document->licensetype == "unclassified" || $document->licensetype == "cc-by-2.0" || $document->licensetype == "frontiers") {
            $uploadButton = false;
        } else {
            $servername = $ini['mysqlServer'];
            $username = $ini['mysqlUser'];
            $password = $ini['mysqlPassword'];
            $dbname = $ini['mysqlDB'];

            $conn = new mysqli($servername, $username, $password, $dbname);

            if ($conn->connect_error) {
                die("Connection failed: " . $conn->connect_error);
            }
            // Don't show upload button if image was already uploaded
            $sql = "SELECT UPLOADED FROM `viewedimages` WHERE HASH =\"" . hash("md5", $document->url) . "\"";
            $sqlresult = mysqli_fetch_assoc($conn->query($sql));
            $conn->close();
            if ($sqlresult !== null) {
                if ($sqlresult['UPLOADED'] == 1) {
                    $uploadButton = false;
                }
            }
        }
        ?>
        <div class="tile col-sm-3 col-xs-6 gallery-image">
            <img class="img-responsive" alt="No preview available" src="loadimg.php?src=<?php print_r(urlencode($document->tiburl)); ?>"/>
            <div class="image-overlay">

                <div class="positioning-icons">
                    <div class="get-position-right" role="button" data-toggle="modal" data-target="#show-large<?php echo $counter; ?>">
                        <span style="padding-left:100%" class="glyphicon glyphicon-zoom-in"></span>
                    </div>
                </div>
            </div>

            <div class="large-image modal fade gallery-metadata-modal" id="large-image<?php echo $counter; ?>" tabindex="-1" role="dialog" aria-labelledby="large-image">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <button class="toggle-close" type="button" data-toggle="modal" data-target="#large-image<?php echo $counter; ?>">
                            <span class="glyphicon glyphicon-remove"></span> Close
                        </button>
                        <img class="img-responsive" alt="No preview available" src="loadimg.php?src=<?php print_r(urlencode($document->tiburl)); ?>"/>
                    </div>
                </div>
            </div>

            <div class="modal fade gallery-metadata-modal" id="show-large<?php echo $counter; ?>" tabindex="-1" role="dialog" aria-labelledby="show-large">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <button class="toggle-close" id="closeButtonG<?php echo $counter; ?>" type="button" data-toggle="modal" data-target="#show-large<?php echo $counter; ?>">
                            <span class="glyphicon glyphicon-remove"></span> Close
                        </button>
                        <script>
                            var closeButton = document.getElementById("closeButtonG<?php echo $counter; ?>");
                            closeButton.onclick = function () {
                                document.getElementById('modal-uploadG<?php echo $counter; ?>').style.display = "none";
                                document.getElementById('backButtonG<?php echo $counter; ?>').style.display = "none";
                                document.getElementById('normal-modalG<?php echo $counter; ?>').style.display = "block";
                            }
                        </script>
                        <button class="toggle-close" id="backButtonG<?php echo $counter; ?>" style="float:left;padding-left:30px;display:none" type="button" data-target="#show-large<?php echo $counter; ?>">
                            <span class="glyphicon glyphicon-arrow-left"></span> Back
                        </button>
                        <script>
                            var backButton = document.getElementById("backButtonG<?php echo $counter; ?>");
                            backButton.onclick = function () {
                                document.getElementById('modal-uploadG<?php echo $counter; ?>').style.display = "none";
                                document.getElementById('backButtonG<?php echo $counter; ?>').style.display = "none";
                                document.getElementById('normal-modalG<?php echo $counter; ?>').style.display = "block";
                            }
                        </script>
                        <div class="metadata">

                            <label for="table" id="modal-uploadG<?php echo $counter; ?>" style="display:none" class="get-position-right" style="float:left;">
                                <?php
                                $id = "G" . $counter;
                                include ("upload-dialog.php");
                                ?>
                            </label>
                            <label for="table" id="normal-modalG<?php echo $counter; ?>" class="get-position-right" style="float:left;">
                                <div style="min-width:40%;float:left"><img class="img-responsive" style="float:right;margin-right:20px;height:100%;max-width:700px;max-height:600px" alt="No preview available" src="<?php print_r($document->url); ?>"/></div>                             
                                <div class="hiddenmetadata">
                                    <div class='cite-author'>
                                        <?php
                                        if (property_exists($document, "author")) {
                                            $cite_author = implode(' and ', $document->author);
                                            print_r($cite_author);
                                        }
                                        ?>
                                    </div>
                                    <div class='cite-paper'>
                                        <?php print_r($document->title); ?> 
                                    </div>
                                    <div class='cite-journal'>
                                        <?php print_r($document->journal); ?> 
                                    </div>
                                    <div class='cite-year'>
                                        <?php print_r($document->year); ?> 
                                    </div>
                                    <div class='cite-publisher'>
                                        <?php print_r($document->publisher); ?> 
                                    </div>
                                    <div class='cite-doi'>
                                        <?php print_r($document->doi); ?> 
                                    </div>
                                </div>

                                <table style="min-width:400px;max-width:600px">
                                    <tr>
                                        <th>Metadata</th>
                                    </tr>
                                    <tr>
                                        <td class="table-res col-md-2 col-sm-2 col-xs-1">Caption:</td>
                                        <td class="table-res col-md-10 col-sm-10 col-xs-2 cite-caption">
                                            <?php print_r($document->caption); ?>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>Paper:</td>
                                        <td class='cite-paper'>
                                            <a target="_blank" href="https://dx.doi.org/<?php print_r($document->doi); ?>">
                                                <?php print_r($document->title); ?>
                                                <span class="glyphicon glyphicon-new-window"/>
                                            </a>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>Journal:</td>
                                        <td class="cite-journal">
                                            <?php print_r($document->journal); ?>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>Year:</td>
                                        <td class="cite-year">
                                            <?php print_r($document->year); ?>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>Author:</td>
                                        <td class="cite-author">
                                            <?php
                                            if (property_exists($document, "author")) {
                                                $author = $document->author[0];
                                                if (count($document->author) > 3) {
                                                    $author = $author . " et al.";
                                                } else {
                                                    for ($i = 1; $i < count($document->author); $i++) {
                                                        $author = $author . " and " . $document->author[$i];
                                                    }
                                                }
                                                print_r($author);
                                            }
                                            ?>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>Copyright:</td>
                                        <td class='journal'>
                                            <?php print_r($document->licensetype); ?> 
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>Disciplines:</td>
                                        <td class='discipline'>
                                            <?php
                                            if (property_exists($document, "discipline")) {
                                                $discipline = $document->discipline[0];
                                                for ($i = 1; $i < count($document->discipline); $i++) {
                                                    $discipline = $discipline . ", " . $document->discipline[$i];
                                                }
                                                print_r($discipline);
                                            }
                                            ?>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding-right:10px;">Categories:</td>
                                        <td>
                                            <?php
                                            if (property_exists($document, "cat")) {
                                                for ($i = 0; $i < count($document->cat); $i++) {
                                                    ?>
                                                    <div class='category'>
                                                        <form action="search.php" method="get" style="float:left;">
                                                            <input type="hidden" value='cat:"<?php print_r($document->cat[$i]) ?>"' name="suchbegriff">
                                                            <input type ="hidden" name="start" value="0">
                                                            <button type="submit" class="category"><?php print_r($document->cat[$i]) ?>  <span class="glyphicon glyphicon-new-window"/></button>
                                                        </form>
                                                    </div>
                                                    <?php
                                                }
                                            }
                                            ?>
                                        </td>
                                    </tr>
                                </table>
                                <div class="metadata-buttons">
                                    <a href="<?php print_r($document->url); ?>" target="_blank">
                                        <button type="button" class="noa-button">Open in new tab</button>
                                    </a>                                                                                                       
                                    <button type="button" class="noa-button" onclick="cite(this, 'gallery', 'ris')">
                                        Cite (.ris)
                                    </button>
                                    <button type="button" class="noa-button" onclick="cite(this, 'gallery', 'bibtex')">
                                        Cite (BibTeX)
                                    </button>
                                    <?php
                                    if ($uploadButton) {
                                        //if no Wikimedia account is connected redirect the user for authorization, else allow user to upload images
                                        if ($wikiUsername == NULL) {
                                            ?>
                                            <button id="uploadButton" type="button" class="noa-button"  onClick="uploadtool('redirect')">		
                                                Upload to Wikimedia
                                            </button>
                                            <?php
                                        } else {
                                            ?>
                                            <button id="myBtnG<?php echo $counter; ?>" type="button" class="noa-button" data-toggle="modal">		
                                                Upload to Wikimedia
                                            </button>
                                            <script>
                                                var btn = document.getElementById("myBtnG<?php echo $counter; ?>");
                                                btn.onclick = function () {
                                                    document.getElementById('normal-modalG<?php echo $counter; ?>').style.display = "none";
                                                    document.getElementById('modal-uploadG<?php echo $counter; ?>').style.display = "block";
                                                    document.getElementById('backButtonG<?php echo $counter; ?>').style.display = "block";
                                                }
                                            </script><?php
                                        }
                                    }
                                    ?>
                                </div>
                            </label>
                        </div>
                    </div>
                </div>
                <div class="loading" id="loadingG<?php echo $counter; ?>">
                    <center><img style="margin-top:10%" src="img/spinner.gif"/></center>
                </div>
            </div>
        </div>
        <?php
        $counter++;
    }
    ?>
</div>
