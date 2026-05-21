package com.example.todolist.network

import com.example.todolist.model.SocialLoginRequest
import com.example.todolist.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/login/social")
    fun sendSocialLoginData(@Body request: SocialLoginRequest): Call<LoginResponse>
}