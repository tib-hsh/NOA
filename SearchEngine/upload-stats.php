<?php
/**
This page displays the images that have been uploaded to Wikimedia Commons via the NOA Upload Tool
*/

$inifile = 'data/config.ini';
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

$sql = "SELECT URL, UPLOADDATE FROM `viewedimages` WHERE UPLOADED = 1 ORDER BY UPLOADDATE DESC";

$res = $conn->query($sql);

$conn->close();

while ($row = mysqli_fetch_assoc($res)) {
    echo $row['UPLOADDATE'] . " | " . $row['URL'] . "<br>";
}


