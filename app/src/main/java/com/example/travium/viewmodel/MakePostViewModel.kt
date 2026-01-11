package com.example.travium.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.travium.model.Comment
import com.example.travium.model.MakePostModel
import com.example.travium.model.NotificationModel
import com.example.travium.repository.MakePostRepo

class MakePostViewModel(private val makePostRepo: MakePostRepo) : ViewModel() {

    private val _allPosts = MutableLiveData<List<MakePostModel>>()
    val allPosts: LiveData<List<MakePostModel>> = _allPosts

    private val _notifications = MutableLiveData<List<NotificationModel>>()
    val notifications: LiveData<List<NotificationModel>> = _notifications

    fun createPost(post: MakePostModel, callback: (Boolean, String) -> Unit){
        makePostRepo.createPost(post, callback)
    }

    fun getAllPosts() {
        makePostRepo.getAllPost { success, message, productList ->
            if (success) {
                _allPosts.value = productList
            }
        }
    }

    fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        makePostRepo.uploadImage(context, imageUri, callback)
    }

    fun likePost(postId: String, userId: String, callback: (Boolean) -> Unit) {
        makePostRepo.likePost(postId, userId, callback)
    }

    fun addComment(postId: String, comment: Comment, callback: (Boolean) -> Unit) {
        makePostRepo.addComment(postId, comment, callback)
    }

    fun getNotifications(userId: String) {
        makePostRepo.getNotifications(userId) { success, message, notificationList ->
            if (success) {
                _notifications.value = notificationList
            }
        }
    }
}
