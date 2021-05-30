package com.jarvislin.drugstores.extension

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import com.jarvislin.drugstores.R
import org.jetbrains.anko.toast
import timber.log.Timber


fun Context.openMap(
    lat: Double,
    lng: Double,
    onSuccess: (() -> Unit)? = null,
    onError: ((ex: Exception) -> Unit)? = null
) {
    Uri.parse("google.navigation:q=$lat,$lng").let {
        Intent(Intent.ACTION_VIEW, it).apply {
            try {
                startActivity(this)
                onSuccess?.invoke()
            } catch (ex: Exception) {
                Timber.e(ex)
                onError?.invoke(ex)
                toast("本裝置未安裝地圖服務")
            }
        }
    }
}

fun Context.shareText(
    subject: String,
    content: String,
    onSuccess: (() -> Unit)? = null,
    onError: ((ex: Exception) -> Unit)? = null
) {
    Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, content)

        try {
            onSuccess?.invoke()
            startActivity(Intent.createChooser(this, "分享至..."))
        } catch (ex: Exception) {
            Timber.e(ex)
            onError?.invoke(ex)
            toast("本裝置找不到可分享之 App")
        }
    }
}

fun Context.openWeb(
    url: String,
    onSuccess: (() -> Unit)? = null,
    onError: ((ex: Exception) -> Unit)? = null
) {
    Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        try {
            onSuccess?.invoke()
            startActivity(this)
        } catch (ex: Exception) {
            Timber.e(ex)
            onError?.invoke(ex)
            toast("無法開啟網頁")
        }
    }

}

fun Context.sendSMS(
    phone: String,
    body: String,
    onSuccess: (() -> Unit)? = null,
    onError: ((ex: java.lang.Exception) -> Unit)? = null
) {
    Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"))
        .apply { putExtra("sms_body", body) }
        .let {
            try {
                startActivity(it)
                onSuccess?.invoke()
            } catch (ex: Exception) {
                Timber.e(ex)
                onError?.invoke(ex)
                toast("本裝置未安裝簡訊服務")
            }
        }
}

fun Context.hasPermission(vararg permission: String): Boolean {
    return permission.all {
        ActivityCompat.checkSelfPermission(
            this,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }
}

fun Context.call(
    phone: String,
    onSuccess: (() -> Unit)? = null,
    onError: ((ex: Exception) -> Unit)? = null
) {
    Intent(Intent.ACTION_DIAL).apply {
        try {
            data = Uri.parse("tel:$phone")
            startActivity(this)
            onSuccess?.invoke()
        } catch (ex: Exception) {
            Timber.e(ex)
            onError?.invoke(ex)
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

fun Context.vibrateOneShot() {
    val vibrator = getSystemService(this, Vibrator::class.java)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        //deprecated in API 26
        vibrator?.vibrate(100)
    }
}