package com.jarvislin.drugstores.extension

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.View.*
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.base.App
import java.text.SimpleDateFormat
import java.util.*


fun View.show() {
    visibility = VISIBLE
}

fun View.hide(invisible: Boolean = false) {
    visibility = if (invisible) INVISIBLE else GONE
}

fun Drawable.tint(@ColorInt color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        mutate().colorFilter = BlendModeColorFilter(color, BlendMode.SRC_IN)
    } else {
        mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }
}

fun Int.toBackground(): Drawable? {
    return ContextCompat.getDrawable(
        App.instance(), when {
            this == 0 -> R.drawable.background_empty
            this in 1..20 -> R.drawable.background_warning
            else -> R.drawable.background_sufficient
        }
    )
}

fun Drawable.getBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(
        this.intrinsicWidth,
        this.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    this.setBounds(0, 0, canvas.width, canvas.height)
    this.draw(canvas)
    return bitmap
}

fun String.toUpdateWording(): String {
    val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
    return try {
        val current = System.currentTimeMillis()
        val date = format.parse(this)
        val diffHour = (current - date.time) / 60 / 60 / 1000
        val diffMinute = (current - date.time) / 60 / 1000
        val diffSecond = (current - date.time) / 1000
        when {
            diffHour > 0 -> "資料更新於 $diffHour 小時前"
            diffMinute > 0 -> "資料更新於 $diffMinute 分鐘前"
            else -> "資料更新於 $diffSecond 秒前"
        }
    } catch (ex: Exception) {
        "更新於：$this"
    }
}