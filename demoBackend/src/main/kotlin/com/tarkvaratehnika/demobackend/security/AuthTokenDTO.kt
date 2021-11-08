package com.tarkvaratehnika.demobackend.security

import com.fasterxml.jackson.annotation.JsonProperty

class AuthTokenDTO (val token : String, val challenge : String) {
}