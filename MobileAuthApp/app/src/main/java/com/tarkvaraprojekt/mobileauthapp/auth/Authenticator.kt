package com.tarkvaraprojekt.mobileauthapp.auth

import android.os.Message
import android.util.Log
import com.tarkvaraprojekt.mobileauthapp.NFC.Comms
import io.jsonwebtoken.SignatureAlgorithm
import org.bouncycastle.util.encoders.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.crypto.Mac
import kotlin.experimental.and

class Authenticator(val comms : Comms) {

    val type = "JWT"
    val algorithm = "ES384"
    var iss = "https://self-issued.me" // Will be specified at a later date.
    val algorithmUsedForSigning = SignatureAlgorithm.ES384

    fun authenticate(challenge: String, originUrl: String, pin1: String) : String {

        // Ask PIN 1 from the user and get the authentication certificate from the ID card.
        val authenticationCertificate : ByteArray = comms.getCertificate(true);

        // Encode the certificate in base64.
        val base64cert = String(Base64.encode(authenticationCertificate))

        // Get current epoch time.
        val epoch = LocalDateTime.now(ZoneOffset.UTC).atZone(ZoneOffset.UTC).toEpochSecond()

        // Get expiration time.
        val exp = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(5 * 60L).atZone(ZoneOffset.UTC).toEpochSecond()

        // Get subject value.
        val sub = authenticationCertificate[0] // TODO:

        // Get header and claims.
        val header = """{"typ":"$type","alg":"$algorithm","x5c":"$base64cert"}"""
        val claims = """{"iat":"$epoch","exp":"$exp","aud":"$originUrl","iss":"$iss","sub":"$sub","nonce":"$challenge","cnf":{"tbh":""}}"""

        var jwt = String(Base64.encode(header.toByteArray(Charsets.UTF_8))) + "." + String(Base64.encode(claims.toByteArray(Charsets.UTF_8)))
        jwt = jwt.replace("=", "")

        Log.v("JWT", jwt)

        // Send the authentication token hash to the ID card for signing and get signed authentication token as response.
        val encoded = MessageDigest.getInstance("SHA-384").digest(jwt.toByteArray())
        val signed = comms.authenticate(pin1, encoded)

        val jws = jwt + "." + String(Base64.encode(signed))

        Log.v("Token", jws)

        // Return the signed authentication token.
        return jws
    }


}