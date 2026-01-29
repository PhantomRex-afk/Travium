package com.example.travium.repository

import com.example.travium.model.GuideModel

interface GuideRepo {
    fun registerGuide(guide: GuideModel, callback: (Boolean, String) -> Unit)
    fun getGuide(guideId: String, callback: (GuideModel?, String?) -> Unit)
}
