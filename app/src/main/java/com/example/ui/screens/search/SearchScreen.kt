package com.example.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FolderFilterChip
import com.example.ui.components.NoteCard
import com.example.ui.components.NotoSearchBar
import com.example.ui.components.NotoTopAppBar
import com.example.ui.components.TagFilterChip

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    initialTag: String?,
    initialFolderId: Long?,
    onNavigateBack: () -> Unit,
    onNavigateNoteDetail: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(initialTag, initialFolderId) {
        viewModel.setInitialFilters(initialTag, initialFolderId)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("search_screen"),
        topBar = {
            NotoTopAppBar(
                title = "Search Notes",
                showBackButton = true,
                onNavigationClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Realtime Search Bar
            NotoSearchBar(
                query = uiState.query,
                onQueryChange = { viewModel.onQueryChange(it) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // Horizontal Filters (Folders & Tags)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Folder chips
                items(uiState.folders, key = { "search_folder_${it.id}" }) { folder ->
                    FolderFilterChip(
                        folder = folder,
                        isSelected = uiState.selectedFolderId == folder.id,
                        onClick = { viewModel.selectFolder(folder.id) }
                    )
                }

                // Tag chips
                items(uiState.tags, key = { "search_tag_${it.id}" }) { tag ->
                    TagFilterChip(
                        tag = tag,
                        isSelected = uiState.selectedTag == tag.name,
                        onClick = { viewModel.selectTag(tag.name) }
                    )
                }
            }

            // Results count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.totalCount} results found",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.results.isEmpty()) {
                EmptyStateView(
                    title = "No Matches Found",
                    subtitle = if (uiState.query.isNotBlank()) "No notes match '${uiState.query}'. Try searching by other keywords, folder names, or tags." else "Type in the search bar above to look through titles, note content, and tags.",
                    icon = Icons.Default.SearchOff,
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
                    items(uiState.results, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onNavigateNoteDetail(note.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(note.id, note.isFavorite) },
                            onTogglePin = { viewModel.togglePin(note.id, note.isPinned) }
                        )
                    }
                }
            }
        }
    }
}
