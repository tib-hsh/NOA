<!--Dialog for uploading images to Wikimedia Commons included in view-gallery.php and view-list.php-->


<div style="min-width:40%;float:left"><img class="img-responsive" style="float:right;margin-right:20px;height:100%;max-width:700px;max-height:600px" alt="No preview available" src="loadimg.php?src=<?php print_r(urlencode($document->tiburl)); ?>"/></div>

<table style="min-width:400px;max-width:600px;">
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
</table>
<?php if (property_exists($document, "wmcat")) { ?>
    Choose suitable categories (optional):<br>      

    <fieldset class="checkboxgroup">
        <?php
        foreach ($document->wmcat as $value) {
            ?>
            <div id="ck-button">
                <label style="font-weight:500"><input id="<?php print_r($value . $id); ?>" type="checkbox" hidden><span><?php print_r($value); ?></span></label>
            </div>
        <?php }
        ?>

    </fieldset>
<?php } ?>
<br>
<div class="metadata-buttons">
    <button id="uploadButton<?php echo $id; ?>" type="button" class="noa-button">		
        Upload to Wikimedia
    </button>
    <script>
        var btn = document.getElementById("uploadButton<?php echo $id; ?>");
        btn.onclick = function () {
            uploadtool('upload', '<?php echo str_replace("'", "\'", json_encode($document)); ?>', '<?php echo $id; ?>', null);
        }
    </script>
    <div id="success<?php echo $id; ?>" style="padding-top:10px;display:none;"><font size="+1">Success! You can view your upload <a id="linkToImage<?php echo $id; ?>" target="_blank" href="" ><font color="blue"><u>here</u></font></a>.</font></div>
</div>