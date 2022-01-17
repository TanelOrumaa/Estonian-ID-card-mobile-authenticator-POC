package com.tarkvaratehnika.demobackend.dto

import org.springframework.security.core.GrantedAuthority

data class AuthDto(var roles: ArrayList<GrantedAuthority>, var userData: HashMap<String, String>, var errorCode: Int)