<?php
/**
Increments view count of image which is passed by uploadtool.js in mysql db
*/

$document = json_decode($_GET['document']);
$url = $document->url;

$inifile = "$_SERVER[DOCUMENT_ROOT]/../conf/config.ini";
$ini = parse_ini_file($inifile);
$servername = $ini['mysqlServer'];
$username = $ini['mysqlUser'];
$password = $ini['mysqlPassword'];
$dbname = $ini['mysqlDB'];

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);
// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$sql = "INSERT INTO `viewedimages` (HASH, URL, VIEWS)
VALUES (\"" . hash("md5", $url) . "\", \"" . $url . "\", 1) ON DUPLICATE KEY UPDATE VIEWS = VIEWS + 1";

$conn->query($sql);

$conn->close();
?>