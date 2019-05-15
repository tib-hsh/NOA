<?php
/**
File for handeling OAuth authorization and other functions regarding the Upload Tool
*/

session_name('OAuthNOA');
$params = session_get_cookie_params();
session_set_cookie_params(
        $params['lifetime'], dirname($_SERVER['SCRIPT_NAME'])
);

$inifile = "$_SERVER[DOCUMENT_ROOT]/../conf/config.ini";
$ini = parse_ini_file($inifile);

$gUserAgent = $ini['agent'];
$gConsumerKey = $ini['consumerKey'];
$gConsumerSecret = $ini['consumerSecret'];
$mwOAuthAuthorizeUrl = 'https://commons.wikimedia.org/wiki/Special:OAuth/authorize';
$mwOAuthUrl = 'https://commons.wikimedia.org/w/index.php?title=Special:OAuth';
$mwOAuthIW = 'commons';



$apiUrl = 'https://commons.wikimedia.org/w/api.php';
$errorCode = 200;
$gTokenSecret = '';
$gTokenKey = '';
session_start();
if (isset($_SESSION['tokenKey'])) {
    $gTokenKey = $_SESSION['tokenKey'];
    $gTokenSecret = $_SESSION['tokenSecret'];
}
session_write_close();

function doAuthorizationRedirect() {
    global $mwOAuthUrl, $mwOAuthAuthorizeUrl, $gUserAgent, $gConsumerKey, $gTokenSecret, $errorCode;

    // First, we need to fetch a request token.
    // The request is signed with an empty token secret and no token key.
    $gTokenSecret = '';
    $url = $mwOAuthUrl . '/initiate';
    $url .= strpos($url, '?') ? '&' : '?';
    $url .= http_build_query(array(
        'format' => 'json',
        // OAuth information
        'oauth_callback' => 'oob', // Must be "oob" or something prefixed by the configured callback URL
        'oauth_consumer_key' => $gConsumerKey,
        'oauth_version' => '1.0',
        'oauth_nonce' => md5(microtime() . mt_rand()),
        'oauth_timestamp' => time(),
        // We're using secret key signatures here.
        'oauth_signature_method' => 'HMAC-SHA1',
    ));
    $signature = sign_request('GET', $url);
    $url .= "&oauth_signature=" . urlencode($signature);
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_USERAGENT, $gUserAgent);
    curl_setopt($ch, CURLOPT_HEADER, 0);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    $data = curl_exec($ch);
    if (!$data) {
        header("HTTP/1.1 $errorCode Internal Server Error");
        echo 'Curl error: ' . htmlspecialchars(curl_error($ch));
        exit(0);
    }
    curl_close($ch);
    $token = json_decode($data);
    if (is_object($token) && isset($token->error)) {
        header("HTTP/1.1 $errorCode Internal Server Error");
        echo 'Error retrieving token: ' . htmlspecialchars($token->error) . '<br>' . htmlspecialchars($token->message);
        exit(0);
    }
    if (!is_object($token) || !isset($token->key) || !isset($token->secret)) {
        header("HTTP/1.1 $errorCode Internal Server Error");
        echo 'Invalid response from token request';
        exit(0);
    }

    // Now we have the request token, we need to save it for later.
    session_start();
    $_SESSION['tokenKey'] = $token->key;
    $_SESSION['tokenSecret'] = $token->secret;
    session_write_close();

    // Then we send the user off to authorize
    $url = $mwOAuthAuthorizeUrl;
    $url .= strpos($url, '?') ? '&' : '?';
    $url .= http_build_query(array(
        'oauth_token' => $token->key,
        'oauth_consumer_key' => $gConsumerKey,
    ));

    return htmlspecialchars($url);
}

/**
 * Utility function to sign a request
 *
 * Note this doesn't properly handle the case where a parameter is set both in 
 * the query string in $url and in $params, or non-scalar values in $params.
 *
 * @param string $method Generally "GET" or "POST"
 * @param string $url URL string
 * @param array $params Extra parameters for the Authorization header or post 
 * 	data (if application/x-www-form-urlencoded).
 * @return string Signature
 */
