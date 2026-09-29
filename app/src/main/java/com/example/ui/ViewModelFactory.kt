package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.di.AppContainer
import com.example.ui.screens.auth.AuthViewModel
import com.example.ui.screens.editor.NoteEditorViewModel
import com.example.ui.screens.folders.FoldersViewModel
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.search.SearchViewModel
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.tags.TagsViewModel
import com.example.ui.screens.trash.TrashViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val appContainer: AppContainer,
    private val extraNoteId: Long = -1L
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(
                    authRepository = appContainer.authRepository,
                    syncRepository = appContainer.syncRepository,
                    userPreferencesRepository = appContainer.userPreferencesRepository
                ) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    noteRepository = appContainer.noteRepository,
                    folderRepository = appContainer.folderRepository,
                    tagRepository = appContainer.tagRepository,
                    userPreferencesRepository = appContainer.userPreferencesRepository
                ) as T
            }
            modelClass.isAssignableFrom(NoteEditorViewModel::class.java) -> {
                NoteEditorViewModel(
                    noteId = extraNoteId,
                    noteRepository = appContainer.noteRepository,
                    folderRepository = appContainer.folderRepository,
                    tagRepository = appContainer.tagRepository,
                    reminderRepository = appContainer.reminderRepository,
                    syncRepository = appContainer.syncRepository
                ) as T
            }
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(
                    noteRepository = appContainer.noteRepository,
                    folderRepository = appContainer.folderRepository,
                    tagRepository = appContainer.tagRepository
                ) as T
            }
            modelClass.isAssignableFrom(FoldersViewModel::class.java) -> {
                FoldersViewModel(
                    folderRepository = appContainer.folderRepository,
                    noteRepository = appContainer.noteRepository
                ) as T
            }
            modelClass.isAssignableFrom(TagsViewModel::class.java) -> {
                TagsViewModel(
                    tagRepository = appContainer.tagRepository,
                    noteRepository = appContainer.noteRepository
                ) as T
            }
            modelClass.isAssignableFrom(TrashViewModel::class.java) -> {
                TrashViewModel(
                    noteRepository = appContainer.noteRepository
                ) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    userPreferencesRepository = appContainer.userPreferencesRepository,
                    authRepository = appContainer.authRepository,
                    syncRepository = appContainer.syncRepository,
                    noteRepository = appContainer.noteRepository,
                    folderRepository = appContainer.folderRepository,
                    tagRepository = appContainer.tagRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
