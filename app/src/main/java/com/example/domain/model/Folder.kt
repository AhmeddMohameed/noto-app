package com.example.domain.model

data class Folder(
    val id: Long = 0,
    val name: String,
    val icon: String = "folder",
    val color: Long = 0xFF4F46E5,
    val createdAt: Long = System.currentTimeMillis(),
    val parentId: Long? = null
)
