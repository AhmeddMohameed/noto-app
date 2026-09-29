package com.example.data.repository

import com.example.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val userPreferencesRepository: UserPreferencesRepository
) : AuthRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    override val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    override val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    init {
        // Listen to Firebase Auth state changes for session persistence
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                scope.launch {
                    val profile = fetchOrCreateUserProfile(
                        uid = firebaseUser.uid,
                        fallbackName = firebaseUser.displayName ?: "User",
                        fallbackEmail = firebaseUser.email ?: "",
                        photoUrl = firebaseUser.photoUrl?.toString()
                    )
                    _currentUser.value = profile
                    userPreferencesRepository.setLoggedIn(true, profile.email, profile.name)
                }
            } else {
                _currentUser.value = null
            }
        }
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override suspend fun signInWithEmail(email: String, password: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw Exception("Authentication returned empty user")
            val profile = fetchOrCreateUserProfile(
                uid = user.uid,
                fallbackName = user.displayName ?: email.substringBefore("@").replaceFirstChar { it.uppercase() },
                fallbackEmail = user.email ?: email,
                photoUrl = user.photoUrl?.toString()
            )
            _currentUser.value = profile
            userPreferencesRepository.setLoggedIn(true, profile.email, profile.name)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(name: String, email: String, password: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw Exception("User registration failed")

            // Update Firebase User Profile display name
            try {
                user.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(name.trim())
                        .build()
                ).await()
            } catch (_: Exception) {
                // Ignore profile update failure if core account is created
            }

            val profile = UserProfile(
                uid = user.uid,
                name = name.trim().ifEmpty { email.substringBefore("@") },
                email = user.email ?: email.trim(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // Save user document in Firestore users/{userId}
            try {
                firestore.collection("users")
                    .document(user.uid)
                    .set(profile.toMap(), SetOptions.merge())
                    .await()
            } catch (_: Exception) {
                // If offline, Firestore local cache stores it
            }

            _currentUser.value = profile
            userPreferencesRepository.setLoggedIn(true, profile.email, profile.name)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw Exception("Google sign in returned empty user")

            val profile = fetchOrCreateUserProfile(
                uid = user.uid,
                fallbackName = user.displayName ?: "Google User",
                fallbackEmail = user.email ?: "",
                photoUrl = user.photoUrl?.toString()
            )

            _currentUser.value = profile
            userPreferencesRepository.setLoggedIn(true, profile.email, profile.name)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Unit = withContext(Dispatchers.IO) {
        try {
            auth.signOut()
        } catch (_: Exception) { }
        _currentUser.value = null
        userPreferencesRepository.logout()
    }

    override suspend fun refreshUserProfile(): UserProfile? = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext null
        val profile = fetchOrCreateUserProfile(
            uid = user.uid,
            fallbackName = user.displayName ?: "User",
            fallbackEmail = user.email ?: "",
            photoUrl = user.photoUrl?.toString()
        )
        _currentUser.value = profile
        profile
    }

    private suspend fun fetchOrCreateUserProfile(
        uid: String,
        fallbackName: String,
        fallbackEmail: String,
        photoUrl: String?
    ): UserProfile {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists() && doc.data != null) {
                UserProfile.fromMap(uid, doc.data!!)
            } else {
                val newProfile = UserProfile(
                    uid = uid,
                    name = fallbackName,
                    email = fallbackEmail,
                    photoUrl = photoUrl,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                firestore.collection("users").document(uid).set(newProfile.toMap(), SetOptions.merge())
                newProfile
            }
        } catch (e: Exception) {
            UserProfile(
                uid = uid,
                name = fallbackName,
                email = fallbackEmail,
                photoUrl = photoUrl
            )
        }
    }
}
