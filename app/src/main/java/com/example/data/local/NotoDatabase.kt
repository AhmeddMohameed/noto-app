package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.converter.NotoConverters
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        NoteEntity::class,
        FolderEntity::class,
        TagEntity::class,
        ReminderEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(NotoConverters::class)
abstract class NotoDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun tagDao(): TagDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: NotoDatabase? = null

        fun getDatabase(context: Context): NotoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotoDatabase::class.java,
                    "noto_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(NotoDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class NotoDatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: NotoDatabase) {
                val folderDao = database.folderDao()
                val tagDao = database.tagDao()
                val noteDao = database.noteDao()

                // Initial Folders
                val personalFolderId = folderDao.insertFolder(
                    FolderEntity(name = "Personal", icon = "favorite", color = 0xFF6366F1)
                )
                val workFolderId = folderDao.insertFolder(
                    FolderEntity(name = "Work", icon = "work", color = 0xFF0D9488)
                )
                folderDao.insertFolder(
                    FolderEntity(name = "Ideas", icon = "lightbulb", color = 0xFFF59E0B)
                )

                // Initial Tags
                tagDao.insertTag(TagEntity(name = "Important", color = 0xFFEF4444))
                tagDao.insertTag(TagEntity(name = "Daily", color = 0xFF3B82F6))
                tagDao.insertTag(TagEntity(name = "Productivity", color = 0xFF10B981))
                tagDao.insertTag(TagEntity(name = "Books", color = 0xFF8B5CF6))

                // Welcome Starter Note
                val welcomeChecklist = listOf(
                    ChecklistItem(text = "Explore Noto clean design", isChecked = true),
                    ChecklistItem(text = "Create your first custom note", isChecked = false),
                    ChecklistItem(text = "Try formatting text and checklists", isChecked = false),
                    ChecklistItem(text = "Organize with folders and tags", isChecked = false),
                    ChecklistItem(text = "Switch between Dark and Light mode in Settings", isChecked = false)
                )

                noteDao.insertNote(
                    NoteEntity(
                        title = "Welcome to Noto ✦",
                        content = "Noto is your sleek, distraction-free space for capturing thoughts, checklists, and great ideas.\n\n" +
                                "✨ Key Capabilities:\n" +
                                "• Instant auto-save so your writing is never lost\n" +
                                "• Rich text formatting (Bold, Italic, Headings, Lists)\n" +
                                "• Interactive checklists with live progress\n" +
                                "• Folder and color-coding organization\n" +
                                "• Full offline privacy with Room database\n" +
                                "• Dark and Light themes crafted for focus\n\n" +
                                "Tap the editor icons below to format your notes, or start a fresh one using the floating button!",
                        isPinned = true,
                        isFavorite = true,
                        folderId = personalFolderId,
                        folderName = "Personal",
                        tags = listOf("Important", "Productivity"),
                        color = 0xFF1E293B,
                        noteType = NoteType.CHECKLIST.name,
                        checklist = welcomeChecklist
                    )
                )

                // Second starter note
                noteDao.insertNote(
                    NoteEntity(
                        title = "Product Strategy & Architecture",
                        content = "Clean Architecture principles applied to modern mobile apps:\n\n" +
                                "1. Presentation Layer: Jetpack Compose + MVVM\n" +
                                "2. Domain Layer: Business rules & Models\n" +
                                "3. Data Layer: Repository Pattern + Local Room DB\n\n" +
                                "Ready for extensions, attachments, and export!",
                        isPinned = false,
                        isFavorite = false,
                        folderId = workFolderId,
                        folderName = "Work",
                        tags = listOf("Productivity"),
                        color = 0xFF0D9488,
                        noteType = NoteType.WORK.name
                    )
                )
            }
        }
    }
}
