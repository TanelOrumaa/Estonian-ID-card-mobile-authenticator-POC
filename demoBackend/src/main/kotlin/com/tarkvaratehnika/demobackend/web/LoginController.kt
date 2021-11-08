package com.tarkvaratehnika.demobackend.web

import com.tarkvaratehnika.demobackend.config.ApplicationConfiguration
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class LoginController {

    @GetMapping
    fun login(model : Model) : String {
        model.addAttribute("intentUrl", ApplicationConfiguration.AUTH_APP_LAUNCH_INTENT)
        model.addAttribute("challengeUrl", ApplicationConfiguration.CHALLENGE_ENDPOINT_URL)
        model.addAttribute("originUrl", ApplicationConfiguration.WEBSITE_ORIGIN_URL)
        model.addAttribute("loggedInUrl", "/signature")
        model.addAttribute("authenticationRequestUrl", ApplicationConfiguration.AUTHENTICATION_ENDPOINT_URL)
        return "index"
    }
}