#include <drogon/drogon.h>
#include <iostream>
#include <sstream>
#include <iomanip>
#include <string>
#include <vector>
#include <openssl/hmac.h>
#include <cstdlib>

using namespace drogon;

// Function to compute HMAC-SHA256 signature
std::string computeSignature(const std::string &canonicalString, const std::string &secret)
{
    unsigned char result[EVP_MAX_MD_SIZE];
    unsigned int result_len = 0;

    HMAC(EVP_sha256(), secret.c_str(), secret.size(),
         reinterpret_cast<const unsigned char *>(canonicalString.c_str()), canonicalString.size(), result, &result_len);

    std::ostringstream oss;
    for (unsigned int i = 0; i < result_len; i++)
    {
        oss << std::hex << std::setw(2) << std::setfill('0') << static_cast<int>(result[i]);
    }

    return oss.str();
}

// Handler function with the correct signature
void verifyRequestHandler(const HttpRequestPtr &req, std::function<void(const HttpResponsePtr &)> &&callback)
{
    std::string incomingSignature = req->getHeader("x-contentful-signature");

    // Fetch the secret from the environment variable
    const char* env_secret = std::getenv("CONTENTFUL_SIGNING_SECRET");
    if (!env_secret) {
        std::cerr << "Error: CONTENTFUL_SIGNING_SECRET environment variable is not set." << std::endl;
        auto resp = HttpResponse::newHttpResponse();
        resp->setStatusCode(HttpStatusCode::k500InternalServerError);
        resp->setBody("Server configuration error.");
        callback(resp);
        return;
    }
    std::string secret = env_secret;

    std::ostringstream canonicalString;
    canonicalString << req->getMethodString() << "\n";
    canonicalString << req->getPath() << "\n";

    const std::string signedHeadersString = req->getHeader("x-contentful-signed-headers");
    std::istringstream headersStream(signedHeadersString);
    std::string header;

    std::string headersString;
    bool firstHeader = true;
    while (std::getline(headersStream, header, ','))
    {
        std::string headerValue = req->getHeader(header);

        if (!firstHeader)
        {
            headersString += ";";
        }
        headersString += header + ":" + headerValue;

        firstHeader = false;
    }

    canonicalString << headersString << "\n";
    
    std::string body(req->body());
    canonicalString << body;

    std::string computedSignature = computeSignature(canonicalString.str(), secret);

    auto resp = HttpResponse::newHttpResponse();
    if (computedSignature == incomingSignature)
    {
        resp->setStatusCode(HttpStatusCode::k200OK);
        resp->setBody("Signature verified successfully.");
    }
    else
    {
        resp->setStatusCode(HttpStatusCode::k403Forbidden);
        resp->setBody("Signature mismatch: Verification failed.");
    }

    callback(resp);
}

int main()
{
    app().registerHandler("/", &verifyRequestHandler, {Post});
    app().addListener("0.0.0.0", 8080).run();
}
