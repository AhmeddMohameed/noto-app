package com.example.ui.screens.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.text.TextRange
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Folder
import com.example.domain.model.NoteType
import com.example.ui.components.ColorPickerRow
import com.example.ui.components.RemovableTagChip
import com.example.ui.components.RichFormatToolbar
import com.example.ui.util.RichTextHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteEditorViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showMoreMenu by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var isPreviewMode by remember { mutableStateOf(false) }
    var lastSelection by remember { mutableStateOf(TextRange.Zero) }

    val handleBack = {
        viewModel.saveNow()
        onNavigateBack()
    }

    BackHandler {
        handleBack()
    }

    val backgroundColor = if (uiState.color != 0L) {
        Color(uiState.color)
    } else {
        MaterialTheme.colorScheme.background
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("note_editor_screen")
            .imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (uiState.isSaving) "Saving..." else "Saved",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.testTag("editor_save_status")
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = handleBack,
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Save and go back"
                        )
                    }
                },
                actions = {
                    // Pin
                    IconButton(
                        onClick = { viewModel.togglePin() },
                        modifier = Modifier.testTag("editor_pin_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Note",
                            tint = if (uiState.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Favorite
                    IconButton(
                        onClick = { viewModel.toggleFavorite() },
                        modifier = Modifier.testTag("editor_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite Note",
                            tint = if (uiState.isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Color Swatch
                    IconButton(
                        onClick = { showColorPicker = !showColorPicker },
                        modifier = Modifier.testTag("editor_color_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = "Pick Color"
                        )
                    }

                    // Folder
                    IconButton(
                        onClick = { showFolderDialog = true },
                        modifier = Modifier.testTag("editor_folder_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Assign Folder",
                            tint = if (uiState.selectedFolderId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Tag
                    IconButton(
                        onClick = { showTagDialog = true },
                        modifier = Modifier.testTag("editor_tag_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tag,
                            contentDescription = "Add Tag"
                        )
                    }

                    // Preview / Edit Mode Toggle
                    IconButton(
                        onClick = { isPreviewMode = !isPreviewMode },
                        modifier = Modifier.testTag("editor_preview_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isPreviewMode) Icons.Default.Edit else Icons.Default.Visibility,
                            contentDescription = if (isPreviewMode) "Edit Mode" else "Preview Mode",
                            tint = if (isPreviewMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // More Menu
                    IconButton(
                        onClick = { showMoreMenu = true },
                        modifier = Modifier.testTag("editor_more_button")
                    ) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Options")
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Reminder") },
                            leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                showReminderDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (uiState.isArchived) "Unarchive" else "Archive") },
                            leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                viewModel.toggleArchive()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete to Trash", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMoreMenu = false
                                viewModel.deleteNote(onDeleted = onNavigateBack)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        bottomBar = {
            if (!isPreviewMode) {
                RichFormatToolbar(
                    onActionClick = { action ->
                        viewModel.applyRichTextAction(action, lastSelection)
                    },
                    canUndo = uiState.canUndo,
                    canRedo = uiState.canRedo
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor)
        ) {
            // Optional inline color picker
            AnimatedVisibility(visible = showColorPicker) {
                ColorPickerRow(
                    selectedColor = uiState.color,
                    onColorSelected = {
                        viewModel.setColor(it)
                        showColorPicker = false
                    }
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Folder / Tags / Reminder Badges Row
                if (uiState.selectedFolderName != null || uiState.tags.isNotEmpty() || uiState.reminderTime != null) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (uiState.selectedFolderName != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = uiState.selectedFolderName ?: "",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove folder",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { viewModel.setFolder(null) }
                                    )
                                }
                            }
                        }

                        uiState.tags.forEach { tag ->
                            RemovableTagChip(
                                tagName = tag,
                                onRemove = { viewModel.removeTag(tag) }
                            )
                        }

                        if (uiState.reminderTime != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(uiState.reminderTime!!)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove reminder",
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { viewModel.setReminder(null) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Title Input
                BasicTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("editor_title_input"),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (uiState.title.isEmpty()) {
                            Text(
                                text = "Note Title",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                        }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Checklist Items Section
                if (uiState.checklist.isNotEmpty() || uiState.noteType == NoteType.CHECKLIST) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        uiState.checklist.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isChecked,
                                    onCheckedChange = { viewModel.toggleChecklistItem(item.id) },
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                BasicTextField(
                                    value = item.text,
                                    onValueChange = { viewModel.updateChecklistItemText(item.id, it) },
                                    modifier = Modifier.weight(1f),
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    decorationBox = { inner ->
                                        if (item.text.isEmpty()) {
                                            Text(
                                                text = "List item...",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                        }
                                        inner()
                                    }
                                )
                                IconButton(
                                    onClick = { viewModel.removeChecklistItem(item.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove item",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        TextButton(
                            onClick = { viewModel.addChecklistItem("") },
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .testTag("editor_add_checklist_item_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add checklist item")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Note Body: Switch between Interactive Editor and Rich Preview
                val editorTextColor = MaterialTheme.colorScheme.onSurface
                val editorAccentColor = MaterialTheme.colorScheme.primary

                if (isPreviewMode) {
                    val formattedDisplay = remember(uiState.content, editorTextColor, editorAccentColor) {
                        RichTextHelper.parseToDisplayAnnotatedString(uiState.content, editorTextColor, editorAccentColor)
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("editor_preview_container")
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "Preview Mode (Tap eye icon to return to edit)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (uiState.content.isBlank()) {
                            Text(
                                text = "No content to preview.",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 28.sp,
                                    color = editorTextColor.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("editor_preview_text")
                            )
                        } else {
                            Text(
                                text = formattedDisplay,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 28.sp,
                                    color = editorTextColor
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("editor_preview_text")
                            )
                        }
                    }
                } else {
                    BasicTextField(
                        value = uiState.contentValue,
                        onValueChange = { newValue ->
                            if (newValue.selection.start != newValue.selection.end) {
                                lastSelection = newValue.selection
                            }
                            viewModel.updateContentValue(newValue)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .testTag("editor_content_input"),
                        visualTransformation = remember(editorTextColor, editorAccentColor) {
                            RichTextHelper.createMarkdownVisualTransformation(editorTextColor, editorAccentColor)
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = editorTextColor,
                            lineHeight = 26.sp
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            if (uiState.content.isEmpty()) {
                                Text(
                                    text = "Start typing your note here…\nHighlight text and tap Bold/Italic/Underline to format.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    lineHeight = 26.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }

    // Folder selection dialog
    if (showFolderDialog) {
        AlertDialog(
            onDismissRequest = { showFolderDialog = false },
            title = { Text("Select Folder") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            viewModel.setFolder(null)
                            showFolderDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("No Folder (General)")
                    }
                    uiState.allFolders.forEach { folder ->
                        TextButton(
                            onClick = {
                                viewModel.setFolder(folder)
                                showFolderDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(folder.name)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFolderDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Tag dialog
    if (showTagDialog) {
        var newTagName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("Add Tag") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        label = { Text("Tag Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Available Tags:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        uiState.allAvailableTags.forEach { availableTag ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.clickable {
                                    viewModel.addTag(availableTag.name)
                                    showTagDialog = false
                                }
                            ) {
                                Text(
                                    text = "#${availableTag.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTagName.isNotBlank()) {
                            viewModel.addTag(newTagName)
                        }
                        showTagDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reminder dialog
    if (showReminderDialog) {
        val now = System.currentTimeMillis()
        val options = listOf(
            Pair("In 1 hour", now + 60 * 60 * 1000L),
            Pair("Tonight at 8 PM", now + 4 * 60 * 60 * 1000L),
            Pair("Tomorrow Morning", now + 24 * 60 * 60 * 1000L),
            Pair("In 3 Days", now + 3 * 24 * 60 * 60 * 1000L)
        )

        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = { Text("Set Reminder") },
            text = {
                Column {
                    options.forEach { (label, time) ->
                        TextButton(
                            onClick = {
                                viewModel.setReminder(time)
                                showReminderDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReminderDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
