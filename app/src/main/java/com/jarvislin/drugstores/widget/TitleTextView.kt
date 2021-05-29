package com.jarvislin.drugstores.widget

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.jarvislin.drugstores.R
import kotlinx.android.synthetic.main.view_open_time.view.*
import kotlinx.android.synthetic.main.view_title_text.view.*
import java.util.*

class TitleTextView @kotlin.jvm.JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_title_text, this, true)

        val a = context.obtainStyledAttributes(attrs, R.styleable.TitleTextView, 0, 0)
        for (i in 0 until a.indexCount) {
            when (a.getIndex(i)) {
                R.styleable.TitleTextView_title ->
                    textTitle.text = a.getString(R.styleable.TitleTextView_title)
                R.styleable.TitleTextView_android_text ->
                    textContent.text = a.getString(R.styleable.TitleTextView_android_text)
            }
        }
        a.recycle()
    }

    fun setTitle(text: String) {
        textTitle.text = text
    }

    fun setContent(text: String) {
        textContent.text = text
    }
}