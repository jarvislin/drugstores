package com.jarvislin.drugstores.extension

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.jetbrains.anko.toast


fun Context.openMap(lat: Double, lng: Double) {
    val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lng")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    try {
        this.startActivity(mapIntent)
    } catch (ex: Exception) {
        toast("本裝置未安裝地圖服務")
    }
}

fun Context.shareText(subject: String, content: String) {
    val sharingIntent = Intent(Intent.ACTION_SEND)
    sharingIntent.type = "text/plain"
    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    sharingIntent.putExtra(Intent.EXTRA_TEXT, content)
    startActivity(Intent.createChooser(sharingIntent, "分享至..."))
}