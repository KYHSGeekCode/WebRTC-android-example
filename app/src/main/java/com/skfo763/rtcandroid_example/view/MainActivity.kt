package com.skfo763.rtcandroid_example.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.skfo763.rtc.contracts.IFaceChatViewModelListener
import com.skfo763.rtc.contracts.IVoiceChatViewModelListener
import com.skfo763.rtc.contracts.VoiceChatUiEvent
import com.skfo763.rtc.data.UserJoinInfo
import com.skfo763.rtcandroid_example.R
import com.skfo763.rtcandroid_example.viewmodel.MainViewModel
import com.skfo763.rtcandroid_example.viewmodel.ViewModelFactories
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), IFaceChatViewModelListener {

    companion object {
        const val REQUEST_CODE_PERMISSION = 1001
    }

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactories(this, this)
        ).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermission(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(!isPermissionGranted(Manifest.permission.CAMERA)
            || !isPermissionGranted(Manifest.permission.RECORD_AUDIO)
            || requestCode != REQUEST_CODE_PERMISSION
        ) {
            AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("You cannot use service due to denying of permission.")
                .setCancelable(false)
                .setPositiveButton("Back") { dialog, _ -> dialog.dismiss(); finish() }
                .create()
                .show()
        } else {
            viewModel.setRtcWaiting()
        }
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(vararg permissions: String) {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSION)
    }

    override fun updateWaitInfo(text: String) {


    }

    override fun onUiEvent(uiEvent: VoiceChatUiEvent) {


    }

    override fun onError(e: Any) {
        runOnUiThread {
            if(this.isFinishing) return@runOnUiThread
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Unexpected error has occurred. Please retry in few seconds")
                .setCancelable(false)
                .setPositiveButton("Back") { dialog, _ -> dialog.dismiss(); finish() }
                .create()
                .show()
        }
    }

    override fun callUserInfo(): UserJoinInfo {
        return viewModel.getUserJoinInfo()
    }

    override fun sendTimerAndIdx(duration: Int, otherIdx: Int) {



    }

    override fun sendFinishInfo(displayRating: Boolean?, matchIdx: Int?) {


    }
}