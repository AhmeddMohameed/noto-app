package com.example.ui.screens.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FolderRepository
import com.example.data.repository.NoteRepository
import com.example.domain.model.Folder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class FolderWithCount(
    val folder: Folder,
    val noteCount: Int,
    val subfoldersCount: Int = 0
)

data class FoldersUiState(
    val folders: List<FolderWithCount> = emptyList(),
    val isLoading: Boolean = true,
    val selectedParentFolderId: Long? = null
)

class FoldersViewModel(
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoldersUiState())
    val uiState: StateFlow<FoldersUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                folderRepository.getAllFolders(),
                noteRepository.getAllActiveNotes()
            ) { folders, notes ->
                folders.map { folder ->
                    FolderWithCount(
                        folder = folder,
                        noteCount = notes.count { it.folderId == folder.id },
                        subfoldersCount = folders.count { it.parentId == folder.id }
                    )
                }
            }.collect { foldersWithCount ->
                _uiState.value = _uiState.value.copy(folders = foldersWithCount, isLoading = false)
            }
        }
    }

    fun selectParentFolder(parentId: Long?) {
        _uiState.value = _uiState.value.copy(selectedParentFolderId = parentId)
    }

    fun createFolder(name: String, color: Long = 0xFF4F46E5, parentId: Long? = null) {
        if (name.isBlank()) return
        viewModelScope.launch {
            folderRepository.insertFolder(
                Folder(name = name.trim(), color = color, parentId = parentId)
            )
        }
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            folderRepository.deleteFolderById(folderId)
        }
    }
}
