package com.example

import com.example.data.local.converter.NotoConverters
import com.example.domain.model.ChecklistItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotoConvertersTest {

    private val converters = NotoConverters()

    @Test
    fun `test string list converter`() {
        val list = listOf("Work", "Design", "Personal")
        val json = converters.fromStringList(list)
        val restored = converters.toStringList(json)

        assertEquals(3, restored.size)
        assertEquals("Work", restored[0])
        assertEquals("Design", restored[1])
        assertEquals("Personal", restored[2])
    }

    @Test
    fun `test checklist converter`() {
        val items = listOf(
            ChecklistItem(id = "1", text = "Buy milk", isChecked = false),
            ChecklistItem(id = "2", text = "Ship app", isChecked = true)
        )
        val json = converters.fromChecklist(items)
        val restored = converters.toChecklist(json)

        assertEquals(2, restored.size)
        assertEquals("Buy milk", restored[0].text)
        assertFalse(restored[0].isChecked)
        assertEquals("Ship app", restored[1].text)
        assertTrue(restored[1].isChecked)
    }
}
