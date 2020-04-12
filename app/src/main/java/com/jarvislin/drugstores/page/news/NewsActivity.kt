package com.jarvislin.drugstores.page.news

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ethanhua.skeleton.Skeleton
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.base.BaseActivity
import com.jarvislin.drugstores.extension.hide
import com.jarvislin.drugstores.extension.show
import kotlinx.android.synthetic.main.activity_news.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewsActivity : BaseActivity() {

    private val adapter by inject<NewsAdapter>()

    companion object {
        fun start(context: Context) {
            Intent(context, NewsActivity::class.java).apply {
                context.startActivity(this)
            }
        }
    }

    override val viewModel by viewModel<NewsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_news)

        toolbar.setNavigationOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        ContextCompat.getDrawable(this, R.drawable.divider_news)?.let {
            val itemDecorator = DividerItemDecoration(this, RecyclerView.VERTICAL)
            itemDecorator.setDrawable(it)
            recyclerView.addItemDecoration(itemDecorator)
        }

        val skeletonScreen = Skeleton.bind(recyclerView)
            .adapter(adapter)
            .load(R.layout.item_skeleton_news)
            .show()

        swipeRefreshLayout.setOnRefreshListener { viewModel.fetchNews() }

        viewModel.news.observe(this, Observer {
            textHint.hide()
            adapter.update(it)
            recyclerView.show()
            swipeRefreshLayout.isRefreshing = false
            skeletonScreen.hide()
        })

        viewModel.fetchedFailed.observe(this, Observer {
            swipeRefreshLayout.isRefreshing = false
            skeletonScreen.hide()
            recyclerView.hide()
            showError()
        })

        viewModel.fetchNews()
    }

    private fun showError() {
        AlertDialog.Builder(this)
            .setTitle("讀取失敗")
            .setMessage("目前無法讀取內容，請稍候再重試")
            .setPositiveButton("知道了") { _, _ -> }
            .show()

        textHint.text = "讀取失敗\n下拉頁面以重新整理"
        textHint.show()
    }
}

