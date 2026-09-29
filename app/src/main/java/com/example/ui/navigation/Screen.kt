package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot_password")
    data object Home : Screen("home")
    data object AllNotes : Screen("all_notes")
    data object NoteEditor : Screen("note_editor?noteId={noteId}") {
        fun createRoute(noteId: Long = -1L): String = "note_editor?noteId=$noteId"
    }
    data object NoteDetails : Screen("note_details/{noteId}") {
        fun createRoute(noteId: Long): String = "note_details/$noteId"
    }
    data object Favorites : Screen("favorites")
    data object Trash : Screen("trash")
    data object Archived : Screen("archived")
    data object Folders : Screen("folders")
    data object Tags : Screen("tags")
    data object Settings : Screen("settings")
    data object Search : Screen("search?tag={tag}&folderId={folderId}") {
        fun createRoute(tag: String? = null, folderId: Long? = null): String {
            val tagPart = if (!tag.isNullOrBlank()) "tag=$tag" else "tag="
            val folderPart = if (folderId != null && folderId > 0) "&folderId=$folderId" else "&folderId=-1"
            return "search?$tagPart$folderPart"
        }
    }
    data object About : Screen("about")
}