function sign_request($method, $url, $params = array()) {
    global $gConsumerSecret, $gTokenSecret;

    $parts = parse_url($url);

    // We need to normalize the endpoint URL
    $scheme = isset($parts['scheme']) ? $parts['scheme'] : 'http';
    $host = isset($parts['host']) ? $parts['host'] : '';
    $port = isset($parts['port']) ? $parts['port'] : ( $scheme == 'https' ? '443' : '80' );
    $path = isset($parts['path']) ? $parts['path'] : '';
    if (( $scheme == 'https' && $port != '443' ) ||
            ( $scheme == 'http' && $port != '80' )
    ) {
        // Only include the port if it's not the default
        $host = "$host:$port";
    }

    // Also the parameters
    $pairs = array();
    parse_str(isset($parts['query']) ? $parts['query'] : '', $query);
    $query += $params;
    unset($query['oauth_signature']);
    if ($query) {
        $query = array_combine(
                // rawurlencode follows RFC 3986 since PHP 5.3
                array_map('rawurlencode', array_keys($query)), array_map('rawurlencode', array_values($query))
        );
        ksort($query, SORT_STRING);
        foreach ($query as $k => $v) {
            $pairs[] = "$k=$v";
        }
    }

    $toSign = rawurlencode(strtoupper($method)) . '&' .
            rawurlencode("$scheme://$host$path") . '&' .
            rawurlencode(join('&', $pairs));
    $key = rawurlencode($gConsumerSecret) . '&' . rawurlencode($gTokenSecret);
    return base64_encode(hash_hmac('sha1', $toSign, $key, true));
}

/**
 * Send an API query with OAuth authorization
 *
 * @param array $post Post data
 * @param object $ch Curl handle
 * @return array API results
 */
function doApiQuery($post, &$ch = null, $mode) {
    global $apiUrl, $gUserAgent, $gConsumerKey, $gTokenKey, $errorCode;


    $headerArr = array(
        // OAuth information
        'oauth_consumer_key' => $gConsumerKey,
        'oauth_token' => $gTokenKey,
        'oauth_version' => '1.0',
        'oauth_nonce' => md5(microtime() . mt_rand()),
        'oauth_timestamp' => time(),
        // We're using secret key signatures here.
        'oauth_signature_method' => 'HMAC-SHA1',
    );

    $to_sign = '';
    if ($mode == 'upload') {
        $to_sign = $headerArr;
    } else {
        $to_sign = $post + $headerArr;
    }

    $signature = sign_request('POST', $apiUrl, $to_sign);
    $headerArr['oauth_signature'] = $signature;

    $header = array();
    foreach ($headerArr as $k => $v) {
        $header[] = rawurlencode($k) . '="' . rawurlencode($v) . '"';
    }
    $header = 'Authorization: OAuth ' . join(', ', $header);

    $post_fields = '';
    if ($mode == 'upload') {
        $post_fields = $post;
    } else {
        $post_fields = http_build_query($post);
    }

    if (!$ch) {
        $ch = curl_init();
    }
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_URL, $apiUrl);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $post_fields);
    curl_setopt($ch, CURLOPT_HTTPHEADER, array($header));
    //curl_setopt( $ch, CURLOPT_SSL_VERIFYPEER, false );
    curl_setopt($ch, CURLOPT_USERAGENT, $gUserAgent);
    curl_setopt($ch, CURLOPT_HEADER, 0);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    $data = curl_exec($ch);
    if (!$data  && $mode != 'userinfo') {
        header("HTTP/1.1 $errorCode Internal Server Error");
        echo 'Curl error: ' . htmlspecialchars(curl_error($ch));
        exit(0);
    }
    $ret = json_decode($data);
    if ($ret === null  && $mode != 'userinfo') {
        header("HTTP/1.1 $errorCode Internal Server Error");
        echo 'Unparsable API response: <pre>' . htmlspecialchars($data) . '</pre>';
        exit(0);
    }
    return $ret;
}

