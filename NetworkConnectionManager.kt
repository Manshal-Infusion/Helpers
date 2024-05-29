package com.ia.quotesapp.network

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log

private const val TAG = "NetworkConnectionManager"

class NetworkConnectionManager(
   context: Context
) {

    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(ConnectivityManager::class.java)
    }

    fun isNetworkAvailable(): Boolean {
        val networkInfo = connectivityManager.activeNetworkInfo
        if(networkInfo == null) {
            Log.e(TAG, "No network available")
            return false
        }
        return networkInfo.isConnectedOrConnecting
    }
}