package com.tarkvaratehnika.demobackend.config

class ApplicationConfiguration {

    companion object {
        // URL for application. Use ngrok for HTTPS (or a tool of your own choice) and put the HTTPS link here.
        val WEBSITE_ORIGIN_URL = "https://heaid.ee"

        // Authentication request timeout in seconds.
        val AUTH_REQUEST_TIMEOUT_MS = 120000

        val USER_ROLE = "USER"
    }

}