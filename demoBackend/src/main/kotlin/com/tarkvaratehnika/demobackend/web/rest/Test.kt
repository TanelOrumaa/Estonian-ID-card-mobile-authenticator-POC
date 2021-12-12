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
import com.tarkvaratehnika.demobackend.security.WebEidAuthentication
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.webeid.security.nonce.NonceGenerator

@RestController
@RequestMapping("auth")
class Test (val nonceGenerator: NonceGenerator) {

    private val LOG = LoggerFactory.getLogger(ChallengeController::class.java)

    @GetMapping("test")
    fun test(@RequestHeader headers: Map<String, String>): String {
        return "<h1>JOUUUUUUUU</h1>"
    }

    @GetMapping("test2")
    fun test2(@RequestHeader headers: Map<String, String>): String {
        return "<h1>JOUUUUUUUU22222222222222222</h1>"
    }

}

