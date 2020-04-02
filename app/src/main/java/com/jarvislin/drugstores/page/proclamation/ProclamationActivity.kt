package com.jarvislin.drugstores.page.proclamation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jarvislin.domain.entity.Proclamation
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.extension.toJson
import kotlinx.android.synthetic.main.activity_proclamation.*
import org.koin.android.ext.android.inject

class ProclamationActivity : AppCompatActivity() {

    companion object {
        private const val KEY_PROCLAMATIONS = "key_proclamations"
        fun start(context: Context, proclamations: List<Proclamation>) {
            Intent(context, ProclamationActivity::class.java).apply {
                putExtra(KEY_PROCLAMATIONS, ArrayList(proclamations))
                context.startActivity(this)
            }
        }
    }

    private val viewModel by inject<ProclamationViewModel>()
    private val proclamations by lazy { intent.getSerializableExtra(KEY_PROCLAMATIONS) as List<Proclamation> }
    private val adapter by inject<ProclamationAdapter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proclamation)

        toolbar.setNavigationOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        ContextCompat.getDrawable(this, R.drawable.divider)?.let {
            val itemDecorator = DividerItemDecoration(this, RecyclerView.VERTICAL)
            itemDecorator.setDrawable(it)
            recyclerView.addItemDecoration(itemDecorator)
        }

        adapter.update(proclamations)

        viewModel.saveProclamations(proclamations.toJson())
    }

    override fun onDestroy() {
        Glide.get(this).clearMemory()
        super.onDestroy()
    }
}