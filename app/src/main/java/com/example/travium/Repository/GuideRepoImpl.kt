package com.example.travium.repository

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.travium.model.GuideModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.util.concurrent.Executors

class GuideRepoImpl : GuideRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val guidesRef: DatabaseReference = database.getReference("guides")

    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dh1lppcqa",
            "api_key" to "776547271472658",
            "api_secret" to "7P4Yg51yr6lWM6mtKsTvGOdGojs"
        )
    )

    override fun uploadImages(context: Context, imageUris: List<Uri>, callback: (List<String>?) -> Unit) {
        val executor = Executors.newFixedThreadPool(imageUris.size.coerceAtLeast(1))
        val uploadedUrls = mutableListOf<String>()
        var uploadCount = 0

        if (imageUris.isEmpty()) {
            callback(emptyList())
            return
        }

        imageUris.forEach { uri ->
            executor.execute {
                try {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    val response = cloudinary.uploader().upload(
                        inputStream, ObjectUtils.asMap("resource_type", "image")
                    )
                    val imageUrl = (response["url"] as String?)?.replace("http://", "https://")
                    
                    synchronized(uploadedUrls) {
                        if (imageUrl != null) uploadedUrls.add(imageUrl)
                        uploadCount++
                        if (uploadCount == imageUris.size) {
                            Handler(Looper.getMainLooper()).post {
                                callback(uploadedUrls)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    synchronized(uploadedUrls) {
                        uploadCount++
                        if (uploadCount == imageUris.size) {
                            Handler(Looper.getMainLooper()).post {
                                callback(if (uploadedUrls.isEmpty()) null else uploadedUrls)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun addGuide(guide: GuideModel, callback: (Boolean, String) -> Unit) {
        val guideId = guidesRef.push().key ?: ""
        val newGuide = guide.copy(guideId = guideId)

        guidesRef.child(guideId).setValue(newGuide)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Guide published successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to publish guide")
                }
            }
    }

    override fun getAllGuides(callback: (Boolean, String, List<GuideModel>?) -> Unit) {
        guidesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val guidesList = mutableListOf<GuideModel>()
                for (guideSnapshot in snapshot.children) {
                    val guide = guideSnapshot.getValue(GuideModel::class.java)
                    guide?.let { guidesList.add(it) }
                }
                callback(true, "Guides retrieved successfully", guidesList)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun deleteGuide(guideId: String, callback: (Boolean, String) -> Unit) {
        if (guideId.isEmpty()) {
            callback(false, "Invalid Guide ID")
            return
        }
        guidesRef.child(guideId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Guide deleted successfully")
            } else {
                callback(false, task.exception?.message ?: "Failed to delete guide")
            }
        }
    }
}
