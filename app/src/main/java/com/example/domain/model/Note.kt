package com.example.domain.model

data class Note(
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
    val color: Long = 0, // 0 means default theme card color
    val noteType: NoteType = NoteType.STANDARD,
    val checklist: List<ChecklistItem> = emptyList(),
    val reminderTime: Long? = null,
    val attachments: List<String> = emptyList(),
    val isLocked: Boolean = false,
    val voiceAttachmentPath: String? = null,
    val drawingData: String? = null
)
