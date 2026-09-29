package com.example.ui.screens.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.NoteRepository
import com.example.data.repository.TagRepository
import com.example.domain.model.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class TagWithCount(
    val tag: Tag,
    val noteCount: Int
)

data class TagsUiState(
    val tags: List<TagWithCount> = emptyList(),
    val isLoading: Boolean = true
)

class TagsViewModel(
    private val tagRepository: TagRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagsUiState())
    val uiState: StateFlow<TagsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                tagRepository.getAllTags(),
                noteRepository.getAllActiveNotes()
            ) { tags, notes ->
                tags.map { tag ->
                    TagWithCount(
                        tag = tag,
                        noteCount = notes.count { it.tags.contains(tag.name) }
                    )
                }
            }.collect { tagsWithCount ->
                _uiState.value = TagsUiState(tags = tagsWithCount, isLoading = false)
            }
        }
    }

    fun createTag(name: String, color: Long = 0xFF0D9488) {
        val cleanName = name.trim().removePrefix("#")
        if (cleanName.isBlank()) return
        viewModelScope.launch {
            tagRepository.insertTag(Tag(name = cleanName, color = color))
        }
    }

    fun deleteTag(tagId: Long) {
        viewModelScope.launch {
            tagRepository.deleteTagById(tagId)
        }
    }
}
