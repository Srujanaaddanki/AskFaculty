package com.example.askfaculty.data.repository

import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.domain.model.Community
import com.example.askfaculty.domain.model.Post
import com.example.askfaculty.domain.model.PostReply
import com.example.askfaculty.domain.model.Resource
import com.example.askfaculty.domain.repository.CommunityRepository
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
class CommunityRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : CommunityRepository {

    private val commCol get() = firestore.collection(Constants.COLLECTION_COMMUNITIES)

    companion object {
        val IN_MEMORY_COMMUNITIES = mutableListOf(
            Community(
                id = "demo_comm_1",
                name = "CS 301: Advanced Data Structures & Algorithms",
                description = "Course syllabus discussions, algorithmic complexity problem sets, and lab reference implementations.",
                subject = "Data Structures",
                facultyId = "30456",
                facultyName = "Prof. Gowthami",
                coverImageUrl = "",
                memberCount = 68,
                postCount = 14,
                members = listOf("30456", "std_101", "std_102"),
                isActive = true,
                isPublic = true,
                createdAt = Timestamp.now(),
            ),
            Community(
                id = "demo_comm_2",
                name = "CS 402: Operating Systems & Distributed Architecture",
                description = "Process scheduling, thread synchronization benchmarks, memory management, and deadlock resolution discussions.",
                subject = "Operating Systems",
                facultyId = "30456",
                facultyName = "Prof. Gowthami",
                coverImageUrl = "",
                memberCount = 52,
                postCount = 11,
                members = listOf("30456", "std_101"),
                isActive = true,
                isPublic = true,
                createdAt = Timestamp.now(),
            ),
            Community(
                id = "demo_comm_3",
                name = "CS 405: Cloud Computing & Distributed Networks",
                description = "Microservice architectures, container orchestration, socket programming, and protocol analysis.",
                subject = "Computer Networks",
                facultyId = "30457",
                facultyName = "Dr. Rajesh Kumar",
                coverImageUrl = "",
                memberCount = 44,
                postCount = 9,
                members = listOf("30457", "std_102"),
                isActive = true,
                isPublic = true,
                createdAt = Timestamp.now(),
            ),
        )

        val IN_MEMORY_POSTS = mutableMapOf(
            "demo_comm_1" to mutableListOf(
                Post(
                    id = "p1",
                    communityId = "demo_comm_1",
                    authorId = "30456",
                    authorName = "Prof. Gowthami",
                    authorRole = Constants.ROLE_FACULTY,
                    content = "📢 Pinned Announcement: Midterm Problem Set 2 on Dynamic Programming and Dijkstra algorithm variations has been released. Check Resources section.",
                    isAnnouncement = true,
                    isPinned = true,
                    isOfficialAnswer = true,
                    helpfulCount = 28,
                    replyCount = 3,
                    createdAt = Timestamp(System.currentTimeMillis() / 1000 - 86400, 0),
                ),
                Post(
                    id = "p2",
                    communityId = "demo_comm_1",
                    authorId = "std_101",
                    authorName = "Aarav Sharma",
                    authorRole = Constants.ROLE_STUDENT,
                    content = "Has anyone implemented the A* heuristic search using Manhattan distance? How did you optimize the closed set lookup for grid-based pathfinding?",
                    codeSnippet = "int h = abs(node.x - goal.x) + abs(node.y - goal.y);",
                    isAnnouncement = false,
                    isPinned = false,
                    helpfulCount = 12,
                    replyCount = 2,
                    createdAt = Timestamp(System.currentTimeMillis() / 1000 - 43200, 0),
                ),
            ),
            "demo_comm_2" to mutableListOf(
                Post(
                    id = "p3",
                    communityId = "demo_comm_2",
                    authorId = "30456",
                    authorName = "Prof. Gowthami",
                    authorRole = Constants.ROLE_FACULTY,
                    content = "📢 Lab 4 Guidelines: Please review the POSIX pthread mutex lock and semaphore implementation guide uploaded under course resources.",
                    isAnnouncement = true,
                    isPinned = true,
                    isOfficialAnswer = true,
                    helpfulCount = 34,
                    replyCount = 1,
                    createdAt = Timestamp(System.currentTimeMillis() / 1000 - 172800, 0),
                )
            )
        )

        val IN_MEMORY_REPLIES = mutableMapOf(
            "p2" to mutableListOf(
                PostReply(
                    id = "rep_1",
                    postId = "p2",
                    communityId = "demo_comm_1",
                    authorId = "30456",
                    authorName = "Prof. Gowthami",
                    authorRole = Constants.ROLE_FACULTY,
                    content = "Using an unordered_set with a custom hash function for coordinate pairs O(1) or a 2D boolean lookup matrix provides significant speedup over standard set O(log N).",
                    isOfficial = true,
                    createdAt = Timestamp(System.currentTimeMillis() / 1000 - 36000, 0),
                )
            )
        )

        val IN_MEMORY_RESOURCES = mutableListOf(
            Resource(
                id = "res_1",
                communityId = "demo_comm_1",
                subject = "Data Structures",
                uploaderId = "30456",
                uploaderName = "Prof. Gowthami",
                uploaderRole = Constants.ROLE_FACULTY,
                title = "Shortest Path & Graph Algorithms Master Notes",
                description = "Comprehensive analysis of Dijkstra, Bellman-Ford, Floyd-Warshall, and Johnson's algorithm with complexity proofs.",
                fileUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                fileType = "PDF",
                fileName = "Graph_Algorithms_Complete.pdf",
                fileSizeBytes = 2450000L,
                isPinned = true,
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 86400 * 2, 0),
            ),
            Resource(
                id = "res_2",
                communityId = "demo_comm_1",
                subject = "Data Structures",
                uploaderId = "30456",
                uploaderName = "Prof. Gowthami",
                uploaderRole = Constants.ROLE_FACULTY,
                title = "Advanced Trees: AVL, Red-Black & B+ Tree Cheatsheet",
                description = "Rotation cases, insertion rules, and disk storage indexing architectures.",
                fileUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                fileType = "PDF",
                fileName = "Advanced_Trees_Lecture_Notes.pdf",
                fileSizeBytes = 1840000L,
                isPinned = false,
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 86400 * 4, 0),
            ),
            Resource(
                id = "res_3",
                communityId = "demo_comm_2",
                subject = "Operating Systems",
                uploaderId = "30456",
                uploaderName = "Prof. Gowthami",
                uploaderRole = Constants.ROLE_FACULTY,
                title = "Operating Systems Kernel Synchronization & IPC",
                description = "Detailed guide on semaphores, mutexes, monitor abstraction, and dining philosophers problem.",
                fileUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                fileType = "PDF",
                fileName = "OS_Kernel_Sync_Guide.pdf",
                fileSizeBytes = 3100000L,
                isPinned = true,
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 86400 * 3, 0),
            ),
            Resource(
                id = "res_4",
                communityId = "demo_comm_3",
                subject = "Computer Networks",
                uploaderId = "30457",
                uploaderName = "Dr. Rajesh Kumar",
                uploaderRole = Constants.ROLE_FACULTY,
                title = "TCP/IP Protocol Suite & Flow Control Mechanisms",
                description = "Packet traces, sliding window protocol, congestion window evolution, and slow start.",
                fileUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                fileType = "PDF",
                fileName = "TCP_Flow_Control_Architecture.pdf",
                fileSizeBytes = 2200000L,
                isPinned = false,
                createdAt = Timestamp(System.currentTimeMillis() / 1000 - 86400 * 5, 0),
            ),
        )
    }

    override fun getCommunities(facultyId: String): Flow<List<Community>> = callbackFlow {
        val reg = commCol.whereEqualTo("facultyId", facultyId)
            .whereEqualTo("isActive", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                val list = snap?.documents?.mapNotNull { it.toCommunity() } ?: emptyList()
                if (list.isEmpty()) {
                    trySend(IN_MEMORY_COMMUNITIES.filter { it.facultyId == facultyId && it.isActive })
                } else {
                    trySend(list)
                }
            }
        awaitClose { reg.remove() }
    }

    override fun getAllCommunities(): Flow<List<Community>> = callbackFlow {
        val reg = commCol.whereEqualTo("isActive", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                val list = snap?.documents?.mapNotNull { it.toCommunity() } ?: emptyList()
                if (list.isEmpty()) {
                    trySend(IN_MEMORY_COMMUNITIES.filter { it.isActive })
                } else {
                    trySend(list)
                }
            }
        awaitClose { reg.remove() }
    }

    override fun getJoinedCommunities(userId: String): Flow<List<Community>> = callbackFlow {
        val reg = commCol.whereArrayContains("members", userId)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snap, err ->
                val list = snap?.documents?.mapNotNull { it.toCommunity() } ?: emptyList()
                if (list.isEmpty()) {
                    trySend(IN_MEMORY_COMMUNITIES.filter { it.members.contains(userId) && it.isActive })
                } else {
                    trySend(list)
                }
            }
        awaitClose { reg.remove() }
    }

    override suspend fun getCommunity(communityId: String): Result<Community> = runCatching {
        val local = IN_MEMORY_COMMUNITIES.find { it.id == communityId }
        if (local != null) return@runCatching local

        val snap = commCol.document(communityId).get().await()
        snap.toCommunity() ?: throw Exception("Community not found")
    }

    override suspend fun createCommunity(community: Community): Result<String> = runCatching {
        val commId = "comm_${System.currentTimeMillis()}"
        val newComm = community.copy(
            id = commId,
            memberCount = 1,
            postCount = 0,
            members = listOf(community.facultyId),
            isActive = true,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
        )
        IN_MEMORY_COMMUNITIES.add(0, newComm)

        try {
            val data = mapOf(
                "name"          to newComm.name,
                "description"   to newComm.description,
                "subject"       to newComm.subject,
                "facultyId"     to newComm.facultyId,
                "facultyName"   to newComm.facultyName,
                "coverImageUrl" to newComm.coverImageUrl,
                "memberCount"   to 1,
                "postCount"     to 0,
                "members"       to newComm.members,
                "isActive"      to true,
                "isPublic"      to newComm.isPublic,
                "createdAt"     to newComm.createdAt,
                "updatedAt"     to newComm.updatedAt,
            )
            val docRef = commCol.add(data).await()
            docRef.id
        } catch (_: Exception) {
            commId
        }
    }

    override suspend fun updateCommunity(community: Community): Result<Unit> = runCatching {
        val index = IN_MEMORY_COMMUNITIES.indexOfFirst { it.id == community.id }
        if (index >= 0) {
            IN_MEMORY_COMMUNITIES[index] = community.copy(updatedAt = Timestamp.now())
        }
        if (!community.id.startsWith("demo_") && !community.id.startsWith("comm_")) {
            commCol.document(community.id).set(
                mapOf(
                    "name"          to community.name,
                    "description"   to community.description,
                    "subject"       to community.subject,
                    "coverImageUrl" to community.coverImageUrl,
                    "isPublic"      to community.isPublic,
                    "updatedAt"     to Timestamp.now(),
                ),
                SetOptions.merge()
            ).await()
        }
    }

    override suspend fun deleteCommunity(communityId: String): Result<Unit> = runCatching {
        val index = IN_MEMORY_COMMUNITIES.indexOfFirst { it.id == communityId }
        if (index >= 0) {
            IN_MEMORY_COMMUNITIES[index] = IN_MEMORY_COMMUNITIES[index].copy(isActive = false)
        }
        if (!communityId.startsWith("demo_") && !communityId.startsWith("comm_")) {
            commCol.document(communityId).set(
                mapOf("isActive" to false, "updatedAt" to Timestamp.now()),
                SetOptions.merge()
            ).await()
        }
    }

    override suspend fun joinCommunity(communityId: String, userId: String): Result<Unit> = runCatching {
        val index = IN_MEMORY_COMMUNITIES.indexOfFirst { it.id == communityId }
        if (index >= 0) {
            val comm = IN_MEMORY_COMMUNITIES[index]
            if (!comm.members.contains(userId)) {
                val updatedMembers = comm.members + userId
                IN_MEMORY_COMMUNITIES[index] = comm.copy(
                    members = updatedMembers,
                    memberCount = comm.memberCount + 1,
                    updatedAt = Timestamp.now(),
                )
            }
        }
        if (!communityId.startsWith("demo_") && !communityId.startsWith("comm_")) {
            commCol.document(communityId).update(
                "members", FieldValue.arrayUnion(userId),
                "memberCount", FieldValue.increment(1),
                "updatedAt", Timestamp.now(),
            ).await()
        }
    }

    override suspend fun leaveCommunity(communityId: String, userId: String): Result<Unit> = runCatching {
        val index = IN_MEMORY_COMMUNITIES.indexOfFirst { it.id == communityId }
        if (index >= 0) {
            val comm = IN_MEMORY_COMMUNITIES[index]
            if (comm.members.contains(userId)) {
                val updatedMembers = comm.members - userId
                IN_MEMORY_COMMUNITIES[index] = comm.copy(
                    members = updatedMembers,
                    memberCount = maxOf(0, comm.memberCount - 1),
                    updatedAt = Timestamp.now(),
                )
            }
        }
        if (!communityId.startsWith("demo_") && !communityId.startsWith("comm_")) {
            commCol.document(communityId).update(
                "members", FieldValue.arrayRemove(userId),
                "memberCount", FieldValue.increment(-1),
                "updatedAt", Timestamp.now(),
            ).await()
        }
    }

    override fun getPosts(communityId: String): Flow<List<Post>> = callbackFlow {
        if (IN_MEMORY_POSTS.containsKey(communityId)) {
            trySend(IN_MEMORY_POSTS[communityId] ?: emptyList())
        }
        val reg = commCol.document(communityId)
            .collection(Constants.COLLECTION_POSTS)
            .orderBy("isPinned", Query.Direction.DESCENDING)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                val list = snap?.documents?.mapNotNull { it.toPost() } ?: emptyList()
                if (list.isEmpty() && IN_MEMORY_POSTS.containsKey(communityId)) {
                    trySend(IN_MEMORY_POSTS[communityId] ?: emptyList())
                } else {
                    trySend(list)
                }
            }
        awaitClose { reg.remove() }
    }

    override suspend fun createPost(post: Post): Result<String> = runCatching {
        val postId = "post_${System.currentTimeMillis()}"
        val newPost = post.copy(
            id = postId,
            helpfulCount = 0,
            replyCount = 0,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
        )
        val list = IN_MEMORY_POSTS.getOrPut(post.communityId) { mutableListOf() }
        list.add(0, newPost)

        // Increment community postCount
        val cIndex = IN_MEMORY_COMMUNITIES.indexOfFirst { it.id == post.communityId }
        if (cIndex >= 0) {
            val c = IN_MEMORY_COMMUNITIES[cIndex]
            IN_MEMORY_COMMUNITIES[cIndex] = c.copy(postCount = c.postCount + 1)
        }

        try {
            val data = mapOf(
                "communityId"      to newPost.communityId,
                "authorId"         to newPost.authorId,
                "authorName"       to newPost.authorName,
                "authorRole"       to newPost.authorRole,
                "authorPhotoUrl"   to newPost.authorPhotoUrl,
                "content"          to newPost.content,
                "codeSnippet"      to newPost.codeSnippet,
                "attachmentUrl"    to newPost.attachmentUrl,
                "attachmentType"   to newPost.attachmentType,
                "attachmentName"   to newPost.attachmentName,
                "isAnnouncement"   to newPost.isAnnouncement,
                "isPinned"         to newPost.isPinned,
                "isOfficialAnswer" to newPost.isOfficialAnswer,
                "helpfulCount"     to 0,
                "replyCount"       to 0,
                "createdAt"        to newPost.createdAt,
                "updatedAt"        to newPost.updatedAt,
            )
            val ref = commCol.document(post.communityId)
                .collection(Constants.COLLECTION_POSTS)
                .add(data).await()
            commCol.document(post.communityId).set(
                mapOf("postCount" to FieldValue.increment(1), "updatedAt" to Timestamp.now()),
                SetOptions.merge()
            ).await()
            ref.id
        } catch (_: Exception) {
            postId
        }
    }

    override suspend fun deletePost(communityId: String, postId: String): Result<Unit> = runCatching {
        IN_MEMORY_POSTS[communityId]?.removeAll { it.id == postId }
        if (!communityId.startsWith("demo_") && !communityId.startsWith("comm_")) {
            commCol.document(communityId)
                .collection(Constants.COLLECTION_POSTS)
                .document(postId).delete().await()
        }
    }

    override suspend fun pinPost(communityId: String, postId: String, isPinned: Boolean): Result<Unit> = runCatching {
        val list = IN_MEMORY_POSTS[communityId]
        val index = list?.indexOfFirst { it.id == postId } ?: -1
        if (index >= 0) {
            list!![index] = list[index].copy(isPinned = isPinned)
        }
        if (!communityId.startsWith("demo_") && !communityId.startsWith("comm_")) {
            commCol.document(communityId)
                .collection(Constants.COLLECTION_POSTS)
                .document(postId)
                .set(mapOf("isPinned" to isPinned, "updatedAt" to Timestamp.now()), SetOptions.merge()).await()
        }
    }

    override suspend fun markPostOfficial(communityId: String, postId: String, isOfficial: Boolean): Result<Unit> = runCatching {
        val list = IN_MEMORY_POSTS[communityId]
        val index = list?.indexOfFirst { it.id == postId } ?: -1
        if (index >= 0) {
            list!![index] = list[index].copy(isOfficialAnswer = isOfficial)
        }
        if (!communityId.startsWith("demo_") && !communityId.startsWith("comm_")) {
            commCol.document(communityId)
                .collection(Constants.COLLECTION_POSTS)
                .document(postId)
                .set(mapOf("isOfficialAnswer" to isOfficial, "updatedAt" to Timestamp.now()), SetOptions.merge()).await()
        }
    }

    override suspend fun toggleHelpful(communityId: String, postId: String, userId: String): Result<Unit> = runCatching {
        val list = IN_MEMORY_POSTS[communityId]
        val index = list?.indexOfFirst { it.id == postId } ?: -1
        if (index >= 0) {
            val p = list!![index]
            list[index] = p.copy(helpfulCount = p.helpfulCount + 1)
        }
        if (!communityId.startsWith("demo_") && !communityId.startsWith("comm_")) {
            commCol.document(communityId)
                .collection(Constants.COLLECTION_POSTS)
                .document(postId)
                .set(mapOf("helpfulCount" to FieldValue.increment(1)), SetOptions.merge())
                .await()
        }
    }

    override fun getReplies(communityId: String, postId: String): Flow<List<PostReply>> = callbackFlow {
        if (IN_MEMORY_REPLIES.containsKey(postId)) {
            trySend(IN_MEMORY_REPLIES[postId] ?: emptyList())
        }
        val reg = commCol.document(communityId)
            .collection(Constants.COLLECTION_POSTS)
            .document(postId)
            .collection(Constants.COLLECTION_POST_REPLIES)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                val list = snap?.documents?.mapNotNull { it.toReply() } ?: emptyList()
                if (list.isEmpty() && IN_MEMORY_REPLIES.containsKey(postId)) {
                    trySend(IN_MEMORY_REPLIES[postId] ?: emptyList())
                } else {
                    trySend(list)
                }
            }
        awaitClose { reg.remove() }
    }

    override suspend fun addReply(reply: PostReply): Result<String> = runCatching {
        val repId = "rep_${System.currentTimeMillis()}"
        val newReply = reply.copy(id = repId, createdAt = Timestamp.now())
        val list = IN_MEMORY_REPLIES.getOrPut(reply.postId) { mutableListOf() }
        list.add(newReply)

        // Increment replyCount on the parent post
        val pList = IN_MEMORY_POSTS[reply.communityId]
        val pIndex = pList?.indexOfFirst { it.id == reply.postId } ?: -1
        if (pIndex >= 0) {
            val p = pList!![pIndex]
            pList[pIndex] = p.copy(replyCount = p.replyCount + 1)
        }

        try {
            val data = mapOf(
                "postId"         to newReply.postId,
                "communityId"    to newReply.communityId,
                "authorId"       to newReply.authorId,
                "authorName"     to newReply.authorName,
                "authorRole"     to newReply.authorRole,
                "authorPhotoUrl" to newReply.authorPhotoUrl,
                "content"        to newReply.content,
                "isOfficial"     to newReply.isOfficial,
                "createdAt"      to newReply.createdAt,
            )
            commCol.document(reply.communityId)
                .collection(Constants.COLLECTION_POSTS)
                .document(reply.postId)
                .collection(Constants.COLLECTION_POST_REPLIES)
                .add(data).await()

            commCol.document(reply.communityId)
                .collection(Constants.COLLECTION_POSTS)
                .document(reply.postId)
                .set(mapOf("replyCount" to FieldValue.increment(1)), SetOptions.merge())
                .await()
            repId
        } catch (_: Exception) {
            repId
        }
    }

    override fun getResources(communityId: String): Flow<List<Resource>> = callbackFlow {
        val inMemory = IN_MEMORY_RESOURCES.filter { it.communityId == communityId }
        trySend(inMemory)

        val reg = commCol.document(communityId)
            .collection(Constants.COLLECTION_RESOURCES)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                val list = snap?.documents?.mapNotNull { it.toResource() } ?: emptyList()
                if (list.isEmpty()) {
                    trySend(inMemory)
                } else {
                    trySend(list)
                }
            }
        awaitClose { reg.remove() }
    }

    override fun getAllResources(subject: String?): Flow<List<Resource>> = callbackFlow {
        val filtered = if (!subject.isNullOrBlank()) {
            IN_MEMORY_RESOURCES.filter { it.subject.equals(subject, ignoreCase = true) }
        } else {
            IN_MEMORY_RESOURCES
        }
        trySend(filtered)

        val reg = firestore.collectionGroup(Constants.COLLECTION_RESOURCES)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                val list = snap?.documents?.mapNotNull { it.toResource() } ?: emptyList()
                val result = if (list.isNotEmpty()) {
                    if (!subject.isNullOrBlank()) list.filter { it.subject.equals(subject, ignoreCase = true) } else list
                } else {
                    filtered
                }
                trySend(result)
            }
        awaitClose { reg.remove() }
    }

    override suspend fun addResource(resource: Resource): Result<String> = runCatching {
        val resId = "res_${System.currentTimeMillis()}"
        val newRes = resource.copy(id = resId, createdAt = Timestamp.now(), updatedAt = Timestamp.now())
        IN_MEMORY_RESOURCES.add(0, newRes)

        try {
            val data = mapOf(
                "communityId"   to newRes.communityId,
                "subject"       to newRes.subject,
                "uploaderId"    to newRes.uploaderId,
                "uploaderName"  to newRes.uploaderName,
                "uploaderRole"  to newRes.uploaderRole,
                "title"         to newRes.title,
                "description"   to newRes.description,
                "fileUrl"       to newRes.fileUrl,
                "fileType"      to newRes.fileType,
                "fileName"      to newRes.fileName,
                "fileSizeBytes" to newRes.fileSizeBytes,
                "isLink"        to newRes.isLink,
                "linkUrl"       to newRes.linkUrl,
                "isPinned"      to newRes.isPinned,
                "createdAt"     to newRes.createdAt,
                "updatedAt"     to newRes.updatedAt,
            )
            val docRef = commCol.document(resource.communityId)
                .collection(Constants.COLLECTION_RESOURCES)
                .add(data).await()
            docRef.id
        } catch (_: Exception) {
            resId
        }
    }

    override suspend fun deleteResource(communityId: String, resourceId: String): Result<Unit> = runCatching {
        IN_MEMORY_RESOURCES.removeAll { it.id == resourceId }
        if (!communityId.startsWith("demo_") && !communityId.startsWith("comm_")) {
            commCol.document(communityId)
                .collection(Constants.COLLECTION_RESOURCES)
                .document(resourceId).delete().await()
        }
    }

    // ── Mappers ──────────────────────────────────────────────────────────────
    private fun com.google.firebase.firestore.DocumentSnapshot.toCommunity(): Community? {
        if (!exists()) return null
        @Suppress("UNCHECKED_CAST")
        val membersList = get("members") as? List<String> ?: emptyList()
        return Community(
            id            = id,
            name          = getString("name") ?: "",
            description   = getString("description") ?: "",
            subject       = getString("subject") ?: "",
            facultyId     = getString("facultyId") ?: "",
            facultyName   = getString("facultyName") ?: "",
            coverImageUrl = getString("coverImageUrl") ?: "",
            memberCount   = (getLong("memberCount") ?: 0L).toInt(),
            postCount     = (getLong("postCount") ?: 0L).toInt(),
            members       = membersList,
            isActive      = getBoolean("isActive") ?: true,
            isPublic      = getBoolean("isPublic") ?: true,
            createdAt     = getTimestamp("createdAt"),
            updatedAt     = getTimestamp("updatedAt"),
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toPost(): Post? {
        if (!exists()) return null
        return Post(
            id               = id,
            communityId      = getString("communityId") ?: "",
            authorId         = getString("authorId") ?: "",
            authorName       = getString("authorName") ?: "",
            authorRole       = getString("authorRole") ?: "",
            authorPhotoUrl   = getString("authorPhotoUrl") ?: "",
            content          = getString("content") ?: "",
            codeSnippet      = getString("codeSnippet") ?: "",
            attachmentUrl    = getString("attachmentUrl") ?: "",
            attachmentType   = getString("attachmentType") ?: "",
            attachmentName   = getString("attachmentName") ?: "",
            isAnnouncement   = getBoolean("isAnnouncement") ?: false,
            isPinned         = getBoolean("isPinned") ?: false,
            isOfficialAnswer = getBoolean("isOfficialAnswer") ?: false,
            helpfulCount     = (getLong("helpfulCount") ?: 0L).toInt(),
            replyCount       = (getLong("replyCount") ?: 0L).toInt(),
            createdAt        = getTimestamp("createdAt"),
            updatedAt        = getTimestamp("updatedAt"),
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toReply(): PostReply? {
        if (!exists()) return null
        return PostReply(
            id             = id,
            postId         = getString("postId") ?: "",
            communityId    = getString("communityId") ?: "",
            authorId       = getString("authorId") ?: "",
            authorName     = getString("authorName") ?: "",
            authorRole     = getString("authorRole") ?: "",
            authorPhotoUrl = getString("authorPhotoUrl") ?: "",
            content        = getString("content") ?: "",
            isOfficial     = getBoolean("isOfficial") ?: false,
            createdAt      = getTimestamp("createdAt"),
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toResource(): Resource? {
        if (!exists()) return null
        return Resource(
            id            = id,
            communityId   = getString("communityId") ?: "",
            subject       = getString("subject") ?: "",
            uploaderId    = getString("uploaderId") ?: "",
            uploaderName  = getString("uploaderName") ?: "",
            uploaderRole  = getString("uploaderRole") ?: "FACULTY",
            title         = getString("title") ?: "",
            description   = getString("description") ?: "",
            fileUrl       = getString("fileUrl") ?: "",
            fileType      = getString("fileType") ?: "",
            fileName      = getString("fileName") ?: "",
            fileSizeBytes = getLong("fileSizeBytes") ?: 0L,
            isLink        = getBoolean("isLink") ?: false,
            linkUrl       = getString("linkUrl") ?: "",
            isPinned      = getBoolean("isPinned") ?: false,
            createdAt     = getTimestamp("createdAt"),
            updatedAt     = getTimestamp("updatedAt"),
        )
    }
}
