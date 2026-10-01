package com.example.askfaculty.domain.repository

import com.example.askfaculty.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(userId: String): Flow<List<Notification>>
    suspend fun markAsRead(notificationId: String): Result<Unit>
    suspend fun markAllAsRead(userId: String): Result<Unit>
    fun getUnreadCount(userId: String): Flow<Int>
}
