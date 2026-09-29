package com.example.data.repository

import kotlinx.coroutines.flow.StateFlow

data class SyncState(
    val isSyncing: Boolean = false,
    val isOnline: Boolean = true,
    val lastSyncTime: Long? = null,
    val pendingCount: Int = 0,
    val syncMessage: String? = null,
    val error: String? = null
)

interface SyncRepository {
    val syncState: StateFlow<SyncState>
    suspend fun syncAll(): Result<Unit>
    suspend fun syncNotes(): Result<Unit>
    suspend fun syncFolders(): Result<Unit>
    suspend fun syncTags(): Result<Unit>
    suspend fun syncReminders(): Result<Unit>
    fun startAutoSync()
}
