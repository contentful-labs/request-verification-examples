## C++ Request Verification
This example demonstrates how to verify webhook, app event, and app action requests from Contentful using HMAC-SHA256 in C++ with the Drogon web framework. The server computes a signature from the request's method, path, headers, and body, then compares it to the signature provided by Contentful.

### Requirements
- C++17 or higher
- Drogon
- OpenSSL (for HMAC computation)

### Setup

1. Install Drogon and OpenSSL: Follow the instructions to install Drogon and OpenSSL for your environment.

2. Set the `CONTENTFUL_SIGNING_SECRET` environment variable:
```bash
export CONTENTFUL_SIGNING_SECRET="your_contentful_signing_secret_here"
```

3. Build the application:
```bash
mkdir build
cd build
cmake ..
make
```

4. Run the application:
```bash
./contentful_request_verification
```

The server will start on `http://0.0.0.0:8080`.

### Code Overview
- **computeSignature**: Computes the HMAC-SHA256 signature using the OpenSSL library.
- **verifyRequestHandler**: Handles incoming requests, constructs the canonical string, and verifies the signature.
- **CMakeLists.txt**: Configures the C++ project, including dependencies on Drogon and OpenSSL.
