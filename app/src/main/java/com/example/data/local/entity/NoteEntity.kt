package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.ChecklistItem
import com.example.domain.model.Note
import com.example.domain.model.NoteType

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isTrash: Boolean = false,
    val folderId: Long? = null,
    val folderName: String? = null,
    val tags: List<String> = emptyList(),
    val color: Long = 0,
    val noteType: String = NoteType.STANDARD.name,
    val checklist: List<ChecklistItem> = emptyList(),
    val reminderTime: Long? = null,
    val attachments: List<String> = emptyList(),
    val isLocked: Boolean = false,
    val voiceAttachmentPath: String? = null,
    val drawingData: String? = null
) {
    fun toDomain(): Note {
        return Note(
            id = id,
            title = title,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isPinned = isPinned,
            isFavorite = isFavorite,
            isArchived = isArchived,
            isTrash = isTrash,
            folderId = folderId,
            folderName = folderName,
            tags = tags,
            color = color,
            noteType = try {
                NoteType.valueOf(noteType)
            } catch (e: Exception) {
                NoteType.STANDARD
            },
            checklist = checklist,
            reminderTime = reminderTime,
            attachments = attachments,
            isLocked = isLocked,
            voiceAttachmentPath = voiceAttachmentPath,
            drawingData = drawingData
        )
    }

    companion object {
        fun fromDomain(note: Note): NoteEntity {
            return NoteEntity(
                id = note.id,
                title = note.title,
                content = note.content,
                createdAt = note.createdAt,
                updatedAt = note.updatedAt,
                isPinned = note.isPinned,
                isFavorite = note.isFavorite,
                isArchived = note.isArchived,
                isTrash = note.isTrash,
                folderId = note.folderId,
                folderName = note.folderName,
                tags = note.tags,
                color = note.color,
                noteType = note.noteType.name,
                checklist = note.checklist,
                reminderTime = note.reminderTime,
                attachments = note.attachments,
                isLocked = note.isLocked,
                voiceAttachmentPath = note.voiceAttachmentPath,
                drawingData = note.drawingData
            )
        }
    }
}
