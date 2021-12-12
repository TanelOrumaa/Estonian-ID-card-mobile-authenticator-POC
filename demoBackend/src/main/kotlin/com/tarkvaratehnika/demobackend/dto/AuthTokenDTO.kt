package com.tarkvaratehnika.demobackend.dto

import com.fasterxml.jackson.annotation.JsonProperty

class AuthTokenDTO (@JsonProperty("auth-token") val token : String)