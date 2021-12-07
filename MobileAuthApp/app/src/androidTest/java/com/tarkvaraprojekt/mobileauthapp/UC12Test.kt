package com.tarkvaraprojekt.mobileauthapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*

import org.junit.*

class UC12Test : BaseUCTest() {

    private fun navigateToPINView() {
        onView(withId(R.id.menu_settings_option)).perform(click())
        try {
            // Delete existing PIN
            onView(withText(R.string.pin1_delete)).perform(click())
        } catch (ignore: NoMatchingViewException) {}

        onView(withId(R.id.pin_menu_action)).perform(click())
    }

    @Test
    fun validPIN() {
        navigateToPINView()
        onView(withText(R.string.pin_helper_text)).check(matches(isDisplayed()))
        onView(supportsInputMethods()).perform(typeText("0000"))
        onView(withText(R.string.continue_button)).perform(click())

        onView(withText(R.string.pin_status_saved)).check(matches(isDisplayed()))
    }

    @Test
    fun tooShortPIN() {
        navigateToPINView()
        onView(supportsInputMethods()).perform(typeText("000"))
        onView(withText(R.string.continue_button)).perform(click())

        onView(withText(R.string.pin_helper_text)).check(matches(isDisplayed()))
    }

    @Test
    fun tooLongPIN() {
        navigateToPINView()
        onView(supportsInputMethods()).perform(typeText("0".repeat(13)))
        onView(withText(R.string.continue_button)).perform(click())

        onView(withText(R.string.pin_helper_text)).check(matches(isDisplayed()))
    }
}