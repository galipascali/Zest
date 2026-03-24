package com.example.zest.model

data class User(
    val email: String = "",
    val displayName: String = "",
    val bio: String = "",
    val photoBase64: String? = null
)