package com.example.todolist.network

import com.example.todolist.model.SocialLoginRequest
import com.example.todolist.model.ManualLoginRequest
import com.example.todolist.model.RegisterRequest
import com.example.todolist.model.LoginResponse
import com.example.todolist.model.TaskRequest
import com.example.todolist.model.TaskResponse
import com.example.todolist.model.TaskListResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    // ==========================================
    //            AUTHENTICATION
    // ==========================================

    @POST("api/login/social")
    fun sendSocialLoginData(@Body request: SocialLoginRequest): Call<LoginResponse>

    @POST("api/register")
    fun register(@Body request: RegisterRequest): Call<LoginResponse>

    @POST("api/login") // Pastikan URL ini sesuai dengan routes/api.php di Laravel
    fun login(@Body request: ManualLoginRequest): Call<LoginResponse>


    // ==========================================
    //               TASKS
    // ==========================================

    @POST("api/tasks")
    fun sendTaskData(@Body request: TaskRequest): Call<TaskResponse>

    @GET("api/tasks")
    fun getTasks(): Call<TaskListResponse>
}