package com.example.askfaculty.data.repository

import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.domain.model.DashboardStats
import com.example.askfaculty.domain.model.Doubt
import com.example.askfaculty.domain.model.FrequentTopic
import com.example.askfaculty.domain.model.Message
import com.example.askfaculty.domain.repository.DoubtRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoubtRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : DoubtRepository {

    private val doubtsCol get() = firestore.collection(Constants.COLLECTION_DOUBTS)

    companion object {
        val DEMO_DOUBTS = listOf(
            Doubt(
                id = "demo_doubt_1",
                studentId = "std_101",
                studentName = "Aarav Sharma",
                studentIdNumber = "22CS104",
                facultyId = "30456",
                subject = "Data Structures",
                topic = "Dijkstra with Fibonacci Heap vs Binary Heap",
                question = "Professor, in today's class on shortest path algorithms, could you clarify why Fibonacci heap amortized decrease-key is O(1) whereas binary heap is O(log V)? In our lab benchmark, why does binary heap still run faster?",
                status = Constants.STATUS_PENDING,
                priority = Constants.PRIORITY_HIGH,
                hasUnread = true,
                messageCount = 2,
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 3600, 0),
            ),
            Doubt(
                id = "demo_doubt_2",
                studentId = "std_102",
                studentName = "Priya Patel",
                studentIdNumber = "22CS089",
                facultyId = "30456",
                subject = "Operating Systems",
                topic = "Deadlock Detection vs Prevention in Distributed OS",
                question = "Can we use Chandy-Misra-Haas edge-chasing algorithm when resources can be dynamically requested without ordering?",
                status = Constants.STATUS_IN_PROGRESS,
                priority = Constants.PRIORITY_MEDIUM,
                hasUnread = false,
                messageCount = 3,
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 14400, 0),
            ),
            Doubt(
                id = "demo_doubt_3",
                studentId = "std_103",
                studentName = "Rahul Verma",
                studentIdNumber = "22CS142",
                facultyId = "30456",
                subject = "Database Systems",
                topic = "B-Tree vs B+ Tree Indexing for Range Queries",
                question = "Why do relational databases predominantly use B+ Trees rather than B-Trees for disk-oriented tables?",
                status = Constants.STATUS_ANSWERED,
                priority = Constants.PRIORITY_LOW,
                hasUnread = false,
                messageCount = 4,
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 86400, 0),
            ),
        )

        val DEMO_MESSAGES = mutableMapOf(
            "demo_doubt_1" to mutableListOf(
                Message(
                    id = "m1",
                    doubtId = "demo_doubt_1",
                    senderId = "std_101",
                    senderName = "Aarav Sharma",
                    senderRole = Constants.SENDER_STUDENT,
                    message = "Professor, in today's class on shortest path algorithms, could you clarify why Fibonacci heap amortized decrease-key is O(1) whereas binary heap is O(log V)? In our lab benchmark, why does binary heap still run faster?",
                    createdAt = Timestamp(System.currentTimeMillis() / 1000 - 3600, 0),
                ),
                Message(
                    id = "m2",
                    doubtId = "demo_doubt_1",
                    senderId = "30456",
                    senderName = "Prof. Gowthami",
                    senderRole = Constants.SENDER_FACULTY,
                    message = "Great observation, Aarav. While Fibonacci heap has asymptotic O(1) amortized decrease-key, its constant factors and pointer manipulations are significant in practice. For sparse graphs up to typical sizes, binary heap's array-based contiguous memory layout has superior CPU cache locality.",
                    isOfficial = true,
                    createdAt = Timestamp(System.currentTimeMillis() / 1000 - 1800, 0),
                )
            ),
            "demo_doubt_2" to mutableListOf(
                Message(
                    id = "m3",
                    doubtId = "demo_doubt_2",
                    senderId = "std_102",
                    senderName = "Priya Patel",
                    senderRole = Constants.SENDER_STUDENT,
                    message = "Can we use Chandy-Misra-Haas edge-chasing algorithm when resources can be dynamically requested without ordering?",
                    createdAt = Timestamp(System.currentTimeMillis() / 1000 - 14400, 0),
                )
            ),
            "demo_doubt_3" to mutableListOf(
                Message(
                    id = "m4",
                    doubtId = "demo_doubt_3",
                    senderId = "std_103",
                    senderName = "Rahul Verma",
                    senderRole = Constants.SENDER_STUDENT,
                    message = "Why do relational databases predominantly use B+ Trees rather than B-Trees for disk-oriented tables?",
                    createdAt = Timestamp(System.currentTimeMillis() / 1000 - 86400, 0),
                ),
                Message(
                    id = "m5",
                    doubtId = "demo_doubt_3",
                    senderId = "30456",
                    senderName = "Prof. Gowthami",
                    senderRole = Constants.SENDER_FACULTY,
                    message = "B+ Trees store actual record pointers and data exclusively at leaf nodes linked in sequence, which allows sequential range scans without traversing parent nodes repeatedly. Internal nodes store only search keys, maximizing branching factor per page.",
                    isOfficial = true,
                    createdAt = Timestamp(System.currentTimeMillis() / 1000 - 43200, 0),
                )
            )
        )
    }

    override fun getDoubts(
        facultyId: String,
        status: String?,
        subject: String?,
        priority: String?,
        searchQuery: String?,
    ): Flow<List<Doubt>> = callbackFlow {
        var query: Query = doubtsCol.whereEqualTo("facultyId", facultyId)
        if (!status.isNullOrBlank()) query = query.whereEqualTo("status", status)
        if (!subject.isNullOrBlank()) query = query.whereEqualTo("subject", subject)
        if (!priority.isNullOrBlank()) query = query.whereEqualTo("priority", priority)
        query = query.orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(Constants.PAGE_SIZE)

        val reg = query.addSnapshotListener { snap, err ->
            val list = snap?.documents?.mapNotNull { it.toDoubt() } ?: emptyList()
            val resultList = if (list.isEmpty() && facultyId == "30456") {
                DEMO_DOUBTS.filter { doubt ->
                    (status.isNullOrBlank() || doubt.status == status) &&
                    (subject.isNullOrBlank() || doubt.subject == subject) &&
                    (priority.isNullOrBlank() || doubt.priority == priority)
                }
            } else list

            val filtered = if (!searchQuery.isNullOrBlank()) {
                val q = searchQuery.lowercase()
                resultList.filter {
                    it.topic.lowercase().contains(q) ||
                    it.question.lowercase().contains(q) ||
                    it.studentName.lowercase().contains(q) ||
                    it.subject.lowercase().contains(q)
                }
            } else resultList
            trySend(filtered)
        }
        awaitClose { reg.remove() }
    }

    override fun getPriorityDoubts(facultyId: String, limit: Int): Flow<List<Doubt>> = callbackFlow {
        val reg = doubtsCol
            .whereEqualTo("facultyId", facultyId)
            .whereEqualTo("status", Constants.STATUS_PENDING)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snap, err ->
                val list = snap?.documents?.mapNotNull { it.toDoubt() } ?: emptyList()
                if (list.isEmpty() && facultyId == "30456") {
                    trySend(DEMO_DOUBTS.filter { it.status == Constants.STATUS_PENDING }.take(limit))
                } else {
                    trySend(list)
                }
            }
        awaitClose { reg.remove() }
    }

    override suspend fun getDoubt(doubtId: String): Result<Doubt> = runCatching {
        if (doubtId.startsWith("demo_doubt_")) {
            return@runCatching DEMO_DOUBTS.find { it.id == doubtId } ?: DEMO_DOUBTS.first()
        }
        val snap = doubtsCol.document(doubtId).get().await()
        snap.toDoubt() ?: throw Exception("Doubt not found")
    }

    override fun getMessages(doubtId: String): Flow<List<Message>> = callbackFlow {
        if (doubtId.startsWith("demo_doubt_")) {
            trySend(DEMO_MESSAGES[doubtId] ?: emptyList())
        }
        val reg = doubtsCol.document(doubtId)
            .collection(Constants.COLLECTION_MESSAGES)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                val list = snap?.documents?.mapNotNull { it.toMessage() } ?: emptyList()
                if (list.isEmpty() && doubtId.startsWith("demo_doubt_")) {
                    trySend(DEMO_MESSAGES[doubtId] ?: emptyList())
                } else {
                    trySend(list)
                }
            }
        awaitClose { reg.remove() }
    }

    override suspend fun sendMessage(message: Message): Result<String> = runCatching {
        if (message.doubtId.startsWith("demo_doubt_")) {
            val list = DEMO_MESSAGES.getOrPut(message.doubtId) { mutableListOf() }
            val newMsg = message.copy(id = "msg_${System.currentTimeMillis()}", createdAt = Timestamp.now())
            list.add(newMsg)
            return@runCatching newMsg.id
        }

        val data = mapOf(
            "doubtId"         to message.doubtId,
            "senderId"        to message.senderId,
            "senderName"      to message.senderName,
            "senderRole"      to message.senderRole,
            "senderPhotoUrl"  to message.senderPhotoUrl,
            "message"         to message.message,
            "attachmentUrl"   to message.attachmentUrl,
            "attachmentType"  to message.attachmentType,
            "attachmentName"  to message.attachmentName,
            "isOfficial"      to message.isOfficial,
            "createdAt"       to Timestamp.now(),
        )
        val ref = doubtsCol.document(message.doubtId)
            .collection(Constants.COLLECTION_MESSAGES)
            .add(data).await()
        // Update doubt status & timestamp
        doubtsCol.document(message.doubtId).set(
            mapOf(
                "status"    to Constants.STATUS_IN_PROGRESS,
                "updatedAt" to Timestamp.now(),
            ),
            SetOptions.merge()
        ).await()
        ref.id
    }

    override suspend fun updateDoubtStatus(doubtId: String, status: String): Result<Unit> = runCatching {
        if (doubtId.startsWith("demo_doubt_")) return@runCatching
        doubtsCol.document(doubtId).set(
            mapOf("status" to status, "updatedAt" to Timestamp.now()),
            SetOptions.merge()
        ).await()
    }

    override suspend fun updateDoubtPriority(doubtId: String, priority: String): Result<Unit> = runCatching {
        if (doubtId.startsWith("demo_doubt_")) return@runCatching
        doubtsCol.document(doubtId).set(
            mapOf("priority" to priority, "updatedAt" to Timestamp.now()),
            SetOptions.merge()
        ).await()
    }

    override suspend fun markMessageOfficial(doubtId: String, messageId: String, isOfficial: Boolean): Result<Unit> = runCatching {
        if (doubtId.startsWith("demo_doubt_")) {
            val list = DEMO_MESSAGES[doubtId]
            val index = list?.indexOfFirst { it.id == messageId } ?: -1
            if (index >= 0) {
                list!![index] = list[index].copy(isOfficial = isOfficial)
            }
            return@runCatching
        }
        doubtsCol.document(doubtId)
            .collection(Constants.COLLECTION_MESSAGES)
            .document(messageId)
            .set(mapOf("isOfficial" to isOfficial), SetOptions.merge())
            .await()
    }

    override suspend fun getDashboardStats(facultyId: String): Result<DashboardStats> = runCatching {
        if (facultyId == "30456") {
            try {
                val realSnap = doubtsCol.whereEqualTo("facultyId", facultyId).get().await()
                if (!realSnap.isEmpty) {
                    val pending = realSnap.documents.count { it.getString("status") == Constants.STATUS_PENDING }
                    val unanswered = realSnap.documents.count { (it.getLong("messageCount") ?: 0L) == 0L }
                    return@runCatching DashboardStats(
                        pendingDoubts = pending,
                        unansweredDoubts = unanswered,
                        answeredToday = 3,
                        activeCommunities = 2,
                    )
                }
            } catch (_: Exception) {}
            return@runCatching DashboardStats(
                pendingDoubts = 1,
                unansweredDoubts = 1,
                answeredToday = 4,
                activeCommunities = 2,
            )
        }

        val allDoubts = doubtsCol.whereEqualTo("facultyId", facultyId).get().await()
        val pending    = allDoubts.documents.count { it.getString("status") == Constants.STATUS_PENDING }
        val unanswered = allDoubts.documents.count { (it.getLong("messageCount") ?: 0L) == 0L }

        // Start of today in millis
        val startOfToday = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.time
        val answeredToday = allDoubts.documents.count {
            val resolvedAt = it.getTimestamp("resolvedAt")?.toDate()
            resolvedAt != null && resolvedAt >= startOfToday
        }

        DashboardStats(
            pendingDoubts     = pending,
            unansweredDoubts  = unanswered,
            answeredToday     = answeredToday,
            activeCommunities = 0, // filled separately
        )
    }

    override fun getFrequentTopics(facultyId: String, limit: Int): Flow<List<FrequentTopic>> = callbackFlow {
        val reg = doubtsCol.whereEqualTo("facultyId", facultyId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(200)
            .addSnapshotListener { snap, err ->
                val topics = snap?.documents?.mapNotNull { it.getString("topic") } ?: emptyList()
                val freq = if (topics.isEmpty() && facultyId == "30456") {
                    listOf(
                        FrequentTopic("Shortest Path & Graph Traversal", 14),
                        FrequentTopic("Dynamic Programming Memoization", 11),
                        FrequentTopic("Process Scheduling & Synchronization", 9),
                        FrequentTopic("Virtual Memory & Page Replacement", 8),
                        FrequentTopic("B+ Tree Storage Engine", 6),
                    )
                } else {
                    topics.groupingBy { it }.eachCount()
                        .entries.sortedByDescending { it.value }
                        .take(limit)
                        .map { FrequentTopic(it.key, it.value) }
                }
                trySend(freq)
            }
        awaitClose { reg.remove() }
    }

    // ── Mappers ──────────────────────────────────────────────────────────────
    private fun com.google.firebase.firestore.DocumentSnapshot.toDoubt(): Doubt? {
        if (!exists()) return null
        return Doubt(
            id             = id,
            studentId      = getString("studentId") ?: "",
            studentName    = getString("studentName") ?: "",
            studentPhotoUrl = getString("studentPhotoUrl") ?: "",
            studentIdNumber = getString("studentIdNumber") ?: "",
            facultyId      = getString("facultyId") ?: "",
            subject        = getString("subject") ?: "",
            topic          = getString("topic") ?: "",
            question       = getString("question") ?: "",
            status         = getString("status") ?: Constants.STATUS_PENDING,
            priority       = getString("priority") ?: Constants.PRIORITY_MEDIUM,
            attachmentUrl  = getString("attachmentUrl") ?: "",
            attachmentType = getString("attachmentType") ?: "",
            hasUnread      = getBoolean("hasUnread") ?: false,
            messageCount   = (getLong("messageCount") ?: 0L).toInt(),
            createdAt      = getTimestamp("createdAt"),
            updatedAt      = getTimestamp("updatedAt"),
            resolvedAt     = getTimestamp("resolvedAt"),
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toMessage(): Message? {
        if (!exists()) return null
        return Message(
            id             = id,
            doubtId        = getString("doubtId") ?: "",
            senderId       = getString("senderId") ?: "",
            senderName     = getString("senderName") ?: "",
            senderRole     = getString("senderRole") ?: "",
            senderPhotoUrl = getString("senderPhotoUrl") ?: "",
            message        = getString("message") ?: "",
            attachmentUrl  = getString("attachmentUrl") ?: "",
            attachmentType = getString("attachmentType") ?: "",
            attachmentName = getString("attachmentName") ?: "",
            isOfficial     = getBoolean("isOfficial") ?: false,
            createdAt      = getTimestamp("createdAt"),
        )
    }
}
