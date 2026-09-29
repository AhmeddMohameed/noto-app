package com.example.data.repository

import com.example.domain.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<UserProfile?>
    val isUserLoggedIn: Boolean
    fun getCurrentUserId(): String?
    suspend fun signInWithEmail(email: String, password: String): Result<UserProfile>
    suspend fun signUpWithEmail(name: String, email: String, password: String): Result<UserProfile>
    suspend fun signInWithGoogle(idToken: String): Result<UserProfile>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun signOut()
    suspend fun refreshUserProfile(): UserProfile?
}
