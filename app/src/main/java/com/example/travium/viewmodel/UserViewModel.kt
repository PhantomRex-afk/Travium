package com.example.travium.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.travium.model.UserModel
import com.example.travium.repository.UserRepo

class UserViewModel(private val repo: UserRepo) : ViewModel() {

    private val _allUsers = MutableLiveData<List<UserModel>>()
    val allUsers: LiveData<List<UserModel>> = _allUsers

    private val _userData = MutableLiveData<UserModel?>()
    val userData: LiveData<UserModel?> = _userData

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
        repo.getUserById(userId) { user ->
            _userData.postValue(user)
            callback(user)
        }
    }

    fun getAllUsers() {
        repo.getAllUsers { success, message, userList ->
            if (success && userList != null) {
                _allUsers.postValue(userList)
            } else {
                _allUsers.postValue(emptyList())
            }
        }
    }

    fun searchUsers(query: String, callback: (List<UserModel>) -> Unit) {
        repo.searchUsers(query, callback)
    }
}