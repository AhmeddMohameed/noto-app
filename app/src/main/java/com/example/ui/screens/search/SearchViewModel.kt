package com.example.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FolderRepository
import com.example.data.repository.NoteRepository
import com.example.data.repository.TagRepository
import com.example.domain.model.Folder
import com.example.domain.model.Note
import com.example.domain.model.Tag
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: List<Note> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val selectedFolderId: Long? = null,
    val selectedTag: String? = null,
    val totalCount: Int = 0
)

class SearchViewModel(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    private val _selectedTag = MutableStateFlow<String?>(null)

    private val _filterParams = combine(_query, _selectedFolderId, _selectedTag) { q, folderId, tag ->
        Triple(q, folderId, tag)
    }

    val uiState: StateFlow<SearchUiState> = combine(
        _filterParams,
        noteRepository.getAllActiveNotes(),
        folderRepository.getAllFolders(),
        tagRepository.getAllTags()
    ) { (q, folderId, tag), allNotes, folders, tags ->
        val trimmedQuery = q.trim()

        val filtered = allNotes.filter { note ->
            val matchesFolder = if (folderId != null) note.folderId == folderId else true
            val matchesTag = if (tag != null) note.tags.contains(tag) else true
            val matchesText = if (trimmedQuery.isBlank()) {
                true
            } else {
                note.title.contains(trimmedQuery, ignoreCase = true) ||
                        note.content.contains(trimmedQuery, ignoreCase = true) ||
                        note.tags.any { it.contains(trimmedQuery, ignoreCase = true) } ||
                        (note.folderName?.contains(trimmedQuery, ignoreCase = true) == true)
            }
            matchesFolder && matchesTag && matchesText
        }

        SearchUiState(
            query = q,
            results = filtered,
            folders = folders,
            tags = tags,
            selectedFolderId = folderId,
            selectedTag = tag,
            totalCount = filtered.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState()
    )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun selectFolder(folderId: Long?) {
        _selectedFolderId.value = if (_selectedFolderId.value == folderId) null else folderId
    }

    fun selectTag(tagName: String?) {
        _selectedTag.value = if (_selectedTag.value == tagName) null else tagName
    }

    fun setInitialFilters(tag: String?, folderId: Long?) {
        if (!tag.isNullOrBlank()) _selectedTag.value = tag
        if (folderId != null && folderId > 0) _selectedFolderId.value = folderId
    }

    fun toggleFavorite(noteId: Long, currentFavorite: Boolean) {
        viewModelScope.launch {
            noteRepository.toggleFavorite(noteId, !currentFavorite)
        }
    }

    fun togglePin(noteId: Long, currentPinned: Boolean) {
        viewModelScope.launch {
            noteRepository.togglePin(noteId, !currentPinned)
        }
    }
}
