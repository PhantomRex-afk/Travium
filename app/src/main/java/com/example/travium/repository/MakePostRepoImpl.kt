package com.example.travium.repository


import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log.e
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.travium.model.MakePostModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.util.concurrent.Executors

class MakePostRepoImpl : MakePostRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val postsRef: DatabaseReference = database.getReference("posts")

    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dh1lppcqa",
            "api_key" to "776547271472658",
            "api_secret" to "7P4Yg51yr6lWM6mtKsTvGOdGojs"
        )
    )



    override fun createPost(
        post: MakePostModel,
        callback: (Boolean, String) -> Unit
    ) {
        val postId = postsRef.push().key ?: ""
        val newPost = post.copy(userId = postId)

        postsRef.child(postId).setValue(newPost)
            .addOnCompleteListener { callback (true,"Created a post")}
            .addOnFailureListener { callback(false, it.message ?: "Unknown error occurred") }

    }

    override fun getAllPost(
        callback: (Boolean, String, List<MakePostModel>?) -> Unit
    ) {
        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = mutableListOf<MakePostModel>()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(MakePostModel::class.java)
                    product?.let { productList.add(it) }
                }
                callback(true, "Products retrieved successfully", productList)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })

    }

    override fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                var fileName = getFileNameFromUri(context, imageUri)

                fileName = fileName?.substringBeforeLast(".") ?: "uploaded_image"

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", fileName,
                        "resource_type", "image"
                    )
                )

                var imageUrl = response["url"] as String?

                imageUrl = imageUrl?.replace("http://", "https://")

                Handler(Looper.getMainLooper()).post {
                    callback(imageUrl)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            }
        }
    }



    override fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }
}