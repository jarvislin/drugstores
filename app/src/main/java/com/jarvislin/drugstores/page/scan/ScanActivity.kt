package com.jarvislin.drugstores.page.scan

import android.Manifest.permission.CAMERA
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.URLUtil
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.budiyev.android.codescanner.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.base.BaseActivity
import com.jarvislin.drugstores.base.BaseViewModel
import com.jarvislin.drugstores.extension.hasPermission
import com.jarvislin.drugstores.extension.openSettings
import com.jarvislin.drugstores.extension.openWeb
import com.jarvislin.drugstores.extension.sendSMS
import kotlinx.android.synthetic.main.activity_scan.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast

class ScanActivity : BaseActivity() {

    override val viewModel: BaseViewModel? = null
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
                it.text.let { result ->
                    when {
                        URLUtil.isValidUrl(result) -> {
                            openWeb(url = result, onError = { ex ->
                                codeScanner.startPreview()
                                analytics.logEvent(
                                    "Scan_OpenWebFailed",
                                    Bundle().apply { putString("error", ex.localizedMessage) })
                            })
                            analytics.logEvent("Scan_Web", null)
                        }
                        result.startsWith("SMSTO:1922:場所代碼：") -> {
                            result.replace(
                                "SMSTO:1922:",
                                ""
                            ).let { content ->
                                sendSMS(
                                    phone = "1922",
                                    body = content,
                                    onSuccess = { longToast("請按下送出鍵即可完成") },
                                    onError = { ex ->
                                        codeScanner.startPreview()
                                        analytics.logEvent("Scan_OpenSmsFailed",
                                            Bundle().apply {
                                                putString("error", ex.localizedMessage)
                                            })
                                    })
                            }
                            analytics.logEvent("Scan_1922", null)
                        }
                        else -> {
                            toast(getString(R.string.scan_unsupported_format))
                            codeScanner.startPreview()
                            analytics.logEvent(
                                "Scan_UnsupportedFormat",
                                Bundle().apply { putString("content", result) })
                        }
                    }
                }
            }
        }
        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread { toast(R.string.scan_error) }
            analytics.logEvent(
                "Scan_failed",
                Bundle().apply { putString("error", it.localizedMessage) })
        }

        if (hasPermission(CAMERA).not()) {
            ActivityCompat.requestPermissions(this, arrayOf(CAMERA), PERMISSION_CAMERA)
        }
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
            .setTitle(getString(R.string.scan_dialog_title))
            .setMessage(getString(R.string.scan_dialog_content))
            .setNegativeButton(R.string.scan_dialog_leave) { _, _ ->
                toast(R.string.scan_no_permission)
                finish()
            }
            .setPositiveButton(R.string.scan_dialog_go_to_settings) { _, _ -> openSettings() }
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