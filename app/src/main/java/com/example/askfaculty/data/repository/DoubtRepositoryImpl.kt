package com.example.askfaculty.data.repository

import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.domain.model.DashboardStats
import com.example.askfaculty.domain.model.Doubt
import com.example.askfaculty.domain.model.FrequentTopic
import com.example.askfaculty.domain.model.Message
import com.example.askfaculty.domain.model.StudentDashboardStats
import com.example.askfaculty.domain.repository.DoubtRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
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
        val IN_MEMORY_DOUBTS = mutableListOf(
            Doubt(
                id = "demo_doubt_1",
                studentId = "std_101",
                studentName = "Aarav Sharma",
                studentIdNumber = "22CS104",
                facultyId = "30456",
                facultyName = "Prof. Gowthami",
                subject = "Data Structures",
                topic = "Dijkstra with Fibonacci Heap vs Binary Heap",
                question = "Professor, in today's class on shortest path algorithms, could you clarify why Fibonacci heap amortized decrease-key is O(1) whereas binary heap is O(log V)? In our lab benchmark, why does binary heap still run faster?",
                codeSnippet = "priority_queue<pair<int, int>, vector<pair<int, int>>, greater<>> pq;\npq.push({0, src});\nwhile(!pq.empty()) { ... }",
                codeLanguage = "C++",
                status = Constants.STATUS_ANSWERED,
                priority = Constants.PRIORITY_HIGH,
                isPrivate = false,
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
                facultyName = "Prof. Gowthami",
                subject = "Operating Systems",
                topic = "Deadlock Detection vs Prevention in Distributed OS",
                question = "Can we use Chandy-Misra-Haas edge-chasing algorithm when resources can be dynamically requested without ordering?",
                status = Constants.STATUS_IN_PROGRESS,
                priority = Constants.PRIORITY_MEDIUM,
                isPrivate = true,
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
                facultyName = "Prof. Gowthami",
                subject = "Database Systems",
                topic = "B-Tree vs B+ Tree Indexing for Range Queries",
                question = "Why do relational databases predominantly use B+ Trees rather than B-Trees for disk-oriented tables?",
                status = Constants.STATUS_RESOLVED,
                priority = Constants.PRIORITY_LOW,
                isPrivate = false,
                hasUnread = false,
                messageCount = 4,
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 86400, 0),
                resolvedAt = Timestamp(System.currentTimeMillis() / 1000 - 43200, 0),
            ),
            Doubt(
                id = "demo_doubt_4",
                studentId = "std_101",
                studentName = "Aarav Sharma",
                studentIdNumber = "22CS104",
                facultyId = "30457",
                facultyName = "Dr. Rajesh Kumar",
                subject = "Computer Networks",
                topic = "TCP Congestion Control - CUBIC vs BBR",
                question = "How does Google's BBR congestion control avoid bufferbloat compared to loss-based congestion control algorithms like CUBIC?",
                status = Constants.STATUS_PENDING,
                priority = Constants.PRIORITY_HIGH,
                isPrivate = true,
                hasUnread = false,
                messageCount = 0,
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 1800, 0),
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
                    codeSnippet = "pq.push({dist[v], v});",
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
                    isHelpful = true,
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
                    isHelpful = true,
                    createdAt = Timestamp(System.currentTimeMillis() / 1000 - 43200, 0),
                )
            ),
            "demo_doubt_4" to mutableListOf()
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
            val resultList = if (list.isEmpty() && (facultyId == "30456" || facultyId == "30457")) {
                IN_MEMORY_DOUBTS.filter { doubt ->
                    doubt.facultyId == facultyId &&
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
                if (list.isEmpty() && (facultyId == "30456" || facultyId == "30457")) {
                    trySend(IN_MEMORY_DOUBTS.filter { it.facultyId == facultyId && it.status == Constants.STATUS_PENDING }.take(limit))
                } else {
                    trySend(list)
                }
            }
        awaitClose { reg.remove() }
    }

    override fun getStudentDoubts(
        studentId: String,
        status: String?,
        subject: String?,
        searchQuery: String?,
    ): Flow<List<Doubt>> = callbackFlow {
        var query: Query = doubtsCol.whereEqualTo("studentId", studentId)
        if (!status.isNullOrBlank()) query = query.whereEqualTo("status", status)
        if (!subject.isNullOrBlank()) query = query.whereEqualTo("subject", subject)
        query = query.orderBy("createdAt", Query.Direction.DESCENDING)

        val reg = query.addSnapshotListener { snap, err ->
            val list = snap?.documents?.mapNotNull { it.toDoubt() } ?: emptyList()
            val resultList = if (list.isEmpty() && (studentId == "std_101" || studentId.startsWith("std_"))) {
                IN_MEMORY_DOUBTS.filter { doubt ->
                    doubt.studentId == studentId &&
                    (status.isNullOrBlank() || doubt.status == status) &&
                    (subject.isNullOrBlank() || doubt.subject == subject)
                }
            } else list

            val filtered = if (!searchQuery.isNullOrBlank()) {
                val q = searchQuery.lowercase()
                resultList.filter {
                    it.topic.lowercase().contains(q) ||
                    it.question.lowercase().contains(q) ||
                    it.subject.lowercase().contains(q) ||
                    it.facultyName.lowercase().contains(q)
                }
            } else resultList
            trySend(filtered)
        }
        awaitClose { reg.remove() }
    }

    override fun getRecentAnsweredDoubts(limit: Int): Flow<List<Doubt>> = callbackFlow {
        val reg = doubtsCol
            .whereIn("status", listOf(Constants.STATUS_ANSWERED, Constants.STATUS_RESOLVED))
            .whereEqualTo("isPrivate", false)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snap, err ->
                val list = snap?.documents?.mapNotNull { it.toDoubt() } ?: emptyList()
                if (list.isEmpty()) {
                    trySend(IN_MEMORY_DOUBTS.filter {
                        (it.status == Constants.STATUS_ANSWERED || it.status == Constants.STATUS_RESOLVED) && !it.isPrivate
                    }.take(limit))
                } else {
                    trySend(list)
                }
            }
        awaitClose { reg.remove() }
    }

    override suspend fun getStudentStats(studentId: String): Result<StudentDashboardStats> = runCatching {
        if (studentId == "std_101" || studentId.startsWith("std_")) {
            val studentDoubts = IN_MEMORY_DOUBTS.filter { it.studentId == studentId }
            return@runCatching StudentDashboardStats(
                pendingDoubts = studentDoubts.count { it.status == Constants.STATUS_PENDING || it.status == Constants.STATUS_IN_PROGRESS },
                answeredDoubts = studentDoubts.count { it.status == Constants.STATUS_ANSWERED },
                resolvedDoubts = studentDoubts.count { it.status == Constants.STATUS_RESOLVED },
                joinedCommunities = 2,
            )
        }

        val all = doubtsCol.whereEqualTo("studentId", studentId).get().await()
        val pending = all.documents.count {
            val s = it.getString("status")
            s == Constants.STATUS_PENDING || s == Constants.STATUS_IN_PROGRESS
        }
        val answered = all.documents.count { it.getString("status") == Constants.STATUS_ANSWERED }
        val resolved = all.documents.count { it.getString("status") == Constants.STATUS_RESOLVED }

        StudentDashboardStats(
            pendingDoubts = pending,
            answeredDoubts = answered,
            resolvedDoubts = resolved,
            joinedCommunities = 2,
        )
    }

    override suspend fun submitDoubt(doubt: Doubt): Result<String> = runCatching {
        val now = Timestamp.now()
        val doubtId = "doubt_${System.currentTimeMillis()}"
        val newDoubt = doubt.copy(
            id = doubtId,
            status = Constants.STATUS_PENDING,
            createdAt = now,
            updatedAt = now,
        )

        // Add to in-memory list for instant reactivity
        IN_MEMORY_DOUBTS.add(0, newDoubt)
        DEMO_MESSAGES[doubtId] = mutableListOf(
            Message(
                id = "msg_${System.currentTimeMillis()}",
                doubtId = doubtId,
                senderId = doubt.studentId,
                senderName = doubt.studentName,
                senderRole = Constants.SENDER_STUDENT,
                senderPhotoUrl = doubt.studentPhotoUrl,
                message = doubt.question,
                codeSnippet = doubt.codeSnippet,
                attachmentUrl = doubt.attachmentUrl,
                attachmentType = doubt.attachmentType,
                attachmentName = doubt.attachmentName,
                createdAt = now,
            )
        )

        try {
            val data = mapOf(
                "studentId"       to newDoubt.studentId,
                "studentName"     to newDoubt.studentName,
                "studentPhotoUrl" to newDoubt.studentPhotoUrl,
                "studentIdNumber" to newDoubt.studentIdNumber,
                "facultyId"       to newDoubt.facultyId,
                "facultyName"     to newDoubt.facultyName,
                "subject"         to newDoubt.subject,
                "topic"           to newDoubt.topic,
                "question"        to newDoubt.question,
                "codeSnippet"     to newDoubt.codeSnippet,
                "codeLanguage"    to newDoubt.codeLanguage,
                "status"          to newDoubt.status,
                "priority"        to newDoubt.priority,
                "attachmentUrl"   to newDoubt.attachmentUrl,
                "attachmentType"  to newDoubt.attachmentType,
                "attachmentName"  to newDoubt.attachmentName,
                "isPrivate"       to newDoubt.isPrivate,
                "communityId"     to newDoubt.communityId,
                "hasUnread"       to true,
                "messageCount"    to 1,
                "createdAt"       to now,
                "updatedAt"       to now,
            )
            val docRef = doubtsCol.add(data).await()
            // Also add initial message subcollection document
            val initialMsgData = mapOf(
                "doubtId"        to docRef.id,
                "senderId"       to newDoubt.studentId,
                "senderName"     to newDoubt.studentName,
                "senderRole"     to Constants.SENDER_STUDENT,
                "senderPhotoUrl" to newDoubt.studentPhotoUrl,
                "message"        to newDoubt.question,
                "codeSnippet"    to newDoubt.codeSnippet,
                "attachmentUrl"  to newDoubt.attachmentUrl,
                "attachmentType" to newDoubt.attachmentType,
                "attachmentName" to newDoubt.attachmentName,
                "isOfficial"     to false,
                "createdAt"      to now,
            )
            docRef.collection(Constants.COLLECTION_MESSAGES).add(initialMsgData).await()
            docRef.id
        } catch (_: Exception) {
            doubtId
        }
    }

    override suspend fun updateDoubt(doubt: Doubt): Result<Unit> = runCatching {
        val index = IN_MEMORY_DOUBTS.indexOfFirst { it.id == doubt.id }
        if (index >= 0) {
            IN_MEMORY_DOUBTS[index] = doubt.copy(updatedAt = Timestamp.now())
        }
        if (!doubt.id.startsWith("demo_") && !doubt.id.startsWith("doubt_")) {
            doubtsCol.document(doubt.id).set(
                mapOf(
                    "topic"        to doubt.topic,
                    "question"     to doubt.question,
                    "codeSnippet"  to doubt.codeSnippet,
                    "codeLanguage" to doubt.codeLanguage,
                    "updatedAt"    to Timestamp.now(),
                ),
                SetOptions.merge()
            ).await()
        }
    }

    override suspend fun deleteDoubt(doubtId: String, studentId: String): Result<Unit> = runCatching {
        IN_MEMORY_DOUBTS.removeAll { it.id == doubtId }
        DEMO_MESSAGES.remove(doubtId)
        if (!doubtId.startsWith("demo_") && !doubtId.startsWith("doubt_")) {
            doubtsCol.document(doubtId).delete().await()
        }
    }

    override suspend fun reopenDoubt(doubtId: String, reason: String): Result<Unit> = runCatching {
        val index = IN_MEMORY_DOUBTS.indexOfFirst { it.id == doubtId }
        if (index >= 0) {
            IN_MEMORY_DOUBTS[index] = IN_MEMORY_DOUBTS[index].copy(
                status = Constants.STATUS_IN_PROGRESS,
                reopenReason = reason,
                updatedAt = Timestamp.now(),
            )
        }
        if (!doubtId.startsWith("demo_") && !doubtId.startsWith("doubt_")) {
            doubtsCol.document(doubtId).set(
                mapOf(
                    "status"       to Constants.STATUS_IN_PROGRESS,
                    "reopenReason" to reason,
                    "updatedAt"    to Timestamp.now(),
                ),
                SetOptions.merge()
            ).await()
        }
    }

    override suspend fun markDoubtResolvedByStudent(doubtId: String): Result<Unit> = runCatching {
        val index = IN_MEMORY_DOUBTS.indexOfFirst { it.id == doubtId }
        if (index >= 0) {
            IN_MEMORY_DOUBTS[index] = IN_MEMORY_DOUBTS[index].copy(
                status = Constants.STATUS_RESOLVED,
                resolvedAt = Timestamp.now(),
                updatedAt = Timestamp.now(),
            )
        }
        if (!doubtId.startsWith("demo_") && !doubtId.startsWith("doubt_")) {
            doubtsCol.document(doubtId).set(
                mapOf(
                    "status"     to Constants.STATUS_RESOLVED,
                    "resolvedAt" to Timestamp.now(),
                    "updatedAt"  to Timestamp.now(),
                ),
                SetOptions.merge()
            ).await()
        }
    }

    override suspend fun toggleHelpful(doubtId: String, messageId: String): Result<Unit> = runCatching {
        val list = DEMO_MESSAGES[doubtId]
        val msgIndex = list?.indexOfFirst { it.id == messageId } ?: -1
        if (msgIndex >= 0) {
            val msg = list!![msgIndex]
            list[msgIndex] = msg.copy(isHelpful = !msg.isHelpful)
        }
        if (!doubtId.startsWith("demo_") && !doubtId.startsWith("doubt_")) {
            doubtsCol.document(doubtId)
                .collection(Constants.COLLECTION_MESSAGES)
                .document(messageId)
                .set(mapOf("isHelpful" to true), SetOptions.merge())
                .await()
        }
    }

    override suspend fun getDoubt(doubtId: String): Result<Doubt> = runCatching {
        val local = IN_MEMORY_DOUBTS.find { it.id == doubtId }
        if (local != null) return@runCatching local

        val snap = doubtsCol.document(doubtId).get().await()
        snap.toDoubt() ?: throw Exception("Doubt not found")
    }

    override fun getMessages(doubtId: String): Flow<List<Message>> = callbackFlow {
        if (DEMO_MESSAGES.containsKey(doubtId)) {
            trySend(DEMO_MESSAGES[doubtId] ?: emptyList())
        }
        val reg = doubtsCol.document(doubtId)
            .collection(Constants.COLLECTION_MESSAGES)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                val list = snap?.documents?.mapNotNull { it.toMessage() } ?: emptyList()
                if (list.isEmpty() && DEMO_MESSAGES.containsKey(doubtId)) {
                    trySend(DEMO_MESSAGES[doubtId] ?: emptyList())
                } else {
                    trySend(list)
                }
            }
        awaitClose { reg.remove() }
    }

    override suspend fun sendMessage(message: Message): Result<String> = runCatching {
        if (DEMO_MESSAGES.containsKey(message.doubtId)) {
            val list = DEMO_MESSAGES.getOrPut(message.doubtId) { mutableListOf() }
            val newMsg = message.copy(id = "msg_${System.currentTimeMillis()}", createdAt = Timestamp.now())
            list.add(newMsg)
            // Update doubt message count
            val dIndex = IN_MEMORY_DOUBTS.indexOfFirst { it.id == message.doubtId }
            if (dIndex >= 0) {
                val d = IN_MEMORY_DOUBTS[dIndex]
                val newStatus = if (message.senderRole == Constants.SENDER_FACULTY) Constants.STATUS_ANSWERED else Constants.STATUS_IN_PROGRESS
                IN_MEMORY_DOUBTS[dIndex] = d.copy(messageCount = list.size, status = newStatus, updatedAt = Timestamp.now())
            }
            return@runCatching newMsg.id
        }

        val data = mapOf(
            "doubtId"        to message.doubtId,
            "senderId"       to message.senderId,
            "senderName"     to message.senderName,
            "senderRole"     to message.senderRole,
            "senderPhotoUrl" to message.senderPhotoUrl,
            "message"        to message.message,
            "codeSnippet"    to message.codeSnippet,
            "attachmentUrl"  to message.attachmentUrl,
            "attachmentType" to message.attachmentType,
            "attachmentName" to message.attachmentName,
            "isOfficial"     to message.isOfficial,
            "isHelpful"      to message.isHelpful,
            "createdAt"      to Timestamp.now(),
        )
        val ref = doubtsCol.document(message.doubtId)
            .collection(Constants.COLLECTION_MESSAGES)
            .add(data).await()

        val nextStatus = if (message.senderRole == Constants.SENDER_FACULTY) Constants.STATUS_ANSWERED else Constants.STATUS_IN_PROGRESS
        doubtsCol.document(message.doubtId).set(
            mapOf(
                "status"       to nextStatus,
                "messageCount" to FieldValue.increment(1),
                "updatedAt"    to Timestamp.now(),
            ),
            SetOptions.merge()
        ).await()
        ref.id
    }

    override suspend fun updateDoubtStatus(doubtId: String, status: String): Result<Unit> = runCatching {
        val index = IN_MEMORY_DOUBTS.indexOfFirst { it.id == doubtId }
        if (index >= 0) {
            IN_MEMORY_DOUBTS[index] = IN_MEMORY_DOUBTS[index].copy(status = status, updatedAt = Timestamp.now())
        }
        if (!doubtId.startsWith("demo_") && !doubtId.startsWith("doubt_")) {
            doubtsCol.document(doubtId).set(
                mapOf("status" to status, "updatedAt" to Timestamp.now()),
                SetOptions.merge()
            ).await()
        }
    }

    override suspend fun updateDoubtPriority(doubtId: String, priority: String): Result<Unit> = runCatching {
        val index = IN_MEMORY_DOUBTS.indexOfFirst { it.id == doubtId }
        if (index >= 0) {
            IN_MEMORY_DOUBTS[index] = IN_MEMORY_DOUBTS[index].copy(priority = priority, updatedAt = Timestamp.now())
        }
        if (!doubtId.startsWith("demo_") && !doubtId.startsWith("doubt_")) {
            doubtsCol.document(doubtId).set(
                mapOf("priority" to priority, "updatedAt" to Timestamp.now()),
                SetOptions.merge()
            ).await()
        }
    }

    override suspend fun markMessageOfficial(doubtId: String, messageId: String, isOfficial: Boolean): Result<Unit> = runCatching {
        val list = DEMO_MESSAGES[doubtId]
        val index = list?.indexOfFirst { it.id == messageId } ?: -1
        if (index >= 0) {
            list!![index] = list[index].copy(isOfficial = isOfficial)
        }
        if (!doubtId.startsWith("demo_") && !doubtId.startsWith("doubt_")) {
            doubtsCol.document(doubtId)
                .collection(Constants.COLLECTION_MESSAGES)
                .document(messageId)
                .set(mapOf("isOfficial" to isOfficial), SetOptions.merge())
                .await()
        }
    }

    override suspend fun getDashboardStats(facultyId: String): Result<DashboardStats> = runCatching {
        if (facultyId == "30456" || facultyId == "30457") {
            val facultyDoubts = IN_MEMORY_DOUBTS.filter { it.facultyId == facultyId }
            return@runCatching DashboardStats(
                pendingDoubts = facultyDoubts.count { it.status == Constants.STATUS_PENDING },
                unansweredDoubts = facultyDoubts.count { it.messageCount <= 1 },
                answeredToday = 3,
                activeCommunities = 2,
            )
        }

        val allDoubts = doubtsCol.whereEqualTo("facultyId", facultyId).get().await()
        val pending    = allDoubts.documents.count { it.getString("status") == Constants.STATUS_PENDING }
        val unanswered = allDoubts.documents.count { (it.getLong("messageCount") ?: 0L) <= 1L }

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
            activeCommunities = 2,
        )
    }

    override fun getFrequentTopics(facultyId: String, limit: Int): Flow<List<FrequentTopic>> = callbackFlow {
        val reg = doubtsCol.whereEqualTo("facultyId", facultyId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(200)
            .addSnapshotListener { snap, err ->
                val topics = snap?.documents?.mapNotNull { it.getString("topic") } ?: emptyList()
                val freq = if (topics.isEmpty() && (facultyId == "30456" || facultyId == "30457")) {
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

    override fun searchPublicKnowledgeBase(query: String): Flow<List<Doubt>> = callbackFlow {
        val q = query.trim().lowercase()
        val inMemoryMatches = IN_MEMORY_DOUBTS.filter {
            !it.isPrivate && (
                it.topic.lowercase().contains(q) ||
                it.question.lowercase().contains(q) ||
                it.subject.lowercase().contains(q)
            )
        }
        trySend(inMemoryMatches)

        val reg = doubtsCol.whereEqualTo("isPrivate", false).limit(50).addSnapshotListener { snap, err ->
            val list = snap?.documents?.mapNotNull { it.toDoubt() } ?: emptyList()
            val filtered = (list + inMemoryMatches).distinctBy { it.id }.filter {
                it.topic.lowercase().contains(q) ||
                it.question.lowercase().contains(q) ||
                it.subject.lowercase().contains(q)
            }
            trySend(filtered)
        }
        awaitClose { reg.remove() }
    }

    // ── Mappers ──────────────────────────────────────────────────────────────
    private fun com.google.firebase.firestore.DocumentSnapshot.toDoubt(): Doubt? {
        if (!exists()) return null
        return Doubt(
            id              = id,
            studentId       = getString("studentId") ?: "",
            studentName     = getString("studentName") ?: "",
            studentPhotoUrl = getString("studentPhotoUrl") ?: "",
            studentIdNumber = getString("studentIdNumber") ?: "",
            facultyId       = getString("facultyId") ?: "",
            facultyName     = getString("facultyName") ?: "",
            subject         = getString("subject") ?: "",
            topic           = getString("topic") ?: "",
            question        = getString("question") ?: "",
            codeSnippet     = getString("codeSnippet") ?: "",
            codeLanguage    = getString("codeLanguage") ?: "",
            status          = getString("status") ?: Constants.STATUS_PENDING,
            priority        = getString("priority") ?: Constants.PRIORITY_MEDIUM,
            attachmentUrl   = getString("attachmentUrl") ?: "",
            attachmentType  = getString("attachmentType") ?: "",
            attachmentName  = getString("attachmentName") ?: "",
            isPrivate       = getBoolean("isPrivate") ?: true,
            communityId     = getString("communityId") ?: "",
            hasUnread       = getBoolean("hasUnread") ?: false,
            messageCount    = (getLong("messageCount") ?: 0L).toInt(),
            helpfulCount    = (getLong("helpfulCount") ?: 0L).toInt(),
            reopenReason    = getString("reopenReason") ?: "",
            createdAt       = getTimestamp("createdAt"),
            updatedAt       = getTimestamp("updatedAt"),
            resolvedAt      = getTimestamp("resolvedAt"),
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
            codeSnippet    = getString("codeSnippet") ?: "",
            attachmentUrl  = getString("attachmentUrl") ?: "",
            attachmentType = getString("attachmentType") ?: "",
            attachmentName = getString("attachmentName") ?: "",
            isOfficial     = getBoolean("isOfficial") ?: false,
            isHelpful      = getBoolean("isHelpful") ?: false,
            createdAt      = getTimestamp("createdAt"),
        )
    }
}
