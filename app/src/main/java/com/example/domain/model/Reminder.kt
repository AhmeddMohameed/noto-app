package com.example.domain.model

data class Reminder(
    val id: Long = 0,
    val noteId: Long,
    val noteTitle: String,
    val reminderTime: Long,
    val isCompleted: Boolean = false
)
