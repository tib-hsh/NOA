<?php
include 'OAuthFunctions.php';
$ajax = false;
if (isset($_GET['ajax'])) {
    $ajax = true;
}

$link = doAuthorizationRedirect();

//if ajax call return redirection url and don't display page
if ($ajax) {
    echo $link;
    return;
} else {
    ?>
    <?php include ("navbar.php"); ?>
    <div class="main">

        <div class="container">


            <h1 class="h3">NOA Upload Tool</h1>
            <br>
            Welcome to the NOA Upload Tool. <br>
            This tool will show you random images from Open Access articles. You can choose which of these images are suitable for reuse and upload them to Wikimedia Commons with a single click. Upload any images that you think should be in Wikimedia Commons. You can use a filter if you only want to see images from your preferred discipline.<br>
            <b>Attention:</b> The images will be uploaded using your account. All images come from freely licensed articles. However, some authors use images to which they don't own the rights. If an image caption indicates an external source, you should not upload it.<br><br>
            To use the NOA Upload Tool you first need to authorize it to upload to Wikimedia Commons in your name.<br> 
            Click <a target="_blank" href="<?php echo $link; ?>"><font color="blue"><u>here</u></font></a> to authorize.<br>
            If you don't want to use your Wikimedia Commons account, you can still use the tool without uploading the images to let us know which images might be interesting.<br>
            <a href="random-upload.php"><font color="blue"><u>Continue to the tool.</u></font></a><br><br>

        </div>
    </div>
    </div><!-- #wrapper closes -->

    <?php
    include ("footer.php");
}
?>

