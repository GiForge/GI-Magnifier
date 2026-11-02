package com.example.model

import android.net.Uri

data class CapturedPhoto(
    val id: String,
    val uri: Uri,
    val filePath: String,
    val timestamp: Long,
    val zoomRatio: Float
)
