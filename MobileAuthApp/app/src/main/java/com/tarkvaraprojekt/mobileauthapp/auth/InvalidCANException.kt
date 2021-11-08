package com.tarkvaraprojekt.mobileauthapp.auth

/**
 * An AuthAppException for when the user entered CAN does not match the one read from the ID-card
 * @see AuthAppException
 */
class InvalidCANException : AuthAppException("Invalid CAN", 400)