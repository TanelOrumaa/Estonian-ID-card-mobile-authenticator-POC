package com.tarkvaratehnika.demobackend.config

import com.tarkvaratehnika.demobackend.dto.AuthDto
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetails

@Configuration
class SessionManager {

    companion object {

        private val LOG = LoggerFactory.getLogger(SessionManager::class.java)

        private val sessionRegistry = HashMap<String, AuthDto>()

        fun registerSession(sessionId: String) {
            if (sessionRegistry.containsKey(sessionId)) {
                LOG.debug("Session already exists.")
            } else {
                sessionRegistry[sessionId] = AuthDto(arrayListOf(), hashMapOf(), 200)
            }
        }

        fun addRoleToSession(sessionId: String, role: GrantedAuthority): AuthDto {
            if (sessionRegistry.containsKey(sessionId)) {
                val session = sessionRegistry[sessionId]
                session!!.roles.add(role)
                return session
            } else {
                throw Exception("Session with sessionId: $sessionId does not exist.")
            }
        }

        fun addErrorToSession(sessionId: String?, authDto: AuthDto) {
            // Errors are only sent by authentication app, so we can ignore sessionId being null.
            if (sessionRegistry.containsKey(sessionId)) {
                sessionRegistry[sessionId]!!.errorCode = authDto.errorCode
            }
        }

        fun getError(sessionId: String) : Int {
            if (sessionRegistry.containsKey(sessionId)) {
                if (sessionRegistry[sessionId]!!.errorCode != 200) {
                    return sessionRegistry[sessionId]!!.errorCode
                }
            }
            return 200
        }

        /**
         * Function adds role and userdata specified in authDto to the current session.
         */
        fun addRoleToCurrentSession(authDto: AuthDto) {
            val securityContext = SecurityContextHolder.getContext()
            var sessionId = getSessionId()
            if (sessionId == null) {
                // No sessionId attached to the session, get one from credentials.
                sessionId = securityContext.authentication.credentials.toString()
            }
            val authentication = UsernamePasswordAuthenticationToken(authDto.userData, sessionId, authDto.roles)
            securityContext.authentication = authentication
        }

        fun removeRoleFromCurrentSession(headers: Map<String, String>) {
            val securityContext = SecurityContextHolder.getContext()
            var sessionId = securityContext.authentication.credentials
            if (sessionId == null || sessionId == "") {
                // Fallback to when for some reason session object doesn't have sessionId attached.
                sessionId = getSessionId(headers)
            }
            sessionRegistry[sessionId]?.roles = arrayListOf()
            val authentication = UsernamePasswordAuthenticationToken(null, sessionId, listOf())
            securityContext.authentication = authentication
        }

        fun addUserDataToSession(sessionId: String, name: String, idCode: String): AuthDto {
            if (sessionRegistry.containsKey(sessionId)) {
                val session = sessionRegistry[sessionId]
                session!!.userData["name"] = name
                session.userData["idCode"] = idCode
                return session
            } else {
                throw Exception("Session with sessionId: $sessionId does not exist.")
            }
        }

        fun getSessionHasRole(sessionId: String, role: String): Boolean {
            if (sessionRegistry.containsKey(sessionId)) {
                if (sessionRegistry[sessionId]!!.roles.contains(SimpleGrantedAuthority(role))) {
                    return true
                }
            }
            return false
        }

        fun getSessionAuth(sessionId: String?): AuthDto? {
            if (sessionId == null) {
                return null
            }
            return sessionRegistry[sessionId]
        }

        fun getSessionId(headers: Map<String, String>): String? {
            return headers["sessionid"]
        }

        fun getSessionId(): String? {
            val context = SecurityContextHolder.getContext()
            if (context.authentication != null && context.authentication.details != null) {
                return (context.authentication.details as WebAuthenticationDetails).sessionId
            }
            return null
        }
    }


}