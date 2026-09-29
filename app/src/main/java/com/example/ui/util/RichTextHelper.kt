package com.example.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.example.ui.components.RichTextAction
import kotlin.math.max
import kotlin.math.min

object RichTextHelper {

    /**
     * Applies markdown formatting to a TextFieldValue based on the current selection or cursor position.
     * Supports explicit selection to overcome any focus-loss on button click.
     */
    fun applyFormatting(
        current: TextFieldValue,
        action: RichTextAction,
        explicitSelection: TextRange? = null
    ): TextFieldValue {
        val text = current.text

        // Determine effective selection
        val effectiveSelection = when {
            explicitSelection != null && explicitSelection.start != explicitSelection.end &&
                    explicitSelection.start >= 0 && explicitSelection.end <= text.length -> explicitSelection
            current.selection.start != current.selection.end -> current.selection
            else -> null
        }

        val cursor = current.selection.start.coerceIn(0, text.length)

        val (start, end, hasSelection) = if (effectiveSelection != null) {
            val s = min(effectiveSelection.start, effectiveSelection.end).coerceIn(0, text.length)
            val e = max(effectiveSelection.start, effectiveSelection.end).coerceIn(0, text.length)
            Triple(s, e, true)
        } else {
            // Find word boundary under cursor if possible
            val wordBounds = findWordBounds(text, cursor)
            if (wordBounds != null) {
                Triple(wordBounds.first, wordBounds.second, true)
            } else {
                Triple(cursor, cursor, false)
            }
        }

        return when (action) {
            RichTextAction.BOLD -> {
                if (hasSelection && start < end) {
                    // Check if already enclosed in **...**
                    if (start >= 2 && end + 2 <= text.length &&
                        text.substring(start - 2, start) == "**" &&
                        text.substring(end, end + 2) == "**"
                    ) {
                        // Toggle off outer **
                        val before = text.substring(0, start - 2)
                        val inner = text.substring(start, end)
                        val after = text.substring(end + 2)
                        TextFieldValue(before + inner + after, TextRange(start - 2, start - 2 + inner.length))
                    } else {
                        val selected = text.substring(start, end)
                        if (selected.startsWith("**") && selected.endsWith("**") && selected.length >= 4) {
                            val unwrapped = selected.substring(2, selected.length - 2)
                            val newText = text.replaceRange(start, end, unwrapped)
                            TextFieldValue(newText, TextRange(start, start + unwrapped.length))
                        } else {
                            val wrapped = "**$selected**"
                            val newText = text.replaceRange(start, end, wrapped)
                            TextFieldValue(newText, TextRange(start + 2, end + 2))
                        }
                    }
                } else {
                    val placeholder = "Bold text"
                    val newText = text.substring(0, cursor) + "**$placeholder**" + text.substring(cursor)
                    TextFieldValue(newText, TextRange(cursor + 2, cursor + 2 + placeholder.length))
                }
            }

            RichTextAction.ITALIC -> {
                if (hasSelection && start < end) {
                    if (start >= 1 && end + 1 <= text.length &&
                        text[start - 1] == '*' && text[end] == '*' &&
                        (start < 2 || text[start - 2] != '*')
                    ) {
                        val before = text.substring(0, start - 1)
                        val inner = text.substring(start, end)
                        val after = text.substring(end + 1)
                        TextFieldValue(before + inner + after, TextRange(start - 1, start - 1 + inner.length))
                    } else {
                        val selected = text.substring(start, end)
                        if (selected.startsWith("*") && selected.endsWith("*") && selected.length >= 2 && !selected.startsWith("**")) {
                            val unwrapped = selected.substring(1, selected.length - 1)
                            val newText = text.replaceRange(start, end, unwrapped)
                            TextFieldValue(newText, TextRange(start, start + unwrapped.length))
                        } else {
                            val wrapped = "*$selected*"
                            val newText = text.replaceRange(start, end, wrapped)
                            TextFieldValue(newText, TextRange(start + 1, end + 1))
                        }
                    }
                } else {
                    val placeholder = "Italic text"
                    val newText = text.substring(0, cursor) + "*$placeholder*" + text.substring(cursor)
                    TextFieldValue(newText, TextRange(cursor + 1, cursor + 1 + placeholder.length))
                }
            }

            RichTextAction.UNDERLINE -> {
                if (hasSelection && start < end) {
                    val selected = text.substring(start, end)
                    if (selected.startsWith("<u>") && selected.endsWith("</u>") && selected.length >= 7) {
                        val unwrapped = selected.substring(3, selected.length - 4)
                        val newText = text.replaceRange(start, end, unwrapped)
                        TextFieldValue(newText, TextRange(start, start + unwrapped.length))
                    } else {
                        val wrapped = "<u>$selected</u>"
                        val newText = text.replaceRange(start, end, wrapped)
                        TextFieldValue(newText, TextRange(start + 3, end + 3))
                    }
                } else {
                    val placeholder = "Underlined text"
                    val newText = text.substring(0, cursor) + "<u>$placeholder</u>" + text.substring(cursor)
                    TextFieldValue(newText, TextRange(cursor + 3, cursor + 3 + placeholder.length))
                }
            }

            RichTextAction.STRIKETHROUGH -> {
                if (hasSelection && start < end) {
                    val selected = text.substring(start, end)
                    if (selected.startsWith("~~") && selected.endsWith("~~") && selected.length >= 4) {
                        val unwrapped = selected.substring(2, selected.length - 2)
                        val newText = text.replaceRange(start, end, unwrapped)
                        TextFieldValue(newText, TextRange(start, start + unwrapped.length))
                    } else {
                        val wrapped = "~~$selected~~"
                        val newText = text.replaceRange(start, end, wrapped)
                        TextFieldValue(newText, TextRange(start + 2, end + 2))
                    }
                } else {
                    val placeholder = "Strikethrough text"
                    val newText = text.substring(0, cursor) + "~~$placeholder~~" + text.substring(cursor)
                    TextFieldValue(newText, TextRange(cursor + 2, cursor + 2 + placeholder.length))
                }
            }

            RichTextAction.H1 -> {
                toggleLinePrefix(current, "# ")
            }

            RichTextAction.H2 -> {
                toggleLinePrefix(current, "## ")
            }

            RichTextAction.BULLET_LIST -> {
                toggleLinePrefix(current, "• ")
            }

            RichTextAction.NUMBERED_LIST -> {
                toggleLinePrefix(current, "1. ")
            }

            RichTextAction.ALIGN_LEFT -> {
                toggleLinePrefix(current, "")
            }

            RichTextAction.ALIGN_CENTER -> {
                toggleLinePrefix(current, "   ")
            }

            RichTextAction.ALIGN_RIGHT -> {
                toggleLinePrefix(current, "      ")
            }

            else -> current
        }
    }

