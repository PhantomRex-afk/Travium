package com.example.travium.repository

import com.example.travium.model.UserModel

interface UserRepo {
    fun login(
        email: String, password: String,
        callback: (Boolean, String) -> Unit
    )
    fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit)
    fun addUserToDatabase(
        userId: String,
        userModel: UserModel,
        callback: (Boolean, String) -> Unit)

    fun forgetPassword(
        email: String,callback: (Boolean, String) -> Unit
    )

    fun getUserById(userId: String, callback: (UserModel?) -> Unit)
}
