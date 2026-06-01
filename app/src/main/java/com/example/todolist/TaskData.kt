package com.example.todolist.model

import com.google.gson.annotations.SerializedName

data class TaskData(
    // UBAH INI: Samakan dengan nama kolom primary key di database Laravel kamu (id_task)
    @SerializedName("id_task")
    val id: Int,

    val title: String,
    val category: Category?,
    val is_completed: Int,

    @SerializedName("flag_color")
    val flagColor: String?
)

data class TaskListResponse(
    val success: Boolean,
    val data: List<TaskData>
)

data class Category(
    val id: Int,
    val name: String
)