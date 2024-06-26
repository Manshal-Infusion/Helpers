package com.example.myquotescompose.utils

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import com.example.myquotescompose.R

class AppPromoter(
    private val activity: ComponentActivity
) {
    fun showAppsToShare() {
        activity.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT,APP_SHARING_MSG)
                },
                activity.getString(R.string.intent_title_share_app)
            )
        )
    }

    fun openPlayStoreListing(){
        activity.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(PLAY_STORE_LINK)
            }
        )
    }

    companion object {
        const val PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=com.ia.wishapp"
        const val APP_SHARING_MSG =
            "*Why type when you can swipe?*\n*With Wishly, your wish is our command.*\n\nInstantly share pre-written messages for every celebration.\n\nIt’s like having a pocket-sized genie, minus the lamp-rubbing!\n\nDownload now: $PLAY_STORE_LINK"
    }
}

