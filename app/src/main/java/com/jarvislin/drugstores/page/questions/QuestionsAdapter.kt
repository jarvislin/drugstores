package com.jarvislin.drugstores.page.questions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jarvislin.drugstores.R

class QuestionsAdapter : RecyclerView.Adapter<QuestionViewHolder>() {

    private val pairs = ArrayList<Pair<String, String>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question, parent, false)
            .let { QuestionViewHolder(it) }
    }

    override fun getItemCount() = pairs.size

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        pairs[position].let { holder.bind(it) }
    }

    fun update(pairs: List<Pair<String, String>>) {
        this.pairs.clear()
        this.pairs.addAll(pairs)
        notifyDataSetChanged()
    }
}