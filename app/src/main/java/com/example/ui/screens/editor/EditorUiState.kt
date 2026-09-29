package com.example.ui.screens.editor

import androidx.compose.ui.text.input.TextFieldValue
import com.example.domain.model.ChecklistItem
import com.example.domain.model.Folder
import com.example.domain.model.NoteType
import com.example.domain.model.Tag

data class EditorUiState(
    val noteId: Long = 0L,
    val title: String = "",
    val content: String = "",
    val contentValue: TextFieldValue = TextFieldValue(""),
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isTrash: Boolean = false,
    val selectedFolderId: Long? = null,
    val selectedFolderName: String? = null,
    val tags: List<String> = emptyList(),
    val color: Long = 0L,
    val noteType: NoteType = NoteType.STANDARD,
    val checklist: List<ChecklistItem> = emptyList(),
    val reminderTime: Long? = null,
    val attachments: List<String> = emptyList(),
    val isLocked: Boolean = false,
    val voiceAttachmentPath: String? = null,
    val drawingData: String? = null,
    val allFolders: List<Folder> = emptyList(),
    val allAvailableTags: List<Tag> = emptyList(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = true,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val showFolderDialog: Boolean = false,
    val showTagDialog: Boolean = false,
    val showReminderDialog: Boolean = false,
    val showColorPicker: Boolean = false,
    val errorMessage: String? = null
)
