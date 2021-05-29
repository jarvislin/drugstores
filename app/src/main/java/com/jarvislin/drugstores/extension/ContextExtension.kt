package com.jarvislin.drugstores.extension

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import com.jarvislin.drugstores.R
import org.jetbrains.anko.toast


fun Context.openMap(lat: Double, lng: Double) {
    Uri.parse("google.navigation:q=$lat,$lng").let {
        Intent(Intent.ACTION_VIEW, it).apply {
            try {
                startActivity(this)
            } catch (ex: Exception) {
                toast("本裝置未安裝地圖服務")
            }
        }
    }
}

fun Context.shareText(subject: String, content: String) {
    Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, content)
        startActivity(Intent.createChooser(this, "分享至..."))
    }
}

fun Context.openWeb(url: String) {
    Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        if (resolveActivity(packageManager) != null) {
            startActivity(this)
        } else {
            toast("無法開啟網頁")
        }
    }
}

fun Context.sendSMS(phone: String, body: String) {
    Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"))
        .apply { putExtra("sms_body", body) }
        .let { startActivity(it) }
}

fun Context.hasPermission(vararg permission: String): Boolean {
    return permission.all {
        ActivityCompat.checkSelfPermission(
            this,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }
}

fun Context.call(phone: String) {
    Intent(Intent.ACTION_DIAL).apply {
        try {
            data = Uri.parse("tel:$phone")
            startActivity(this)
        } catch (ex: Exception) {
            toast(getString(R.string.dial_error))
        }
    }
}

fun Context.copyToClipboard(text: String) {
    val clipboard = getSystemService(this, ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText("label", text))
}

fun Context.openSettings() {
    val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    startActivity(intent)
}