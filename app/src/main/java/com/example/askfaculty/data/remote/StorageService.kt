package com.example.askfaculty.data.remote

import android.net.Uri
import com.example.askfaculty.core.util.Constants
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageService @Inject constructor(
    private val storage: FirebaseStorage,
) {
    data class UploadProgress(val progress: Float, val downloadUrl: String? = null, val error: String? = null)

    fun uploadFile(uri: Uri, path: String, fileName: String): Flow<UploadProgress> = callbackFlow {
        val ref = storage.reference.child("$path/$fileName")
        val task = ref.putFile(uri)
        task.addOnProgressListener { snapshot ->
            val progress = snapshot.bytesTransferred.toFloat() / snapshot.totalByteCount.toFloat()
            trySend(UploadProgress(progress))
        }
        task.addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { url ->
                trySend(UploadProgress(1f, url.toString()))
                close()
            }
        }
        task.addOnFailureListener { e ->
            trySend(UploadProgress(0f, error = e.message ?: "Upload failed"))
            close(e)
        }
        awaitClose { task.cancel() }
    }

    suspend fun uploadBytes(bytes: ByteArray, path: String, fileName: String): Result<String> = runCatching {
        val ref = storage.reference.child("$path/$fileName")
        ref.putBytes(bytes).await()
        ref.downloadUrl.await().toString()
    }

    suspend fun deleteFile(url: String): Result<Unit> = runCatching {
        storage.getReferenceFromUrl(url).delete().await()
    }
}
