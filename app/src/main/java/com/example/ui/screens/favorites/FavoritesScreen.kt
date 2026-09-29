package com.example.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.EmptyStateView
import com.example.ui.components.NoteCard
import com.example.ui.components.NotoTopAppBar
import com.example.ui.screens.home.HomeViewModel

@Composable
fun FavoritesScreen(
    homeViewModel: HomeViewModel,
    onNavigateNoteDetail: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("favorites_screen"),
        topBar = {
            NotoTopAppBar(
                title = "Favorites (${uiState.favoriteNotes.size})",
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
            if (uiState.favoriteNotes.isEmpty()) {
                EmptyStateView(
                    title = "No Favorites Yet",
                    subtitle = "Mark notes with a heart to keep them easily accessible here.",
                    icon = Icons.Default.Favorite,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.favoriteNotes, key = { it.id }) { note ->
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
