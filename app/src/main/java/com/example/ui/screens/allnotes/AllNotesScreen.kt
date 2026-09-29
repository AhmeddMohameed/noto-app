package com.example.ui.screens.allnotes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.NoteType
import com.example.ui.components.EmptyStateView
import com.example.ui.components.NoteCard
import com.example.ui.components.NotoTopAppBar
import com.example.ui.screens.home.HomeViewModel

@Composable
fun AllNotesScreen(
    homeViewModel: HomeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateNoteDetail: (Long) -> Unit,
    onNavigateNewNote: () -> Unit
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    var selectedTypeFilter by remember { mutableStateOf<NoteType?>(null) }

    val filteredNotes = if (selectedTypeFilter != null) {
        uiState.allActiveNotes.filter { it.noteType == selectedTypeFilter }
    } else {
        uiState.allActiveNotes
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("all_notes_screen"),
        topBar = {
            NotoTopAppBar(
                title = "All Notes (${uiState.allActiveNotes.size})",
                showBackButton = true,
                onNavigationClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateNewNote,
                modifier = Modifier.testTag("all_notes_fab_add"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add note")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Note type filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedTypeFilter == null,
                        onClick = { selectedTypeFilter = null },
                        label = { Text("All") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                items(NoteType.entries.toTypedArray()) { type ->
                    FilterChip(
                        selected = selectedTypeFilter == type,
                        onClick = { selectedTypeFilter = if (selectedTypeFilter == type) null else type },
                        label = { Text(type.displayName) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            if (filteredNotes.isEmpty()) {
                EmptyStateView(
                    title = "No notes found",
                    subtitle = if (selectedTypeFilter != null) "No notes match the filter: ${selectedTypeFilter?.displayName}" else "You haven't created any notes yet.",
                    actionLabel = "Create Note",
                    onActionClick = onNavigateNewNote,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onNavigateNoteDetail(note.id) },
                            onToggleFavorite = { homeViewModel.toggleFavorite(note.id, note.isFavorite) },
                            onTogglePin = { homeViewModel.togglePin(note.id, note.isPinned) }
                        )
                    }
                }
            }
        }
    }
}
