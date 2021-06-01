package com.jarvislin.drugstores.page.scan

import android.Manifest.permission.CAMERA
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.webkit.URLUtil
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.budiyev.android.codescanner.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.base.BaseActivity
import com.jarvislin.drugstores.extension.*
import kotlinx.android.synthetic.main.activity_scan.*
import org.jetbrains.anko.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.*

class ScanActivity : BaseActivity() {

    override val viewModel: ScanViewModel by viewModel()
    private lateinit var codeScanner: CodeScanner
    private val analytics: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        toolbar.setNavigationOnClickListener { finish() }

        codeScanner = CodeScanner(this, viewScanner)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                vibrateOneShot()
                it.text.let { result ->
                    when {
                        URLUtil.isValidUrl(result) -> {
                            if (viewModel.isEnabledWebDialog()) {
                                showWebDialog(result)
                            } else {
                                openWeb(result)
                            }
                            analytics.logEvent("Scan_Web", null)
                        }
                        result.toUpperCase(Locale.getDefault()).startsWith("SMSTO:1922:場所代碼：") -> {
                            val result = result.toUpperCase(Locale.getDefault())
                            val content = result.replace("SMSTO:1922:", "")
                            if (viewModel.isEnabledSmsDialog()) {
                                showSmsDialog(content)
                            } else {
                                sendSms1922(content)
                            }
                            analytics.logEvent("Scan_1922", null)
                        }
                        else -> {
                            toast(getString(R.string.scan_unsupported_format))
                            codeScanner.startPreview()
                            Timber.e(result)
                            analytics.logEvent("Scan_UnsupportedFormat",
                                Bundle().apply { putString("content", result) })
                        }
                    }
                }
            }
        }
        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread {
                toast(R.string.scan_error)
                codeScanner.startPreview()
            }
            analytics.logEvent(
                "Scan_ScanFailed",
                Bundle().apply { putString("error", it.localizedMessage) })
        }

        if (hasPermission(CAMERA).not()) {
            ActivityCompat.requestPermissions(this, arrayOf(CAMERA), PERMISSION_CAMERA)
        }
    }

    private fun showWebDialog(result: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.view_checkbox, null)
        val checkBox = view.findViewById<CheckBox>(R.id.checkbox)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.scan_web_dialog_title))
            .setMessage(getString(R.string.scan_web_dialog_content))
            .setView(view)
            .setNegativeButton(R.string.dismiss) { _, _ -> }
            .setPositiveButton(R.string.scan_web_browse_button) { _, _ -> openWeb(result) }
            .setOnDismissListener {
                viewModel.saveWebDialogSetting(checkBox.isChecked)
                codeScanner.startPreview()
            }
            .show()
    }

    private fun openWeb(url: String) {
        openWeb(url = url, onError = {
            analytics.logEvent("Scan_OpenWebFailed",
                Bundle().apply { putString("error", it.localizedMessage) })
        })
    }

    private fun showSmsDialog(content: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.view_checkbox, null)
        val checkBox = view.findViewById<CheckBox>(R.id.checkbox)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.scan_sms_dialog_title))
            .setMessage(getString(R.string.scan_sms_dialog_content))
            .setView(view)
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .setPositiveButton(R.string.scan_sms_dialog_next_button) { _, _ -> sendSms1922(content) }
            .setOnDismissListener {
                viewModel.saveSmsDialogSetting(checkBox.isChecked)
                codeScanner.startPreview()
            }
            .show()
    }

    private fun sendSms1922(content: String) {
        sendSMS(
            phone = "1922",
            body = content,
            onError = {
                analytics.logEvent("Scan_OpenSmsFailed",
                    Bundle().apply { putString("error", it.localizedMessage) })
            })
    }

    override fun onResume() {
        super.onResume()
        takeIf { hasPermission(CAMERA) }?.let { codeScanner.startPreview() }
    }

    override fun onPause() {
        takeIf { hasPermission(CAMERA) }?.let { codeScanner.releaseResources() }
        super.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (hasPermission(CAMERA)) {
            codeScanner.startPreview()
            analytics.logEvent("Scan_GrantCameraPermission", null)
        } else {
            analytics.logEvent("Scan_RejectCameraPermission", null)
            showPermissionDialog()
        }
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.scan_permission_dialog_title))
            .setMessage(getString(R.string.scan_permission_dialog_content))
            .setNegativeButton(R.string.scan_permission_dialog_leave) { _, _ ->
                toast(R.string.scan_no_permission)
                finish()
            }
            .setPositiveButton(R.string.scan_permission_dialog_go_to_settings) { _, _ -> openSettings() }
            .show()
    }

    companion object {
        private const val PERMISSION_CAMERA = 7890

        fun start(context: Context) {
            Intent(context, ScanActivity::class.java).let {
                context.startActivity(it)
            }
        }
    }
}