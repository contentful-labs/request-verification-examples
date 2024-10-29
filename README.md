# Request Verification Examples

This repository contains examples of Contentful request verification in multiple programming languages. Each example demonstrates how to verify that incoming webhook, app event, or app action requests are authentic by validating the signature using a shared secret.

## Supported Languages

- [C#](./csharp)
- [C++](./cplusplus)
- [Elixir](./elixir)
- [Go](./go)
- [Java](./java)
- [Kotlin](./kotlin)
- [PHP](./php)
- [Python](./python)
- [Ruby](./ruby)
- [Rust](./rust)

## How Request Verification Works

Contentful request verification is based on calculating a signature using HMAC-SHA256. The signature is calculated from a "canonical string" formed from the HTTP request's method, path, headers, and body. The server then compares the generated signature with the signature sent in the webhook headers.

### General Steps:

1. Extract relevant headers and body from the request.
2. Build a canonical string.
3. Use the shared secret to compute an HMAC-SHA256 signature.
4. Compare the computed signature with the signature from the request.

## Getting Started

### Prerequisites

- Basic knowledge of HMAC-SHA256 hashing.
- Each example is structured as a standalone project in its respective language, with instructions provided for building and running.

### Run an Example

1. Clone the repository:
   ```bash
   git clone https://github.com/contentful-labs/request-verification-examples.git
   cd request-verification-examples
   ```

2. Navigate to the folder of the language you want to test:
   ```bash
   cd python
   ```

3. Follow the README instructions within each folder for setting up dependencies and running the example.

## Folder Structure

```bash
├── cplusplus
│   ├── CMakeLists.txt
│   ├── README.md
│   ├── main.cpp
├── csharp
│   ├── ContentfulWebhookVerification.csproj
│   ├── ContentfulWebhookVerification.http
│   ├── Program.cs
│   ├── Properties
│   │   └── launchSettings.json
│   ├── README.md
│   ├── WebhookController.cs
│   ├── appsettings.Development.json
│   ├── appsettings.json
├── elixir
│   ├── README.md
│   ├── config
│   │   └── config.exs
│   ├── lib
│   │   └── request_verification.ex
│   ├── mix.exs
│   ├── mix.lock
├── go
│   ├── README.md
│   ├── go.mod
│   ├── go.sum
│   ├── main.go
├── java
│   ├── README.md
│   ├── pom.xml
│   ├── src
│   │   ├── main
│   │   │   ├── java
│   │   │   │   └── com
│   │   │   │       └── example
│   │   │   │           └── demo
│   │   │   │               ├── DemoApplication.java
│   │   │   │               └── RequestVerifier.java
├── kotlin
│   ├── README.md
│   ├── build.gradle.kts
│   ├── src
│   │   └── main
│   │       └── kotlin
│   │           └── org
│   │               └── example
│   │                   └── Application.kt
├── php
│   ├── README.md
│   ├── index.php
├── python
│   ├── README.md
│   ├── app.py
├── ruby
│   ├── README.md
│   ├── app.rb
├── rust
│   ├── Cargo.lock
│   ├── Cargo.toml
│   ├── README.md
│   ├── src
│   │   └── main.rs
└── README.md
```

## Contributing

Feel free to open issues or submit pull requests if you'd like to contribute additional languages or improve the existing examples. 
