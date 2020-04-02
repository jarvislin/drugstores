package com.jarvislin.drugstores.page.preview

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.glide.GlideImageLoader
import com.jarvislin.drugstores.R
import kotlinx.android.synthetic.main.activity_preview.*

class PreviewActivity : AppCompatActivity() {

    companion object {
        private const val KEY_URL = "url"
        fun start(context: Context, url: String) {
            Intent(context, PreviewActivity::class.java).apply {
                putExtra(KEY_URL, url)
                context.startActivity(this)
            }
        }
    }

    private val url by lazy { intent.getStringExtra(KEY_URL) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // init before set view
        BigImageViewer.initialize(GlideImageLoader.with(this))

        setContentView(R.layout.activity_preview)
        bigImageView.showImage(Uri.parse(url))

        toolbar.setNavigationOnClickListener { finish() }
    }

    override fun onDestroy() {
        Glide.get(this).clearMemory()
        super.onDestroy()
    }
}