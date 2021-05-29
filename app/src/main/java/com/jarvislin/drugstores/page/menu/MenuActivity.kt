package com.jarvislin.drugstores.page.menu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.base.BaseActivity
import com.jarvislin.drugstores.extension.bind
import com.jarvislin.drugstores.extension.click
import com.jarvislin.drugstores.extension.hide
import com.jarvislin.drugstores.extension.show
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
        imageInfo.click { showInfoDialog() }

        viewModel.confirmedCase.observe(this, { data ->
            cardDashboard.show()

            textConfirmedCount.text = data.confirmedCount
            textExcludedCount.text = data.excludedCount
            textTestingCount.text = data.testingCount

            textYesterdayConfirmedCount.text = data.yesterdayConfirmedCount
            textYesterdayExcludedCount.text = data.yesterdayExcludedCount
            textYesterdayTestingCount.text = data.yesterdayTestingCount

            textDeathCount.text = data.deathCount
            textRecoveredCount.text = data.recoveredCount
        })
    }

    private fun showInfoDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dashboard_dialog_title)
            .setMessage(R.string.dashboard_dialog_content)
            .setPositiveButton(R.string.dismiss) { _, _ -> }
            .show()
    }

    override fun onResume() {
        super.onResume()
        cardDashboard.hide(invisible = true)
        viewModel.fetchCaseData()
    }

    companion object {
        fun start(context: Context) {
            Intent(context, MenuActivity::class.java).let { context.startActivity(it) }
        }
    }
}