require 'sinatra'
require 'json'
require 'openssl'

# Retrieve the signing secret from an environment variable
SECRET = ENV['CONTENTFUL_SIGNING_SECRET']
if SECRET.nil? || SECRET.empty?
  raise 'Environment variable CONTENTFUL_SIGNING_SECRET is not set'
end

# Helper method to verify the request
def verify_request(secret, method, path, headers, body)
  begin
    # Retrieve necessary headers
    signature = headers['HTTP_X_CONTENTFUL_SIGNATURE']
    signed_headers = headers['HTTP_X_CONTENTFUL_SIGNED_HEADERS']&.split(',')

    # Return false if headers are missing
    return false if signature.nil? || signed_headers.nil?

    # Build the canonical string
    canonical_string = build_canonical_string(method, path, headers, signed_headers, body)

    # Calculate the generated signature
    generated_signature = calculate_signature(secret, canonical_string)

    # Compare the generated signature with the incoming one
    secure_compare(generated_signature, signature)
  rescue => e
    puts "Verification failed: #{e}"
    false
  end
end

# Build the canonical string based on the method, path, headers, and body
def build_canonical_string(method, path, headers, signed_headers, body)
  headers_string = signed_headers.map do |header|
    key = "HTTP_" + header.strip.upcase.tr('-', '_')
    value = headers[key]&.strip || ''

    # Handle content-type header
    value = headers["CONTENT_TYPE"] if header.strip.downcase == "content-type" && value.empty?

    "#{header.strip.downcase}:#{value}"
  end.join(';')

  "#{method}\n#{path}\n#{headers_string}\n#{body}"
end

# Calculate the HMAC SHA256 signature
def calculate_signature(secret, canonical_string)
  OpenSSL::HMAC.hexdigest(OpenSSL::Digest.new('sha256'), secret, canonical_string)
end

# Secure compare to avoid timing attacks
def secure_compare(a, b)
  OpenSSL.fixed_length_secure_compare(a, b)
end

post '/' do
  method = request.request_method
  path = request.path
  body = request.body.read
  headers = request.env

  # Verify request signature and respond accordingly
  if verify_request(SECRET, method, path, headers, body)
    puts "Request verified successfully"
    content_type :json
    status 200
    { message: "Hello, World!" }.to_json
  else
    puts "Invalid signature detected"
    content_type :json
    status 403
    { error: "Invalid signature" }.to_json
  end
end
