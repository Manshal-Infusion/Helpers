package com.ia.quotesapp.data

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ia.quotesapp.model.APIResponse
import com.ia.quotesapp.network.NetworkConnectionManager
import com.ia.quotesapp.network.NoNetworkException

interface DataRepository {

    val currentUser: FirebaseUser
        get() = Firebase.auth.currentUser!!

    val networkConnectionManager: NetworkConnectionManager

    fun fetchAll(response: (APIResponse<Any>) -> Unit)

    fun add(data: Any, response: (APIResponse<Any>) -> Unit)

    fun proceedIfNetworkAvailable() {
        if (networkConnectionManager.isNetworkAvailable().not()){
            throw NoNetworkException()
        }
    }
}