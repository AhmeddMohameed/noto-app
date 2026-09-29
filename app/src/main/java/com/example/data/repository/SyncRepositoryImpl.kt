package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.data.local.dao.FolderDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.dao.ReminderDao
import com.example.data.local.dao.TagDao
import com.example.data.local.entity.FolderEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.ReminderEntity
import com.example.data.local.entity.TagEntity
import com.example.domain.model.ChecklistItem
import com.example.domain.model.NoteType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SyncRepositoryImpl(
    private val context: Context,
    private val authRepository: AuthRepository,
    private val noteDao: NoteDao,
    private val folderDao: FolderDao,
    private val tagDao: TagDao,
    private val reminderDao: ReminderDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : SyncRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val _syncState = MutableStateFlow(
        SyncState(
            isOnline = isNetworkAvailableInitially(context)
        )
    )
    override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    init {
        startAutoSync()
    }

    override fun startAutoSync() {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager != null) {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _syncState.update { it.copy(isOnline = true) }
                    scope.launch {
                        if (authRepository.isUserLoggedIn) {
                            syncAll()
                        }
                    }
                }

                override fun onLost(network: Network) {
                    _syncState.update { it.copy(isOnline = false) }
                }
            })
        }
    }

    override suspend fun syncAll(): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = authRepository.getCurrentUserId()
        if (userId.isNullOrBlank()) {
            return@withContext Result.success(Unit) // Offline guest mode works 100% locally
        }

        _syncState.update { it.copy(isSyncing = true, syncMessage = "Syncing with cloud...") }

        try {
            syncFolders()
            syncTags()
            syncNotes()
            syncReminders()

            _syncState.update {
                it.copy(
                    isSyncing = false,
                    lastSyncTime = System.currentTimeMillis(),
                    syncMessage = "Synced just now",
                    error = null
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            _syncState.update {
                it.copy(
                    isSyncing = false,
                    error = e.localizedMessage ?: "Sync error occurred"
                )
            }
            Result.failure(e)
        }
    }

    override suspend fun syncNotes(): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = authRepository.getCurrentUserId() ?: return@withContext Result.success(Unit)
        val notesCol = firestore.collection("users").document(userId).collection("notes")

        try {
            // 1. Fetch local notes
            val localNotes = noteDao.getAllActiveNotes().firstOrNull() ?: emptyList()
            val trashNotes = noteDao.getTrashNotes().firstOrNull() ?: emptyList()
            val archivedNotes = noteDao.getArchivedNotes().firstOrNull() ?: emptyList()
            val allLocalNotes = (localNotes + trashNotes + archivedNotes).distinctBy { it.id }

            // 2. Fetch remote notes
            val remoteDocs = notesCol.get().await()
            val remoteNotesMap = remoteDocs.documents.associateBy { it.id }

            // 3. Push or resolve conflict from local to remote
            for (local in allLocalNotes) {
                val docId = "note_${local.id}"
                val remoteDoc = remoteNotesMap[docId]

                if (remoteDoc == null || !remoteDoc.exists()) {
                    // Remote does not have it -> Push local to remote
                    val noteMap = noteEntityToFirestoreMap(local)
                    notesCol.document(docId).set(noteMap, SetOptions.merge()).await()
                } else {
                    val remoteUpdatedAt = (remoteDoc.get("updatedAt") as? Number)?.toLong() ?: 0L
                    if (local.updatedAt >= remoteUpdatedAt) {
                        // Local is newer -> Push to remote
                        val noteMap = noteEntityToFirestoreMap(local)
                        notesCol.document(docId).set(noteMap, SetOptions.merge()).await()
                    } else {
                        // Remote is newer -> Conflict resolution: Remote wins, update local Room
                        val remoteEntity = firestoreDocToNoteEntity(local.id, remoteDoc.data ?: emptyMap())
                        noteDao.updateNote(remoteEntity)
                    }
                }
            }

            // 4. Pull remote notes that don't exist locally
            for (doc in remoteDocs.documents) {
                val idStr = doc.id.removePrefix("note_")
                val localId = idStr.toLongOrNull()
                val existsLocally = allLocalNotes.any { it.id == localId }

                if (!existsLocally) {
                    val remoteEntity = firestoreDocToNoteEntity(0L, doc.data ?: emptyMap())
                    noteDao.insertNote(remoteEntity)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncFolders(): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = authRepository.getCurrentUserId() ?: return@withContext Result.success(Unit)
        val foldersCol = firestore.collection("users").document(userId).collection("folders")

        try {
            val localFolders = folderDao.getAllFolders().firstOrNull() ?: emptyList()
            val remoteDocs = foldersCol.get().await()
            val remoteFoldersMap = remoteDocs.documents.associateBy { it.id }

            for (local in localFolders) {
                val docId = "folder_${local.id}"
                val map = mapOf(
                    "id" to local.id,
                    "name" to local.name,
                    "icon" to local.icon,
                    "color" to local.color,
                    "createdAt" to local.createdAt
                )
                foldersCol.document(docId).set(map, SetOptions.merge()).await()
            }

            for (doc in remoteDocs.documents) {
                val idStr = doc.id.removePrefix("folder_")
                val localId = idStr.toLongOrNull()
                val existsLocally = localFolders.any { it.id == localId || it.name == doc.getString("name") }

                if (!existsLocally) {
                    val newFolder = FolderEntity(
                        name = doc.getString("name") ?: "Folder",
                        icon = doc.getString("icon") ?: "folder",
                        color = (doc.get("color") as? Number)?.toLong() ?: 0xFF4F46E5,
                        createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                    folderDao.insertFolder(newFolder)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncTags(): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = authRepository.getCurrentUserId() ?: return@withContext Result.success(Unit)
        val tagsCol = firestore.collection("users").document(userId).collection("tags")

        try {
            val localTags = tagDao.getAllTags().firstOrNull() ?: emptyList()
            val remoteDocs = tagsCol.get().await()

            for (local in localTags) {
                val docId = "tag_${local.id}"
                val map = mapOf(
                    "id" to local.id,
                    "name" to local.name,
                    "color" to local.color
                )
                tagsCol.document(docId).set(map, SetOptions.merge()).await()
            }

            for (doc in remoteDocs.documents) {
                val name = doc.getString("name") ?: continue
                val existsLocally = localTags.any { it.name.equals(name, ignoreCase = true) }
                if (!existsLocally) {
                    val newTag = TagEntity(
                        name = name,
                        color = (doc.get("color") as? Number)?.toLong() ?: 0xFF3B82F6
                    )
                    tagDao.insertTag(newTag)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncReminders(): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = authRepository.getCurrentUserId() ?: return@withContext Result.success(Unit)
        val remindersCol = firestore.collection("users").document(userId).collection("reminders")

        try {
            val localReminders = reminderDao.getAllReminders().firstOrNull() ?: emptyList()
            for (local in localReminders) {
                val docId = "reminder_${local.id}"
                val map = mapOf(
                    "id" to local.id,
                    "noteId" to local.noteId,
                    "noteTitle" to local.noteTitle,
                    "reminderTime" to local.reminderTime,
                    "isCompleted" to local.isCompleted
                )
                remindersCol.document(docId).set(map, SetOptions.merge()).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun noteEntityToFirestoreMap(entity: NoteEntity): Map<String, Any?> {
        val checklistMaps = entity.checklist.map {
            mapOf("id" to it.id, "text" to it.text, "isChecked" to it.isChecked)
        }
        return mapOf(
            "id" to entity.id,
            "title" to entity.title,
            "content" to entity.content,
            "createdAt" to entity.createdAt,
            "updatedAt" to entity.updatedAt,
            "isPinned" to entity.isPinned,
            "isFavorite" to entity.isFavorite,
            "isArchived" to entity.isArchived,
            "isTrash" to entity.isTrash,
            "folderId" to entity.folderId,
            "folderName" to entity.folderName,
            "tags" to entity.tags,
            "color" to entity.color,
            "noteType" to entity.noteType,
            "checklist" to checklistMaps,
            "reminderTime" to entity.reminderTime,
            "attachments" to entity.attachments
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun firestoreDocToNoteEntity(fallbackId: Long, data: Map<String, Any?>): NoteEntity {
        val rawChecklist = data["checklist"] as? List<Map<String, Any?>> ?: emptyList()
        val checklistItems = rawChecklist.map { map ->
            ChecklistItem(
                id = map["id"] as? String ?: java.util.UUID.randomUUID().toString(),
                text = map["text"] as? String ?: "",
                isChecked = map["isChecked"] as? Boolean ?: false
            )
        }

        return NoteEntity(
            id = (data["id"] as? Number)?.toLong() ?: fallbackId,
            title = data["title"] as? String ?: "",
            content = data["content"] as? String ?: "",
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            isPinned = data["isPinned"] as? Boolean ?: false,
            isFavorite = data["isFavorite"] as? Boolean ?: false,
            isArchived = data["isArchived"] as? Boolean ?: false,
            isTrash = data["isTrash"] as? Boolean ?: false,
            folderId = (data["folderId"] as? Number)?.toLong(),
            folderName = data["folderName"] as? String,
            tags = (data["tags"] as? List<String>) ?: emptyList(),
            color = (data["color"] as? Number)?.toLong() ?: 0L,
            noteType = data["noteType"] as? String ?: NoteType.STANDARD.name,
            checklist = checklistItems,
            reminderTime = (data["reminderTime"] as? Number)?.toLong(),
            attachments = (data["attachments"] as? List<String>) ?: emptyList()
        )
    }

    private fun isNetworkAvailableInitially(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
