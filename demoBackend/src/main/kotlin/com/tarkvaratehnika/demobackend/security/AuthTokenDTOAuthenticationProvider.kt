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

import com.tarkvaratehnika.demobackend.config.ValidationConfiguration
import com.tarkvaratehnika.demobackend.config.ValidationConfiguration.Companion.ROLE_USER
import com.tarkvaratehnika.demobackend.web.rest.AuthenticationController
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Component
import org.webeid.security.exceptions.TokenValidationException
import org.webeid.security.validator.AuthTokenValidator
import java.security.cert.CertificateEncodingException
import java.security.cert.X509Certificate


@Component
object AuthTokenDTOAuthenticationProvider {

    private val LOG = LoggerFactory.getLogger(AuthTokenDTOAuthenticationProvider::class.java)


    private val USER_ROLE: GrantedAuthority = SimpleGrantedAuthority(ROLE_USER)


    val tokenValidator: AuthTokenValidator = ValidationConfiguration().validator()

    @Throws(AuthenticationException::class)
    fun authenticate(auth : Authentication) : Authentication {
        val authentication = auth as PreAuthenticatedAuthenticationToken
        val token = (authentication.credentials as AuthTokenDTO).token
        val challenge = (authentication.credentials as AuthTokenDTO).challenge
        val authorities = arrayListOf<GrantedAuthority>()
        authorities.add(USER_ROLE)

        try {
            val userCertificate: X509Certificate = tokenValidator.validate(token)
            return WebEidAuthentication.fromCertificate(userCertificate, authorities, challenge)
        } catch (e : TokenValidationException) {
            // Validation failed.
            throw AuthenticationServiceException("Token validation failed. " + e.message)
        } catch (e : CertificateEncodingException) {
            // Failed to extract subject fields from the certificate.
            throw AuthenticationServiceException("Incorrect certificate subject fields: " + e.message)
        }
    }

}