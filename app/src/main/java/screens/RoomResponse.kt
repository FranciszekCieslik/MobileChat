package com.example.MobileChat.models

data class RoomResponse(
    val id: String,
    val name: String,
    val secure: Boolean = false,
    val password: String? = null
)
