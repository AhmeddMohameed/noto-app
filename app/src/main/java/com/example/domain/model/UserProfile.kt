package com.example.domain.model

data class UserProfile(
    val uid: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val preferences: Map<String, Any> = emptyMap()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "photoUrl" to photoUrl,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "preferences" to preferences
        )
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(uid: String, map: Map<String, Any?>): UserProfile {
            val prefs = map["preferences"] as? Map<String, Any> ?: emptyMap()
            return UserProfile(
                uid = uid,
                name = map["name"] as? String ?: "User",
                email = map["email"] as? String ?: "",
                photoUrl = map["photoUrl"] as? String,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                preferences = prefs
            )
        }
    }
}
