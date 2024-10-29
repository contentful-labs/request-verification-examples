## Python Request Verification

This example demonstrates how to verify webhook, app event, and app action requests from Contentful using HMAC-SHA256 in Python with Flask. The server computes a signature from the request's method, path, headers, and body, then compares it to the signature provided by Contentful.

### Requirements

- Python 3.x
- Flask

### Setup

1. Install Flask:
```bash
pip install flask
```

2. Set the `CONTENTFUL_SIGNING_SECRET` environment variable:
```bash
export CONTENTFUL_SIGNING_SECRET="your_contentful_signing_secret_here"
```

3. Run the application:
```bash
python app.py
```

The server will start on `http://0.0.0.0:8080`.

### Code Overview

- **verify_request**: Verifies the incoming request's signature.
- **build_canonical_string**: Constructs the canonical string from the request method, path, signed headers, and body.
- **calculate_signature**: Computes the HMAC SHA256 signature from the canonical string and secret.
