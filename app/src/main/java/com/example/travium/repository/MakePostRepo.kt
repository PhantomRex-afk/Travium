package com.example.travium.repository

interface MakePostRepo {
    fun register(
        email : String, password: String,
        callback:(Boolean, String, String)-> Unit
    )
}