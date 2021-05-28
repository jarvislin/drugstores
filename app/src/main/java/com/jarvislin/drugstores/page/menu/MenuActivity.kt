package com.jarvislin.drugstores.page.menu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.base.BaseActivity
import com.jarvislin.drugstores.base.BaseViewModel
import com.jarvislin.drugstores.extension.bind
import com.jarvislin.drugstores.extension.click
import com.jarvislin.drugstores.page.map.MapsActivity
import com.jarvislin.drugstores.page.scan.ScanActivity
import kotlinx.android.synthetic.main.activity_menu.*

class MenuActivity(override val viewModel: BaseViewModel? = null) : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        cardMap.click { MapsActivity.start(this) }.bind(this)
        cardScan.click { ScanActivity.start(this) }.bind(this)
    }

    companion object {
        fun start(context: Context) {
            Intent(context, MenuActivity::class.java).let {
                context.startActivity(it)
            }
        }
    }
}