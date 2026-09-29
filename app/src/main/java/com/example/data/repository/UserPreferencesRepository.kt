package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class NotesViewMode {
    STAGGERED_GRID,
    LIST
}

interface UserPreferencesRepository {
    val themeMode: StateFlow<ThemeMode>
    val viewMode: StateFlow<NotesViewMode>
    val isOnboardingCompleted: StateFlow<Boolean>
    val isLoggedIn: StateFlow<Boolean>
    val userEmail: StateFlow<String?>
    val userName: StateFlow<String?>
    val isAppLockEnabled: StateFlow<Boolean>
    val appLockPin: StateFlow<String?>

    fun setThemeMode(mode: ThemeMode)
    fun setViewMode(mode: NotesViewMode)
    fun setOnboardingCompleted(completed: Boolean)
    fun setLoggedIn(loggedIn: Boolean, email: String? = null, name: String? = null)
    fun setAppLock(enabled: Boolean, pin: String? = null)
    fun logout()
}

class UserPreferencesRepositoryImpl(context: Context) : UserPreferencesRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("noto_user_preferences", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(readThemeMode())
    override val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _viewMode = MutableStateFlow(readViewMode())
    override val viewMode: StateFlow<NotesViewMode> = _viewMode.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false))
    override val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean(KEY_IS_LOGGED_IN, false))
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userEmail = MutableStateFlow(prefs.getString(KEY_USER_EMAIL, "user@noto.app"))
    override val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString(KEY_USER_NAME, "Alex Morgan"))
    override val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _isAppLockEnabled = MutableStateFlow(prefs.getBoolean(KEY_APP_LOCK_ENABLED, false))
    override val isAppLockEnabled: StateFlow<Boolean> = _isAppLockEnabled.asStateFlow()

    private val _appLockPin = MutableStateFlow(prefs.getString(KEY_APP_LOCK_PIN, "1234"))
    override val appLockPin: StateFlow<String?> = _appLockPin.asStateFlow()

    private fun readThemeMode(): ThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    private fun readViewMode(): NotesViewMode {
        val name = prefs.getString(KEY_VIEW_MODE, NotesViewMode.STAGGERED_GRID.name) ?: NotesViewMode.STAGGERED_GRID.name
        return try {
            NotesViewMode.valueOf(name)
        } catch (e: Exception) {
            NotesViewMode.STAGGERED_GRID
        }
    }

    override fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    override fun setViewMode(mode: NotesViewMode) {
        prefs.edit().putString(KEY_VIEW_MODE, mode.name).apply()
        _viewMode.value = mode
    }

    override fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
        _isOnboardingCompleted.value = completed
    }

    override fun setLoggedIn(loggedIn: Boolean, email: String?, name: String?) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, loggedIn)
            .putString(KEY_USER_EMAIL, email ?: "user@noto.app")
            .putString(KEY_USER_NAME, name ?: "Alex Morgan")
            .apply()
        _isLoggedIn.value = loggedIn
        _userEmail.value = email ?: "user@noto.app"
        _userName.value = name ?: "Alex Morgan"
    }

    override fun setAppLock(enabled: Boolean, pin: String?) {
        val editor = prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled)
        if (pin != null) {
            editor.putString(KEY_APP_LOCK_PIN, pin)
            _appLockPin.value = pin
        }
        editor.apply()
        _isAppLockEnabled.value = enabled
    }

    override fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .apply()
        _isLoggedIn.value = false
        _userEmail.value = null
        _userName.value = null
    }

    companion object {
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_VIEW_MODE = "key_view_mode"
        private const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_APP_LOCK_ENABLED = "key_app_lock_enabled"
        private const val KEY_APP_LOCK_PIN = "key_app_lock_pin"
    }
}
