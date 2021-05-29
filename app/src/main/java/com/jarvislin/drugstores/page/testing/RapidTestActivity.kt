package com.jarvislin.drugstores.page.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jarvislin.domain.entity.*
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.base.BaseActivity
import com.jarvislin.drugstores.extension.hide
import com.jarvislin.drugstores.extension.show
import kotlinx.android.synthetic.main.activity_rapid_test.*
import kotlinx.android.synthetic.main.activity_rapid_test.recyclerView
import org.jetbrains.anko.toast
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class RapidTestActivity : BaseActivity() {
    override val viewModel by viewModel<RapidTestViewModel>()
    private val locationAdapter by inject<LocationAdapter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rapid_test)

        // init views
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.inflateMenu(R.menu.rapid_test)
        toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.refresh) {
                viewModel.fetchRapidTestLocations()
                true
            } else false
        }

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = locationAdapter
        ContextCompat.getDrawable(this, R.drawable.divider)?.let {
            val itemDecorator = DividerItemDecoration(this, RecyclerView.VERTICAL)
            itemDecorator.setDrawable(it)
            recyclerView.addItemDecoration(itemDecorator)
        }

        spinnerCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val city = spinnerCity.getItemAtPosition(position) as String
                viewModel.filterLocationByCity(city)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }
        }

        // handle observe

        viewModel.cityNames.observe(this, {
            spinnerCity.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1, it.toList()
            )

            if (it.isEmpty()) {
                cardCity.hide()
                toast("未取得資料，請稍後再試")
            } else {
                cardCity.show()
            }
        })

        viewModel.selectedLocations.observe(this) { locationAdapter.update(it) }
        viewModel.progress.observe(this) {
            when (it) {
                StartDownloading -> {
                    cardCity.hide()
                    progressBar.show()
                    recyclerView.hide()
                }
                LatestDataDownloaded -> {
                    progressBar.hide()
                    recyclerView.show()
                }
                UpdateFailed -> {
                    progressBar.hide()
                }
            }
        }

        viewModel.fetchRapidTestLocations()
    }

    companion object {
        fun start(context: Context) {
            Intent(context, RapidTestActivity::class.java).let { context.startActivity(it) }
        }
    }
}
