package com.example.todolist.model

import com.google.gson.annotations.SerializedName

data class TaskData(
    @SerializedName("id_task")
    val id: Int,

    val title: String,

    // TAMBAHAN: Menangkap tanggal deadline dari Laravel
    val deadline: String?,

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