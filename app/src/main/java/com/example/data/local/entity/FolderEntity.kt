package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Folder

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String = "folder",
    val color: Long = 0xFF4F46E5,
    val createdAt: Long = System.currentTimeMillis(),
    val parentId: Long? = null
) {
    fun toDomain(): Folder = Folder(
        id = id,
        name = name,
        icon = icon,
        color = color,
        createdAt = createdAt,
        parentId = parentId
    )

    companion object {
        fun fromDomain(folder: Folder): FolderEntity = FolderEntity(
            id = folder.id,
            name = folder.name,
            icon = folder.icon,
            color = folder.color,
            createdAt = folder.createdAt,
            parentId = folder.parentId
        )
    }
}
