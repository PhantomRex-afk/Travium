package com.example.travium.repository

import android.content.Context
import android.net.Uri
import com.example.travium.model.Comment
import com.example.travium.model.MakePostModel

interface MakePostRepo {
    fun createPost(post: MakePostModel, callback: (Boolean, String) -> Unit)

    fun getAllPost(callback: (Boolean, String, List<MakePostModel>?) -> Unit)

    fun uploadImage(context : Context, imageUri : Uri, callback: (String?) -> Unit)

    fun getFileNameFromUri(context: Context, uri: Uri): String?

    fun likePost(postId: String, userId: String, callback: (Boolean) -> Unit)

    fun addComment(postId: String, comment: Comment, callback: (Boolean) -> Unit)
}
