package com.example.travium

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.travium.view.ForgetPasswordActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ForgetPasswordInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ForgetPasswordActivity>()

    @Test
    fun testForgetPasswordForm_inputVerification() {
        // Type Email
        composeRule.onNodeWithTag("emailInput")
            .performTextInput("user@example.com")

        // Click Send Reset Link Button
        composeRule.onNodeWithTag("sendResetLinkButton")
            .performClick()
        
        // Note: In a real environment, this would call Firebase.
    }
}
