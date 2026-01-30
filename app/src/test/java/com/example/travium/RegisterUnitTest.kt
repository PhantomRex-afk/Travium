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

class RegisterUnitTest {

    @Test
    fun register_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        // Mock the register behavior
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(2)
            callback(true, "mockUserId", "Registration successful")
            null
        }.`when`(repo).register(eq("newuser@gmail.com"), eq("password123"), any())

        var successResult = false
        var messageResult = ""
        var userIdResult = ""

        viewModel.register("newuser@gmail.com", "password123") { success, userId, msg ->
            successResult = success
            userIdResult = userId
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Registration successful", messageResult)
        assertEquals("mockUserId", userIdResult)

        verify(repo).register(eq("newuser@gmail.com"), eq("password123"), any())
    }
}
