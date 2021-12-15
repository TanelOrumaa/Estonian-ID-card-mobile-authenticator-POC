package com.tarkvaraprojekt.mobileauthapp.auth

/**
 * An AuthAppException for when the user entered PIN is not correct
 * @see AuthAppException
 */
class InvalidPINException(val remainingAttempts: Int) : AuthAppException(
        "Invalid PIN" + (if (remainingAttempts>0) "" else ". Authentication method blocked."),
        if (remainingAttempts>0) 401 else 446
)