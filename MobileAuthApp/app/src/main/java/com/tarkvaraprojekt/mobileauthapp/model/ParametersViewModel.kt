package com.tarkvaraprojekt.mobileauthapp.model

import android.util.Log
import android.util.Log.WARN
import androidx.lifecycle.ViewModel

class ParametersViewModel: ViewModel() {

    private var _challenge: String = ""
    val challenge get() = _challenge

    private var _authUrl: String = ""
    val authUrl get() = _authUrl

    private var _token: String = ""
    val token get() = _token

    private var _origin: String = ""
    val origin get() = _origin

    private var _headers: Map<String, String> = HashMap<String, String>()
    val headers get() =_headers

    fun setChallenge(newChallenge: String) {
        _challenge = newChallenge
    }

    fun setAuthUrl(newAuthUrl: String) {
        _authUrl = newAuthUrl
    }

    fun setToken(newToken: String) {
        _token = newToken
    }

    fun setOrigin(newOrigin: String) {
        _origin = newOrigin
    }

    fun setHeaders(newHeaders: Map<String, String>) {
        Log.i("HEADERS", newHeaders.toList().toString())
        _headers = newHeaders
    }
}