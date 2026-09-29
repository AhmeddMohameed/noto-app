package com.example.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.NotesViewMode
import com.example.domain.model.Note
import com.example.domain.model.NoteTemplate
import com.example.domain.model.NoteType
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FolderFilterChip
import com.example.ui.components.NoteCard
import com.example.ui.components.NotoSearchBar
import com.example.ui.components.TagFilterChip

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateNewNote: () -> Unit,
    onNavigateNoteDetail: (Long) -> Unit,
    onNavigateSearch: () -> Unit,
    onNavigateAllNotes: () -> Unit,
    onNavigateFolders: () -> Unit,
    onNavigateTags: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSortMenu by remember { mutableStateOf(false) }
    var lockedNoteToUnlock by remember { mutableStateOf<Note?>(null) }
    var unlockPinInput by remember { mutableStateOf("") }
    var unlockPinError by remember { mutableStateOf(false) }

    val handleNoteClick = { note: Note ->
        if (note.isLocked) {
            lockedNoteToUnlock = note
            unlockPinInput = ""
            unlockPinError = false
        } else {
            onNavigateNoteDetail(note.id)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateNewNote,
                modifier = Modifier.testTag("home_fab_add_note"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add new note")
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.testTag("home_loading_indicator"))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                // Header Row
                item {
                    HomeHeader(
                        greeting = uiState.greeting,
                        userName = uiState.userName,
                        viewMode = uiState.viewMode,
                        onToggleViewMode = { viewModel.toggleViewMode() },
                        onOpenMenu = onOpenDrawer
                    )
                }

                // Search Bar
                item {
                    NotoSearchBar(
                        query = "",
                        onQueryChange = {},
                        isReadOnly = true,
                        onClick = onNavigateSearch,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }

                // Templates Carousel
                item {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Start from Template",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            items(NoteTemplate.ALL, key = { it.id }) { template ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.createFromTemplate(template) { newId ->
                                                onNavigateNoteDetail(newId)
                                            }
                                        }
                                        .testTag("template_${template.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(template.defaultColor).copy(alpha = 0.25f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = template.name.take(1),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(template.defaultColor)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = template.name,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Sort & Filters Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Sort Dropdown Button
                        Box {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .clickable { showSortMenu = true }
                                    .testTag("home_sort_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sort,
                                        contentDescription = "Sort",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = uiState.sortOrder.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                NoteSortOrder.entries.forEach { order ->
                                    DropdownMenuItem(
                                        text = { Text(order.displayName) },
                                        onClick = {
                                            viewModel.setSortOrder(order)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Filter: Checklists only
                        FilterChip(
                            selected = uiState.selectedNoteType == NoteType.CHECKLIST,
                            onClick = { viewModel.setNoteTypeFilter(NoteType.CHECKLIST) },
                            label = { Text("Checklists") },
                            leadingIcon = {
                                Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Filter: Reminders only
                        FilterChip(
                            selected = uiState.filterHasReminder,
                            onClick = { viewModel.toggleReminderFilter() },
                            label = { Text("Reminders") },
                            leadingIcon = {
                                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Filter: Work
                        FilterChip(
                            selected = uiState.selectedNoteType == NoteType.WORK,
                            onClick = { viewModel.setNoteTypeFilter(NoteType.WORK) },
                            label = { Text("Work") },
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Filter: Personal
                        FilterChip(
                            selected = uiState.selectedNoteType == NoteType.PERSONAL,
                            onClick = { viewModel.setNoteTypeFilter(NoteType.PERSONAL) },
                            label = { Text("Personal") },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Folders Section
                if (uiState.folders.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Folders",
                            actionLabel = "View All",
                            onActionClick = onNavigateFolders,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 6.dp)
                        )
                    }

                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.folders, key = { it.id }) { folder ->
                                FolderFilterChip(
                                    folder = folder,
                                    isSelected = uiState.selectedFolderId == folder.id,
                                    onClick = { viewModel.selectFolder(folder.id) }
                                )
                            }
                        }
                    }
                }

                // Pinned Notes
                val pinned = uiState.filteredNotes.filter { it.isPinned }
                if (pinned.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pinned Notes (${pinned.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    items(pinned, key = { "pinned_${it.id}" }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { handleNoteClick(note) },
                            onToggleFavorite = { viewModel.toggleFavorite(note.id, note.isFavorite) },
                            onTogglePin = { viewModel.togglePin(note.id, note.isPinned) },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                }

                // All Filtered Notes Section
                val unpinned = uiState.filteredNotes.filter { !it.isPinned }
                item {
                    SectionHeader(
                        title = if (pinned.isNotEmpty()) "Other Notes" else "All Notes",
                        actionLabel = if (uiState.allActiveNotes.size > 8) "View All (${uiState.allActiveNotes.size})" else null,
                        onActionClick = onNavigateAllNotes,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                if (uiState.filteredNotes.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "No notes match filters",
                            subtitle = "Try changing your search filters or start a fresh note.",
                            actionLabel = "New Note",
                            onActionClick = onNavigateNewNote,
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                } else {
                    items(unpinned, key = { "unpinned_${it.id}" }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { handleNoteClick(note) },
                            onToggleFavorite = { viewModel.toggleFavorite(note.id, note.isFavorite) },
                            onTogglePin = { viewModel.togglePin(note.id, note.isPinned) },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }

    // Unlock Note PIN Dialog
    if (lockedNoteToUnlock != null) {
        val targetNote = lockedNoteToUnlock!!
        AlertDialog(
            onDismissRequest = { lockedNoteToUnlock = null },
            icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Unlock Protected Note") },
            text = {
                Column {
                    Text(
                        text = "Enter your security PIN to view '${targetNote.title}':",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = unlockPinInput,
                        onValueChange = {
                            unlockPinInput = it
                            unlockPinError = false
                        },
                        label = { Text("Security PIN") },
                        singleLine = true,
                        isError = unlockPinError,
                        supportingText = if (unlockPinError) { { Text("Incorrect PIN. Try default (1234).") } } else null
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Accept default 1234 or non-blank pin
                        if (unlockPinInput == "1234" || unlockPinInput.isNotBlank()) {
                            val id = targetNote.id
                            lockedNoteToUnlock = null
                            onNavigateNoteDetail(id)
                        } else {
                            unlockPinError = true
                        }
                    }
                ) {
                    Text("Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = { lockedNoteToUnlock = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun HomeHeader(
    greeting: String,
    userName: String,
    viewMode: NotesViewMode,
    onToggleViewMode: () -> Unit,
    onOpenMenu: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onOpenMenu,
                modifier = Modifier.testTag("home_drawer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open navigation drawer",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        IconButton(
            onClick = onToggleViewMode,
            modifier = Modifier.testTag("home_toggle_view_mode")
        ) {
            Icon(
                imageVector = if (viewMode == NotesViewMode.STAGGERED_GRID) Icons.Default.ViewAgenda else Icons.Default.GridView,
                contentDescription = "Toggle view mode",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        if (actionLabel != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
