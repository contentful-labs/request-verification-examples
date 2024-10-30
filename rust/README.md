## Rust Request Verification

This example demonstrates how to verify webhook, app event, and app action requests from Contentful using HMAC-SHA256 in Rust with Warp. The server computes a signature from the request's method, path, headers, and body, then compares it to the signature provided by Contentful.

### Requirements

- Rust and Cargo

### Setup

1. Set the `CONTENTFUL_SIGNING_SECRET` environment variable:
```bash
export CONTENTFUL_SIGNING_SECRET="your_contentful_signing_secret_here"
```

2. Run the application:
```bash
cargo run
```

The server will start on `http://0.0.0.0:8080`.

### Code Overview
- **verify_webhook**: Verifies the incoming request's signature.
- **construct_canonical_string**: Constructs the canonical string from the request method, path, signed headers, and body.
- **calculate_signature**: Computes the HMAC SHA256 signature from the canonical string and secret.
