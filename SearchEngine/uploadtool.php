<?php include ("navbar.php"); ?>

<?php
/**
Landing page for OAuth authorization
*/

include 'OAuthFunctions.php';

// Fetch the access token if this is the callback from requesting authorization
if (isset($_GET['oauth_verifier']) && $_GET['oauth_verifier']) {
    fetchAccessToken();
}

$res = doApiQuery(array(
    'format' => 'json',
    'action' => 'query',
    'meta' => 'userinfo',
        ), $ch, "");

if (isset($res->error->code) && $res->error->code === 'mwoauth-invalid-authorization') {
    // We're not authorized!
    header("Location: upload.php");
}
if (isset($res->query->userinfo)) {
    $wikiUsername = $res->query->userinfo->name;
}
if (isset($res->query->userinfo->anon)) {
    header("HTTP/1.1 $errorCode Internal Server Error");
    echo 'Not logged in. (How did that happen?)';
    exit(0);
}
?>

<div class="main">
    <div class="container" style="width:1000px">  
        <br>
        <font size="-1">You are now logged in as <b><?php print_r($wikiUsername); ?></b>.<br>
        To revoke access go to settings in your Wikimedia account.<br>
        You can close this tab and continue uploading images.
    </div>
</div>
</div><!-- #wrapper closes -->
<?php include ("footer.php"); ?>