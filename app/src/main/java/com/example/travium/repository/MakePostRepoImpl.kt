package com.example.travium.repository


import android.util.Log.e
import com.example.travium.model.MakePostModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MakePostRepoImpl : MakePostRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val postsRef: DatabaseReference = database.getReference("posts")



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
}