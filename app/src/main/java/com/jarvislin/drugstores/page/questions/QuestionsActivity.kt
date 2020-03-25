package com.jarvislin.drugstores.page.questions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jarvislin.drugstores.R
import kotlinx.android.synthetic.main.activity_questions.*
import org.koin.android.ext.android.inject

class QuestionsActivity : AppCompatActivity() {

    private val adapter by inject<QuestionsAdapter>()

    companion object {
        fun start(context: Context) {
            Intent(context, QuestionsActivity::class.java).apply {
                context.startActivity(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_questions)

        toolbar.setNavigationOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        ContextCompat.getDrawable(this, R.drawable.divider)?.let {
            val itemDecorator = DividerItemDecoration(this, RecyclerView.VERTICAL)
            itemDecorator.setDrawable(it)
            recyclerView.addItemDecoration(itemDecorator)
        }

        val questions = resources.getStringArray(R.array.questions)
        val answers = resources.getStringArray(R.array.answers)

        questions.mapIndexed { index: Int, s: String -> Pair(s, answers[index]) }
            .let { adapter.update(it) }
    }
}

