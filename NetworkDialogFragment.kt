package com.ia.quotesapp.ui.fragments

import android.os.Bundle
import android.view.View
import com.ia.quotesapp.enums.NetworkState
import com.ia.quotesapp.utils.showOfflineDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class NetworkDialogFragment : NetworkFragment(){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservers()
    }

    private fun setUpObservers() {
        networkViewModel.networkState.observe(viewLifecycleOwner) {
            when (it) {
                NetworkState.ONLINE -> {}

                NetworkState.OFFLINE -> {
                    showOfflineDialog()
                }
            }
        }
    }
}
