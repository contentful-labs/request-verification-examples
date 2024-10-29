using Microsoft.AspNetCore.Mvc;
using System.IO;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using Microsoft.Extensions.Primitives;
using Microsoft.Extensions.Logging;

namespace ContentfulRequestVerification.Controllers
{
    [ApiController]
    [Route("/")]
    public class WebhookController : ControllerBase
    {
        private readonly string _secret;
        private readonly ILogger<WebhookController> _logger;

        public WebhookController(ILogger<WebhookController> logger)
        {
            _secret = Environment.GetEnvironmentVariable("CONTENTFUL_SIGNING_SECRET") ?? string.Empty;
            _logger = logger;
        }

        [HttpPost]
        public async Task<IActionResult> VerifyWebhook()
        {
            if (string.IsNullOrEmpty(_secret))
            {
                _logger.LogError("Signing secret is not set.");
                return StatusCode(500, "Server configuration error");
            }

            // Step 1: Read the request body asynchronously
            string requestBody;
            using (StreamReader reader = new StreamReader(Request.Body, Encoding.UTF8))
            {
                requestBody = (await reader.ReadToEndAsync()).Trim();
            }

            // Step 2: Retrieve required headers
            string signature = Request.Headers["X-Contentful-Signature"].FirstOrDefault();
            string signedHeaders = Request.Headers["X-Contentful-Signed-Headers"].FirstOrDefault();

            if (string.IsNullOrEmpty(signature) || string.IsNullOrEmpty(signedHeaders))
            {
                _logger.LogWarning("Missing signature or signed headers");
                return Unauthorized();
            }

            // Step 3: Log headers for debugging
            foreach (var header in Request.Headers)
            {
                _logger.LogInformation("Header {Key}: {Value}", header.Key, header.Value);
            }

            // Step 4: Build the canonical string
            var canonicalString = BuildCanonicalString(signedHeaders, requestBody);
            _logger.LogInformation("Final Canonical String (Raw): {CanonicalString}", canonicalString);

            // Step 5: Calculate the HMAC SHA256 hash
            string generatedSignature = CalculateHMACSHA256(canonicalString, _secret);
            _logger.LogInformation("Generated Signature (Raw): {GeneratedSignature}", generatedSignature);
            _logger.LogInformation("Incoming Signature: {IncomingSignature}", signature);

            // Step 6: Compare the calculated signature with the incoming signature
            if (!string.Equals(generatedSignature, signature, StringComparison.OrdinalIgnoreCase))
            {
                _logger.LogWarning("Invalid signature. Generated signature did not match.");
                return Unauthorized();
            }

            _logger.LogInformation("Signature matched successfully.");
            return Ok();
        }

        private string BuildCanonicalString(string signedHeaders, string body)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("POST\n");
            sb.Append("/\n");

            var headersList = signedHeaders.Split(',');

            // Iterate through headers without adding a trailing semicolon after the last header
            for (int i = 0; i < headersList.Length; i++)
            {
                string header = headersList[i].ToLower().Trim();
                string headerValue = Request.Headers[header].FirstOrDefault()?.Trim() ?? string.Empty;
                sb.Append($"{header}:{headerValue}");

                // Append semicolon only between headers, not at the end
                if (i < headersList.Length - 1)
                {
                    sb.Append(";");
                }
            }

            sb.Append("\n");
            sb.Append(body);

            return sb.ToString();
        }

        private string CalculateHMACSHA256(string data, string secret)
        {
            using (var hmac = new HMACSHA256(Encoding.UTF8.GetBytes(secret)))
            {
                var hash = hmac.ComputeHash(Encoding.UTF8.GetBytes(data));
                return BitConverter.ToString(hash).Replace("-", "").ToLower();
            }
        }
    }
}
