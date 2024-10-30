import os
from flask import Flask, request, jsonify
import hmac
import hashlib

app = Flask(__name__)

# Retrieve the signing secret from an environment variable
SECRET = os.getenv('CONTENTFUL_SIGNING_SECRET')
if not SECRET:
    raise ValueError("Environment variable CONTENTFUL_SIGNING_SECRET is not set")

def verify_request(secret, method, path, headers, body):
    """
    Verifies the request by calculating the signature and comparing it to the provided signature.
    """
    try:
        # Extract the required headers
        signature = headers.get('x-contentful-signature')
        signed_headers = headers.get('x-contentful-signed-headers', '').split(',')
        
        # Build the canonical string
        canonical_string = build_canonical_string(method, path, headers, signed_headers, body)
        
        # Calculate the HMAC SHA256 signature
        generated_signature = calculate_signature(secret, canonical_string)
        
        # Compare generated and incoming signatures in a secure way
        return hmac.compare_digest(generated_signature, signature)
    except Exception as e:
        print(f"Verification failed: {e}")
        return False

def build_canonical_string(method, path, headers, signed_headers, body):
    """
    Constructs the canonical string by concatenating HTTP method, path, headers, and body.
    """
    sorted_headers = [f"{header.lower().strip()}:{headers.get(header.strip())}" for header in signed_headers if header.strip().lower() in headers]
    headers_string = ';'.join(sorted_headers)
    canonical_string = f"{method}\n{path}\n{headers_string}\n{body}"
    return canonical_string

def calculate_signature(secret, canonical_string):
    """
    Computes the HMAC SHA256 signature.
    """
    mac = hmac.new(secret.encode('utf-8'), msg=canonical_string.encode('utf-8'), digestmod=hashlib.sha256)
    return mac.hexdigest()

@app.route("/", methods=["POST"])
def webhook():
    """
    Endpoint to receive and verify webhook requests.
    """
    method = request.method
    path = request.path
    headers = {k.lower(): v for k, v in request.headers.items()}
    body = request.get_data(as_text=True)

    # Verify request signature
    if verify_request(SECRET, method, path, headers, body):
        print("Request verified successfully")
        return jsonify({"message": "Hello, World!"}), 200
    else:
        print("Invalid signature detected")
        return jsonify({"error": "Invalid signature"}), 403

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8080)

