## Java Request Verification

This example demonstrates how to verify webhook, app event, and app action requests from Contentful using HMAC-SHA256 in Java with Spring Boot. The server computes a signature from the request's method, path, headers, and body, then compares it to the signature provided by Contentful.

### Requirements

- Java 8 or higher
- Apache Maven

### Setup

1. Set the `CONTENTFUL_SIGNING_SECRET` environment variable:
```bash
export CONTENTFUL_SIGNING_SECRET="your_contentful_signing_secret_here"
```

2. Install dependencies:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The server will start on `http://localhost:8080`.

### Code Overview
- **DemoApplication.java**: The main entry point of the Spring Boot application. Defines the endpoint for incoming requests and handles the verification process.
- **RequestVerifier.java**: Contains the verification logic, including signing the request and building the canonical string.

#### Canonical String Construction
The Java implementation builds a canonical string based on:

- HTTP Method: Always "POST" in this case.
- Path: The endpoint path, e.g., /.
- Signed Headers: The headers specified in the X-Contentful-Signed-Headers header, normalized and concatenated with ;.
- Body: The raw request body.

#### Signature Generation
The generated signature is an HMAC SHA256 hash of the canonical string, using the CONTENTFUL_SIGNING_SECRET as the key. The signature is then compared to the signature provided by Contentful in the X-Contentful-Signature header.
