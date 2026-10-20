package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Report(
    @DocumentId val id: String = "",
    val reporterId: String = "",
    val targetType: String = "post", // "post" or "user"
    val targetId: String = "",
    val reason: String = "",
    val status: String = "pending", // "pending", "reviewed", "dismissed"
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "reporterId" to reporterId,
            "targetType" to targetType,
            "targetId" to targetId,
            "reason" to reason,
            "status" to status,
            "createdAt" to createdAt
        ).filterValues { it != null }
    }
}
