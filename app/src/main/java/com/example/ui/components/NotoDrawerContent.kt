package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.navigation.Screen

@Composable
fun NotoDrawerContent(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .testTag("noto_drawer_content"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 24.dp, horizontal = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = "Noto",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Noto",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Premium Notes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Items
            DrawerItem(
                label = "Home",
                icon = Icons.Default.Home,
                selected = currentRoute == Screen.Home.route,
                onClick = {
                    onCloseDrawer()
                    onNavigate(Screen.Home.route)
                },
                tag = "drawer_item_home"
            )

            DrawerItem(
                label = "All Notes",
                icon = Icons.Default.Description,
                selected = currentRoute == Screen.AllNotes.route,
                onClick = {
                    onCloseDrawer()
                    onNavigate(Screen.AllNotes.route)
                },
                tag = "drawer_item_all_notes"
            )

            DrawerItem(
                label = "Favorites",
                icon = Icons.Default.Favorite,
                selected = currentRoute == Screen.Favorites.route,
                onClick = {
                    onCloseDrawer()
                    onNavigate(Screen.Favorites.route)
                },
                tag = "drawer_item_favorites"
            )

            DrawerItem(
                label = "Folders",
                icon = Icons.Default.Folder,
                selected = currentRoute == Screen.Folders.route,
                onClick = {
                    onCloseDrawer()
                    onNavigate(Screen.Folders.route)
                },
                tag = "drawer_item_folders"
            )

            DrawerItem(
                label = "Tags",
                icon = Icons.Default.Tag,
                selected = currentRoute == Screen.Tags.route,
                onClick = {
                    onCloseDrawer()
                    onNavigate(Screen.Tags.route)
                },
                tag = "drawer_item_tags"
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            DrawerItem(
                label = "Archived",
                icon = Icons.Default.Archive,
                selected = currentRoute == Screen.Archived.route,
                onClick = {
                    onCloseDrawer()
                    onNavigate(Screen.Archived.route)
                },
                tag = "drawer_item_archived"
            )

            DrawerItem(
                label = "Trash",
                icon = Icons.Default.Delete,
                selected = currentRoute == Screen.Trash.route,
                onClick = {
                    onCloseDrawer()
                    onNavigate(Screen.Trash.route)
                },
                tag = "drawer_item_trash"
            )

            DrawerItem(
                label = "Settings",
                icon = Icons.Default.Settings,
                selected = currentRoute == Screen.Settings.route,
                onClick = {
                    onCloseDrawer()
                    onNavigate(Screen.Settings.route)
                },
                tag = "drawer_item_settings"
            )

            DrawerItem(
                label = "About",
                icon = Icons.Default.Info,
                selected = currentRoute == Screen.About.route,
                onClick = {
                    onCloseDrawer()
                    onNavigate(Screen.About.route)
                },
                tag = "drawer_item_about"
            )
        }
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    NavigationDrawerItem(
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        icon = { Icon(icon, contentDescription = null) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier
            .padding(vertical = 2.dp)
            .testTag(tag),
        shape = RoundedCornerShape(14.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
