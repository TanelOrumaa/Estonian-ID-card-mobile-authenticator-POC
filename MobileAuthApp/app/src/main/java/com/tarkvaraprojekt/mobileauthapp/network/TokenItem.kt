package com.tarkvaraprojekt.mobileauthapp.network

/**
 * TokenItem for making POST request.
 */
data class TokenItem (
    val token: String,
    val challenge: String,
)