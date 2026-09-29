package com.example.data.repository

import com.example.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllActiveNotes(): Flow<List<Note>>
    fun getPinnedNotes(): Flow<List<Note>>
    fun getFavoriteNotes(): Flow<List<Note>>
    fun getArchivedNotes(): Flow<List<Note>>
    fun getTrashNotes(): Flow<List<Note>>
    fun getNotesByFolder(folderId: Long): Flow<List<Note>>
    fun getNoteById(id: Long): Flow<Note?>
    suspend fun getNoteByIdDirect(id: Long): Note?
    fun searchNotes(query: String): Flow<List<Note>>
    suspend fun insertOrUpdate(note: Note): Long
    suspend fun deletePermanently(note: Note)
    suspend fun deletePermanentlyById(id: Long)
    suspend fun moveToTrash(id: Long)
    suspend fun restoreFromTrash(id: Long)
    suspend fun emptyTrash()
    suspend fun restoreAllTrash()
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)
    suspend fun togglePin(id: Long, isPinned: Boolean)
    suspend fun toggleArchive(id: Long, isArchived: Boolean)
    suspend fun duplicateNote(id: Long): Long
    suspend fun toggleLock(id: Long, isLocked: Boolean)
}
