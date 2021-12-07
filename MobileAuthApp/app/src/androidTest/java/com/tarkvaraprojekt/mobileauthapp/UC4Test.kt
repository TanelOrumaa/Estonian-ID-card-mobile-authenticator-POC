package com.tarkvaraprojekt.mobileauthapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*

import org.junit.*

class UC4Test : BaseUCTest() {

    private fun navigateToCANView() {
        onView(withId(R.id.menu_settings_option)).perform(click())
        try {
            // Delete existing CAN
            onView(withText(R.string.can_delete)).perform(click())
        } catch (ignore: NoMatchingViewException) {}

        onView(withId(R.id.can_menu_action)).perform(click())
    }

    @Test
    fun validCAN() {
        navigateToCANView()
        onView(withText(R.string.can_helper_text)).check(matches(isDisplayed()))
        onView(supportsInputMethods()).perform(typeText("123456"))
        onView(withText(R.string.can_delete)).perform(closeSoftKeyboard())

        onView(withText(R.string.can_status_saved)).check(matches(isDisplayed()))
    }

    @Test
    fun invalidCAN() {
        navigateToCANView()
        onView(supportsInputMethods()).perform(typeText("12345"))
        onView(withText(R.string.can_helper_text)).check(matches(isDisplayed()))
    }
}