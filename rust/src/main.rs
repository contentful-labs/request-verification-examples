use warp::{Filter, Reply, Rejection, http::HeaderMap};
use bytes::Bytes;
use hmac::{Hmac, Mac};
use sha2::Sha256;
use hex;
use std::str;
use std::collections::HashMap;
use std::env;

type HmacSha256 = Hmac<Sha256>;

// Define the handler function
async fn verify_request(headers: HeaderMap, body: Bytes) -> Result<impl Reply, Rejection> {
    // Convert the headers into a HashMap for easier access
    let mut headers_map = HashMap::new();
    for (key, value) in headers.iter() {
        headers_map.insert(key.as_str().to_lowercase(), value.to_str().unwrap_or("").to_string());
    }

    // Retrieve the incoming signature and the secret
    let signature = match headers_map.get("x-contentful-signature") {
        Some(sig) => sig,
        None => return Ok(warp::reply::with_status("Signature Missing", warp::http::StatusCode::BAD_REQUEST)),
    };

    let secret = env::var("CONTENTFUL_SIGNING_SECRET").expect("CONTENTFUL_SIGNING_SECRET environment variable not set");

    // Extract the signed headers list
    let signed_headers = headers_map.get("x-contentful-signed-headers")
        .map(|h| h.split(',').map(|s| s.trim().to_string()).collect::<Vec<String>>())
        .unwrap_or_default();

    // Construct the canonical string
    let canonical_string = construct_canonical_string("POST", "/", &headers_map, &signed_headers, &body);

    // Calculate HMAC-SHA256
    let mut mac = HmacSha256::new_from_slice(secret.as_bytes())
        .expect("HMAC can take key of any size");
    mac.update(canonical_string.as_bytes());
    let result = mac.finalize().into_bytes();
    let generated_signature = hex::encode(result);

    // Compare signatures
    if generated_signature == *signature {
        Ok(warp::reply::with_status("Hello, World!", warp::http::StatusCode::OK))
    } else {
        println!("Generated and Incoming signatures did not match");
        Ok(warp::reply::with_status("Unauthorized", warp::http::StatusCode::UNAUTHORIZED))
    }
}

fn construct_canonical_string(
    method: &str,
    path: &str,
    headers: &HashMap<String, String>,
    signed_headers: &[String],
    body: &Bytes,
) -> String {
    let headers_string = signed_headers.iter()
        .map(|header| {
            let value = headers.get(header).map(|v| v.as_str()).unwrap_or("");
            format!("{}:{}", header.to_lowercase(), value)
        })
        .collect::<Vec<String>>()
        .join(";");

    let body_str = str::from_utf8(body).unwrap_or_default();
    format!("{}\n{}\n{}\n{}", method, path, headers_string, body_str)
}

// Main function to start the Warp server
#[tokio::main]
async fn main() {
    // Load environment variables
    dotenv::dotenv().ok();

    // Define the route
    let webhook_route = warp::post()
        .and(warp::path::end())
        .and(warp::header::headers_cloned())
        .and(warp::body::bytes())
        .and_then(verify_request);

    // Start the server
    warp::serve(webhook_route).run(([0, 0, 0, 0], 8080)).await;
}
