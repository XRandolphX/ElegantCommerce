package com.xrandolphx.elegantcommerce.data

import android.net.Uri

data class ImageAnalysisState(
    val imageUri: Uri? = null,
    val response: String = "",
    val isLoading: Boolean = false,
    val conversations: List<Conversation> = emptyList(),
    val remainingQueries: Int = 5
)