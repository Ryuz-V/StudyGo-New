package com.example.todolist.model

data class TaskRequest(
    val title: String,
    val user_id: Int,
    val category: String
)