/**
 * Handle a callback to fetch the access token
 * @return void
 */
function fetchAccessToken() {
    global $mwOAuthUrl, $gUserAgent, $gConsumerKey, $gTokenKey, $gTokenSecret, $errorCode;

    $url = $mwOAuthUrl . '/token';
    $url .= strpos($url, '?') ? '&' : '?';
    $url .= http_build_query(array(
        'format' => 'json',
        'oauth_verifier' => $_GET['oauth_verifier'],
        // OAuth information
        'oauth_consumer_key' => $gConsumerKey,
        'oauth_token' => $gTokenKey,
        'oauth_version' => '1.0',
        'oauth_nonce' => md5(microtime() . mt_rand()),
        'oauth_timestamp' => time(),
        // We're using secret key signatures here.
        'oauth_signature_method' => 'HMAC-SHA1',
    ));
    $signature = sign_request('GET', $url);
    $url .= "&oauth_signature=" . urlencode($signature);
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    //curl_setopt( $ch, CURLOPT_SSL_VERIFYPEER, false );
    curl_setopt($ch, CURLOPT_USERAGENT, $gUserAgent);
    curl_setopt($ch, CURLOPT_HEADER, 0);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    $data = curl_exec($ch);
    if (!$data) {
        header("HTTP/1.1 $errorCode Internal Server Error");
        echo 'Curl error: ' . htmlspecialchars(curl_error($ch));
        exit(0);
    }
    curl_close($ch);
    $token = json_decode($data);
    if (is_object($token) && isset($token->error)) {
        header("HTTP/1.1 $errorCode Internal Server Error");
        echo 'Error retrieving token: ' . htmlspecialchars($token->error) . '<br>' . htmlspecialchars($token->message);
        exit(0);
    }
    if (!is_object($token) || !isset($token->key) || !isset($token->secret)) {
        header("HTTP/1.1 $errorCode Internal Server Error");
        echo 'Invalid response from token request';
        exit(0);
    }
    // Save the access token
    session_start();
    $_SESSION['tokenKey'] = $gTokenKey = $token->key;
    $_SESSION['tokenSecret'] = $gTokenSecret = $token->secret;
    session_write_close();
}

function endsWith($haystack, $needle) {
    $length = strlen($needle);
    if ($length == 0) {
        return true;
    }
    return (substr($haystack, -$length) === $needle);
}

function startsWith($haystack, $needle) {
    $length = strlen($needle);
    return (substr($haystack, 0, $length) === $needle);
}

function getFileName($caption, $url) {
    $period = strpos($caption, ".");

    $firstSentence = $caption;
    if ($period != FALSE) {
        $firstSentence = substr($caption, 0, $period);
    }
    if (strlen($firstSentence) < 12) {
        $period = strpos($caption, ".", $period + 1);
        if ($period != FALSE) {
            $firstSentence = substr($caption, 0, $period);
        } else {
            $firstSentence = $caption;
        }
    }if (strlen($firstSentence) < 12) {
        $period = strpos($caption, ".", $period + 1);
        if ($period != FALSE) {
            $firstSentence = substr($caption, 0, $period);
        } else {
            $firstSentence = $caption;
        }
    }

    //delete everything but letters and numbers, replace spaces with "-"
    $firstSentence = preg_replace("/\s+/", "-", $firstSentence);
    $firstSentence = preg_replace("/[^a-zA-Z0-9-]+/", "", $firstSentence);
    if (strlen($firstSentence) > 120) {
        $firstSentence = substr($firstSentence, 0, 120);
    }
    while (endsWith($firstSentence, "-") || endsWith($firstSentence, ".")) {
        $firstSentence = substr($firstSentence, 0, strlen($firstSentence) - 1);
    }
    while (startsWith($firstSentence, "-") || startsWith($firstSentence, ".")) {
        $firstSentence = substr($firstSentence, 1, strlen($firstSentence));
    }
    //add file extension
    $startEndung = strripos($url, ".");
    $endung = substr($url, $startEndung);
    return $firstSentence . $endung;
}