    private fun findWordBounds(text: String, cursor: Int): Pair<Int, Int>? {
        if (text.isEmpty() || cursor < 0 || cursor > text.length) return null
        var start = cursor
        var end = cursor

        val isAtWordChar = cursor < text.length && !text[cursor].isWhitespace()
        val isAfterWordChar = cursor > 0 && !text[cursor - 1].isWhitespace()

        if (!isAtWordChar && !isAfterWordChar) return null

        if (isAfterWordChar) {
            start = cursor - 1
            while (start > 0 && !text[start - 1].isWhitespace()) {
                start--
            }
        }
        while (end < text.length && !text[end].isWhitespace()) {
            end++
        }
        return if (start < end) Pair(start, end) else null
    }

    private fun toggleLinePrefix(current: TextFieldValue, prefix: String): TextFieldValue {
        val text = current.text
        val cursor = current.selection.start.coerceIn(0, text.length)

        var lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0))
        lineStart = if (lineStart == -1) 0 else lineStart + 1

        var lineEnd = text.indexOf('\n', cursor)
        if (lineEnd == -1) lineEnd = text.length

        val currentLine = text.substring(lineStart, lineEnd)

        val newLine = when {
            prefix.isEmpty() -> currentLine.trimStart('#', ' ', '•', '1', '.')
            currentLine.startsWith(prefix) -> currentLine.removePrefix(prefix)
            else -> {
                val cleaned = currentLine.trimStart('#', ' ', '•', '1', '.')
                "$prefix$cleaned"
            }
        }

        val newText = text.replaceRange(lineStart, lineEnd, newLine)
        val offsetDiff = newLine.length - currentLine.length
        val newCursor = (cursor + offsetDiff).coerceIn(0, newText.length)
        return TextFieldValue(newText, TextRange(newCursor))
    }

    /**
     * VisualTransformation for the TextField so that markdown styles (bold, italic, strikethrough, underline, headers)
     * are rendered with real rich styling in the editor while preserving 1:1 character index offsets!
     */
    fun createMarkdownVisualTransformation(
        textColor: Color,
        accentColor: Color
    ): VisualTransformation {
        return VisualTransformation { text ->
            val annotated = buildMarkdownAnnotatedString(text.text, textColor, accentColor)
            TransformedText(annotated, OffsetMapping.Identity)
        }
    }

    /**
     * Parses markdown text into an AnnotatedString for viewing in NoteDetailsScreen or NoteCard.
     */
    fun parseToDisplayAnnotatedString(
        rawText: String,
        textColor: Color,
        accentColor: Color
    ): AnnotatedString {
        return buildDisplayAnnotatedString(rawText, textColor, accentColor)
    }

    /**
     * Builds styled AnnotatedString retaining all character positions (for editor with 1:1 OffsetMapping).
     */
    private fun buildMarkdownAnnotatedString(
        text: String,
        textColor: Color,
        accentColor: Color
    ): AnnotatedString {
        return buildAnnotatedString {
            append(text)

            val tokenColor = textColor.copy(alpha = 0.35f)

            // 1. Headings: lines starting with # or ##
            val lines = text.split('\n')
            var currentOffset = 0
            for (line in lines) {
                if (line.startsWith("# ")) {
                    addStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = accentColor
                        ),
                        currentOffset,
                        currentOffset + line.length
                    )
                    addStyle(SpanStyle(color = tokenColor, fontWeight = FontWeight.Normal), currentOffset, currentOffset + 2)
                } else if (line.startsWith("## ")) {
                    addStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = Color(0xFF0D9488)
                        ),
                        currentOffset,
                        currentOffset + line.length
                    )
                    addStyle(SpanStyle(color = tokenColor, fontWeight = FontWeight.Normal), currentOffset, currentOffset + 3)
                } else if (line.startsWith("• ")) {
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold, color = accentColor), currentOffset, currentOffset + 2)
                }
                currentOffset += line.length + 1
            }

            // 2. Bold: **...**
            val boldRegex = Regex("""\*\*(.*?)\*\*""")
            boldRegex.findAll(text).forEach { match ->
                val range = match.range
                addStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        background = accentColor.copy(alpha = 0.12f)
                    ),
                    range.first,
                    range.last + 1
                )
                addStyle(SpanStyle(color = tokenColor), range.first, range.first + 2)
                addStyle(SpanStyle(color = tokenColor), range.last - 1, range.last + 1)
            }

            // 3. Italic: *...*
            val italicRegex = Regex("""(?<!\*)\*([^*]+)\*(?!\*)""")
            italicRegex.findAll(text).forEach { match ->
                val range = match.range
                addStyle(
                    SpanStyle(
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF0D9488),
                        background = Color(0xFF0D9488).copy(alpha = 0.12f)
                    ),
                    range.first,
                    range.last + 1
                )
                addStyle(SpanStyle(color = tokenColor), range.first, range.first + 1)
                addStyle(SpanStyle(color = tokenColor), range.last, range.last + 1)
            }

            // 4. Strikethrough: ~~...~~
            val strikeRegex = Regex("""~~(.*?)~~""")
            strikeRegex.findAll(text).forEach { match ->
                val range = match.range
                addStyle(
                    SpanStyle(
                        textDecoration = TextDecoration.LineThrough,
                        color = Color(0xFFEF4444),
                        background = Color(0xFFEF4444).copy(alpha = 0.12f)
                    ),
                    range.first,
                    range.last + 1
                )
                addStyle(SpanStyle(color = tokenColor), range.first, range.first + 2)
                addStyle(SpanStyle(color = tokenColor), range.last - 1, range.last + 1)
            }

            // 5. Underline: <u>...</u>
            val underlineRegex = Regex("""<u>(.*?)</u>""", RegexOption.IGNORE_CASE)
            underlineRegex.findAll(text).forEach { match ->
                val range = match.range
                addStyle(
                    SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        color = Color(0xFF7C3AED),
                        background = Color(0xFF7C3AED).copy(alpha = 0.12f)
                    ),
                    range.first,
                    range.last + 1
                )
                addStyle(SpanStyle(color = tokenColor), range.first, range.first + 3)
                addStyle(SpanStyle(color = tokenColor), range.last - 3, range.last + 1)
            }
        }
    }

    /**
     * Builds cleanly formatted AnnotatedString for display (stripping markdown tokens and applying styles).
     */
    private fun buildDisplayAnnotatedString(
        rawText: String,
        textColor: Color,
        accentColor: Color
    ): AnnotatedString {
        return buildAnnotatedString {
            val lines = rawText.split('\n')
            lines.forEachIndexed { index, line ->
                val isH1 = line.startsWith("# ")
                val isH2 = line.startsWith("## ")
                val isBullet = line.startsWith("• ")
                val isNumbered = Regex("""^\d+\.\s""").containsMatchIn(line)

                val rawLineContent = when {
                    isH1 -> line.removePrefix("# ")
                    isH2 -> line.removePrefix("## ")
                    else -> line
                }

                val lineStart = length

                val regex = Regex("""(\*\*(.*?)\*\*)|((?<!\*)\*([^*]+)\*(?!\*))|(~~(.*?)~~)|(<u>(.*?)</u>)""", RegexOption.IGNORE_CASE)
                var lastIdx = 0
                regex.findAll(rawLineContent).forEach { match ->
                    if (match.range.first > lastIdx) {
                        append(rawLineContent.substring(lastIdx, match.range.first))
                    }
                    val tokenStart = length
                    when {
                        match.value.startsWith("**") -> {
                            val inner = match.groupValues[2]
                            append(inner)
                            addStyle(SpanStyle(fontWeight = FontWeight.Bold), tokenStart, length)
                        }
                        match.value.startsWith("~~") -> {
                            val inner = match.groupValues[6]
                            append(inner)
                            addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), tokenStart, length)
                        }
                        match.value.startsWith("<u", ignoreCase = true) -> {
                            val inner = match.groupValues[8]
                            append(inner)
                            addStyle(SpanStyle(textDecoration = TextDecoration.Underline), tokenStart, length)
                        }
                        match.value.startsWith("*") -> {
                            val inner = match.groupValues[4]
                            append(inner)
                            addStyle(SpanStyle(fontStyle = FontStyle.Italic), tokenStart, length)
                        }
                    }
                    lastIdx = match.range.last + 1
                }
                if (lastIdx < rawLineContent.length) {
                    append(rawLineContent.substring(lastIdx))
                }
                val lineEnd = length

                if (isH1) {
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, color = accentColor), lineStart, lineEnd)
                } else if (isH2) {
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = accentColor), lineStart, lineEnd)
                }

                if (index < lines.size - 1) {
                    append("\n")
                }
            }
        }
    }
}
