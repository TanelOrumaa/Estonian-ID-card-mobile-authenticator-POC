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

package com.tarkvaratehnika.demobackend.web.rest

import com.tarkvaratehnika.demobackend.config.SessionManager
import com.tarkvaratehnika.demobackend.dto.ChallengeDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.server.ResponseStatusException
import org.webeid.security.nonce.NonceGenerator


@RestController
@RequestMapping("auth")
class ChallengeController (val nonceGenerator: NonceGenerator) {

    private val LOG = LoggerFactory.getLogger(ChallengeController::class.java)

    @GetMapping("challenge")
    fun challenge(@RequestHeader headers: Map<String, String>): ChallengeDto {

        val sessionId = SessionManager.getSessionId(headers)

        if (sessionId == null) {
            LOG.warn("SESSION ID MISSING FOR CHALLENGE")
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "SessionId missing.")
        }

        SessionManager.registerSession(sessionId)

//        val context = SecurityContextHolder.getContext()
//        val authorities = arrayListOf<GrantedAuthority>()
//        authorities.add(SimpleGrantedAuthority("USER"))
//        authorities.add(SimpleGrantedAuthority("ROLE_USER"))
//        val auth = context.authentication
//
//        val newAuth: Authentication =
//            UsernamePasswordAuthenticationToken(auth.principal, auth.credentials, authorities)
//        SecurityContextHolder.getContext().authentication = newAuth;

//        SessionManager.createSession(SessionManager.getSessionId(headers))
        val challengeDto = ChallengeDto(nonceGenerator.generateAndStoreNonce())
        LOG.warn(challengeDto.nonce)
        return challengeDto
    }

}

