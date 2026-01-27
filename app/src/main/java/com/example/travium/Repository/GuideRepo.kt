package com.example.travium.repository

import android.content.Context
import android.net.Uri
import com.example.travium.model.GuideModel

interface GuideRepo {
    fun uploadImages(context: Context, imageUris: List<Uri>, callback: (List<String>?) -> Unit)
    fun addGuide(guide: GuideModel, callback: (Boolean, String) -> Unit)
    fun getAllGuides(callback: (Boolean, String, List<GuideModel>?) -> Unit)
    fun deleteGuide(guideId: String, callback: (Boolean, String) -> Unit)
}
