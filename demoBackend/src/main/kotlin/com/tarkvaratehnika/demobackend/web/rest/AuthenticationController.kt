package com.tarkvaratehnika.demobackend.web.rest

import com.tarkvaratehnika.demobackend.security.AuthTokenDTO
import com.tarkvaratehnika.demobackend.security.AuthTokenDTOAuthenticationProvider
import com.tarkvaratehnika.demobackend.security.WebEidAuthentication
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("auth")
class AuthenticationController {

    private val LOG = LoggerFactory.getLogger(AuthenticationController::class.java)


    @PostMapping("authentication", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun authenticate(@RequestBody body : String): Authentication {
        val parts = body.split("\"")
        val authToken = AuthTokenDTO(parts[3], parts[7])
        // Create Spring Security Authentication object with supplied token as credentials.
        val auth = PreAuthenticatedAuthenticationToken(null, authToken)

        // Return authentication object if success.
        return AuthTokenDTOAuthenticationProvider.authenticate(auth)
    }

    @GetMapping("authentication", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAuthenticated(headers: String) : Authentication? {
        val auth = WebEidAuthentication.fromChallenge(challenge)
        if (auth == null) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed.")
        }
        return auth
    }
}