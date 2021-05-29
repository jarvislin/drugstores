package com.jarvislin.drugstores.page.news

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.jarvislin.domain.entity.News
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.extension.openWeb

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.NewsHolder>() {
    private val news = ArrayList<News>()
    private lateinit var analytics: FirebaseAnalytics

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        analytics = FirebaseAnalytics.getInstance(recyclerView.context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsHolder {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
            .let { NewsHolder(it) }
    }

    override fun getItemCount() = news.size

    override fun onBindViewHolder(holder: NewsHolder, position: Int) {
        news[position].let { holder.bind(it) }
    }

    fun update(news: List<News>) {
        this.news.clear()
        this.news.addAll(news)
        notifyDataSetChanged()
    }

    inner class NewsHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textTitle: TextView = itemView.findViewById(
            R.id.textTitle
        )
        private val textDescription: TextView = itemView.findViewById(
            R.id.textDescription
        )

        fun bind(news: News) {
            textTitle.text = news.title ?: "無標題"

            textDescription.text = if (news.description.isNullOrEmpty()) "無內文"
            else HtmlCompat.fromHtml(news.description!!, HtmlCompat.FROM_HTML_MODE_LEGACY)

            if (news.link == null) {
                itemView.setOnClickListener(null)
            } else {
                itemView.setOnClickListener {
                    analytics.logEvent("news_click_item", null)
                    itemView.context.openWeb(news.link!!)
                }
            }
        }
    }
}