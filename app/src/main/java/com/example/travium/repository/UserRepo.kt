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

    fun changePassword(newPassword: String, callback: (Boolean, String) -> Unit)

    fun getAllUsers(callback: (Boolean, String, List<UserModel>?) -> Unit)

    fun followUser(currentUserId: String, targetUserId: String, callback: (Boolean, String) -> Unit)
    fun unfollowUser(currentUserId: String, targetUserId: String, callback: (Boolean, String) -> Unit)
    fun getFollowersCount(userId: String, callback: (Long) -> Unit)
    fun getFollowingCount(userId: String, callback: (Long) -> Unit)
    fun isFollowing(currentUserId: String, targetUserId: String, callback: (Boolean) -> Unit)
}
