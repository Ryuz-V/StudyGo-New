package com.example.todolist.network

import com.example.todolist.model.SocialLoginRequest
import com.example.todolist.model.LoginResponse
import com.example.todolist.model.TaskRequest
import com.example.todolist.model.TaskResponse
import com.example.todolist.model.TaskListResponse // Ini import yang bikin error tadi!
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("api/login/social")
    fun sendSocialLoginData(@Body request: SocialLoginRequest): Call<LoginResponse>

    @POST("api/tasks")
    fun sendTaskData(@Body request: TaskRequest): Call<TaskResponse>

    @GET("api/tasks")
    fun getTasks(): Call<TaskListResponse>
}