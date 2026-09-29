package com.example.ui.screens.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FolderRepository
import com.example.data.repository.NoteRepository
import com.example.data.repository.ReminderRepository
import com.example.data.repository.SyncRepository
import com.example.data.repository.TagRepository
import com.example.domain.model.ChecklistItem
import com.example.domain.model.Folder
import com.example.domain.model.Note
import com.example.domain.model.NoteType
import com.example.domain.model.Reminder
import com.example.ui.components.RichTextAction
import com.example.ui.util.RichTextHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoteEditorViewModel(
    private val noteId: Long,
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
    private val tagRepository: TagRepository,
    private val reminderRepository: ReminderRepository,
    private val syncRepository: SyncRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState(noteId = if (noteId > 0) noteId else 0L))
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<TextFieldValue>()
    private val redoStack = mutableListOf<TextFieldValue>()

    private var autoSaveJob: Job? = null

    init {
        loadMetadata()
        if (noteId > 0) {
            loadNote(noteId)
        }
    }

    private fun loadMetadata() {
        viewModelScope.launch {
            combine(
                folderRepository.getAllFolders(),
                tagRepository.getAllTags()
            ) { folders, tags ->
                Pair(folders, tags)
            }.collect { (folders, tags) ->
                _uiState.update {
                    it.copy(
                        allFolders = folders,
                        allAvailableTags = tags
                    )
                }
            }
        }
    }

    private fun loadNote(id: Long) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(id).firstOrNull()
            if (note != null) {
                val initialValue = TextFieldValue(note.content, TextRange(note.content.length))
                undoStack.clear()
                redoStack.clear()
                undoStack.add(initialValue)

                _uiState.update {
                    it.copy(
                        noteId = note.id,
                        title = note.title,
                        content = note.content,
                        contentValue = initialValue,
                        isPinned = note.isPinned,
                        isFavorite = note.isFavorite,
                        isArchived = note.isArchived,
                        isTrash = note.isTrash,
                        selectedFolderId = note.folderId,
                        selectedFolderName = note.folderName,
                        tags = note.tags,
                        color = note.color,
                        noteType = note.noteType,
                        checklist = note.checklist,
                        reminderTime = note.reminderTime,
                        attachments = note.attachments,
                        isLocked = note.isLocked,
                        voiceAttachmentPath = note.voiceAttachmentPath,
                        drawingData = note.drawingData,
                        isSaved = true
                    )
                }
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle, isSaved = false) }
        scheduleAutoSave()
    }

    fun updateContentValue(newValue: TextFieldValue) {
        val currentContent = _uiState.value.contentValue
        if (newValue.text != currentContent.text) {
            if (undoStack.isEmpty() || undoStack.last().text != currentContent.text) {
                undoStack.add(currentContent)
                if (undoStack.size > 50) undoStack.removeAt(0)
            }
            redoStack.clear()
        }

        _uiState.update {
            it.copy(
                content = newValue.text,
                contentValue = newValue,
                isSaved = false,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        scheduleAutoSave()
    }

    fun updateContent(newContent: String) {
        val current = _uiState.value.contentValue
        val newValue = current.copy(text = newContent, selection = TextRange(newContent.length))
        updateContentValue(newValue)
    }

    fun applyRichTextAction(action: RichTextAction, explicitSelection: TextRange? = null) {
        when (action) {
            RichTextAction.UNDO -> undo()
            RichTextAction.REDO -> redo()
            RichTextAction.CHECKLIST -> addChecklistItem("New task")
            else -> {
                val current = _uiState.value.contentValue
                val formatted = RichTextHelper.applyFormatting(current, action, explicitSelection)
                updateContentValue(formatted)
            }
        }
    }

    private fun undo() {
        if (undoStack.isNotEmpty()) {
            val current = _uiState.value.contentValue
            redoStack.add(current)
            val previous = undoStack.removeAt(undoStack.lastIndex)
            _uiState.update {
                it.copy(
                    content = previous.text,
                    contentValue = previous,
                    isSaved = false,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = true
                )
            }
            scheduleAutoSave()
        }
    }

    private fun redo() {
        if (redoStack.isNotEmpty()) {
            val current = _uiState.value.contentValue
            undoStack.add(current)
            val next = redoStack.removeAt(redoStack.lastIndex)
            _uiState.update {
                it.copy(
                    content = next.text,
                    contentValue = next,
                    isSaved = false,
                    canUndo = true,
                    canRedo = redoStack.isNotEmpty()
                )
            }
            scheduleAutoSave()
        }
    }

    // Checklist operations
    fun addChecklistItem(text: String = "") {
        val updated = _uiState.value.checklist.toMutableList().apply {
            add(ChecklistItem(text = text, isChecked = false))
        }
        _uiState.update {
            it.copy(
                checklist = updated,
                noteType = NoteType.CHECKLIST,
                isSaved = false
            )
        }
        scheduleAutoSave()
    }

    fun toggleChecklistItem(id: String) {
        val updated = _uiState.value.checklist.map {
            if (it.id == id) it.copy(isChecked = !it.isChecked) else it
        }
        _uiState.update { it.copy(checklist = updated, isSaved = false) }
        scheduleAutoSave()
    }

    fun updateChecklistItemText(id: String, newText: String) {
        val updated = _uiState.value.checklist.map {
            if (it.id == id) it.copy(text = newText) else it
        }
        _uiState.update { it.copy(checklist = updated, isSaved = false) }
        scheduleAutoSave()
    }

    fun removeChecklistItem(id: String) {
        val updated = _uiState.value.checklist.filter { it.id != id }
        _uiState.update { it.copy(checklist = updated, isSaved = false) }
        scheduleAutoSave()
    }

    // Note properties
    fun togglePin() {
        _uiState.update { it.copy(isPinned = !it.isPinned, isSaved = false) }
        scheduleAutoSave()
    }

    fun toggleFavorite() {
        _uiState.update { it.copy(isFavorite = !it.isFavorite, isSaved = false) }
        scheduleAutoSave()
    }

    fun toggleArchive() {
        _uiState.update { it.copy(isArchived = !it.isArchived, isSaved = false) }
        scheduleAutoSave()
    }

    fun setColor(colorValue: Long) {
        _uiState.update { it.copy(color = colorValue, isSaved = false) }
        scheduleAutoSave()
    }

    fun setFolder(folder: Folder?) {
        _uiState.update {
            it.copy(
                selectedFolderId = folder?.id,
                selectedFolderName = folder?.name,
                isSaved = false
            )
        }
        scheduleAutoSave()
    }

    fun addTag(tagName: String) {
        val cleanTag = tagName.trim().removePrefix("#")
        if (cleanTag.isNotBlank() && !_uiState.value.tags.contains(cleanTag)) {
            val updated = _uiState.value.tags + cleanTag
            _uiState.update { it.copy(tags = updated, isSaved = false) }
            scheduleAutoSave()
        }
    }

    fun removeTag(tagName: String) {
        val updated = _uiState.value.tags - tagName
        _uiState.update { it.copy(tags = updated, isSaved = false) }
        scheduleAutoSave()
    }

    fun setReminder(time: Long?) {
        _uiState.update { it.copy(reminderTime = time, isSaved = false) }
        scheduleAutoSave()
    }

    fun deleteNote(onDeleted: () -> Unit) {
        viewModelScope.launch {
            if (_uiState.value.noteId > 0) {
                noteRepository.moveToTrash(_uiState.value.noteId)
                syncRepository?.syncNotes()
            }
            onDeleted()
        }
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(500)
            saveNoteInternal()
        }
    }

    fun saveNow() {
        autoSaveJob?.cancel()
        viewModelScope.launch {
            saveNoteInternal()
        }
    }

    private suspend fun saveNoteInternal() {
        val state = _uiState.value
        // Only save if there's at least a title, content, or checklist
        if (state.title.isBlank() && state.content.isBlank() && state.checklist.isEmpty()) {
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        val note = Note(
            id = state.noteId,
            title = state.title.ifBlank { "Untitled Note" },
            content = state.content,
            isPinned = state.isPinned,
            isFavorite = state.isFavorite,
            isArchived = state.isArchived,
            isTrash = state.isTrash,
            folderId = state.selectedFolderId,
            folderName = state.selectedFolderName,
            tags = state.tags,
            color = state.color,
            noteType = state.noteType,
            checklist = state.checklist,
            reminderTime = state.reminderTime,
            attachments = state.attachments,
            isLocked = state.isLocked,
            voiceAttachmentPath = state.voiceAttachmentPath,
            drawingData = state.drawingData
        )

        val newId = noteRepository.insertOrUpdate(note)
        if (state.noteId == 0L && newId > 0) {
            _uiState.update { it.copy(noteId = newId) }
        }

        // Also update reminder if set
        if (state.reminderTime != null && newId > 0) {
            reminderRepository.insertReminder(
                Reminder(
                    noteId = if (state.noteId > 0) state.noteId else newId,
                    noteTitle = state.title.ifBlank { "Note Reminder" },
                    reminderTime = state.reminderTime
                )
            )
        }

        _uiState.update { it.copy(isSaving = false, isSaved = true) }
        syncRepository?.syncNotes()
    }

    fun addAttachment(uriString: String) {
        val current = _uiState.value.attachments
        _uiState.update { it.copy(attachments = current + uriString, isSaved = false) }
        scheduleAutoSave()
    }

    fun removeAttachment(index: Int) {
        val current = _uiState.value.attachments.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _uiState.update { it.copy(attachments = current, isSaved = false) }
            scheduleAutoSave()
        }
    }

    fun toggleLock(locked: Boolean) {
        _uiState.update { it.copy(isLocked = locked, isSaved = false) }
        scheduleAutoSave()
    }

    fun setVoiceNote(path: String?) {
        _uiState.update { it.copy(voiceAttachmentPath = path, isSaved = false) }
        scheduleAutoSave()
    }

    fun setDrawing(data: String?) {
        _uiState.update { it.copy(drawingData = data, isSaved = false) }
        scheduleAutoSave()
    }

    fun duplicateCurrentNote(onDuplicated: (Long) -> Unit) {
        viewModelScope.launch {
            val currentId = _uiState.value.noteId
            if (currentId > 0) {
                val newId = noteRepository.duplicateNote(currentId)
                if (newId > 0) {
                    onDuplicated(newId)
                }
            }
        }
    }
}
