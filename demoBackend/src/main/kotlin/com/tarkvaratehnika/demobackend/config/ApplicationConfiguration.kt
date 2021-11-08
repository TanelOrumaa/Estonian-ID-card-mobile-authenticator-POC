package com.tarkvaratehnika.demobackend.config

class ApplicationConfiguration {

    companion object {
        // URL for intent, do not edit.
        val AUTH_APP_LAUNCH_INTENT = "authapp://start/"
        // Endpoint for challenge.
        val CHALLENGE_ENDPOINT_URL = "/auth/challenge"
        // Endpoint for authentication
        val AUTHENTICATION_ENDPOINT_URL = "/auth/authentication"
        // URL for application. Use ngrok for HTTPS (or a tool of your own choice) and put the HTTPS link here.
        val WEBSITE_ORIGIN_URL = "https://6bb0-85-253-195-252.ngrok.io"
    }

}