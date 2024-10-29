## Go Request Verification

This example demonstrates how to verify webhook, app event, and app action requests from Contentful using HMAC-SHA256 in Go with the Gin framework. The server computes a signature from the request's method, path, headers, and body, then compares it to the signature provided by Contentful.

### Requirements

- Go 1.x
- Gin framework

### Setup

1. Install Gin:
```bash
go get -u github.com/gin-gonic/gin
```

2. Set the `CONTENTFUL_SIGNING_SECRET` environment variable:
```bash
export CONTENTFUL_SIGNING_SECRET="your_contentful_signing_secret_here"
```

3. Run the application:
```bash
go run main.go
```

The server will start on `http://0.0.0.0:8080`.

### Code Overview
- **verifyRequest**: Verifies the incoming request's signature.
- **buildCanonicalString**: Constructs the canonical string from the request method, path, signed headers, and body.
- **calculateSignature**: Computes the HMAC SHA256 signature from the canonical string and secret.
