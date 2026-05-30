package com.example.todolist.model

// --- REQUEST MODELS ---

data class SocialLoginRequest(
    val email: String,
    val name: String,
    val provider_name: String,
    val provider_id: String,
    val avatar: String?
)

data class ManualLoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val password_confirmation: String
)

// --- RESPONSE MODELS ---

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: TokenData
)

data class TokenData(
    val token: String,
    val token_type: String,
    // (Opsional) Tambahkan ini jika API Laravel mengembalikan detail user
    val user: UserDetail? = null
)

data class UserDetail(
    val id: Int,
    val name: String,
    val email: String
)