package com.example.askfaculty.data.repository

import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.domain.model.OfficeHour
import com.example.askfaculty.domain.model.User
import com.example.askfaculty.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AuthRepository {

    private val _demoUser = MutableStateFlow<User?>(null)

    companion object {
        val DEMO_FACULTY_USER = User(
            uid = "30456",
            name = "Prof. Gowthami",
            email = "faculty.30456@askfaculty.edu",
            role = Constants.ROLE_FACULTY,
            designation = "Associate Professor",
            department = "Department of Computer Science & Engineering",
            college = "University College of Engineering",
            availability = Constants.AVAILABILITY_AVAILABLE,
            subjects = listOf("Data Structures", "Design & Analysis of Algorithms", "Artificial Intelligence", "Operating Systems"),
            officeHours = listOf(
                OfficeHour("Monday", "10:00 AM", "12:00 PM"),
                OfficeHour("Wednesday", "02:00 PM", "04:00 PM"),
                OfficeHour("Friday", "11:00 AM", "01:00 PM"),
            ),
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentUser: Flow<User?> = _demoUser.flatMapLatest { demo ->
        if (demo != null) {
            flowOf<User?>(demo)
        } else {
            callbackFlow<User?> {
                val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                    val fbUser = firebaseAuth.currentUser
                    if (fbUser == null) {
                        trySend(null)
                    } else {
                        firestore.collection(Constants.COLLECTION_USERS)
                            .document(fbUser.uid)
                            .get()
                            .addOnSuccessListener { snap ->
                                trySend(snap.toUser())
                            }
                            .addOnFailureListener {
                                trySend(null)
                            }
                    }
                }
                auth.addAuthStateListener(listener)
                awaitClose { auth.removeAuthStateListener(listener) }
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw Exception("Authentication failed")
        val snap = firestore.collection(Constants.COLLECTION_USERS).document(uid).get().await()
        val user = snap.toUser() ?: throw Exception("User profile not found")
        if (user.role != Constants.ROLE_FACULTY) {
            auth.signOut()
            throw Exception("Access denied. This app is for faculty members only.")
        }
        user
    }

    override suspend fun loginWithFacultyId(facultyId: String, password: String): Result<User> = runCatching {
        val trimmed = facultyId.trim()

        // Dummy / Demo test account credentials requested for UI verification
        if (trimmed == "30456" && password == "Gow@1415") {
            try {
                val authEmail = "faculty.30456@askfaculty.edu"
                val result = auth.signInWithEmailAndPassword(authEmail, password).await()
                val uid = result.user?.uid ?: "30456"
                val snap = firestore.collection(Constants.COLLECTION_USERS).document(uid).get().await()
                val user = snap.toUser()
                if (user != null && user.role == Constants.ROLE_FACULTY) {
                    _demoUser.value = null
                    return@runCatching user
                }
            } catch (_: Exception) {
                // Not provisioned in Firebase yet, use verified dummy faculty profile
            }
            _demoUser.value = DEMO_FACULTY_USER
            return@runCatching DEMO_FACULTY_USER
        }

        val authEmail = if (trimmed.contains("@")) trimmed else "faculty.${trimmed}@askfaculty.edu"
        val result = auth.signInWithEmailAndPassword(authEmail, password).await()
        val uid = result.user?.uid ?: throw Exception("Authentication failed")
        val snap = firestore.collection(Constants.COLLECTION_USERS).document(uid).get().await()
        val user = snap.toUser() ?: throw Exception("Faculty profile not found")
        if (user.role != Constants.ROLE_FACULTY) {
            auth.signOut()
            throw Exception("Access restricted. This application is exclusively for registered faculty members.")
        }
        user
    }

    override suspend fun logout() {
        _demoUser.value = null
        auth.signOut()
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    override suspend fun sendPasswordResetForFacultyId(facultyId: String): Result<Unit> = runCatching {
        val trimmed = facultyId.trim()
        if (trimmed == "30456") {
            // Immediate success for demo ID
            return@runCatching
        }
        val authEmail = if (trimmed.contains("@")) trimmed else "faculty.${trimmed}@askfaculty.edu"
        auth.sendPasswordResetEmail(authEmail).await()
    }

    override suspend fun getFacultyProfile(uid: String): Result<User> = runCatching {
        if (_demoUser.value?.uid == uid || uid == "30456") {
            return@runCatching _demoUser.value ?: DEMO_FACULTY_USER
        }
        val snap = firestore.collection(Constants.COLLECTION_USERS).document(uid).get().await()
        snap.toUser() ?: throw Exception("Profile not found")
    }

    override suspend fun updateFcmToken(uid: String, token: String): Result<Unit> = runCatching {
        if (uid == "30456") return@runCatching
        firestore.collection(Constants.COLLECTION_USERS).document(uid)
            .set(mapOf("fcmToken" to token), SetOptions.merge()).await()
    }

    override fun isLoggedIn(): Boolean = _demoUser.value != null || auth.currentUser != null

    private fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User? {
        if (!exists()) return null
        @Suppress("UNCHECKED_CAST")
        val officeHoursRaw = get("officeHours") as? List<Map<String, Any>> ?: emptyList()
        val officeHours = officeHoursRaw.map {
            OfficeHour(
                day = it["day"] as? String ?: "",
                startTime = it["startTime"] as? String ?: "",
                endTime = it["endTime"] as? String ?: "",
            )
        }
        @Suppress("UNCHECKED_CAST")
        val subjectsRaw = get("subjects") as? List<String> ?: emptyList()

        return User(
            uid = id,
            name = getString("name") ?: "",
            email = getString("email") ?: "",
            role = getString("role") ?: Constants.ROLE_FACULTY,
            designation = getString("designation") ?: "",
            department = getString("department") ?: "",
            college = getString("college") ?: "",
            photoUrl = getString("photoUrl") ?: "",
            subjects = subjectsRaw,
            fcmToken = getString("fcmToken") ?: "",
            availability = getString("availability") ?: Constants.AVAILABILITY_AVAILABLE,
            officeHours = officeHours,
            createdAt = getTimestamp("createdAt"),
            updatedAt = getTimestamp("updatedAt"),
        )
    }
}
