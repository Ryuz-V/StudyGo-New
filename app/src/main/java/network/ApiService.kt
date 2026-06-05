package com.example.todolist.network

import com.example.todolist.model.SocialLoginRequest
import com.example.todolist.model.ManualLoginRequest
import com.example.todolist.model.RegisterRequest
import com.example.todolist.model.LoginResponse
import com.example.todolist.model.StatsResponse
import com.example.todolist.model.SubtaskRequest
import com.example.todolist.model.TaskRequest
import com.example.todolist.model.TaskResponse
import com.example.todolist.model.TaskData
import com.example.todolist.model.TaskListResponse // TAMBAHKAN IMPORT INI
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded

interface ApiService {

    @POST("api/login/social")
    fun sendSocialLoginData(@Body request: SocialLoginRequest): Call<LoginResponse>

    @POST("api/register")
    fun register(@Body request: RegisterRequest): Call<LoginResponse>

    @POST("api/login")
    fun login(@Body request: ManualLoginRequest): Call<LoginResponse>

    @POST("api/tasks")
    fun sendTaskData(
        @Header("Authorization") token: String,
        @Body request: TaskRequest
    ): Call<TaskResponse>

    @GET("api/tasks")
    fun getTasks(@Header("Authorization") token: String): Call<List<TaskData>>

    @FormUrlEncoded
    @PATCH("api/tasks/{id}/color")
    fun updateTaskColor(
        @Header("Authorization") token: String,
        @Path("id") taskId: Int,
        @Field("flag_color") flagColor: String
    ): Call<TaskResponse>

    @FormUrlEncoded
    @PATCH("api/tasks/{id}/complete") // Pastikan kamu membuat route ini di Laravel nantinya
    fun completeTask(
        @Header("Authorization") token: String,
        @Path("id") taskId: Int,
        @Field("is_completed") isCompleted: Int = 1
    ): Call<TaskResponse>

    @GET("api/tasks/completed")
    fun getCompletedTasks(@Header("Authorization") token: String): Call<List<TaskData>>

    @GET("api/tasks/stats")
    fun getTaskStats(@Header("Authorization") token: String): Call<StatsResponse>
    // PERHATIKAN: Ada tambahan 'api/' di depan tasks
    @POST("api/tasks/{task_id}/subtasks")
    fun addSubtask(
        @Header("Authorization") token: String,
        @Path("task_id") taskId: Int,
        @Body request: com.example.todolist.model.SubtaskRequest
    ): retrofit2.Call<com.example.todolist.model.TaskResponse>
}