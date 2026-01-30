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

@RunWith(AndroidJUnit4::class)
class RegisterInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<RegisterActivity>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testNavigationToLogin_whenSignInClicked() {
        // Click on "Sign In" text
        composeRule.onNodeWithTag("signInText")
            .performClick()

        // Verify navigation to LoginActivity
        Intents.intended(hasComponent(LoginActivity::class.java.name))
    }

    @Test
    fun testRegisterForm_inputVerification() {
        // Type Full Name
        composeRule.onNodeWithTag("fullName")
            .performTextInput("John Doe")

        // Type Email
        composeRule.onNodeWithTag("email")
            .performTextInput("johndoe@example.com")

        // Type Password
        composeRule.onNodeWithTag("password")
            .performTextInput("password123")

        // Type Confirm Password
        composeRule.onNodeWithTag("confirmPassword")
            .performTextInput("password123")

        // Click on Country Box (to trigger dropdown)
        composeRule.onNodeWithTag("countryBox")
            .performClick()

        // Click on Terms checkbox row
        composeRule.onNodeWithTag("termsRow")
            .performClick()

        // Click Register Button
        composeRule.onNodeWithTag("registerButton")
            .performClick()
        
        // Note: Actual registration will attempt to call Firebase. 
        // For pure UI logic testing, you'd usually mock the ViewModel.
    }
}
