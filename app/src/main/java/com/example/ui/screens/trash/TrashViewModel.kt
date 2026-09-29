package com.example.ui.screens.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.NoteRepository
import com.example.domain.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TrashUiState(
    val trashNotes: List<Note> = emptyList(),
    val isLoading: Boolean = true
)

class TrashViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrashUiState())
    val uiState: StateFlow<TrashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            noteRepository.getTrashNotes().collect { notes ->
                _uiState.value = TrashUiState(trashNotes = notes, isLoading = false)
            }
        }
    }

    fun restoreNote(noteId: Long) {
        viewModelScope.launch {
            noteRepository.restoreFromTrash(noteId)
        }
    }

    fun deletePermanently(noteId: Long) {
        viewModelScope.launch {
            noteRepository.deletePermanentlyById(noteId)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            noteRepository.emptyTrash()
        }
    }

    fun restoreAll() {
        viewModelScope.launch {
            noteRepository.restoreAllTrash()
        }
    }
}
