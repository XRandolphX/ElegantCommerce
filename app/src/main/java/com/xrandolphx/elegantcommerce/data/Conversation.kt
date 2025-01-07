package com.xrandolphx.elegantcommerce.data

data class Conversation(
    val prompt: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis()
)
