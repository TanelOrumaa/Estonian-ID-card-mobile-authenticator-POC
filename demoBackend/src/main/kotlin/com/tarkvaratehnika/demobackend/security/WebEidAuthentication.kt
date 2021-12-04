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

import org.webeid.security.certificate.CertificateData

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.collections.ArrayList
import kotlin.math.log

class WebEidAuthentication(
    private val principalName: String,
    private val idCode: String,
    private val authorities: ArrayList<GrantedAuthority>
) : PreAuthenticatedAuthenticationToken(principalName, idCode, authorities), Authentication {

    // Companion object is for static functions.
    companion object {

        private val loggedInUsers = HashMap<String, Authentication>()

        fun fromCertificate(
            userCertificate: X509Certificate,
            authorities: ArrayList<GrantedAuthority>,
            challenge: String
        ): Authentication {
            val principalName = getPrincipalNameFromCertificate(userCertificate)
            val idCode = Objects.requireNonNull(CertificateData.getSubjectIdCode(userCertificate))
            val authentication = WebEidAuthentication(principalName, idCode, authorities)
            loggedInUsers[challenge] = authentication
            return authentication
        }

        /**
         * Function for getting a Spring authentication object by supplying a challenge.
         * TODO: Figure out a more secure solution in the future.
         */
        fun fromChallenge(challenge: String): Authentication? {
//            if (ThreadLocalRandom.current().nextFloat() < 0.5f) { // TODO: For testing.
//                return null
//            }
            val auth = loggedInUsers[challenge]
            if (auth != null) {
                // If challenge is valid, delete the authentication object from the map (so this can only be fetched once).
                loggedInUsers.remove(challenge)
            } else {
                return null
            }
            return auth
        }

//        // TODO: DELETE
//
//        const val ROLE_USER: String = "ROLE_USER"
//        private val USER_ROLE: GrantedAuthority = SimpleGrantedAuthority(ROLE_USER)
//
//        fun addAuth(challenge: String) {
//            val authorities = arrayListOf<GrantedAuthority>()
//            authorities.add(USER_ROLE)
//            val auth = WebEidAuthentication("Somename", "11111111111", authorities)
//            loggedInUsers[challenge] = auth
//        }
//
//
//        // TODO: DELETE UNTIL

        private fun getPrincipalNameFromCertificate(userCertificate: X509Certificate): String {
            return Objects.requireNonNull(CertificateData.getSubjectGivenName(userCertificate)) + " " +
                    Objects.requireNonNull(CertificateData.getSubjectSurname(userCertificate))
        }
    }


}