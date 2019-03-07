<?php

if (empty($_GET)) {
    header("Location: api-documentation.php");
    exit;
}

header("Content-Type: application/json");

$inifile = "$_SERVER[DOCUMENT_ROOT]/../conf/config.ini";
$ini = parse_ini_file($inifile);

$query = (array_key_exists('query', $_GET) ? $_GET['query'] : '*');
$results = (array_key_exists('results', $_GET) ? $_GET['results'] : '10');
$start = (array_key_exists('start', $_GET) ? $_GET['start'] : '0');

if ($results > 100) {
    $results = 100;
}

$json = file_get_contents('http://' . $ini['solrHost'] . ':' . $ini['solrPort'] . $ini['solrPath'] . '/select?q=' . urlencode($query) . '&rows=' . $results . '&start=' . $start);
echo $json;
?>

