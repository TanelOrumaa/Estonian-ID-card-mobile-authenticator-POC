package com.tarkvaratehnika.demobackend.web.rest

import com.tarkvaratehnika.demobackend.config.SessionManager
import com.tarkvaratehnika.demobackend.dto.AuthDto
import com.tarkvaratehnika.demobackend.dto.AuthTokenDTO
import com.tarkvaratehnika.demobackend.security.AuthTokenDTOAuthenticationProvider
import com.tarkvaratehnika.demobackend.security.WebEidAuthentication
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("auth")
class AuthenticationController {

    private val LOG = LoggerFactory.getLogger(AuthenticationController::class.java)


    @PostMapping("login", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun authenticate(@RequestHeader headers: Map<String, String>, @RequestBody body : AuthTokenDTO): AuthDto {

        val sessionId = SessionManager.getSessionId(headers)

        // Create Spring Security Authentication object with supplied token as credentials.
        val auth = PreAuthenticatedAuthenticationToken(null, body)

        // Return authentication object if success.
        return AuthTokenDTOAuthenticationProvider.authenticate(auth, sessionId)
    }


    @GetMapping("login", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAuthenticated(@RequestHeader headers: HashMap<String, String>) : AuthDto {
        return WebEidAuthentication.fromSession(headers)
    }

    @GetMapping("userData", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUserData(@RequestHeader headers: Map<String, String>) : AuthDto? {
        return SessionManager.getSessionAuth(SessionManager.getSessionId(headers))
    }

    @PostMapping("logout")
    fun logOut(@RequestHeader headers: Map<String, String>) : HttpStatus? {
        SessionManager.removeRoleFromCurrentSession(headers)
        return HttpStatus.ACCEPTED

    }
}