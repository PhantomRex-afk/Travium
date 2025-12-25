package com.example.travium.viewmodel

import androidx.lifecycle.ViewModel
import com.example.travium.model.MakePostModel
import com.example.travium.repository.MakePostRepo

class MakePostViewModel(private val MakePostRepo: MakePostRepo) : ViewModel() {
    fun createPost(post: MakePostModel, callback: (Boolean, String) -> Unit){
        MakePostRepo.createPost(post, callback)
    }
}