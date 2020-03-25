package com.jarvislin.drugstores.page.questions

import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.extension.openWeb


class QuestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    companion object {
        private const val TARGET_STRING = "「防疫口罩管控系統」VPN 登錄作業使用者手冊"
    }

    fun bind(pair: Pair<String, String>) {
        question.text = pair.first
        answer.text = pair.second

        pair.second.let { answerWording ->
            if (answerWording.contains(TARGET_STRING)) {
                val string = SpannableString(answerWording)
                string.setSpan(
                    object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            widget.context.openWeb("https://www.facebook.com/PharmacistAssociations/posts/2702135833198083/")
                        }
                    },
                    answerWording.indexOf(TARGET_STRING),
                    answerWording.indexOf(TARGET_STRING) + TARGET_STRING.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                answer.highlightColor =
                    ContextCompat.getColor(itemView.context, android.R.color.transparent)
                answer.movementMethod = LinkMovementMethod.getInstance()
                answer.text = string
            }
        }

    }

    private val question: TextView = itemView.findViewById(R.id.textQuestion)
    private val answer: TextView = itemView.findViewById(R.id.textAnswer)
}