package com.example.data.repository

import com.example.data.local.dao.NoteDao
import com.example.data.local.entity.NoteEntity
import com.example.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getAllActiveNotes(): Flow<List<Note>> {
        return noteDao.getAllActiveNotes().map { list -> list.map { it.toDomain() } }
    }

    override fun getPinnedNotes(): Flow<List<Note>> {
        return noteDao.getPinnedNotes().map { list -> list.map { it.toDomain() } }
    }

    override fun getFavoriteNotes(): Flow<List<Note>> {
        return noteDao.getFavoriteNotes().map { list -> list.map { it.toDomain() } }
    }

    override fun getArchivedNotes(): Flow<List<Note>> {
        return noteDao.getArchivedNotes().map { list -> list.map { it.toDomain() } }
    }

    override fun getTrashNotes(): Flow<List<Note>> {
        return noteDao.getTrashNotes().map { list -> list.map { it.toDomain() } }
    }

    override fun getNotesByFolder(folderId: Long): Flow<List<Note>> {
        return noteDao.getNotesByFolder(folderId).map { list -> list.map { it.toDomain() } }
    }

    override fun getNoteById(id: Long): Flow<Note?> {
        return noteDao.getNoteById(id).map { it?.toDomain() }
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return noteDao.searchNotes(query).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getNoteByIdDirect(id: Long): Note? {
        return noteDao.getNoteByIdDirect(id)?.toDomain()
    }

    override suspend fun insertOrUpdate(note: Note): Long {
        val updatedNote = note.copy(updatedAt = System.currentTimeMillis())
        val entity = NoteEntity.fromDomain(updatedNote)
        return noteDao.insertNote(entity)
    }

    override suspend fun deletePermanently(note: Note) {
        noteDao.deleteNote(NoteEntity.fromDomain(note))
    }

    override suspend fun deletePermanentlyById(id: Long) {
        noteDao.deleteNoteById(id)
    }

    override suspend fun moveToTrash(id: Long) {
        noteDao.updateTrash(id, true)
    }

    override suspend fun restoreFromTrash(id: Long) {
        noteDao.updateTrash(id, false)
    }

    override suspend fun emptyTrash() {
        noteDao.emptyTrash()
    }

    override suspend fun restoreAllTrash() {
        noteDao.restoreAllTrash()
    }

    override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        noteDao.updateFavorite(id, isFavorite)
    }

    override suspend fun togglePin(id: Long, isPinned: Boolean) {
        noteDao.updatePinned(id, isPinned)
    }

    override suspend fun toggleArchive(id: Long, isArchived: Boolean) {
        noteDao.updateArchived(id, isArchived)
    }

    override suspend fun duplicateNote(id: Long): Long {
        val original = getNoteByIdDirect(id) ?: return -1L
        val copy = original.copy(
            id = 0L,
            title = "${original.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return insertOrUpdate(copy)
    }

    override suspend fun toggleLock(id: Long, isLocked: Boolean) {
        noteDao.updateLocked(id, isLocked)
    }
}
