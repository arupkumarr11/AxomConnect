package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Comment
import com.example.data.model.Like
import com.example.data.model.Post
import com.example.data.model.Report
import com.example.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class AxomRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        FirebaseAuth.getInstance()
    )

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid
    fun getCurrentUserEmail(): String? = auth.currentUser?.email
    fun getCurrentUserDisplayName(): String? = auth.currentUser?.displayName
    fun getCurrentUserPhotoUrl(): String? = auth.currentUser?.photoUrl?.toString()

    // --- User Profile ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        val path = "users/$userId"
        return db.collection("users").document(userId)
            .snapshots()
            .map { it.toObject(UserProfile::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                throw error
            }
    }

    suspend fun getUserProfile(userId: String): Result<UserProfile?> {
        val path = "users/$userId"
        return try {
            val doc = db.collection("users").document(userId).get().await()
            Result.success(doc.toObject(UserProfile::class.java))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            Result.failure(e)
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid"
        return try {
            val docRef = db.collection("users").document(uid)
            val existing = docRef.get().await()
            val payload = if (existing.exists()) {
                mapOf(
                    "userId" to uid,
                    "displayName" to profile.displayName,
                    "username" to profile.username,
                    "bio" to profile.bio,
                    "avatarUrl" to profile.avatarUrl,
                    "location" to profile.location,
                    "interests" to profile.interests,
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
            } else {
                mapOf(
                    "userId" to uid,
                    "displayName" to profile.displayName,
                    "username" to profile.username,
                    "bio" to profile.bio,
                    "avatarUrl" to profile.avatarUrl,
                    "location" to profile.location,
                    "interests" to profile.interests,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
            }

            if (existing.exists()) {
                docRef.update(payload).await()
            } else {
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- Posts ---

    fun observePosts(): Flow<List<Post>> {
        val path = "posts"
        return db.collection(path)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun createPost(
        content: String,
        mediaUrl: String? = null,
        mediaType: String? = "none",
        tag: String? = null
    ): Result<String> {
        val uid = requireUserId()
        val path = "posts"
        val authorName = auth.currentUser?.displayName ?: "Axom Community Member"
        val authorAvatar = auth.currentUser?.photoUrl?.toString()

        return try {
            val payload = mapOf(
                "authorId" to uid,
                "authorName" to authorName,
                "authorAvatarUrl" to authorAvatar,
                "content" to content,
                "mediaUrl" to mediaUrl,
                "mediaType" to (mediaType ?: "none"),
                "tag" to tag,
                "likesCount" to 0L,
                "commentsCount" to 0L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            val docRef = db.collection(path).add(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deletePost(postId: String): Result<Unit> {
        requireUserId()
        val path = "posts/$postId"
        return try {
            db.collection("posts").document(postId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    // --- Likes ---

    suspend fun toggleLike(postId: String): Result<Boolean> {
        val uid = requireUserId()
        val likeDocRef = db.collection("posts").document(postId).collection("likes").document(uid)
        val postDocRef = db.collection("posts").document(postId)
        return try {
            val existingLike = likeDocRef.get().await()
            if (existingLike.exists()) {
                // Unlike
                likeDocRef.delete().await()
                postDocRef.update(
                    mapOf(
                        "likesCount" to FieldValue.increment(-1),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                Result.success(false)
            } else {
                // Like
                val payload = mapOf(
                    "userId" to uid,
                    "postId" to postId,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                likeDocRef.set(payload).await()
                postDocRef.update(
                    mapOf(
                        "likesCount" to FieldValue.increment(1),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                Result.success(true)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "posts/$postId/likes/$uid")
            Result.failure(e)
        }
    }

    fun observeUserLikesForPost(postId: String): Flow<Boolean> {
        val uid = getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(false)
        return db.collection("posts").document(postId).collection("likes").document(uid)
            .snapshots()
            .map { it.exists() }
            .catch { emit(false) }
    }

    // --- Comments ---

    fun observeComments(postId: String): Flow<List<Comment>> {
        val path = "posts/$postId/comments"
        return db.collection("posts").document(postId).collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Comment::class.java)?.copy(id = doc.id)
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun addComment(postId: String, text: String): Result<String> {
        val uid = requireUserId()
        val path = "posts/$postId/comments"
        val authorName = auth.currentUser?.displayName ?: "Community Member"
        val authorAvatar = auth.currentUser?.photoUrl?.toString()

        return try {
            val payload = mapOf(
                "postId" to postId,
                "authorId" to uid,
                "authorName" to authorName,
                "authorAvatarUrl" to authorAvatar,
                "text" to text,
                "createdAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            val docRef = db.collection("posts").document(postId).collection("comments").add(payload).await()
            // Increment comment count on post
            db.collection("posts").document(postId).update(
                mapOf(
                    "commentsCount" to FieldValue.increment(1),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteComment(postId: String, commentId: String): Result<Unit> {
        requireUserId()
        val path = "posts/$postId/comments/$commentId"
        return try {
            db.collection("posts").document(postId).collection("comments").document(commentId).delete().await()
            db.collection("posts").document(postId).update(
                mapOf(
                    "commentsCount" to FieldValue.increment(-1),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    // --- Moderation & Reports ---

    suspend fun submitReport(targetType: String, targetId: String, reason: String): Result<String> {
        val uid = requireUserId()
        val path = "reports"
        return try {
            val payload = mapOf(
                "reporterId" to uid,
                "targetType" to targetType,
                "targetId" to targetId,
                "reason" to reason,
                "status" to "pending",
                "createdAt" to FieldValue.serverTimestamp()
            )
            val docRef = db.collection(path).add(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observeReports(): Flow<List<Report>> {
        val path = "reports"
        return db.collection(path)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Report::class.java)?.copy(id = doc.id)
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun updateReportStatus(reportId: String, newStatus: String): Result<Unit> {
        val path = "reports/$reportId"
        return try {
            db.collection("reports").document(reportId).update("status", newStatus).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun isUserAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        if (user.email == "arupkumarr11@gmail.com") return true
        return try {
            val doc = db.collection("admins").document(user.uid).get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }
}
