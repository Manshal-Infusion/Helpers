package com.ia.quotesapp.utils

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import com.ia.quotesapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun Int.getDrawable(context: Context) = AppCompatResources.getDrawable(context, this)

fun NavController.navigateSafely(
    @IdRes currentDestinationId: Int,
    @IdRes id: Int,
    args: Bundle? = null
) {
    Log.e("Navigation","trying to navigate")
    if (currentDestination?.id != currentDestinationId) return
    navigate(id, args)
}

fun FragmentActivity.hideKeyboard(){
    currentFocus?.let { view ->
        val imm = ContextCompat.getSystemService(this, InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun Fragment.showLogoutConfirmationDialog(
    onLogout: () -> Unit
) {
    AlertDialog.Builder(requireContext()).apply {
        setTitle(getString(R.string.title_alert))
        setMessage(getString(R.string.msg_logout_confirmation))
        setPositiveButton(getString(R.string.action_logout)) { _, _ ->
            onLogout()
        }
        setNegativeButton(getString(R.string.action_cancel)) { di, _ ->
            di.dismiss()
        }
        show()
    }
}

fun Context.showOfflineDialog() {
    showAlertDialog(
        getString(R.string.title_offline),
        getString(R.string.dialog_msg_please_check_and_try_again)
    )
}

fun Fragment.showOfflineDialog() {
    requireContext().showOfflineDialog()
}

fun View.showOfflineSnackBar(){
    Snackbar.make(
        this,
        context.getString(R.string.msg_no_internet_connection), Snackbar.LENGTH_SHORT
    ).show()
}

fun Fragment.showAlertDialog(
    title: String = getString(R.string.title_alert),
    msg: String,
    btnAction: String = getString(R.string.text_okay),
    isCancellable: Boolean = true,
    onBtnClick: ((DialogInterface) -> Unit)? = null
) {
    requireContext().showAlertDialog(title, msg, btnAction, isCancellable, onBtnClick)
}

fun Context.showAlertDialog(
    title: String,
    msg: String,
    btnAction: String = getString(R.string.text_okay),
    isCancellable: Boolean = true,
    onBtnClick: ((DialogInterface) -> Unit)? = null
): AlertDialog.Builder {
    return AlertDialog.Builder(this).apply {
        setTitle(title)
        setMessage(msg)
        setCancelable(isCancellable)
        setPositiveButton(
            btnAction
        ) { dialogInterface, _ ->
            onBtnClick?.invoke(dialogInterface)
        }
        show()
    }
}

fun Fragment.showConfirmationDialog(
    msg: String,
    title: String = getString(R.string.title_alert),
    positiveButtonText: String = getString(R.string.text_okay),
    negativeButtonText: String = getString(R.string.action_cancel),
    onPositiveBtnClick: () -> Unit,
    onNegativeBtnClick: ((DialogInterface) -> Unit)? = null
) {
    requireContext().showConfirmationDialog(
        msg,
        title,
        positiveButtonText,
        negativeButtonText,
        onPositiveBtnClick,
        onNegativeBtnClick
    )
}

fun Context.showConfirmationDialog(
    msg: String,
    title: String = getString(R.string.title_alert),
    positiveButtonText: String = getString(R.string.text_okay),
    negativeButtonText: String = getString(R.string.action_cancel),
    onSubmit: () -> Unit,
    onCancel: ((DialogInterface) -> Unit)? = null
) {
    AlertDialog.Builder(this).apply {
        setTitle(title)
        setMessage(msg)
        setPositiveButton(positiveButtonText) { _, _ ->
            onSubmit()
        }
        setNegativeButton(negativeButtonText) { di, _ ->
            onCancel?.invoke(di)
            di.dismiss()
        }
        show()
    }
}

fun Fragment.rootNavController(): NavController {
    val navHostFragment =
        requireActivity().supportFragmentManager
            .findFragmentById(R.id.fragmentContainer) as NavHostFragment

    return navHostFragment.navController
}

fun Fragment.setBackPressHandlerWithSafetyDialog(
    showConfirmationDialogIf: () -> Boolean
) {
    requireActivity().onBackPressedDispatcher.addCallback {
        fun navigateBack(){
            isEnabled = false
            requireActivity().onBackPressed()
        }
        if (showConfirmationDialogIf()) {
            showConfirmationDialog(
                msg = getString(R.string.warning_discard_inputs),
                positiveButtonText = getString(R.string.action_discard),
                negativeButtonText = getString(R.string.action_keep),
                onPositiveBtnClick = {
                    navigateBack()
                }
            )
        }else{
            navigateBack()
        }
    }
}

fun View.makeDraggable(
    positionListener : ((x: Float,y: Float) -> Unit)? = null
){
    var touchJob : Job? = null
    var touchDuration = 0
    setOnTouchListener { view, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Save the initial touch position
                view.tag = Pair(view.x - event.rawX, view.y - event.rawY)
                touchJob = CoroutineScope(Dispatchers.Main).launch {
                    while (true){
                        touchDuration += 20
                        delay(20)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                touchJob?.cancel()
                // Update the view position based on the touch movement
                val (dx, dy) = view.tag as Pair<Float, Float>
                val (finalX,finalY) = Pair(event.rawX + dx, event.rawY + dy)
                view.animate()
                    .x(finalX)
                    .y(finalY)
                    .setDuration(0)
                    .withEndAction {
                        positionListener?.invoke(view.x, view.y)
                    }.start()
            }
            MotionEvent.ACTION_UP -> {
                if (touchDuration < 200){
                    performClick()
                    touchJob?.cancel()
                    touchJob = null
                    touchDuration = 0
                }
            }
        }
        true
    }
}


fun View.makeScalable(){
    val scaleListener = ScaleListener(this, 6.0f)
    val scaleGestureDetector = ScaleGestureDetector(context, scaleListener)
    setOnTouchListener{ _, event ->
        scaleGestureDetector.onTouchEvent(event)
    }
}

class ScaleListener(val view: View, private var scaleFactor: Float) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        scaleFactor *= detector.scaleFactor
        scaleFactor = if (scaleFactor < 1.0f) 1.0f else scaleFactor
        scaleFactor = if (scaleFactor > 5.0f) 5.0f else scaleFactor

        view.scaleX = scaleFactor
        view.scaleY = scaleFactor
        return true
    }
}


