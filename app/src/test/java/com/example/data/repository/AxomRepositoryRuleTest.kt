package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AxomRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createPost_and_observePosts_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = AxomRepository(firestore, auth)

        val postResult = withTimeout(TIMEOUT_MS) {
            repository.createPost(
                content = "Rongali Bihu celebration in Guwahati!",
                tag = "Bihu"
            )
        }
        assertTrue(postResult.isSuccess)
        val postId = postResult.getOrThrow()
        assertNotNull(postId)

        val posts = withTimeout(TIMEOUT_MS) {
            repository.observePosts().first { list -> list.any { p -> p.id == postId } }
        }
        assertTrue(posts.any { it.id == postId })
    }

    @Test
    fun saveUserProfile_authenticatedUser_succeeds() = runBlocking {
        val uid = signInTestUser(BOB_EMAIL)
        val repository = AxomRepository(firestore, auth)

        val profile = UserProfile(
            userId = uid,
            displayName = "Bob Barua",
            username = "bob_b",
            bio = "Tea garden lover from Jorhat",
            location = "Jorhat, Assam"
        )

        val saveResult = withTimeout(TIMEOUT_MS) {
            repository.saveUserProfile(profile)
        }
        assertTrue(saveResult.isSuccess)

        val fetchedProfile = withTimeout(TIMEOUT_MS) {
            repository.getUserProfile(uid)
        }
        assertTrue(fetchedProfile.isSuccess)
        assertEquals("Bob Barua", fetchedProfile.getOrThrow()?.displayName)
    }

    @Test
    fun toggleLike_and_addComment_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = AxomRepository(firestore, auth)
        val postId = withTimeout(TIMEOUT_MS) {
            aliceRepo.createPost("Exploring Kaziranga National Park!").getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = AxomRepository(firestore, auth)

        // Like the post
        val likeResult = withTimeout(TIMEOUT_MS) {
            bobRepo.toggleLike(postId)
        }
        assertTrue(likeResult.isSuccess)

        // Add a comment
        val commentResult = withTimeout(TIMEOUT_MS) {
            bobRepo.addComment(postId, "Beautiful wildlife! Rhino spotted?")
        }
        assertTrue(commentResult.isSuccess)
    }

    @Test
    fun deletePost_unauthorizedUser_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = AxomRepository(firestore, auth)
        val postId = withTimeout(TIMEOUT_MS) {
            aliceRepo.createPost("Alice personal thoughts").getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = AxomRepository(firestore, auth)
        val deleteResult = withTimeout(TIMEOUT_MS) {
            bobRepo.deletePost(postId)
        }
        assertTrue(deleteResult.isFailure)
        val exception = deleteResult.exceptionOrNull() as? FirebaseFirestoreException
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, exception?.code)
    }

    @Test
    fun unauthenticatedAccess_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        try {
            withTimeout(TIMEOUT_MS) {
                firestore.collection("posts").get().await()
            }
            fail("Expected exception for unauthenticated get query")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        } catch (e: Exception) {
            val fse = e as? FirebaseFirestoreException ?: e.cause as? FirebaseFirestoreException
            assertNotNull("Expected FirebaseFirestoreException but got $e", fse)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fse?.code)
        }
    }

    companion object {
        private const val ALICE_EMAIL = "alice@example.com"
        private const val BOB_EMAIL = "bob@example.com"
        private const val TIMEOUT_MS = 8000L
    }
}
