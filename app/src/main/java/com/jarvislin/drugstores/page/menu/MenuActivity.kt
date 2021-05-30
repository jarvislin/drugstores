package com.jarvislin.drugstores.page.menu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.base.BaseActivity
import com.jarvislin.drugstores.extension.*
import com.jarvislin.drugstores.page.map.MapsActivity
import com.jarvislin.drugstores.page.news.NewsActivity
import com.jarvislin.drugstores.page.scan.ScanActivity
import com.jarvislin.drugstores.page.testing.RapidTestActivity
import kotlinx.android.synthetic.main.activity_menu.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MenuActivity : BaseActivity() {
    override val viewModel by viewModel<MenuViewModel>()
    private val analytics: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        cardMap.click {
            MapsActivity.start(this)
            analytics.logEvent("Menu_clickMap", null)
        }.bind(this)

        cardScan.click {
            ScanActivity.start(this)
            analytics.logEvent("Menu_clickScan", null)
        }.bind(this)

        cardNews.click {
            NewsActivity.start(this)
            analytics.logEvent("Menu_clickSews", null)
        }.bind(this)

        cardRapidTest.click {
            RapidTestActivity.start(this)
            analytics.logEvent("Menu_clickRapidTestLocations", null)
        }.bind(this)

        imageInfo.click {
            showInfoDialog()
            analytics.logEvent("Menu_clickDashboardInfo", null)
        }

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
            .setPositiveButton(R.string.dashboard_dialog_share_button) { _, _ ->
                analytics.logEvent("Menu_ShareDashboard", null)
                shareText(
                    "防疫資訊站",
                    getWording()
                )
            }
            .setNegativeButton(R.string.dismiss) { _, _ -> }
            .show()
    }

    private fun getWording(): String {
        return if (viewModel.confirmedCase.value == null) {
            "無內容"
        } else {
            val data = viewModel.confirmedCase.value!!
            """
                截至目前為止，
                總確診數：${data.confirmedCount} 人
                總通報數：${data.testingCount} 人
                總排除數：${data.excludedCount} 人
                解除隔離：${data.recoveredCount} 人
                死亡：${data.deathCount} 人
                昨日確診：${data.yesterdayConfirmedCount} 人
                昨日通報：${data.yesterdayTestingCount} 人
                昨日排除：${data.yesterdayExcludedCount} 人
                
                更多內容請參考防疫資訊 App：https://play.google.com/store/apps/details?id=com.jarvislin.drugstores
            """.trimIndent()
        }
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