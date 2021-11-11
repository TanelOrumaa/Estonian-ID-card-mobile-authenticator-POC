package com.tarkvaratehnika.demobackend.web

import com.tarkvaratehnika.demobackend.config.ValidationConfiguration.Companion.ROLE_USER
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SignatureController {


    @PreAuthorize("hasAuthority('$ROLE_USER')")
    @GetMapping("signature")
    fun signature(model : Model) : String {
//        model.addAttribute("intentUrl", ApplicationConfiguration.AUTH_APP_LAUNCH_INTENT)
//        model.addAttribute("challengeUrl", ApplicationConfiguration.CHALLENGE_ENDPOINT_URL)
        return "signature"
    }
}