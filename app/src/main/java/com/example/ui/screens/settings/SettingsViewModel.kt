package com.example.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import com.example.data.repository.FolderRepository
import com.example.data.repository.NoteRepository
import com.example.data.repository.NotesViewMode
import com.example.data.repository.SyncRepository
import com.example.data.repository.SyncState
import com.example.data.repository.TagRepository
import com.example.data.repository.ThemeMode
import com.example.data.repository.UserPreferencesRepository
import com.example.domain.model.UserProfile
import com.example.ui.util.NoteShareHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = userPreferencesRepository.themeMode
    val viewMode: StateFlow<NotesViewMode> = userPreferencesRepository.viewMode
    val isLoggedIn: StateFlow<Boolean> = userPreferencesRepository.isLoggedIn
    val userEmail: StateFlow<String?> = userPreferencesRepository.userEmail
    val userName: StateFlow<String?> = userPreferencesRepository.userName
    val isAppLockEnabled: StateFlow<Boolean> = userPreferencesRepository.isAppLockEnabled
    val appLockPin: StateFlow<String?> = userPreferencesRepository.appLockPin

    val currentUser: StateFlow<UserProfile?> = authRepository.currentUser
    val syncState: StateFlow<SyncState> = syncRepository.syncState

    private val _language = MutableStateFlow("English")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        userPreferencesRepository.setThemeMode(mode)
    }

    fun setViewMode(mode: NotesViewMode) {
        userPreferencesRepository.setViewMode(mode)
    }

    fun setLanguage(lang: String) {
        _language.value = lang
    }

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    fun setAppLock(enabled: Boolean, pin: String? = null) {
        userPreferencesRepository.setAppLock(enabled, pin)
    }

    fun triggerSync() {
        viewModelScope.launch {
            syncRepository.syncAll()
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onLoggedOut()
        }
    }

    suspend fun createBackupFile(context: Context): File? {
        val notes = noteRepository.getAllActiveNotes().firstOrNull() ?: emptyList()
        val folders = folderRepository.getAllFolders().firstOrNull() ?: emptyList()
        val tags = tagRepository.getAllTags().firstOrNull() ?: emptyList()
        return NoteShareHelper.exportFullBackup(context, notes, folders, tags)
    }

    suspend fun restoreBackup(jsonString: String) {
        val (notes, folders, tags) = NoteShareHelper.parseBackupJson(jsonString)
        for (f in folders) { folderRepository.insertFolder(f) }
        for (t in tags) { tagRepository.insertTag(t) }
        for (n in notes) { noteRepository.insertOrUpdate(n) }
        syncRepository.syncAll()
    }
}
