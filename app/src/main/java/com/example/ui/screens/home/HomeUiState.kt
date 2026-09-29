package com.example.ui.screens.home

import com.example.data.repository.NotesViewMode
import com.example.domain.model.Folder
import com.example.domain.model.Note
import com.example.domain.model.NoteType
import com.example.domain.model.Tag

enum class NoteSortOrder(val displayName: String) {
    DATE_MODIFIED_DESC("Date Modified (Newest)"),
    DATE_MODIFIED_ASC("Date Modified (Oldest)"),
    DATE_CREATED_DESC("Date Created"),
    TITLE_ASC("Title (A-Z)"),
    TITLE_DESC("Title (Z-A)"),
    COLOR("Color")
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val greeting: String = "Hello",
    val userName: String = "Alex Morgan",
    val allActiveNotes: List<Note> = emptyList(),
    val pinnedNotes: List<Note> = emptyList(),
    val recentNotes: List<Note> = emptyList(),
    val favoriteNotes: List<Note> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val selectedFolderId: Long? = null,
    val selectedTagName: String? = null,
    val selectedNoteType: NoteType? = null,
    val filterHasReminder: Boolean = false,
    val sortOrder: NoteSortOrder = NoteSortOrder.DATE_MODIFIED_DESC,
    val viewMode: NotesViewMode = NotesViewMode.STAGGERED_GRID,
    val errorMessage: String? = null
) {
    val filteredNotes: List<Note>
        get() {
            var list = allActiveNotes

            if (selectedFolderId != null) {
                list = list.filter { it.folderId == selectedFolderId }
            }

            if (!selectedTagName.isNullOrBlank()) {
                list = list.filter { it.tags.any { tag -> tag.equals(selectedTagName, ignoreCase = true) } }
            }

            if (selectedNoteType != null) {
                list = list.filter { it.noteType == selectedNoteType }
            }

            if (filterHasReminder) {
                list = list.filter { it.reminderTime != null }
            }

            return when (sortOrder) {
                NoteSortOrder.DATE_MODIFIED_DESC -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt })
                NoteSortOrder.DATE_MODIFIED_ASC -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenBy { it.updatedAt })
                NoteSortOrder.DATE_CREATED_DESC -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.createdAt })
                NoteSortOrder.TITLE_ASC -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenBy { it.title.lowercase() })
                NoteSortOrder.TITLE_DESC -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.title.lowercase() })
                NoteSortOrder.COLOR -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.color })
            }
        }
}
