package com.example.di

import android.content.Context
import com.example.data.local.NotoDatabase
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.FolderRepository
import com.example.data.repository.FolderRepositoryImpl
import com.example.data.repository.NoteRepository
import com.example.data.repository.NoteRepositoryImpl
import com.example.data.repository.ReminderRepository
import com.example.data.repository.ReminderRepositoryImpl
import com.example.data.repository.SyncRepository
import com.example.data.repository.SyncRepositoryImpl
import com.example.data.repository.TagRepository
import com.example.data.repository.TagRepositoryImpl
import com.example.data.repository.UserPreferencesRepository
import com.example.data.repository.UserPreferencesRepositoryImpl

interface AppContainer {
    val noteRepository: NoteRepository
    val folderRepository: FolderRepository
    val tagRepository: TagRepository
    val reminderRepository: ReminderRepository
    val userPreferencesRepository: UserPreferencesRepository
    val authRepository: AuthRepository
    val syncRepository: SyncRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: NotoDatabase by lazy {
        NotoDatabase.getDatabase(context)
    }

    override val noteRepository: NoteRepository by lazy {
        NoteRepositoryImpl(database.noteDao())
    }

    override val folderRepository: FolderRepository by lazy {
        FolderRepositoryImpl(database.folderDao())
    }

    override val tagRepository: TagRepository by lazy {
        TagRepositoryImpl(database.tagDao())
    }

    override val reminderRepository: ReminderRepository by lazy {
        ReminderRepositoryImpl(database.reminderDao())
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepositoryImpl(context)
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(userPreferencesRepository = userPreferencesRepository)
    }

    override val syncRepository: SyncRepository by lazy {
        SyncRepositoryImpl(
            context = context,
            authRepository = authRepository,
            noteDao = database.noteDao(),
            folderDao = database.folderDao(),
            tagDao = database.tagDao(),
            reminderDao = database.reminderDao()
        )
    }
}
