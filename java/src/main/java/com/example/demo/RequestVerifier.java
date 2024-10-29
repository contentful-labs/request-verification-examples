package com.example.demo;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class RequestVerifier {

    public static class ExpiredRequestException extends RuntimeException {
        public ExpiredRequestException(long ttl) {
            super("Requests are expected to be verified within " + ttl + "s from their signature.");
        }
    }

    public static boolean verifyRequest(String secret, CanonicalRequest canonicalRequest, long timeToLive) {
        try {
            // Normalize headers
            Map<String, String> normalizedHeaders = normalizeHeaders(canonicalRequest.getHeaders());

            // Get the signed headers from the normalized headers
            String signedHeadersValue = normalizedHeaders.get("x-contentful-signed-headers");
            String[] signedHeadersArray = signedHeadersValue.split(",");

            // Pick headers based on the signed headers list
            Map<String, String> signedHeadersMap = new LinkedHashMap<>();
            for (String headerKey : signedHeadersArray) {
                String normalizedKey = headerKey.trim().toLowerCase();
                if (normalizedHeaders.containsKey(normalizedKey)) {
                    signedHeadersMap.put(normalizedKey, normalizedHeaders.get(normalizedKey));
                }
            }

            CanonicalRequest requestToValidate = new CanonicalRequest(
                canonicalRequest.getMethod(),
                canonicalRequest.getPath(),
                signedHeadersMap,
                canonicalRequest.getBody()
            );

            // Generate the signature
            String generatedSignature = signRequest(secret, requestToValidate);

            // Compare the generated signature with the incoming signature
            return generatedSignature.equals(normalizedHeaders.get("x-contentful-signature"));
        } catch (Exception e) {
            System.out.println("Verification failed: " + e.getMessage());
            return false;
        }
    }

    private static String signRequest(String secret, CanonicalRequest request) {
        try {
            String canonicalString = buildCanonicalString(request);

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hashBytes = mac.doFinal(canonicalString.getBytes(StandardCharsets.UTF_8));

            return bytesToHex(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error generating signature", e);
        }
    }

    private static String buildCanonicalString(CanonicalRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getMethod()).append("\n");
        sb.append(request.getPath()).append("\n");
        sb.append(headersToString(request.getHeaders())).append("\n");
        sb.append(request.getBody());
        return sb.toString();
    }

    private static String headersToString(Map<String, String> headers) {
        return headers.entrySet().stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.joining(";"));
    }

    private static Map<String, String> normalizeHeaders(Map<String, String> headers) {
        Map<String, String> normalized = new HashMap<>();
        headers.forEach((key, value) -> {
            normalized.put(key.trim().toLowerCase(), value.trim());
        });
        return normalized;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static class CanonicalRequest {
        private final String method;
        private final String path;
        private final Map<String, String> headers;
        private final String body;

        public CanonicalRequest(String method, String path, Map<String, String> headers, String body) {
            this.method = method;
            this.path = path;
            this.headers = headers;
            this.body = body;
        }

        public String getMethod() {
            return method;
        }

        public String getPath() {
            return path;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public String getBody() {
            return body;
        }
    }
}
