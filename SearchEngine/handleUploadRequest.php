<?php
/**
 * Uploads image which is passed by uploadtool.js via the Wikimedia API
 */

include 'OAuthFunctions.php';
session_name('OAuthNOA');
$params = session_get_cookie_params();
session_set_cookie_params(
        $params['lifetime'], dirname($_SERVER['SCRIPT_NAME'])
);

$document = json_decode($_GET['document']);
$categories = $_GET['categories'];

$imgurl = 'https://osl.tib.eu/noa/pictures/' . $document->tiburl;

$caption = utf8_encode($document->caption);
$filename = getFileName($caption, $document->url);
$date = $document->year . "-" . str_pad($document->month, 2, '0', STR_PAD_LEFT) . "-" . str_pad($document->day, 2, '0', STR_PAD_LEFT);
$firstauthor = trim(explode(", ", $document->author[0])[1] . " " . explode(", ", $document->author[0])[0], "\n\t");
$authors = $firstauthor;
for ($i = 1; $i < count($document->author); $i++) {
    $authors = $authors . ", " . explode(", ", $document->author[$i])[1] . " " . explode(", ", $document->author[$i])[0];
}
$authors = trim($authors, "\n\t");
if (count($document->author) > 1) {
    $firstauthor = $firstauthor . " et. al. ";
} else {
    $firstauthor = $firstauthor . ". ";
}

$externalurl = $document->url;
$doi = $document->doi;

function isUploaded() {
    global $doi, $caption, $externalurl;
    $post = [
        'action' => 'query',
        'list' => 'search',
        'srwhat' => 'text',
        'srlimit' => '50',
        'srnamespace' => '6',
        'format' => 'json',
        'srsearch' => '"' . $doi . '"'
    ];

    $ch = curl_init('https://commons.wikimedia.org/w/api.php');
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $post);
    $response = json_decode(curl_exec($ch));
    curl_close($ch);

    //if DOI does not return any search results, assume that image was not uploaded
    if ($response->query->searchinfo->totalhits == 0) {
        return false;
    }
    $url_fragment = end(explode("/", $externalurl));
    $length = strlen($url_fragment);
    $filename_fragment = substr($url_fragment, 0, $length - 4);

    foreach ($response->query->search as $page) {
        //if a search reuslt contains the file name assume that it is the image to be uploaded
        if (strpos($page->title, $filename_fragment) !== FALSE) {
            return true;
        }
    }

    $first_sentence_of_caption = explode(".", $caption)[0];
	if (strlen($first_sentence_of_caption) == 0) {
        $first_sentence_of_caption = $caption;
    } else if (strlen($first_sentence_of_caption) < 6 && strlen($caption) >= 6) {
        $first_sentence_of_caption = substr($caption, 0, 6);;
    }
    $query_string = $doi . " " . $first_sentence_of_caption;

    $post = [
        'action' => 'query',
        'list' => 'search',
        'srwhat' => 'text',
        'srnamespace' => '6',
        'format' => 'json',
        'srsearch' => $query_string
    ];

    $ch = curl_init('https://commons.wikimedia.org/w/api.php');
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $post);
    $response = json_decode(curl_exec($ch));
    curl_close($ch);

    //if doi and first sentence of caption return one result, assume that it is the image to be uploaded
    if ($response->query->searchinfo->totalhits == 1) {
        return true;
    }
    return false;
}

