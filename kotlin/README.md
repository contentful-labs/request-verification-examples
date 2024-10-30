## Kotlin Request Verification

This example demonstrates how to verify webhook, app event, and app action requests from Contentful using HMAC-SHA256 in Kotlin with Ktor. The server computes a signature from the request's method, path, headers, and body, then compares it to the signature provided by Contentful.

### Requirements

- Kotlin 1.6+
- Gradle 7.0+

### Setup

1. Set the `CONTENTFUL_SIGNING_SECRET` environment variable:
```bash
export CONTENTFUL_SIGNING_SECRET="your_contentful_signing_secret_here"
```

2. Run the application:
```bash
gradle run
```

The server will start on `http://0.0.0.0:8083`.

### Code Overview
- **verifyRequest**: Verifies the incoming request's signature.
- **buildCanonicalString**: Constructs the canonical string from the request method, path, signed headers, and body.
- **calculateSignature**: Computes the HMAC SHA256 signature from the canonical string and secret.
