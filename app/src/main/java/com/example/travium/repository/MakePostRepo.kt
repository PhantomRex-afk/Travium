package com.example.travium.repository

import com.example.travium.model.MakePostModel

interface MakePostRepo {
    fun createPost(post: MakePostModel, callback: (Boolean, String) -> Unit)

    fun getAllPost(callback: (Boolean, String, List<MakePostModel>?) -> Unit)
}


