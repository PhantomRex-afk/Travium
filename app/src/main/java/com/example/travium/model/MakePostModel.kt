package com.example.travium.model

data class MakePostModel(
    val postId: String = "",
    val userId : String = "",
    val caption: String = "",
    val location : String = "",
    val imageUrl: String = "",
    val likes: List<String> = emptyList(),
    val comments: List<String> = emptyList()
){

}
