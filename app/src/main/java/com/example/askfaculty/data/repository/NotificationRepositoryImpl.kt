package com.example.askfaculty.data.repository

import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.domain.model.Notification
import com.example.askfaculty.domain.repository.NotificationRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : NotificationRepository {

    private val notifCol get() = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)

    companion object {
        val DEMO_NOTIFICATIONS = listOf(
            Notification(
                id = "demo_notif_1",
                recipientId = "30456",
                title = "New Academic Doubt",
                body = "Aarav Sharma submitted a high priority doubt: 'Dijkstra with Fibonacci Heap'",
                type = Constants.NOTIF_NEW_DOUBT,
                doubtId = "demo_doubt_1",
                isRead = false,
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 3600, 0),
            ),
            Notification(
                id = "demo_notif_2",
                recipientId = "30456",
                title = "Student Follow-up",
                body = "Priya Patel replied to discussion thread on Distributed OS",
                type = Constants.NOTIF_FOLLOW_UP,
                doubtId = "demo_doubt_2",
                isRead = true,
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 14400, 0),
            ),
            Notification(
                id = "demo_notif_3",
                recipientId = "30456",
                title = "New Announcement in Space",
                body = "Midterm review session schedules published in CS 301",
                type = Constants.NOTIF_COMMUNITY,
                isRead = true,
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 86400, 0),
            ),
        )
    }

    override fun getNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val reg = notifCol
            .whereEqualTo("recipientId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snap, err ->
                val list = snap?.documents?.mapNotNull { it.toNotification() } ?: emptyList()
                if (list.isEmpty() && userId == "30456") {
                    trySend(DEMO_NOTIFICATIONS)
                } else {
                    trySend(list)
                }
            }
        awaitClose { reg.remove() }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> = runCatching {
        if (notificationId.startsWith("demo_notif_")) return@runCatching
        notifCol.document(notificationId)
            .set(mapOf("isRead" to true), SetOptions.merge()).await()
    }

    override suspend fun markAllAsRead(userId: String): Result<Unit> = runCatching {
        if (userId == "30456") return@runCatching
        val unread = notifCol.whereEqualTo("recipientId", userId)
            .whereEqualTo("isRead", false).get().await()
        val batch = firestore.batch()
        unread.documents.forEach { batch.update(it.reference, "isRead", true) }
        batch.commit().await()
    }

    override fun getUnreadCount(userId: String): Flow<Int> = callbackFlow {
        val reg = notifCol.whereEqualTo("recipientId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snap, err ->
                val count = snap?.size() ?: 0
                if (count == 0 && userId == "30456") {
                    trySend(1)
                } else {
                    trySend(count)
                }
            }
        awaitClose { reg.remove() }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toNotification(): Notification? {
        if (!exists()) return null
        return Notification(
            id          = id,
            recipientId = getString("recipientId") ?: "",
            type        = getString("type") ?: "",
            title       = getString("title") ?: "",
            body        = getString("body") ?: "",
            doubtId     = getString("doubtId") ?: "",
            communityId = getString("communityId") ?: "",
            senderId    = getString("senderId") ?: "",
            senderName  = getString("senderName") ?: "",
            isRead      = getBoolean("isRead") ?: false,
            createdAt   = getTimestamp("createdAt"),
        )
    }
}
