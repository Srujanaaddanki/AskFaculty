package com.example.askfaculty.data.repository

import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.domain.model.OfficeHour
import com.example.askfaculty.domain.model.User
import com.example.askfaculty.domain.repository.ProfileRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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

    private val localBookmarks = mutableMapOf<String, MutableSet<String>>(
        "std_101" to mutableSetOf("demo_doubt_1", "res_1")
    )

    override suspend fun getUserProfile(uid: String): Result<User> = runCatching {
        if (uid == "30456") {
            return@runCatching AuthRepositoryImpl.DEMO_FACULTY_USER
        }
        if (uid == "30457") {
            return@runCatching AuthRepositoryImpl.DEMO_FACULTY_USER_2
        }
        if (uid == "std_101" || uid.startsWith("std_")) {
            return@runCatching AuthRepositoryImpl.DEMO_STUDENT_USER.copy(
                bookmarks = localBookmarks[uid]?.toList() ?: listOf("demo_doubt_1")
            )
        }

        val snap = firestore.collection(Constants.COLLECTION_USERS).document(uid).get().await()
        if (!snap.exists()) throw Exception("Profile not found")
        @Suppress("UNCHECKED_CAST")
        val officeHoursRaw = snap.get("officeHours") as? List<Map<String, Any>> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val joinedCommunitiesRaw = snap.get("joinedCommunities") as? List<String> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val bookmarksRaw = snap.get("bookmarks") as? List<String> ?: emptyList()

        User(
            uid               = snap.id,
            name              = snap.getString("name") ?: "",
            email             = snap.getString("email") ?: "",
            role              = snap.getString("role") ?: "",
            studentId         = snap.getString("studentId") ?: "",
            academicYear      = snap.getString("academicYear") ?: "",
            semester          = snap.getString("semester") ?: "",
            designation       = snap.getString("designation") ?: "",
            department        = snap.getString("department") ?: "",
            college           = snap.getString("college") ?: "",
            photoUrl          = snap.getString("photoUrl") ?: "",
            subjects          = (snap.get("subjects") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            joinedCommunities = joinedCommunitiesRaw,
            bookmarks         = bookmarksRaw,
            availability      = snap.getString("availability") ?: "AVAILABLE",
            officeHours       = officeHoursRaw.map {
                OfficeHour(it["day"] as? String ?: "", it["startTime"] as? String ?: "", it["endTime"] as? String ?: "")
            },
            createdAt         = snap.getTimestamp("createdAt"),
            updatedAt         = snap.getTimestamp("updatedAt"),
        )
    }

    override suspend fun updateProfile(user: User): Result<Unit> = runCatching {
        val data = mapOf(
            "name"         to user.name,
            "designation"  to user.designation,
            "department"   to user.department,
            "college"      to user.college,
            "studentId"    to user.studentId,
            "academicYear" to user.academicYear,
            "semester"     to user.semester,
            "subjects"     to user.subjects,
            "updatedAt"    to Timestamp.now(),
        )
        if (!user.uid.startsWith("std_") && user.uid != "30456" && user.uid != "30457") {
            firestore.collection(Constants.COLLECTION_USERS)
                .document(user.uid).set(data, SetOptions.merge()).await()
        }
    }

    override suspend fun updateAvailability(uid: String, availability: String): Result<Unit> = runCatching {
        if (!uid.startsWith("std_") && uid != "30456" && uid != "30457") {
            firestore.collection(Constants.COLLECTION_USERS).document(uid)
                .set(mapOf("availability" to availability, "updatedAt" to Timestamp.now()), SetOptions.merge()).await()
        }
    }

    override suspend fun updateAvailability(uid: String, availability: String, officeHours: List<OfficeHour>): Result<Unit> = runCatching {
        val hoursData = officeHours.map { mapOf("day" to it.day, "startTime" to it.startTime, "endTime" to it.endTime) }
        val data = mapOf(
            "availability" to availability,
            "officeHours" to hoursData,
            "updatedAt" to Timestamp.now(),
        )
        if (!uid.startsWith("std_") && uid != "30456" && uid != "30457") {
            firestore.collection(Constants.COLLECTION_USERS).document(uid)
                .set(data, SetOptions.merge()).await()
        }
    }

    override suspend fun updateOfficeHours(uid: String, officeHours: List<OfficeHour>): Result<Unit> = runCatching {
        val data = officeHours.map { mapOf("day" to it.day, "startTime" to it.startTime, "endTime" to it.endTime) }
        if (!uid.startsWith("std_") && uid != "30456" && uid != "30457") {
            firestore.collection(Constants.COLLECTION_USERS).document(uid)
                .set(mapOf("officeHours" to data, "updatedAt" to Timestamp.now()), SetOptions.merge()).await()
        }
    }

    override suspend fun uploadProfilePhoto(uid: String, imageBytes: ByteArray): Result<String> = runCatching {
        try {
            val ref = storage.reference.child("${Constants.STORAGE_AVATARS}/$uid.jpg")
            ref.putBytes(imageBytes).await()
            val url = ref.downloadUrl.await().toString()
            firestore.collection(Constants.COLLECTION_USERS).document(uid)
                .set(mapOf("photoUrl" to url, "updatedAt" to Timestamp.now()), SetOptions.merge()).await()
            url
        } catch (_: Exception) {
            // Offline placeholder
            "https://ui-avatars.com/api/?name=User&background=4F46E5&color=fff"
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: return@runCatching // if demo, immediate success
        val credential = EmailAuthProvider.getCredential(user.email ?: "", currentPassword)
        user.reauthenticate(credential).await()
        user.updatePassword(newPassword).await()
    }

    override suspend fun toggleBookmark(uid: String, itemId: String): Result<Boolean> = runCatching {
        val set = localBookmarks.getOrPut(uid) { mutableSetOf() }
        val isBookmarkedNow = if (set.contains(itemId)) {
            set.remove(itemId)
            if (!uid.startsWith("std_") && uid != "30456") {
                firestore.collection(Constants.COLLECTION_USERS).document(uid)
                    .update("bookmarks", FieldValue.arrayRemove(itemId)).await()
            }
            false
        } else {
            set.add(itemId)
            if (!uid.startsWith("std_") && uid != "30456") {
                firestore.collection(Constants.COLLECTION_USERS).document(uid)
                    .update("bookmarks", FieldValue.arrayUnion(itemId)).await()
            }
            true
        }
        isBookmarkedNow
    }

    override suspend fun getBookmarks(uid: String): Result<List<String>> = runCatching {
        val local = localBookmarks[uid]?.toList() ?: emptyList()
        try {
            val snap = firestore.collection(Constants.COLLECTION_USERS).document(uid).get().await()
            @Suppress("UNCHECKED_CAST")
            val remote = snap.get("bookmarks") as? List<String>
            if (remote != null) return@runCatching (remote + local).distinct()
        } catch (_: Exception) {}
        local
    }
}
