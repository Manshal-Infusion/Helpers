package com.ia.quotesapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ia.quotesapp.enums.NetworkState
import com.ia.quotesapp.network.NetworkConnectionManager
import com.ia.quotesapp.network.NoNetworkException

open class NetworkViewModel (
    protected val networkConnectionManager: NetworkConnectionManager
): ViewModel() {

    private val _networkState = MutableLiveData<NetworkState>()

    val networkState : LiveData<NetworkState> get() =  _networkState

    fun setNetworkState(value: NetworkState){
        _networkState.postValue(value)
    }

    fun checkNetworkAvailability(){
        _networkState.value = if(networkConnectionManager.isNetworkAvailable()){
            NetworkState.ONLINE
        }else{
            NetworkState.OFFLINE
        }
    }

    fun executeByHandlingNoNetwork(
        networkCallBlock : () -> Unit
    ){
        try {
            networkCallBlock()
        }catch (e: NoNetworkException){
            setNetworkState(NetworkState.OFFLINE)
        }
    }
}