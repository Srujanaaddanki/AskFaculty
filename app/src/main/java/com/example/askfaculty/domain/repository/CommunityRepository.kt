package com.example.askfaculty.domain.repository

import com.example.askfaculty.domain.model.Community
import com.example.askfaculty.domain.model.Post
import com.example.askfaculty.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    fun getCommunities(facultyId: String): Flow<List<Community>>

    suspend fun getCommunity(communityId: String): Result<Community>

    suspend fun createCommunity(community: Community): Result<String>

    suspend fun updateCommunity(community: Community): Result<Unit>

    suspend fun deleteCommunity(communityId: String): Result<Unit>

    fun getPosts(communityId: String): Flow<List<Post>>

    suspend fun createPost(post: Post): Result<String>

    suspend fun deletePost(communityId: String, postId: String): Result<Unit>

    suspend fun pinPost(communityId: String, postId: String, isPinned: Boolean): Result<Unit>

    suspend fun markPostOfficial(communityId: String, postId: String, isOfficial: Boolean): Result<Unit>

    suspend fun toggleHelpful(communityId: String, postId: String, userId: String): Result<Unit>

    fun getResources(communityId: String): Flow<List<Resource>>

    suspend fun addResource(resource: Resource): Result<String>

    suspend fun deleteResource(communityId: String, resourceId: String): Result<Unit>
}
