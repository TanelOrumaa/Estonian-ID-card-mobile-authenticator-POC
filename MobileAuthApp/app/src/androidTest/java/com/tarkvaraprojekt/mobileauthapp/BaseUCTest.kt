package com.tarkvaraprojekt.mobileauthapp

//import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.IdlingPolicies
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule

import org.junit.*
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
open class BaseUCTest {
    @get:Rule
    var activityActivityTestRule: ActivityTestRule<MainActivity> = ActivityTestRule(
        MainActivity::class.java
    )

    @Before
    fun setUp() {
        IdlingPolicies.setMasterPolicyTimeout(3, TimeUnit.SECONDS)
        IdlingPolicies.setIdlingResourceTimeout(3, TimeUnit.SECONDS)
        activityActivityTestRule.activity
            .supportFragmentManager.beginTransaction()
    }

    @After
    fun tearDown() {
    }
}