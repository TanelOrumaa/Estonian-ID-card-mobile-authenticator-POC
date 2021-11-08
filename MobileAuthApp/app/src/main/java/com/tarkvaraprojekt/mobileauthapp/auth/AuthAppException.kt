package com.tarkvaraprojekt.mobileauthapp.auth

/**
 * A specialised RuntimeException class for exceptions related to the mobile authentication app.
 * Possible error codes can be found at
 * https://github.com/TanelOrumaa/Estonian-ID-card-mobile-authenticator-POC/wiki/Error-codes
 * @param message Error message
 * @param code An error code defined in the project wiki
 */
open class AuthAppException(message: String, var code: Int) : RuntimeException(message)