<?php

// Fetch the signing secret from the environment
$secret = getenv('CONTENTFUL_SIGNING_SECRET');

function fetchAllHeaders() {
    $headers = [];
    foreach ($_SERVER as $name => $value) {
        if (substr($name, 0, 5) == 'HTTP_') {
            $headers[str_replace(' ', '-', ucwords(str_replace('_', ' ', strtolower(substr($name, 5)))))] = $value;
        }
    }

    // Add Content-Type if it is not prefixed with 'HTTP_'
    if (isset($_SERVER['CONTENT_TYPE'])) {
        $headers['Content-Type'] = $_SERVER['CONTENT_TYPE'];
    }

    return $headers;
}

// Verify the request
function verifyRequest($secret, $method, $path, $headers, $body) {
    $signature = $headers['X-Contentful-Signature'] ?? null;
    $signedHeaders = isset($headers['X-Contentful-Signed-Headers']) ? explode(',', $headers['X-Contentful-Signed-Headers']) : [];

    if (!$signature || empty($signedHeaders)) {
        error_log("Missing required headers.");
        return false;
    }

    // Build the canonical string
    $canonicalString = buildCanonicalString($method, $path, $headers, $signedHeaders, $body);

    // Generate the signature
    $generatedSignature = calculateSignature($secret, $canonicalString);

    return hash_equals($generatedSignature, $signature);
}

// Build the canonical string
function buildCanonicalString($method, $path, $headers, $signedHeaders, $body) {
    $headersString = [];
    foreach ($signedHeaders as $header) {
        $headerKey = strtolower(trim($header));
        $headerValue = '';

        // Check if the header exists in any case format
        foreach ($headers as $key => $value) {
            if (strtolower($key) === $headerKey) {
                $headerValue = $value;
                break;
            }
        }

        $headersString[] = "$headerKey:" . trim($headerValue);
    }

    $headersString = implode(';', $headersString);
    return "$method\n$path\n$headersString\n$body";
}

// Calculate the HMAC SHA256 signature
function calculateSignature($secret, $canonicalString) {
    return hash_hmac('sha256', $canonicalString, $secret);
}

// Handle the POST request
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $method = $_SERVER['REQUEST_METHOD'];
    $path = $_SERVER['REQUEST_URI'];
    $body = file_get_contents('php://input');
    $headers = fetchAllHeaders();

    if (verifyRequest($secret, $method, $path, $headers, $body)) {
        header('Content-Type: application/json');
        echo json_encode(['message' => 'Hello, World!']);
    } else {
        header('HTTP/1.1 403 Forbidden');
        header('Content-Type: application/json');
        echo json_encode(['error' => 'Invalid signature']);
    }
}
