package com.jarvislin.drugstores.extension

import android.content.Context
import android.content.Intent
import android.net.Uri
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