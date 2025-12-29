package com.example.practice.repository

import android.content.Context
import android.net.Uri
import com.example.travium.Model.ProfileModel

interface ProductRepo {

    fun addProduct(
        model: ProfileModel, callback: (Boolean, String) -> Unit
    )

    fun updateProduct(
        model: ProfileModel, callback: (Boolean, String) -> Unit
    )

    fun deleteProduct(
        productID: String, callback: (Boolean, String) -> Unit
    )

    fun getAllProduct(
        callback: (Boolean, String, List<ProfileModel>) -> Unit
    )

    fun getProductById(
        productID: String, callback: (Boolean, String, ProfileModel?) -> Unit
    )

    fun getProductByCategory(
        categoryId: String, callback: (Boolean, String, List<ProfileModel>) -> Unit
    )

    fun uploadImage(
        context: Context, imageUri: Uri, callback: (Boolean, String?) -> Unit
    )
}
