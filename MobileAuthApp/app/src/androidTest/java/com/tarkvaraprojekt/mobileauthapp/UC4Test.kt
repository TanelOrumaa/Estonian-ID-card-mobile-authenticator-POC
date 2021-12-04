package com.tarkvaraprojekt.mobileauthapp

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.*

import org.junit.*
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class UC4Test {
    @get:Rule
    var activityActivityTestRule: ActivityTestRule<MainActivity> = ActivityTestRule(
        MainActivity::class.java
    )

    @Before
    fun setUp() {
        IdlingPolicies.setMasterPolicyTimeout(1, TimeUnit.SECONDS)
        IdlingPolicies.setIdlingResourceTimeout(1, TimeUnit.SECONDS)
        activityActivityTestRule.activity
            .supportFragmentManager.beginTransaction()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun test() {
        onView(withId(R.id.menu_settings_option)).perform(click())
        onView(withId(R.id.can_menu_action)).perform(click())
        onView(supportsInputMethods()).perform(typeText("123456"))
        onView(withId(R.id.next_button)).perform(click())
        onView(withText(R.string.can_status_saved)).inRoot(
            withDecorView(not(`is`(activityActivityTestRule.activity.getWindow().getDecorView())))
        ).check(matches(isDisplayed()))
    }
}