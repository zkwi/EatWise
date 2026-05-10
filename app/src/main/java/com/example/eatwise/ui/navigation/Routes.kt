package com.example.eatwise.ui.navigation

import android.net.Uri

object Routes {
    const val Home = "home"
    const val History = "history"
    const val Settings = "settings"
    const val Camera = "camera"
    const val Analysis = "analysis/{imagePath}"
    const val Detail = "detail/{recordId}"

    fun analysis(imagePath: String) = "analysis/${Uri.encode(imagePath)}"
    fun detail(recordId: String) = "detail/${Uri.encode(recordId)}"
}
