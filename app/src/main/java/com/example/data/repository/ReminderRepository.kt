package com.example.data.repository

import com.example.data.local.dao.ReminderDao
import com.example.data.local.entity.ReminderEntity
import com.example.domain.model.Reminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ReminderRepository {
    fun getAllReminders(): Flow<List<Reminder>>
    fun getUpcomingReminders(now: Long = System.currentTimeMillis()): Flow<List<Reminder>>
    suspend fun insertReminder(reminder: Reminder): Long
    suspend fun updateReminder(reminder: Reminder)
    suspend fun deleteReminderById(id: Long)
    suspend fun deleteRemindersForNote(noteId: Long)
}

class ReminderRepositoryImpl(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override fun getAllReminders(): Flow<List<Reminder>> {
        return reminderDao.getAllReminders().map { list -> list.map { it.toDomain() } }
    }

    override fun getUpcomingReminders(now: Long): Flow<List<Reminder>> {
        return reminderDao.getUpcomingReminders(now).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(ReminderEntity.fromDomain(reminder))
    }

    override suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(ReminderEntity.fromDomain(reminder))
    }

    override suspend fun deleteReminderById(id: Long) {
        reminderDao.deleteReminderById(id)
    }

    override suspend fun deleteRemindersForNote(noteId: Long) {
        reminderDao.deleteRemindersForNote(noteId)
    }
}
