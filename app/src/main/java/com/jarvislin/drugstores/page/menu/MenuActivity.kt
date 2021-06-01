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
import kotlinx.android.synthetic.main.view_dashboard.*
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
                ${getString(R.string.dashboard_confirmed)}: ${data.confirmedCount}
                ${getString(R.string.dashboard_testings)}: ${data.testingCount}
                ${getString(R.string.dashboard_excluded)}: ${data.excludedCount}
                ${getString(R.string.dashboard_recovered)}: ${data.recoveredCount}
                ${getString(R.string.dashboard_death)}: ${data.deathCount}
                ${getString(R.string.dashboard_share_wording_yesterday)}
                ${getString(R.string.dashboard_new_confirmed)}: ${data.yesterdayConfirmedCount}
                ${getString(R.string.dashboard_new_testings)}: ${data.yesterdayTestingCount}
                ${getString(R.string.dashboard_new_excluded)}: ${data.yesterdayExcludedCount}
                
                ${getString(R.string.dashboard_promotion)}
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