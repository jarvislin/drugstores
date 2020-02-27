package com.jarvislin.drugstores.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.jarvislin.drugstores.R
import kotlinx.android.synthetic.main.view_open_time.view.*

class OpenTimeView @kotlin.jvm.JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_open_time, this, true)
    }

    fun setOpenTime(dayOfWeek: String, opens: Triple<Boolean, Boolean, Boolean>) {
        textDay.text = dayOfWeek
        imageMorning.setImageDrawable(getDrawable(opens.first))
        imageAfternoon.setImageDrawable(getDrawable(opens.second))
        imageNight.setImageDrawable(getDrawable(opens.third))
    }

    private fun getDrawable(opened: Boolean): Drawable? {
        val drawableId = if (opened) R.drawable.ic_open else R.drawable.ic_close
        return ContextCompat.getDrawable(context, drawableId)
    }
}