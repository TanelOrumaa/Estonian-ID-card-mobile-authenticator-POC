package com.tarkvaratehnika.demobackend.config

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy

@EnableWebSecurity
class SecurityConfiguration : WebSecurityConfigurerAdapter() {

    override fun configure(auth: AuthenticationManagerBuilder?) {
        auth?.inMemoryAuthentication()?.withUser("justSomeUser")?.password("someBackdoorPasswordThisDoesntMatterItsADemo")
            ?.roles("USER")
    }

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
            ?.antMatchers("/welcome")?.hasRole("USER")
            ?.and()
            ?.sessionManagement()?.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            ?.and()?.csrf()?.disable()
    }
}