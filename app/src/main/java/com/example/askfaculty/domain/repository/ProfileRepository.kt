package com.example.askfaculty.domain.repository

import com.example.askfaculty.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun getUserProfile(uid: String): Result<User>
    suspend fun updateProfile(user: User): Result<Unit>
    suspend fun updateAvailability(uid: String, availability: String): Result<Unit>
    suspend fun updateAvailability(uid: String, availability: String, officeHours: List<com.example.askfaculty.domain.model.OfficeHour>): Result<Unit>
    suspend fun updateOfficeHours(uid: String, officeHours: List<com.example.askfaculty.domain.model.OfficeHour>): Result<Unit>
    suspend fun uploadProfilePhoto(uid: String, imageBytes: ByteArray): Result<String>
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
}
