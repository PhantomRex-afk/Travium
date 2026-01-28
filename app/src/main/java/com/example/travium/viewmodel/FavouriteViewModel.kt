package com.example.travium.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.travium.repository.FavouriteRepository

class FavouriteViewModel(private val repository: FavouriteRepository) : ViewModel() {

    fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        repository.uploadImage(context, imageUri, callback)
    }
}
