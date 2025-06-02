package com.example.MobileChat

data class RoomResponse(
    val id: String,
    val name: String,
    val secure: Boolean = false,
    val password: String? = null
)
