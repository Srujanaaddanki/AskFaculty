package com.example.askfaculty.data.repository

import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.domain.model.Community
import com.example.askfaculty.domain.model.Post
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
        val DEMO_COMMUNITIES = listOf(
            Community(
                id = "demo_comm_1",
                name = "CS 301: Advanced Data Structures & Algorithms",
                description = "Course syllabus discussions, algorithmic complexity problem sets, and lab reference implementations.",
                subject = "Data Structures",
                facultyId = "30456",
                facultyName = "Prof. Gowthami",
                memberCount = 68,
                postCount = 14,
            ),
            Community(
                id = "demo_comm_2",
                name = "Faculty Mentorship & Capstone Projects 2026",
                description = "Weekly checkpoint submissions, literature survey templates, and thesis evaluation criteria.",
                subject = "Computer Science",
                facultyId = "30456",
                facultyName = "Prof. Gowthami",
                memberCount = 34,
                postCount = 8,
            )
        )
    }

    override fun getCommunities(facultyId: String): Flow<List<Community>> = callbackFlow {
        val reg = commCol.whereEqualTo("facultyId", facultyId)
            .whereEqualTo("isActive", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                val list = snap?.documents?.mapNotNull { it.toCommunity() } ?: emptyList()
                if (list.isEmpty() && facultyId == "30456") {
                    trySend(DEMO_COMMUNITIES)
                } else {
                    trySend(list)
                }
            }
        awaitClose { reg.remove() }
    }

    override suspend fun getCommunity(communityId: String): Result<Community> = runCatching {
        if (communityId.startsWith("demo_comm_")) {
            return@runCatching DEMO_COMMUNITIES.find { it.id == communityId } ?: DEMO_COMMUNITIES.first()
        }
        val snap = commCol.document(communityId).get().await()
        snap.toCommunity() ?: throw Exception("Community not found")
    }

    override suspend fun createCommunity(community: Community): Result<String> = runCatching {
        val data = mapOf(
            "name"          to community.name,
            "description"   to community.description,
            "subject"       to community.subject,
            "facultyId"     to community.facultyId,
            "facultyName"   to community.facultyName,
            "coverImageUrl" to community.coverImageUrl,
            "memberCount"   to 0,
            "postCount"     to 0,
            "isActive"      to true,
            "isPublic"      to community.isPublic,
            "createdAt"     to Timestamp.now(),
            "updatedAt"     to Timestamp.now(),
        )
        commCol.add(data).await().id
    }

    override suspend fun updateCommunity(community: Community): Result<Unit> = runCatching {
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

    override suspend fun deleteCommunity(communityId: String): Result<Unit> = runCatching {
        commCol.document(communityId).set(
            mapOf("isActive" to false, "updatedAt" to Timestamp.now()),
            SetOptions.merge()
        ).await()
    }

    override fun getPosts(communityId: String): Flow<List<Post>> = callbackFlow {
        val reg = commCol.document(communityId)
            .collection(Constants.COLLECTION_POSTS)
            .orderBy("isPinned", Query.Direction.DESCENDING)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toPost() } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    override suspend fun createPost(post: Post): Result<String> = runCatching {
        val data = mapOf(
            "communityId"    to post.communityId,
            "authorId"       to post.authorId,
            "authorName"     to post.authorName,
            "authorRole"     to post.authorRole,
            "authorPhotoUrl" to post.authorPhotoUrl,
            "content"        to post.content,
            "attachmentUrl"  to post.attachmentUrl,
            "attachmentType" to post.attachmentType,
            "attachmentName" to post.attachmentName,
            "isAnnouncement" to post.isAnnouncement,
            "isPinned"       to post.isPinned,
            "isOfficialAnswer" to post.isOfficialAnswer,
            "helpfulCount"   to 0,
            "replyCount"     to 0,
            "createdAt"      to Timestamp.now(),
            "updatedAt"      to Timestamp.now(),
        )
        val ref = commCol.document(post.communityId)
            .collection(Constants.COLLECTION_POSTS)
            .add(data).await()
        commCol.document(post.communityId).set(
            mapOf("postCount" to FieldValue.increment(1), "updatedAt" to Timestamp.now()),
            SetOptions.merge()
        ).await()
        ref.id
    }

    override suspend fun deletePost(communityId: String, postId: String): Result<Unit> = runCatching {
        commCol.document(communityId)
            .collection(Constants.COLLECTION_POSTS)
            .document(postId).delete().await()
    }

    override suspend fun pinPost(communityId: String, postId: String, isPinned: Boolean): Result<Unit> = runCatching {
        commCol.document(communityId)
            .collection(Constants.COLLECTION_POSTS)
            .document(postId)
            .set(mapOf("isPinned" to isPinned, "updatedAt" to Timestamp.now()), SetOptions.merge()).await()
    }

    override suspend fun markPostOfficial(communityId: String, postId: String, isOfficial: Boolean): Result<Unit> = runCatching {
        commCol.document(communityId)
            .collection(Constants.COLLECTION_POSTS)
            .document(postId)
            .set(mapOf("isOfficialAnswer" to isOfficial, "updatedAt" to Timestamp.now()), SetOptions.merge()).await()
    }

    override suspend fun toggleHelpful(communityId: String, postId: String, userId: String): Result<Unit> = runCatching {
        commCol.document(communityId)
            .collection(Constants.COLLECTION_POSTS)
            .document(postId)
            .set(
                mapOf("helpfulCount" to FieldValue.increment(1)),
                SetOptions.merge()
            ).await()
    }

    override fun getResources(communityId: String): Flow<List<Resource>> = callbackFlow {
        val reg = commCol.document(communityId)
            .collection(Constants.COLLECTION_RESOURCES)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toResource() } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    override suspend fun addResource(resource: Resource): Result<String> = runCatching {
        val data = mapOf(
            "communityId"   to resource.communityId,
            "uploaderId"    to resource.uploaderId,
            "uploaderName"  to resource.uploaderName,
            "title"         to resource.title,
            "description"   to resource.description,
            "fileUrl"       to resource.fileUrl,
            "fileType"      to resource.fileType,
            "fileName"      to resource.fileName,
            "fileSizeBytes" to resource.fileSizeBytes,
            "isLink"        to resource.isLink,
            "linkUrl"       to resource.linkUrl,
            "createdAt"     to Timestamp.now(),
            "updatedAt"     to Timestamp.now(),
        )
        commCol.document(resource.communityId)
            .collection(Constants.COLLECTION_RESOURCES)
            .add(data).await().id
    }

    override suspend fun deleteResource(communityId: String, resourceId: String): Result<Unit> = runCatching {
        commCol.document(communityId)
            .collection(Constants.COLLECTION_RESOURCES)
            .document(resourceId).delete().await()
    }

    // ── Mappers ──────────────────────────────────────────────────────────────
    private fun com.google.firebase.firestore.DocumentSnapshot.toCommunity(): Community? {
        if (!exists()) return null
        return Community(
            id           = id,
            name         = getString("name") ?: "",
            description  = getString("description") ?: "",
            subject      = getString("subject") ?: "",
            facultyId    = getString("facultyId") ?: "",
            facultyName  = getString("facultyName") ?: "",
            coverImageUrl = getString("coverImageUrl") ?: "",
            memberCount  = (getLong("memberCount") ?: 0L).toInt(),
            postCount    = (getLong("postCount") ?: 0L).toInt(),
            isActive     = getBoolean("isActive") ?: true,
            isPublic     = getBoolean("isPublic") ?: true,
            createdAt    = getTimestamp("createdAt"),
            updatedAt    = getTimestamp("updatedAt"),
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toPost(): Post? {
        if (!exists()) return null
        return Post(
            id              = id,
            communityId     = getString("communityId") ?: "",
            authorId        = getString("authorId") ?: "",
            authorName      = getString("authorName") ?: "",
            authorRole      = getString("authorRole") ?: "",
            authorPhotoUrl  = getString("authorPhotoUrl") ?: "",
            content         = getString("content") ?: "",
            attachmentUrl   = getString("attachmentUrl") ?: "",
            attachmentType  = getString("attachmentType") ?: "",
            attachmentName  = getString("attachmentName") ?: "",
            isAnnouncement  = getBoolean("isAnnouncement") ?: false,
            isPinned        = getBoolean("isPinned") ?: false,
            isOfficialAnswer = getBoolean("isOfficialAnswer") ?: false,
            helpfulCount    = (getLong("helpfulCount") ?: 0L).toInt(),
            replyCount      = (getLong("replyCount") ?: 0L).toInt(),
            createdAt       = getTimestamp("createdAt"),
            updatedAt       = getTimestamp("updatedAt"),
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toResource(): Resource? {
        if (!exists()) return null
        return Resource(
            id            = id,
            communityId   = getString("communityId") ?: "",
            uploaderId    = getString("uploaderId") ?: "",
            uploaderName  = getString("uploaderName") ?: "",
            title         = getString("title") ?: "",
            description   = getString("description") ?: "",
            fileUrl       = getString("fileUrl") ?: "",
            fileType      = getString("fileType") ?: "",
            fileName      = getString("fileName") ?: "",
            fileSizeBytes = getLong("fileSizeBytes") ?: 0L,
            isLink        = getBoolean("isLink") ?: false,
            linkUrl       = getString("linkUrl") ?: "",
            createdAt     = getTimestamp("createdAt"),
            updatedAt     = getTimestamp("updatedAt"),
        )
    }
}
