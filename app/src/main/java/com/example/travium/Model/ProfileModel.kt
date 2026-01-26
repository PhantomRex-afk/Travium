package com.example.travium.model

data class ProfileModel(
var id : String = "",
var username : String = "",
var image: String = "",
)

{
    fun  toMap() : Map<String,Any>{
        return mapOf(
            "id" to id,
            "name" to username,

        )
    }
}