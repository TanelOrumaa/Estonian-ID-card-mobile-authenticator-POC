package com.tarkvaraprojekt.mobileauthapp.auth

import android.nfc.tech.IsoDep
import com.tarkvaraprojekt.mobileauthapp.NFC.Comms
import java.math.BigInteger

class Authenticator(val comms : Comms) {

    public fun authenticate(nonce: BigInteger, challengeUrl: String, pin1: String) {

        // Ask PIN 1 from the user and get the authentication certificate from the ID card.
        val authenticationCertificate : ByteArray = comms.getAuthenticationCertificate();

        // Create the authentication token (OpenID X509)

        // Hash the authentication token.

        // Send the authentication token hash to the ID card for signing and get signed authentication token as response.

        // Return the signed authentication token.
    }
}