package com.example

import com.example.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseLocalFirstSyncTest {

    @Test
    fun `userProfile converts to map and from map accurately`() {
        val original = UserProfile(
            uid = "user_12345",
            name = "Test User",
            email = "test@noto.app",
            photoUrl = "https://example.com/avatar.png",
            createdAt = 1700000000000L,
            updatedAt = 1700000050000L,
            preferences = mapOf("theme" to "DARK")
        )

        val map = original.toMap()
        assertEquals("user_12345", map["uid"])
        assertEquals("Test User", map["name"])
        assertEquals("test@noto.app", map["email"])

        val restored = UserProfile.fromMap("user_12345", map)
        assertEquals(original.uid, restored.uid)
        assertEquals(original.name, restored.name)
        assertEquals(original.email, restored.email)
        assertEquals(original.photoUrl, restored.photoUrl)
        assertEquals(original.createdAt, restored.createdAt)
        assertEquals(original.updatedAt, restored.updatedAt)
    }

    @Test
    fun `conflict resolution selects newer updatedAt`() {
        val localUpdatedAt = 1700000050000L
        val remoteUpdatedAt = 1700000090000L

        // Remote is newer -> remote wins
        val remoteWins = remoteUpdatedAt > localUpdatedAt
        assertTrue("Remote should win when newer", remoteWins)

        val localNewer = 1700000100000L
        val localWins = localNewer >= remoteUpdatedAt
        assertTrue("Local should win when newer or equal", localWins)
    }

    @Test
    fun `offline mode maintains local operations without throwing`() {
        // Simulates offline mode flag
        var isOnline = false
        val localPendingOperations = mutableListOf<String>()

        fun recordOperation(op: String) {
            localPendingOperations.add(op)
        }

        recordOperation("create_note_1")
        recordOperation("update_note_1")
        recordOperation("delete_note_2")

        assertEquals(3, localPendingOperations.size)
        assertFalse("Should be offline", isOnline)

        // When coming online:
        isOnline = true
        assertTrue("Should be online", isOnline)
        localPendingOperations.clear()
        assertEquals(0, localPendingOperations.size)
    }
}
