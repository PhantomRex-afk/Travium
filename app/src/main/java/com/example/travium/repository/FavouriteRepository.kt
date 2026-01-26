package com.example.travium.repository

import android.content.Context
import android.net.Uri

interface FavouriteRepository {
    fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit)
    fun getFileNameFromUri(context: Context, uri: Uri): String?
}
