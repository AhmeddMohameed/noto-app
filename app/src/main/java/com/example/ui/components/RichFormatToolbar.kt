package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

enum class RichTextAction {
    BOLD,
    ITALIC,
    UNDERLINE,
    STRIKETHROUGH,
    H1,
    H2,
    BULLET_LIST,
    NUMBERED_LIST,
    CHECKLIST,
    ALIGN_LEFT,
    ALIGN_CENTER,
    ALIGN_RIGHT,
    UNDO,
    REDO
}

@Composable
fun RichFormatToolbar(
    onActionClick: (RichTextAction) -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rich_format_toolbar"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ToolbarButton(
                icon = Icons.AutoMirrored.Filled.Undo,
                description = "Undo",
                enabled = canUndo,
                onClick = { onActionClick(RichTextAction.UNDO) },
                tag = "toolbar_undo"
            )
            ToolbarButton(
                icon = Icons.AutoMirrored.Filled.Redo,
                description = "Redo",
                enabled = canRedo,
                onClick = { onActionClick(RichTextAction.REDO) },
                tag = "toolbar_redo"
            )

            ToolbarButton(
                icon = Icons.Default.Title,
                description = "Heading 1",
                onClick = { onActionClick(RichTextAction.H1) },
                tag = "toolbar_h1"
            )
            ToolbarButton(
                icon = Icons.Default.FormatBold,
                description = "Bold",
                onClick = { onActionClick(RichTextAction.BOLD) },
                tag = "toolbar_bold"
            )
            ToolbarButton(
                icon = Icons.Default.FormatItalic,
                description = "Italic",
                onClick = { onActionClick(RichTextAction.ITALIC) },
                tag = "toolbar_italic"
            )
            ToolbarButton(
                icon = Icons.Default.FormatUnderlined,
                description = "Underline",
                onClick = { onActionClick(RichTextAction.UNDERLINE) },
                tag = "toolbar_underline"
            )
            ToolbarButton(
                icon = Icons.Default.FormatStrikethrough,
                description = "Strikethrough",
                onClick = { onActionClick(RichTextAction.STRIKETHROUGH) },
                tag = "toolbar_strikethrough"
            )

            ToolbarButton(
                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                description = "Bullet list",
                onClick = { onActionClick(RichTextAction.BULLET_LIST) },
                tag = "toolbar_bullet_list"
            )
            ToolbarButton(
                icon = Icons.Default.FormatListNumbered,
                description = "Numbered list",
                onClick = { onActionClick(RichTextAction.NUMBERED_LIST) },
                tag = "toolbar_numbered_list"
            )
            ToolbarButton(
                icon = Icons.Default.CheckBox,
                description = "Checklist",
                onClick = { onActionClick(RichTextAction.CHECKLIST) },
                tag = "toolbar_checklist"
            )

            ToolbarButton(
                icon = Icons.AutoMirrored.Filled.FormatAlignLeft,
                description = "Align Left",
                onClick = { onActionClick(RichTextAction.ALIGN_LEFT) },
                tag = "toolbar_align_left"
            )
            ToolbarButton(
                icon = Icons.Default.FormatAlignCenter,
                description = "Align Center",
                onClick = { onActionClick(RichTextAction.ALIGN_CENTER) },
                tag = "toolbar_align_center"
            )
            ToolbarButton(
                icon = Icons.AutoMirrored.Filled.FormatAlignRight,
                description = "Align Right",
                onClick = { onActionClick(RichTextAction.ALIGN_RIGHT) },
                tag = "toolbar_align_right"
            )
        }
    }
}

@Composable
private fun ToolbarButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    tag: String,
    enabled: Boolean = true
) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(38.dp)
            .focusProperties { canFocus = false }
            .testTag(tag),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier.size(20.dp)
        )
    }
}
