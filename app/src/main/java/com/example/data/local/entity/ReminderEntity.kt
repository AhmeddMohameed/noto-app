package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Reminder

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Long,
    val noteTitle: String,
    val reminderTime: Long,
    val isCompleted: Boolean = false
) {
    fun toDomain(): Reminder = Reminder(
        id = id,
        noteId = noteId,
        noteTitle = noteTitle,
        reminderTime = reminderTime,
        isCompleted = isCompleted
    )

    companion object {
        fun fromDomain(reminder: Reminder): ReminderEntity = ReminderEntity(
            id = reminder.id,
            noteId = reminder.noteId,
            noteTitle = reminder.noteTitle,
            reminderTime = reminder.reminderTime,
            isCompleted = reminder.isCompleted
        )
    }
}
