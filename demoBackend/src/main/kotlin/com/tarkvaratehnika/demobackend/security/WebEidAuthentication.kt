/*
 * Copyright (c) 2020, 2021 The Web eID Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.tarkvaratehnika.demobackend.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tarkvaratehnika.demobackend.config.ApplicationConfiguration
import com.tarkvaratehnika.demobackend.config.ApplicationConfiguration.Companion.USER_ROLE
import com.tarkvaratehnika.demobackend.config.SessionManager
import com.tarkvaratehnika.demobackend.dto.AuthDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.webeid.security.certificate.CertificateData

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.web.server.ResponseStatusException
import java.io.Serializable
import java.security.cert.X509Certificate
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class WebEidAuthentication(
    private val principalName: String,
    private val idCode: String,
    private val authorities: ArrayList<GrantedAuthority>
) : PreAuthenticatedAuthenticationToken(principalName, idCode, authorities), Authentication {


    // Companion object is for static functions.
    companion object {
        private val LOG = LoggerFactory.getLogger(WebEidAuthentication::class.java)

        fun fromCertificate(
            userCertificate: X509Certificate,
            sessionId: String?,
        ): AuthDto {
            // Get user data.
            val name = getPrincipalNameFromCertificate(userCertificate)
            val idCode = Objects.requireNonNull(CertificateData.getSubjectIdCode(userCertificate))

            // Fetch valid sessionId.
            var methodIndependentSessionId = sessionId
            if (methodIndependentSessionId == null) {
                methodIndependentSessionId = SessionManager.getSessionId()
                if (methodIndependentSessionId == null) {
                    throw Exception("No session")
                }
            }

            // Add role and user data to the AuthDto and return it.
            SessionManager.addRoleToSession(methodIndependentSessionId, SimpleGrantedAuthority(USER_ROLE))
            return SessionManager.addUserDataToSession(methodIndependentSessionId, name, idCode)
        }

        /**
         * Function for getting a Spring authentication object for this session.
         */
        fun fromSession(headers: HashMap<String, String>): ResponseEntity<String> {
            val mapper = jacksonObjectMapper()

            val currentTime = Date()

            // Get sessionId for current session.
            var sessionId = SessionManager.getSessionId()

            if (sessionId == null) {
                sessionId = SessionManager.getSessionId(headers)
                    if (sessionId == null) {
                        return ResponseEntity.status(400).body(mapper.writeValueAsString(400))
                    }
            }

            while (currentTime.time + ApplicationConfiguration.AUTH_REQUEST_TIMEOUT_MS > Date().time) {
                Thread.sleep(1000)

                // Check if an error has been submitted for this session.
                val error = SessionManager.getError(sessionId)
                if (error != 200) {
                    return ResponseEntity.status(error).body(mapper.writeValueAsString(error))
                }

                // Check if this session has received a role.
                if (SessionManager.getSessionHasRole(sessionId, USER_ROLE)) {
                    // Get AuthDto
                    val auth = SessionManager.getSessionAuth(sessionId)

                    // Set role and user data to current session.
                    SessionManager.addRoleToCurrentSession(auth!!)
                    return ResponseEntity.status(200).body(mapper.writeValueAsString(auth))
                }
            }

            // In case of timeout return 408.
            return ResponseEntity.status(408).body(mapper.writeValueAsString(408))
        }

        private fun getPrincipalNameFromCertificate(userCertificate: X509Certificate): String {
            return Objects.requireNonNull(CertificateData.getSubjectGivenName(userCertificate)) + " " +
                    Objects.requireNonNull(CertificateData.getSubjectSurname(userCertificate))
        }
    }


}