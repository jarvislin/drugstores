package com.jarvislin.drugstores.extension

import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.base.App


fun View.transparentEffect(effectAlpha: Float = 0.5f) {
    val gestureListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent?): Boolean {
            alpha = effectAlpha
            invalidate()
            return super.onDown(e)
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            callOnClick()
            return super.onSingleTapConfirmed(e)
        }
    }

    val gestureDetector = GestureDetector(context, gestureListener)

    setOnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                alpha = 1.0f
                invalidate()
                return@setOnTouchListener false
            }
            else -> return@setOnTouchListener gestureDetector.onTouchEvent(event)
        }
    }
}

val ViewGroup.children: List<View>
    get() = (0 until childCount).map { getChildAt(it) }

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