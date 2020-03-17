package com.jarvislin.drugstores.page.questions

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jarvislin.drugstores.R

class QuestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(pair: Pair<String, String>) {
        question.text = pair.first
        answer.text = pair.second
    }

    private val question: TextView = itemView.findViewById(
        R.id.textQuestion
    )
    private val answer: TextView = itemView.findViewById(
        R.id.textAnswer
    )
}