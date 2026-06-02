package com.example.todolist.model

import com.google.gson.annotations.SerializedName

data class TaskData(
    @SerializedName("id_task")
    val id: Int,

    val title: String,

    // KITA KEMBALIKAN KE BENTUK CLASS CATEGORY
    val category: Category?,

    val is_completed: Int,

    @SerializedName("flag_color")
    val flagColor: String?
)

data class TaskListResponse(
    val success: Boolean,
    val data: List<TaskData>
)

// Class ini akan menangkap objek kategori dari Laravel
data class Category(
    val id: Int,
    val name: String
)