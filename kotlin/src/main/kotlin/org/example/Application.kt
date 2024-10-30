package org.example

import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json
import io.ktor.http.HttpStatusCode
import io.ktor.http.Headers
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.Base64
import java.math.BigInteger
import org.slf4j.event.Level

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(CallLogging) {
            level = Level.INFO
        }

        install(ContentNegotiation) {
            json(Json { prettyPrint = true })
        }

        routing {
            post("/") {
                val headers = call.request.headers
                val method = call.request.httpMethod.value
                val path = call.request.uri
                val body = call.receiveText()

                val signature = headers["x-contentful-signature"] ?: ""
                val signedHeaders = headers["x-contentful-signed-headers"]?.split(",") ?: emptyList()

                // Fetch the secret from environment variables
                val secret = System.getenv("CONTENTFUL_SIGNING_SECRET") ?: throw IllegalArgumentException("CONTENTFUL_SIGNING_SECRET environment variable not set")

                if (verifyRequest(secret, method, path, headers, signedHeaders, body, signature)) {
                    call.respondText("Hello, World!", status = HttpStatusCode.OK)
                } else {
                    call.respondText("Invalid signature", status = HttpStatusCode.Forbidden)
                }
            }
        }
    }.start(wait = true)
}

fun verifyRequest(secret: String, method: String, path: String, headers: Headers, signedHeaders: List<String>, body: String, incomingSignature: String): Boolean {
    val canonicalString = buildCanonicalString(method, path, headers, signedHeaders, body)
    
    val generatedSignature = calculateSignature(secret, canonicalString)

    return generatedSignature == incomingSignature
}

fun buildCanonicalString(method: String, path: String, headers: Headers, signedHeaders: List<String>, body: String): String {
    val headersString = signedHeaders.joinToString(";") { header ->
        "${header.trim().lowercase()}:${headers[header.trim()] ?: ""}"
    }
    return "$method\n$path\n$headersString\n$body"
}

fun calculateSignature(secret: String, canonicalString: String): String {
    val mac = Mac.getInstance("HmacSHA256")
    val secretKeySpec = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
    mac.init(secretKeySpec)

    val rawHmac = mac.doFinal(canonicalString.toByteArray(Charsets.UTF_8))
    // Convert to hex encoding
    val hexSignature = BigInteger(1, rawHmac).toString(16).padStart(64, '0')

    return hexSignature
}
