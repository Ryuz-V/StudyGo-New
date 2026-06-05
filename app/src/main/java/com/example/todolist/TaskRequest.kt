package com.example.todolist.model

data class TaskRequest(
    val category_id: Int, // Ubah dari String category menjadi Int category_id
    val title: String,
    val description: String,
    val deadline: String,
    val status: String,
    val priority: String,
    val subtasks: List<String>
)

data class SubtaskRequest(
    val title: String
)