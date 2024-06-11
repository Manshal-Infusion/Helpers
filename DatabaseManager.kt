package com.ia.quotesapp

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.ktx.initialize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG = "dts"

data class FirebaseAppCredential(val projectId: String, val applicationId: String, val apiKey: String)

class DatabaseManager(
    context: Context,
    srcCredential: FirebaseAppCredential,
    destCredential: FirebaseAppCredential
){

    private var srcDb : FirebaseFirestore
    private var destDb : FirebaseFirestore

    init {
        val srcOptions = FirebaseOptions.Builder()
            .setProjectId(srcCredential.projectId)
            .setApplicationId(srcCredential.applicationId)
            .setApiKey(srcCredential.apiKey)
            .build()

        Firebase.initialize(context,srcOptions,"src")
        val srcApp = Firebase.app("src")
        srcDb = Firebase.firestore(srcApp)

        val destOptions = FirebaseOptions.Builder()
            .setProjectId(destCredential.projectId)
            .setApplicationId(destCredential.applicationId)
            .setApiKey(destCredential.apiKey)
            .build()

        Firebase.initialize(context,destOptions,"dest")
        val destApp = Firebase.app("dest")
        destDb = Firebase.firestore(destApp)

    }

    suspend fun transferData(collections: List<String>){
        withContext(Dispatchers.IO){
            collections.forEach { collection ->
                val snapshot = srcDb.collection(collection).get().await()
                snapshot.forEach {
                    destDb.collection(collection).document(it.id).set(it.data)
                }
                Log.i(TAG, snapshot.size().toString())
            }
        }
    }
}