if (isUploaded()) {
    echo json_encode(array("The file was already uploaded."));
} else {

    //download the file to be uploaded
    file_put_contents($filename, fopen($imgurl, 'r'));
    $cFile = curl_file_create($filename);

    $ch = null;
    $res = doApiQuery(array(
        'format' => 'json',
        'action' => 'query',
        'meta' => 'tokens'
            ), $ch, "token");

    $csrftoken = $res->query->tokens->csrftoken;

    $journal = $document->journal;
    $journalCategory = 'Media from ' . $journal;
    $res = doApiQuery(array(
        'format' => 'json',
        'action' => 'query',
        'prop' => 'categoryinfo',
        'titles' => 'Category:Media from ' . $journal . '|' . 'Images from ' . $journal
            ), $ch, null);


    //if category doesn't exist create new category with publisher as super category
    if (property_exists($res->query->pages, '-1')) {
        if (!property_exists($res->query->pages, '-2')) {
            if($res->query->pages->{'-1'}->title == "Media from " . $journal) {
                $journalCategory = 'Images from ' . $journal;
            }
        } else {
            $superCategory = '';
            if (strpos($document->publisher, 'Hindawi') !== false) {
                $superCategory = 'Media from Hindawi';
            } else if (strpos($document->publisher, 'Frontiers') !== false) {
                $superCategory = 'Media from Frontiers journals';
            } else if (strpos($document->publisher, 'Copernicus') !== false) {
                $superCategory = 'Media from Copernicus';
            } else {
                $superCategory = 'Media from scholarly journals';
            }
            $res = doApiQuery(array(
                'format' => 'json',
                'action' => 'edit',
                'title' => 'Category:Media from ' . $journal,
                'token' => $csrftoken,
                'createonly' => true,
                'text' => '{{hiddencat}}[[Category:' . $superCategory . '|' . $journal . ']]'
                    ), $ch, null);
        }
    }

    $categories = $categories . "\n[[Category:" . $journalCategory . "]]";

    $text = "=={{int:filedesc}}== \n{{Information\n|description={{en|1=" . $document->caption . "}}\n|date=" . $date . "\n|source=" . $firstauthor . "&quot;" . trim($document->title) . "&quot;, " . $document->journal . " [https://doi.org/" . $document->doi . " doi:" . $document->doi . "]\n|author=" . $authors . "\n|permission=" . $document->license . "\n|other_versions=\n}}\n=={{int:license-header}}=={{" . $document->licensetype . "}}\n[[Category:Uploads from NOA project]]" . $categories;

	
    //try to upload file via Wikimedia API
    $params = array('action' => 'upload', 'filename' => $filename, 'text' => $text, 'format' => 'json', 'token' => $csrftoken, 'file' => $cFile, 'comment' => 'Uploaded by the NOA Upload Tool');
    $res = doApiQuery($params, $ch, "upload");

    if (property_exists($res, 'error')) {
        if ($res->error->code === "fileexists-forbidden") {
            $suffix = 2;
            //if filename already exists try again with suffix
            while (property_exists($res, 'error')) {
                $filename = explode(".", $filename)[0] . $suffix . "." . explode(".", $filename)[1];
                $params = array('action' => 'upload', 'filename' => $filename, 'text' => $text, 'format' => 'json', 'token' => $csrftoken, 'file' => $cFile, 'comment' => 'Uploaded by the NOA Upload Tool');
                $res = doApiQuery($params, $ch, "upload");
                if (property_exists($res, 'error') && $res->error->code !== "fileexists-forbidden") {
                    echo "Error: " . $res->error->info;
                    break;
                } else if ($res->upload->result === "Success") {
                    echo json_encode(array("success", $filename));
                } else if ($res->upload->result === "Warning") {
                    if (property_exists($res->upload->warnings, 'duplicate')) {
                        echo json_encode(array("File was already uploaded: " . $res->upload->warnings->duplicate[0]));
                    }
                }
                $suffix++;
            }
        } else {
            echo json_encode(array("Error: " . $res->error->info));
        }
    } else if ($res->upload->result === "Warning") {
        if (property_exists($res->upload->warnings, 'duplicate')) {
            echo json_encode(array("File was already uploaded: " . $res->upload->warnings->duplicate[0]));
        }
    } else if ($res->upload->result === "Success") {
        //if upload was successfull store data in mysql db
        $servername = $ini['mysqlServer'];
        $username = $ini['mysqlUser'];
        $password = $ini['mysqlPassword'];
        $dbname = $ini['mysqlDB'];

        $conn = new mysqli($servername, $username, $password, $dbname);
        if ($conn->connect_error) {
            die("Connection failed: " . $conn->connect_error);
        }

        $sql = "INSERT INTO `viewedimages` (HASH, URL, UPLOADED, UPLOADDATE)
      VALUES (\"" . hash("md5", $externalurl) . "\", \"" . $externalurl . "\", 1, NOW()) ON DUPLICATE KEY UPDATE UPLOADED = 1, UPLOADDATE = NOW()";

        $conn->query($sql);

        $conn->close();


        echo json_encode(array("success", $filename));
    } else {
        echo json_encode(array("Something went wrong."));
    }

    if (is_file($filename)) {
        unlink($filename);
    }
}
?> 
