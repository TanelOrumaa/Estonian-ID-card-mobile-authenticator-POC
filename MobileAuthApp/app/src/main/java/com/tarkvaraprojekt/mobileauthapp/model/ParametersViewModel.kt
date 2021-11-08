package com.tarkvaraprojekt.mobileauthapp.model

import androidx.lifecycle.ViewModel

class ParametersViewModel: ViewModel() {

    private var _challenge: String = ""
    val challenge get() = _challenge

    private var _authUrl: String = ""
    val authUrl get() = _authUrl

    private var _token: String = ""
    val token get() = _token

    fun setChallenge(newChallenge: String) {
        _challenge = newChallenge
    }

    fun setAuthUrl(newAuthUrl: String) {
        _authUrl = newAuthUrl
    }

    fun setToken(newToken: String) {
        _token = newToken
    }
}