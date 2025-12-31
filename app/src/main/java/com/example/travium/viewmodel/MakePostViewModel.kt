package com.example.travium.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.travium.model.MakePostModel
import com.example.travium.repository.MakePostRepo

class MakePostViewModel(private val MakePostRepo: MakePostRepo) : ViewModel() {

    private val _allPosts = MutableLiveData<List<MakePostModel>>()
    val allPosts: LiveData<List<MakePostModel>> = _allPosts
    fun createPost(post: MakePostModel, callback: (Boolean, String) -> Unit){
        MakePostRepo.createPost(post, callback)
    }

    fun getAllPosts() {
        MakePostRepo.getAllPost { success, message, productList ->
            if (success) {
                _allPosts.value = productList
            }
        }
    }

    fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        MakePostRepo.uploadImage(context, imageUri, callback)
    }
}