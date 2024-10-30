package main

import (
	"crypto/hmac"
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"io/ioutil"
	"net/http"
	"os"
	"strings"

	"github.com/gin-gonic/gin"
)

// verifyRequest validates the incoming request
func verifyRequest(c *gin.Context) bool {
	method := c.Request.Method
	path := c.Request.URL.Path
	bodyBytes, _ := ioutil.ReadAll(c.Request.Body)
	body := string(bodyBytes)

	// Retrieve headers
	signature := c.GetHeader("X-Contentful-Signature")
	signedHeaders := c.GetHeader("X-Contentful-Signed-Headers")

	// Create the canonical string
	canonicalString := buildCanonicalString(method, path, signedHeaders, c, body)

	// Generate the signature
	secret := os.Getenv("CONTENTFUL_SIGNING_SECRET")
	generatedSignature := calculateSignature(secret, canonicalString)

	return hmac.Equal([]byte(generatedSignature), []byte(signature))
}

// buildCanonicalString constructs the canonical string for signing
func buildCanonicalString(method, path, signedHeaders string, c *gin.Context, body string) string {
	headers := strings.Split(signedHeaders, ",")
	var headerParts []string

	for _, header := range headers {
		headerKey := strings.TrimSpace(strings.ToLower(header))
		headerValue := c.GetHeader(header)
		headerParts = append(headerParts, fmt.Sprintf("%s:%s", headerKey, strings.TrimSpace(headerValue)))
	}

	headersString := strings.Join(headerParts, ";")
	return fmt.Sprintf("%s\n%s\n%s\n%s", method, path, headersString, body)
}

// calculateSignature computes the HMAC SHA256 signature
func calculateSignature(secret, canonicalString string) string {
	h := hmac.New(sha256.New, []byte(secret))
	h.Write([]byte(canonicalString))
	return hex.EncodeToString(h.Sum(nil))
}

func main() {
	router := gin.Default()

	router.POST("/", func(c *gin.Context) {
		// Reset the body to read it again after verification
		bodyBytes, _ := ioutil.ReadAll(c.Request.Body)
		c.Request.Body = ioutil.NopCloser(strings.NewReader(string(bodyBytes)))

		if verifyRequest(c) {
			c.JSON(http.StatusOK, gin.H{"message": "Hello, World!"})
		} else {
			c.JSON(http.StatusForbidden, gin.H{"error": "Invalid signature"})
		}
	})

	router.Run(":8080")
}
