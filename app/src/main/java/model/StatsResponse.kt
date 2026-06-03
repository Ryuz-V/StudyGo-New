package com.example.todolist.model

data class StatsResponse(
    val success: Boolean,
    val completed_count: Int,
    val pending_count: Int,
    val chart_data: List<Int>
)