package com.example.MobileChat.states

data class RegisterState(
    val email: String = "",
    val password: String = "",
    var isLoading: Boolean = false,
    val error: String? = null,
)