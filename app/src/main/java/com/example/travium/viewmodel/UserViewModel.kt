package com.example.travium.viewmodel

import androidx.lifecycle.ViewModel
import com.example.travium.model.UserModel
import com.example.travium.repository.UserRepo

class UserViewModel(private val repo: UserRepo) : ViewModel() {

    fun login(
        email: String, password: String,
        callback: (Boolean, String) -> Unit
    ){
        repo.login(email, password, callback)
    }

    fun register(
        email: String, password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        repo.register(email, password, callback)
    }

    fun forgetPassword(
        email: String,callback: (Boolean, String) -> Unit
    ){
        repo.forgetPassword(email, callback)
    }

    fun changePassword(newPassword: String, callback: (Boolean, String) -> Unit) {
        repo.changePassword(newPassword, callback)
    }

    fun addUserToDatabase(
        userId: String,
        userModel: UserModel,
        callback: (Boolean, String) -> Unit
    ){
        repo.addUserToDatabase(userId, userModel, callback)
    }

    fun getUserById(userId: String, callback: (UserModel?) -> Unit) {
        repo.getUserById(userId, callback)
    }
}
