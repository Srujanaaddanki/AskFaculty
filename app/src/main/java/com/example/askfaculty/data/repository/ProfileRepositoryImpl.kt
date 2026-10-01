package com.example.askfaculty.data.repository

import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.domain.model.OfficeHour
import com.example.askfaculty.domain.model.User
import com.example.askfaculty.domain.repository.ProfileRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) : ProfileRepository {

    override suspend fun getUserProfile(uid: String): Result<User> = runCatching {
        val snap = firestore.collection(Constants.COLLECTION_USERS).document(uid).get().await()
        if (!snap.exists()) throw Exception("Profile not found")
        @Suppress("UNCHECKED_CAST")
        val officeHoursRaw = snap.get("officeHours") as? List<Map<String, Any>> ?: emptyList()
        User(
            uid         = snap.id,
            name        = snap.getString("name") ?: "",
            email       = snap.getString("email") ?: "",
            role        = snap.getString("role") ?: "",
            designation = snap.getString("designation") ?: "",
            department  = snap.getString("department") ?: "",
            college     = snap.getString("college") ?: "",
            photoUrl    = snap.getString("photoUrl") ?: "",
            subjects    = (snap.get("subjects") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            availability = snap.getString("availability") ?: "AVAILABLE",
            officeHours = officeHoursRaw.map {
                OfficeHour(it["day"] as? String ?: "", it["startTime"] as? String ?: "", it["endTime"] as? String ?: "")
            },
            createdAt   = snap.getTimestamp("createdAt"),
            updatedAt   = snap.getTimestamp("updatedAt"),
        )
    }

    override suspend fun updateProfile(user: User): Result<Unit> = runCatching {
        val data = mapOf(
            "name"        to user.name,
            "designation" to user.designation,
            "department"  to user.department,
            "college"     to user.college,
            "subjects"    to user.subjects,
            "updatedAt"   to Timestamp.now(),
        )
        firestore.collection(Constants.COLLECTION_USERS)
            .document(user.uid).set(data, SetOptions.merge()).await()
    }

    override suspend fun updateAvailability(uid: String, availability: String): Result<Unit> = runCatching {
        firestore.collection(Constants.COLLECTION_USERS).document(uid)
            .set(mapOf("availability" to availability, "updatedAt" to Timestamp.now()), SetOptions.merge()).await()
    }

    override suspend fun updateAvailability(uid: String, availability: String, officeHours: List<OfficeHour>): Result<Unit> = runCatching {
        val hoursData = officeHours.map { mapOf("day" to it.day, "startTime" to it.startTime, "endTime" to it.endTime) }
        val data = mapOf(
            "availability" to availability,
            "officeHours" to hoursData,
            "updatedAt" to Timestamp.now(),
        )
        firestore.collection(Constants.COLLECTION_USERS).document(uid)
            .set(data, SetOptions.merge()).await()
    }

    override suspend fun updateOfficeHours(uid: String, officeHours: List<OfficeHour>): Result<Unit> = runCatching {
        val data = officeHours.map { mapOf("day" to it.day, "startTime" to it.startTime, "endTime" to it.endTime) }
        firestore.collection(Constants.COLLECTION_USERS).document(uid)
            .set(mapOf("officeHours" to data, "updatedAt" to Timestamp.now()), SetOptions.merge()).await()
    }

    override suspend fun uploadProfilePhoto(uid: String, imageBytes: ByteArray): Result<String> = runCatching {
        val ref = storage.reference.child("${Constants.STORAGE_AVATARS}/$uid.jpg")
        ref.putBytes(imageBytes).await()
        val url = ref.downloadUrl.await().toString()
        firestore.collection(Constants.COLLECTION_USERS).document(uid)
            .set(mapOf("photoUrl" to url, "updatedAt" to Timestamp.now()), SetOptions.merge()).await()
        url
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw Exception("Not authenticated")
        val credential = EmailAuthProvider.getCredential(user.email ?: "", currentPassword)
        user.reauthenticate(credential).await()
        user.updatePassword(newPassword).await()
    }
}
