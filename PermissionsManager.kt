package com.ia.quotesapp.utils

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.ia.quotesapp.R

class PermissionsManager(
    val activity: FragmentActivity,
    private val permissionLauncher: ActivityResultLauncher<String>? = null
) {
    fun hasPermissions(vararg permissions: String): List<String> {
        val deniedPermissions = mutableListOf<String>()
        permissions.forEach { permission ->
            if (hasPermission(permission).not()) {
                deniedPermissions.add(permission)
            }
        }
        return deniedPermissions.toList()
    }

    fun requestPermissions(permissions: List<String>) {
        if (permissions.isEmpty()) return
        ActivityCompat.requestPermissions(activity, permissions.toTypedArray(), 0)
    }

    fun requestPermission(permission: String) {
        if(permissionLauncher != null ){
            permissionLauncher.launch(permission)
        }else{
            ActivityCompat.requestPermissions(activity, arrayListOf(permission).toTypedArray(), 0)
        }
    }

    fun shouldShowPermissionRationale(permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            permission
        )
    }

    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun showPermissionRequiredDialog(
        msg : String,
        onCancelled : () -> Unit
    ) {
        activity.showConfirmationDialog(
            msg = msg,
            title = activity.getString(R.string.title_permission_required),
            positiveButtonText = activity.getString(R.string.action_grant),
            onSubmit = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
                activity.startActivity(intent)
            },
            onCancel = {
                onCancelled()
            }
        )
    }

}