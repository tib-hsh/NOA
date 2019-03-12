<!--List view included in search.php-->

<div class="view-list results col-md-12 col-sm-12 col-xs-12">
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
        <div class="result-row col-xs-12">
            <div class="image-column col-md-3 col-sm-3 col-xs-12">	
                <a href="<?php print_r($document->url); ?>">
                    <img alt="No preview available" src="loadimg.php?src=<?php print_r(urlencode($document->tiburl)); ?>"/>
                </a> 
                <div class="image-overlay">
                    <div class="positioning-icons">
                        <div class="large-image" role="button" data-toggle="modal" data-target="#large-image-list<?php echo $counter; ?>">
                            <span class="glyphicon glyphicon-zoom-in"></span>
                        </div>
                    </div>
                </div>

                <div class="large-image modal fade gallery-metadata-modal" id="large-image-list<?php echo $counter; ?>" tabindex="-1" role="dialog" aria-labelledby="large-image-list">
                    <div class="modal-dialog" role="document">
                        <div class="modal-content">
                            <button class="toggle-close" type="button" data-toggle="modal" data-target="#large-image-list<?php echo $counter; ?>">
                                <span class="glyphicon glyphicon-remove"></span> Close
                            </button>
                            <img class="img-responsive" alt="No preview available" src="loadimg.php?src=<?php print_r(urlencode($document->tiburl)); ?>"/>
                        </div>
                    </div>
                </div>
            </div>
            <div class="description-column col-md-9 col-sm-9 col-xs-12">
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
                    <div class='cite-doi'>
                        <?php print_r($document->doi); ?> 
                    </div>
                    <div class='cite-publisher'>
                        <?php print_r($document->publisher); ?> 
                    </div>
                </div>
                <table>
                    <tr>
                        <td class="table-res col-md-2 col-sm-2 col-xs-1">Caption:</td>
                        <td class="table-res col-md-10 col-sm-10 col-xs-2 cite-caption">
                            <?php print_r($document->caption); ?>
                        </td>
                    </tr>
                    <tr>
                        <td>Paper:</td>
                        <td class='cite-paper'>
                            <a target="_blank" href="https://doi.org/<?php print_r($document->doi); ?>">
                                <u><?php print_r(rtrim($document->title)); ?></u>
                                <span class="glyphicon glyphicon-new-window"/>
                            </a>
                        </td>
                    </tr>
                    <tr>
                        <td>Journal:</td>
                        <td class='journal'>
                            <?php print_r($document->journal); ?> 
                        </td>
                    </tr>
                    <tr>
                        <td>Year:</td>
                        <td class='year'>
                            <?php print_r($document->year); ?>
                        </td>
                    </tr>
                    <tr>
                        <td>Author:</td>
                        <td class='author'>
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
                                            <button type="submit" class="category"><u><?php print_r($document->cat[$i]) ?></u> <span class="glyphicon glyphicon-new-window"/></button>
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
                    <button type="button" class="noa-button" onclick="cite(this, 'list', 'ris')">
                        Cite (.ris)
                    </button>
                    <button type="button" class="noa-button" onclick="cite(this, 'list', 'bibtex')">		
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
                            <button id="myBtn" type="button" class="noa-button" data-toggle="modal" data-target="#large-image-upload<?php echo $counter; ?>">		
                                Upload to Wikimedia
                            </button>

                            <?php
                        }
                    }
                    ?>
                </div>
            </div>
        </div>
        <div class="large-image modal fade gallery-metadata-modal" id="large-image-upload<?php echo $counter; ?>" tabindex="-1" role="dialog" aria-labelledby="large-image-list">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <button class="toggle-close" type="button" data-toggle="modal" data-target="#large-image-upload<?php echo $counter; ?>">
                        <span class="glyphicon glyphicon-remove"></span> Close
                    </button>
                    <div class="metadata">

                        <label for="table" class="get-position-right" style="float:left;">
                            <?php
                            $id = $counter;
                            include ("upload-dialog.php");
                            ?>
                        </label>
                    </div>
                    <div class="loading" id="loading<?php echo $counter; ?>">
                        <center><img src="img/spinner.gif"/></center>
                    </div>
                </div>
            </div>
        </div>
        <?php
        $counter++;
    }
    ?>
</div>