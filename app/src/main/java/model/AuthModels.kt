package com.example.todolist.model

data class SocialLoginRequest(
    val email: String,
    val name: String,
    val provider_name: String,
    val provider_id: String,
    val avatar: String?
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: TokenData
)

data class TokenData(
    val token: String,
    val token_type: String
)