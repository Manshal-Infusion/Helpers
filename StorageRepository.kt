package com.ia.quotesapp.data

import android.net.Uri
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.ktx.storage
import com.ia.quotesapp.model.APIResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private val TAG = "StorageRepository"

class StorageRepository(private val folderReference: String) {
    suspend fun uploadFile(uri: Uri, fileName: String): APIResponse<String> {
        return withContext(Dispatchers.IO) {
            val task = Firebase.storage.getReference("$folderReference/$fileName").putFile(uri)
                .await()

            if (task.task.isSuccessful) {
                val url = task.storage.downloadUrl.await()
                APIResponse.APISuccess(url.toString())
            } else {
                APIResponse.APIFailure(task.error.toString())
            }
        }
    }

    suspend fun uploadImageFile(byteArray: ByteArray, fileName: String): APIResponse<String> {
        return withContext(Dispatchers.IO) {
            val task = Firebase.storage.getReference("$folderReference/$fileName")
                .putBytes(
                    byteArray,
                    StorageMetadata.Builder().setContentType("image/*").build()
                )
                .await()

            if (task.task.isSuccessful) {
                val url = task.storage.downloadUrl.await()
                APIResponse.APISuccess(url.toString())
            } else {
                APIResponse.APIFailure(task.error.toString())
            }
        }
    }

    suspend fun deleteFile(name: String) {
        try {
            Firebase.storage.getReference("$folderReference/$name")
                .delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete due to $e")
        }
    }
}