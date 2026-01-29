package com.example.travium.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.travium.model.UserModel
import com.example.travium.repository.UserRepo

class UserViewModel(private val repo: UserRepo) : ViewModel() {

    private val _allUsers = MutableLiveData<List<UserModel>>()
    val allUsers: LiveData<List<UserModel>> = _allUsers

    private val _bannedUsers = MutableLiveData<List<UserModel>>()
    val bannedUsers: LiveData<List<UserModel>> = _bannedUsers

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

    fun getAllUsers() {
        repo.getAllUsers { userList ->
            _allUsers.value = userList
        }
    }

    fun getBannedUsers() {
        repo.getBannedUsers { userList ->
            _bannedUsers.value = userList
        }
    }

    fun banUser(userId: String, callback: (Boolean, String) -> Unit) {
        repo.banUser(userId, callback)
    }

    fun unbanUser(userId: String, callback: (Boolean, String) -> Unit) {
        repo.unbanUser(userId, callback)
    }

    fun followUser(currentUserId: String, targetUserId: String, callback: (Boolean, String) -> Unit) {
        repo.followUser(currentUserId, targetUserId, callback)
    }

    fun unfollowUser(currentUserId: String, targetUserId: String, callback: (Boolean, String) -> Unit) {
        repo.unfollowUser(currentUserId, targetUserId, callback)
    }

    fun getFollowersCount(userId: String, callback: (Long) -> Unit) {
        repo.getFollowersCount(userId, callback)
    }

    fun getFollowingCount(userId: String, callback: (Long) -> Unit) {
        repo.getFollowingCount(userId, callback)
    }

    fun isFollowing(currentUserId: String, targetUserId: String, callback: (Boolean) -> Unit) {
        repo.isFollowing(currentUserId, targetUserId, callback)
    }

    fun searchUsers(query: String, callback: (List<UserModel>) -> Unit) {
        repo.searchUsers(query, callback)
    }
}
