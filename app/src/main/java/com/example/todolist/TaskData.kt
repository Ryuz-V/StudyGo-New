package com.example.todolist.model

data class TaskData(
    val id: Int,
    val title: String,
    val category: Category?, // Pastikan ini menggunakan class Category?, bukan Any? atau String?
    val is_completed: Int
)

data class TaskListResponse(
    val success: Boolean,
    val data: List<TaskData>
)

data class Category(
    val id: Int,
    val name: String // Pastikan ini ada agar pemanggilan .name di adapter menghasilkan String
)