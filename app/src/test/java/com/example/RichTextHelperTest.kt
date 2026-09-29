package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import com.example.ui.components.RichTextAction
import com.example.ui.util.RichTextHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RichTextHelperTest {

    @Test
    fun `bold formatting wraps selected text`() {
        val original = TextFieldValue("Hello World", TextRange(0, 5)) // "Hello" selected
        val formatted = RichTextHelper.applyFormatting(original, RichTextAction.BOLD)
        assertEquals("**Hello** World", formatted.text)
        assertEquals(TextRange(2, 7), formatted.selection)
    }

    @Test
    fun `bold formatting toggles off when already bold`() {
        val original = TextFieldValue("**Hello** World", TextRange(0, 9)) // "**Hello**" selected
        val formatted = RichTextHelper.applyFormatting(original, RichTextAction.BOLD)
        assertEquals("Hello World", formatted.text)
    }

    @Test
    fun `bold formatting inserts placeholder when nothing selected`() {
        val original = TextFieldValue("Note: ", TextRange(6, 6)) // cursor at end
        val formatted = RichTextHelper.applyFormatting(original, RichTextAction.BOLD)
        assertEquals("Note: **Bold text**", formatted.text)
        assertEquals(TextRange(8, 17), formatted.selection) // "Bold text" selected
    }

    @Test
    fun `italic formatting wraps selected text`() {
        val original = TextFieldValue("Important meeting", TextRange(10, 17)) // "meeting" selected
        val formatted = RichTextHelper.applyFormatting(original, RichTextAction.ITALIC)
        assertEquals("Important *meeting*", formatted.text)
    }

    @Test
    fun `strikethrough formatting wraps selected text`() {
        val original = TextFieldValue("Completed task", TextRange(0, 9)) // "Completed" selected
        val formatted = RichTextHelper.applyFormatting(original, RichTextAction.STRIKETHROUGH)
        assertEquals("~~Completed~~ task", formatted.text)
    }

    @Test
    fun `underline formatting wraps selected text`() {
        val original = TextFieldValue("Underlined word", TextRange(0, 10)) // "Underlined" selected
        val formatted = RichTextHelper.applyFormatting(original, RichTextAction.UNDERLINE)
        assertEquals("<u>Underlined</u> word", formatted.text)
    }

    @Test
    fun `h1 heading toggles line prefix`() {
        val original = TextFieldValue("My Title\nSome content", TextRange(3, 3)) // cursor on first line
        val formatted = RichTextHelper.applyFormatting(original, RichTextAction.H1)
        assertEquals("# My Title\nSome content", formatted.text)

        // Toggle off
        val toggled = RichTextHelper.applyFormatting(formatted, RichTextAction.H1)
        assertEquals("My Title\nSome content", toggled.text)
    }

    @Test
    fun `bullet list toggles line prefix`() {
        val original = TextFieldValue("First item\nSecond item", TextRange(2, 2))
        val formatted = RichTextHelper.applyFormatting(original, RichTextAction.BULLET_LIST)
        assertEquals("• First item\nSecond item", formatted.text)

        val toggled = RichTextHelper.applyFormatting(formatted, RichTextAction.BULLET_LIST)
        assertEquals("First item\nSecond item", toggled.text)
    }

    @Test
    fun `bold formatting with explicit selection overrides collapsed selection`() {
        val original = TextFieldValue("Hello World", TextRange(0, 0)) // cursor collapsed at 0
        val explicitSel = TextRange(6, 11) // "World" was previously highlighted
        val formatted = RichTextHelper.applyFormatting(original, RichTextAction.BOLD, explicitSel)
        assertEquals("Hello **World**", formatted.text)
    }

    @Test
    fun `bold formatting on cursor inside word bolds the entire word`() {
        val original = TextFieldValue("Quick brown fox", TextRange(8, 8)) // cursor inside "brown"
        val formatted = RichTextHelper.applyFormatting(original, RichTextAction.BOLD)
        assertEquals("Quick **brown** fox", formatted.text)
    }

    @Test
    fun `parseToDisplayAnnotatedString applies bold and italic styles`() {
        val markdown = "This is **bold** and *italic* text."
        val annotated = RichTextHelper.parseToDisplayAnnotatedString(
            markdown,
            Color.Black,
            Color.Blue
        )

        // The raw string content stripped of markdown markers for display
        assertEquals("This is bold and italic text.", annotated.text)

        // Check that bold style was applied
        val boldSpan = annotated.spanStyles.find { it.item.fontWeight == FontWeight.Bold }
        assertTrue("Bold span should be present", boldSpan != null)

        // Check that italic style was applied
        val italicSpan = annotated.spanStyles.find { it.item.fontStyle == FontStyle.Italic }
        assertTrue("Italic span should be present", italicSpan != null)
    }
}
