package com.tarkvaraprojekt.mobileauthapp.model

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

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

    private var _gender: String = ""
    val gender get() = _gender

    private var _expiration: String = ""
    val expiration get() = _expiration

    private var _citizenship: String = ""
    val citizenship get() = _citizenship

    fun clearUserInfo() {
        _userPin = ""
        _userCan = ""
        _userFirstName = ""
        _userLastName = ""
        _userIdentificationNumber = ""
        _expiration = ""
        _citizenship = ""
        _gender = ""
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

    fun setExpiration(newExpiration: String) {
        _expiration = newExpiration
    }

    fun setCitizenship(newCitizenship: String) {
        _citizenship = newCitizenship
    }

    fun setGender(newGender: String) {
        _gender = newGender
    }


    private fun getSharedPreferences(context: Context): SharedPreferences {
        val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            "user_creds",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun storeCan(context: Context) {
        val sharedPreferences: SharedPreferences = getSharedPreferences(context)
        sharedPreferences.edit().putString("CAN", userCan).apply()
    }

    fun checkCan(context: Context) {
        val sharedPreferences: SharedPreferences = getSharedPreferences(context)
        val foundCan = sharedPreferences.getString("CAN", null)
        foundCan?.let {
            _userCan = it
        }
    }

    // Must be called from AuthFragment as well, when CAN is wrong.
    fun deleteCan(context: Context) {
        val sharedPreferences: SharedPreferences = getSharedPreferences(context)
        sharedPreferences.edit().remove("CAN").apply()
        _userCan = ""
    }

    fun storePin(context: Context) {
        val sharedPreferences: SharedPreferences = getSharedPreferences(context)
        sharedPreferences.edit().putString("PIN1", userPin).apply()
    }

    fun checkPin(context: Context) {
        val sharedPreferences: SharedPreferences = getSharedPreferences(context)
        val foundPin = sharedPreferences.getString("PIN1", null)
        foundPin?.let {
            _userPin = it
        }
    }

    fun deletePin(context: Context) {
        val sharedPreferences: SharedPreferences = getSharedPreferences(context)
        sharedPreferences.edit().remove("PIN1").apply()
        _userPin = ""
    }

}