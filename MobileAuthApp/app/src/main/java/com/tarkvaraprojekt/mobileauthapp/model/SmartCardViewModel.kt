package com.tarkvaraprojekt.mobileauthapp.model

import androidx.lifecycle.ViewModel

class SmartCardViewModel: ViewModel() {

    private var _userPin: String = ""
    val userPin get() = _userPin

    private var _userCan: String = ""
    val userCan get() = _userCan

    private var _userFirstName: String = ""
    val userFirstName get() = _userFirstName

    private var _userLastName: String = ""
    val userLastName get() = _userLastName

    private var _userIdentificationNumber: String = ""
    val userIdentificationNumber get() = _userIdentificationNumber

    fun clearUserInfo() {
        _userPin = ""
        _userCan = ""
        _userFirstName = ""
        _userLastName = ""
        _userIdentificationNumber = ""
    }

    fun setUserPin(newUserPin: String) {
        _userPin = newUserPin
    }

    fun setUserCan(newUserCan: String) {
        _userCan = newUserCan
    }

    fun setUserFirstName(newUserFirstName: String) {
        _userFirstName = newUserFirstName
    }

    fun setUserLastName(newUserLastName: String) {
        _userLastName = newUserLastName
    }

    fun setUserIdentificationNumber(newUserIdentificationNumber: String) {
        _userIdentificationNumber = newUserIdentificationNumber
    }

}