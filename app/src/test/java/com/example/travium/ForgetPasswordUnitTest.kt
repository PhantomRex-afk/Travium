package com.example.travium

import com.example.travium.repository.UserRepo
import com.example.travium.viewmodel.UserViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ForgetPasswordUnitTest {

    @Test
    fun forgetPassword_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        // Mock the forgetPassword behavior
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Reset link sent")
            null
        }.`when`(repo).forgetPassword(eq("test@example.com"), any())

        var successResult = false
        var messageResult = ""

        viewModel.forgetPassword("test@example.com") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Reset link sent", messageResult)

        verify(repo).forgetPassword(eq("test@example.com"), any())
    }
}
