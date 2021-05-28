package com.jarvislin.drugstores.page.menu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.base.BaseActivity
import com.jarvislin.drugstores.extension.bind
import com.jarvislin.drugstores.extension.click
import com.jarvislin.drugstores.page.map.MapsActivity
import com.jarvislin.drugstores.page.news.NewsActivity
import com.jarvislin.drugstores.page.scan.ScanActivity
import kotlinx.android.synthetic.main.activity_menu.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MenuActivity : BaseActivity() {
    override val viewModel by viewModel<MenuViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        cardMap.click { MapsActivity.start(this) }.bind(this)
        cardScan.click { ScanActivity.start(this) }.bind(this)
        cardNews.click { NewsActivity.start(this) }.bind(this)


        viewModel.confirmedCase.observe(this, { data ->
            textCount.text = String.format(
                getString(R.string.menu_case),
                data.confirmedCount,
                data.testingCount,
                data.excludedCount
            )
        })
        viewModel.fetchCaseData()
    }

    companion object {
        fun start(context: Context) {
            Intent(context, MenuActivity::class.java).let {
                context.startActivity(it)
            }
        }
    }
}