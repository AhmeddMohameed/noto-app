package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.di.AppContainer
import com.example.ui.ViewModelFactory
import com.example.ui.components.NotoBottomBar
import com.example.ui.components.NotoDrawerContent
import com.example.ui.screens.about.AboutScreen
import com.example.ui.screens.allnotes.AllNotesScreen
import com.example.ui.screens.archived.ArchivedNotesScreen
import com.example.ui.screens.auth.AuthViewModel
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.details.NoteDetailsScreen
import com.example.ui.screens.editor.NoteEditorScreen
import com.example.ui.screens.editor.NoteEditorViewModel
import com.example.ui.screens.favorites.FavoritesScreen
import com.example.ui.screens.folders.FoldersScreen
import com.example.ui.screens.folders.FoldersViewModel
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.search.SearchViewModel
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.tags.TagsScreen
import com.example.ui.screens.tags.TagsViewModel
import com.example.ui.screens.trash.TrashScreen
import com.example.ui.screens.trash.TrashViewModel
import kotlinx.coroutines.launch

@Composable
fun NotoNavHost(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val homeViewModel: HomeViewModel = viewModel(
        factory = ViewModelFactory(appContainer)
    )

    // Check if bottom bar should be visible
    val isPrimaryScreen = currentRoute in listOf(
        Screen.Home.route,
        Screen.Favorites.route,
        Screen.Folders.route,
        Screen.Settings.route
    ) || currentRoute?.startsWith("search") == true

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isPrimaryScreen,
        drawerContent = {
            NotoDrawerContent(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            bottomBar = {
                if (isPrimaryScreen) {
                    NotoBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            if (route == Screen.Home.route) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(route) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route
                ) {
                    // 1. Splash Screen
                    composable(Screen.Splash.route) {
                        SplashScreen(
                            isOnboardingCompleted = appContainer.userPreferencesRepository.isOnboardingCompleted.value,
                            onNavigateNext = { completed ->
                                val target = if (completed) Screen.Home.route else Screen.Onboarding.route
                                navController.navigate(target) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    // 2. Onboarding Screen
                    composable(Screen.Onboarding.route) {
                        OnboardingScreen(
                            onFinish = {
                                appContainer.userPreferencesRepository.setOnboardingCompleted(true)
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            },
                            onNavigateLogin = {
                                navController.navigate(Screen.Login.route)
                            }
                        )
                    }

                    // 3. Login Screen
                    composable(Screen.Login.route) {
                        val authViewModel: AuthViewModel = viewModel(
                            factory = ViewModelFactory(appContainer)
                        )
                        LoginScreen(
                            authViewModel = authViewModel,
                            onLoginSuccess = { email, name ->
                                appContainer.userPreferencesRepository.setLoggedIn(true, email, name)
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onNavigateRegister = {
                                navController.navigate(Screen.Register.route)
                            },
                            onNavigateForgotPassword = {
                                navController.navigate(Screen.ForgotPassword.route)
                            },
                            onContinueGuest = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    // 4. Register Screen
                    composable(Screen.Register.route) {
                        val authViewModel: AuthViewModel = viewModel(
                            factory = ViewModelFactory(appContainer)
                        )
                        RegisterScreen(
                            authViewModel = authViewModel,
                            onRegisterSuccess = { email, name ->
                                appContainer.userPreferencesRepository.setLoggedIn(true, email, name)
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Register.route) { inclusive = true }
                                }
                            },
                            onNavigateLogin = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // 5. Forgot Password Screen
                    composable(Screen.ForgotPassword.route) {
                        val authViewModel: AuthViewModel = viewModel(
                            factory = ViewModelFactory(appContainer)
                        )
                        ForgotPasswordScreen(
                            authViewModel = authViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // 6. Home Screen
                    composable(Screen.Home.route) {
                        HomeScreen(
                            viewModel = homeViewModel,
                            onNavigateNewNote = {
                                navController.navigate(Screen.NoteEditor.createRoute(-1L))
                            },
                            onNavigateNoteDetail = { noteId ->
                                navController.navigate(Screen.NoteDetails.createRoute(noteId))
                            },
                            onNavigateSearch = {
                                navController.navigate(Screen.Search.createRoute())
                            },
                            onNavigateAllNotes = {
                                navController.navigate(Screen.AllNotes.route)
                            },
                            onNavigateFolders = {
                                navController.navigate(Screen.Folders.route)
                            },
                            onNavigateTags = {
                                navController.navigate(Screen.Tags.route)
                            },
                            onOpenDrawer = {
                                coroutineScope.launch { drawerState.open() }
                            }
                        )
                    }

                    // 7. All Notes Screen
                    composable(Screen.AllNotes.route) {
                        AllNotesScreen(
                            homeViewModel = homeViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateNoteDetail = { noteId ->
                                navController.navigate(Screen.NoteDetails.createRoute(noteId))
                            },
                            onNavigateNewNote = {
                                navController.navigate(Screen.NoteEditor.createRoute(-1L))
                            }
                        )
                    }

                    // 8. Note Editor Screen
                    composable(
                        route = Screen.NoteEditor.route,
                        arguments = listOf(
                            navArgument("noteId") {
                                type = NavType.LongType
                                defaultValue = -1L
                            }
                        )
                    ) { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
                        val editorViewModel: NoteEditorViewModel = viewModel(
                            factory = ViewModelFactory(appContainer, noteId),
                            key = "note_editor_$noteId"
                        )
                        NoteEditorScreen(
                            viewModel = editorViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // 9. Note Details Screen
                    composable(
                        route = Screen.NoteDetails.route,
                        arguments = listOf(
                            navArgument("noteId") {
                                type = NavType.LongType
                            }
                        )
                    ) { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
                        NoteDetailsScreen(
                            noteId = noteId,
                            homeViewModel = homeViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateEdit = { id ->
                                navController.navigate(Screen.NoteEditor.createRoute(id))
                            }
                        )
                    }

                    // 10. Favorites Screen
                    composable(Screen.Favorites.route) {
                        FavoritesScreen(
                            homeViewModel = homeViewModel,
                            onNavigateNoteDetail = { noteId ->
                                navController.navigate(Screen.NoteDetails.createRoute(noteId))
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // 11. Trash Screen
                    composable(Screen.Trash.route) {
                        val trashViewModel: TrashViewModel = viewModel(
                            factory = ViewModelFactory(appContainer)
                        )
                        TrashScreen(
                            viewModel = trashViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // 12. Archived Notes Screen
                    composable(Screen.Archived.route) {
                        ArchivedNotesScreen(
                            noteRepository = appContainer.noteRepository,
                            onNavigateNoteDetail = { noteId ->
                                navController.navigate(Screen.NoteDetails.createRoute(noteId))
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // 13. Folders Screen
                    composable(Screen.Folders.route) {
                        val foldersViewModel: FoldersViewModel = viewModel(
                            factory = ViewModelFactory(appContainer)
                        )
                        FoldersScreen(
                            viewModel = foldersViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onFolderSelected = { folderId, _ ->
                                navController.navigate(Screen.Search.createRoute(folderId = folderId))
                            }
                        )
                    }

                    // 14. Tags Screen
                    composable(Screen.Tags.route) {
                        val tagsViewModel: TagsViewModel = viewModel(
                            factory = ViewModelFactory(appContainer)
                        )
                        TagsScreen(
                            viewModel = tagsViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onTagSelected = { tagName ->
                                navController.navigate(Screen.Search.createRoute(tag = tagName))
                            }
                        )
                    }

                    // 15. Settings Screen
                    composable(Screen.Settings.route) {
                        val settingsViewModel: SettingsViewModel = viewModel(
                            factory = ViewModelFactory(appContainer)
                        )
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateTrash = { navController.navigate(Screen.Trash.route) },
                            onNavigateArchived = { navController.navigate(Screen.Archived.route) },
                            onNavigateAbout = { navController.navigate(Screen.About.route) },
                            onNavigateLogin = { navController.navigate(Screen.Login.route) }
                        )
                    }

                    // 16. Search Screen
                    composable(
                        route = Screen.Search.route,
                        arguments = listOf(
                            navArgument("tag") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            },
                            navArgument("folderId") {
                                type = NavType.LongType
                                defaultValue = -1L
                            }
                        )
                    ) { backStackEntry ->
                        val tag = backStackEntry.arguments?.getString("tag")
                        val folderId = backStackEntry.arguments?.getLong("folderId")
                        val searchViewModel: SearchViewModel = viewModel(
                            factory = ViewModelFactory(appContainer)
                        )
                        SearchScreen(
                            viewModel = searchViewModel,
                            initialTag = if (tag.isNullOrBlank()) null else tag,
                            initialFolderId = if (folderId != null && folderId > 0) folderId else null,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateNoteDetail = { noteId ->
                                navController.navigate(Screen.NoteDetails.createRoute(noteId))
                            }
                        )
                    }

                    // 17. About Screen
                    composable(Screen.About.route) {
                        AboutScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
