package com.example.demo;

import com.example.demo.RequestVerifier.CanonicalRequest;
import com.example.demo.RequestVerifier.ExpiredRequestException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @RestController
    class HelloController {

        // Load signing secret from environment variable
        private final String secret = System.getenv("CONTENTFUL_SIGNING_SECRET");

        @PostMapping("/")
        public ResponseEntity<String> hello(
                @RequestHeader HttpHeaders headers,
                @RequestBody(required = false) String body
        ) {
            String method = "POST";
            String path = "/";
            Map<String, String> headerMap = new HashMap<>();

            // Populate headerMap with normalized headers
            headers.forEach((key, value) -> headerMap.put(key.trim().toLowerCase(), String.join(",", value)));

            CanonicalRequest request = new CanonicalRequest(method, path, headerMap, body);

            try {
                boolean isVerified = RequestVerifier.verifyRequest(secret, request, 30);

                if (!isVerified) {
                    System.out.println("Invalid signature detected.");
                    return new ResponseEntity<>("Invalid signature", HttpStatus.FORBIDDEN);
                }

                System.out.println("Request verified successfully");
                return new ResponseEntity<>("Hello, World!", HttpStatus.OK);

            } catch (ExpiredRequestException e) {
                System.out.println("Expired Request: " + e.getMessage());
                return new ResponseEntity<>(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
            } catch (Exception e) {
                System.out.println("Verification failed: " + e.getMessage());
                return new ResponseEntity<>("Unable to verify request", HttpStatus.BAD_REQUEST);
            }
        }
    }
}
