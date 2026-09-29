package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.ChecklistItem
import com.example.domain.model.Note
import com.example.domain.model.NoteType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies app name is Noto`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Noto", appName)
    }

    @Test
    fun `note domain model creation and defaults`() {
        val note = Note(
            id = 1L,
            title = "Test Note",
            content = "This is a test note body",
            isPinned = true,
            isFavorite = false,
            tags = listOf("Test", "Android"),
            checklist = listOf(ChecklistItem(text = "Task 1", isChecked = true))
        )

        assertEquals("Test Note", note.title)
        assertTrue(note.isPinned)
        assertFalse(note.isFavorite)
        assertEquals(2, note.tags.size)
        assertEquals(1, note.checklist.size)
        assertTrue(note.checklist[0].isChecked)
        assertEquals(NoteType.STANDARD, note.noteType)
    }
}
