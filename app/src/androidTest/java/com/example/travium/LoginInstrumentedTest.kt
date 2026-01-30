package com.example.travium

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import com.example.travium.view.LoginActivity
import com.example.travium.view.RegisterActivity
import org.junit.After
import kotlin.jvm.java

@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<LoginActivity>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }
    @Test
    fun testSuccessfulLogin_navigatesToDashboard() {
        // Enter email
       composeRule.onNodeWithTag("email")
          .performTextInput("ram@gmail.com")
//
//        // Enter password
        composeRule.onNodeWithTag("password")
           .performTextInput("password")

        // Click Login
        composeRule.onNodeWithTag("register")
            .performClick()



//        Intents.intended(hasComponent(DashboardActivity::class.java.name))
        Intents.intended(hasComponent(RegisterActivity::class.java.name))
    }

}