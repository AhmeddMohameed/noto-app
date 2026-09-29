package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Tag

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Long = 0xFF0D9488,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Tag = Tag(
        id = id,
        name = name,
        color = color,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(tag: Tag): TagEntity = TagEntity(
            id = tag.id,
            name = tag.name,
            color = tag.color,
            createdAt = tag.createdAt
        )
    }
}
