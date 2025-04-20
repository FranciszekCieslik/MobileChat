package com.example.MobileChat.states

data class UserState(
    val email: String = "",
    val name: String = "",
    val bio: String = "",
    val profileUrl: String = "",
    val friends: List<String>?=null,
    val invitedFriends: List<String>?=null,
    val friendRequest: List<String>?=null
)