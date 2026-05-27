package com.example.todolist.model

data class TaskRequest(
    val category: String,
    val title: String,
    val description: String,
    val deadline: String,
    val status: String,
    val priority: String
)