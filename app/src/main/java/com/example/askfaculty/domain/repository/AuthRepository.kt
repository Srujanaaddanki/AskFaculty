package com.example.askfaculty.domain.repository

import com.example.askfaculty.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun loginWithFacultyId(facultyId: String, password: String): Result<User>
    suspend fun logout()
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun sendPasswordResetForFacultyId(facultyId: String): Result<Unit>
    suspend fun getFacultyProfile(uid: String): Result<User>
    suspend fun updateFcmToken(uid: String, token: String): Result<Unit>
    fun isLoggedIn(): Boolean
}
