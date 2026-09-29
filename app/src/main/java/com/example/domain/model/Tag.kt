package com.example.domain.model

data class Tag(
    val id: Long = 0,
    val name: String,
    val color: Long = 0xFF0D9488,
    val createdAt: Long = System.currentTimeMillis()
)
