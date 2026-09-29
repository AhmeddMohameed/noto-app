package com.example.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FolderRepository
import com.example.data.repository.NoteRepository
import com.example.data.repository.NotesViewMode
import com.example.data.repository.TagRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.domain.model.Note
import com.example.domain.model.NoteTemplate
import com.example.domain.model.NoteType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
    private val tagRepository: TagRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        val greetingText = calculateGreeting()
        viewModelScope.launch {
            combine(
                noteRepository.getAllActiveNotes(),
                folderRepository.getAllFolders(),
                tagRepository.getAllTags(),
                userPreferencesRepository.viewMode,
                userPreferencesRepository.userName
            ) { notes, folders, tags, viewMode, userName ->
                val pinned = notes.filter { it.isPinned }
                val favorites = notes.filter { it.isFavorite }
                val recent = notes.sortedByDescending { it.updatedAt }.take(8)

                HomeUiState(
                    isLoading = false,
                    greeting = greetingText,
                    userName = if (!userName.isNullOrBlank()) userName else "Note Taker",
                    allActiveNotes = notes,
                    pinnedNotes = pinned,
                    recentNotes = recent,
                    favoriteNotes = favorites,
                    folders = folders,
                    tags = tags,
                    viewMode = viewMode
                )
            }.collect { newState ->
                _uiState.update { currentState ->
                    newState.copy(
                        selectedFolderId = currentState.selectedFolderId,
                        selectedTagName = currentState.selectedTagName,
                        selectedNoteType = currentState.selectedNoteType,
                        filterHasReminder = currentState.filterHasReminder,
                        sortOrder = currentState.sortOrder
                    )
                }
            }
        }
    }

    private fun calculateGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night"
        }
    }

    fun setSortOrder(order: NoteSortOrder) {
        _uiState.update { it.copy(sortOrder = order) }
    }

    fun setNoteTypeFilter(type: NoteType?) {
        _uiState.update { it.copy(selectedNoteType = if (it.selectedNoteType == type) null else type) }
    }

    fun toggleReminderFilter() {
        _uiState.update { it.copy(filterHasReminder = !it.filterHasReminder) }
    }

    fun togglePin(noteId: Long, currentPinned: Boolean) {
        viewModelScope.launch {
            noteRepository.togglePin(noteId, !currentPinned)
        }
    }

    fun toggleFavorite(noteId: Long, currentFavorite: Boolean) {
        viewModelScope.launch {
            noteRepository.toggleFavorite(noteId, !currentFavorite)
        }
    }

    fun moveToTrash(noteId: Long) {
        viewModelScope.launch {
            noteRepository.moveToTrash(noteId)
        }
    }

    fun duplicateNote(noteId: Long) {
        viewModelScope.launch {
            noteRepository.duplicateNote(noteId)
        }
    }

    fun createFromTemplate(template: NoteTemplate, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val newNote = Note(
                title = template.defaultTitle,
                content = template.defaultContent,
                checklist = template.defaultChecklist,
                color = template.defaultColor,
                noteType = template.defaultType,
                tags = template.defaultTags
            )
            val id = noteRepository.insertOrUpdate(newNote)
            if (id > 0) {
                onCreated(id)
            }
        }
    }

    fun toggleViewMode() {
        val nextMode = if (_uiState.value.viewMode == NotesViewMode.STAGGERED_GRID) {
            NotesViewMode.LIST
        } else {
            NotesViewMode.STAGGERED_GRID
        }
        userPreferencesRepository.setViewMode(nextMode)
    }

    fun selectFolder(folderId: Long?) {
        _uiState.update {
            it.copy(selectedFolderId = if (it.selectedFolderId == folderId) null else folderId)
        }
    }

    fun selectTag(tagName: String?) {
        _uiState.update {
            it.copy(selectedTagName = if (it.selectedTagName == tagName) null else tagName)
        }
    }
}
