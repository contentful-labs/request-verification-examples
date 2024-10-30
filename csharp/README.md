## C# Request Verification

This example demonstrates how to verify webhook, app event, and app action requests from Contentful using HMAC-SHA256 in C# with ASP.NET Core. The server computes a signature from the request's method, path, headers, and body, then compares it to the signature provided by Contentful.

### Requirements
- .NET 6.0 SDK
- ASP.NET Core

### Setup

1. Set the `CONTENTFUL_SIGNING_SECRET` environment variable:
```bash
export CONTENTFUL_SIGNING_SECRET="your_contentful_signing_secret_here"
```

2. Restore dependencies and run the application:
```bash
dotnet restore
dotnet run
```

The server will start on `http://localhost:8080`.

### Code Overview
- **VerifyWebhook**: This method processes incoming POST requests, verifies the signature, and returns a 200 OK response if verified, or a 403 Forbidden response if not.
- **BuildCanonicalString**: Constructs the canonical string from the request method, path, signed headers, and body.
- **CalculateHMACSHA256**: Computes the HMAC SHA256 signature from the canonical string and secret.
