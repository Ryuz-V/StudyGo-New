package com.example.todolist.model

data class TaskData(
    val id: Int,
    val title: String,
    val category: String,
    val is_completed: Int // Di Laravel boolean biasanya dibaca 0 atau 1
)

data class TaskListResponse(
    val success: Boolean,
    val data: List<TaskData>
)