<?php

if (array_key_exists('src', $_GET)) {
    $imgloc = $_GET['src'];
    $imageData = file_get_contents('https://osl.tib.eu/noa/pictures/' . $imgloc);
} else {
    $imageData = file_get_contents('img/noa.png');
}


$im = imagecreatefromstring($imageData);

if ($im !== false) {
    header('Content-Type: image/png');
    imagejpeg($im);
    imagedestroy($im);
} else {
    echo 'An error occurred.';
}
?>