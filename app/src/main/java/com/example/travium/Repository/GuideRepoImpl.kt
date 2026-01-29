package com.example.travium.repository

import com.example.travium.model.GuideModel
import com.google.firebase.database.FirebaseDatabase

class GuideRepoImpl : GuideRepo {
    private val database = FirebaseDatabase.getInstance().getReference("guides")

    override fun registerGuide(guide: GuideModel, callback: (Boolean, String) -> Unit) {
        val guideId = database.push().key ?: return callback(false, "Failed to generate ID")
        val finalGuide = guide.copy(guideId = guideId)

        database.child(guideId).setValue(finalGuide)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Application submitted successfully!")
                } else {
                    callback(false, task.exception?.message ?: "Submission failed")
                }
            }
    }

    override fun getGuide(guideId: String, callback: (GuideModel?, String?) -> Unit) {
        database.child(guideId).get().addOnSuccessListener { snapshot ->
            val guide = snapshot.getValue(GuideModel::class.java)
            callback(guide, null)
        }.addOnFailureListener {
            callback(null, it.message)
        }
    }
}
