package com.jarvislin.drugstores.page.proclamation

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.analytics.FirebaseAnalytics
import com.jarvislin.domain.entity.Proclamation
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.page.preview.PreviewActivity

class ProclamationAdapter : RecyclerView.Adapter<ProclamationAdapter.ProclamationHolder>() {

    private val proclamations = ArrayList<Proclamation>()
    private lateinit var analytics: FirebaseAnalytics

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        analytics = FirebaseAnalytics.getInstance(recyclerView.context)
    }

    fun update(proclamations: List<Proclamation>) {
        this.proclamations.clear()
        this.proclamations.addAll(proclamations)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProclamationHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_proclamation, parent, false)
        return ProclamationHolder(view)
    }

    override fun getItemCount(): Int = proclamations.size

    override fun onBindViewHolder(holder: ProclamationHolder, position: Int) {
        proclamations[position].image?.let { url ->
            Glide.with(holder.itemView.context)
                .load(url)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.imageView.setImageDrawable(resource)
                        resource?.let {
                            val matrix = holder.imageView.matrix
                            val imageWidth = holder.imageView.drawable.intrinsicWidth
                            val screenWidth =
                                holder.imageView.context.resources.displayMetrics.widthPixels
                            val scaleRatio = 1f * screenWidth / imageWidth
                            matrix.postScale(scaleRatio, scaleRatio)
                        }
                        return true
                    }
                })
                .into(holder.imageView)

            holder.itemView.setOnClickListener {
                analytics.logEvent(
                    "proclamation_click_browse",
                    Bundle().apply { putString("url", url) })
                PreviewActivity.start(holder.itemView.context, url)
            }
        }
    }

    inner class ProclamationHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}