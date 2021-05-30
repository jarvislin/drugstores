package com.jarvislin.drugstores.page.detail

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakewharton.rxbinding2.view.RxView
import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.base.BaseActivity
import com.jarvislin.drugstores.base.BaseViewModel
import com.jarvislin.drugstores.extension.*
import com.jarvislin.drugstores.page.map.MarkerInfoManager
import com.jarvislin.drugstores.widget.InfoConverter
import com.jarvislin.drugstores.widget.ModelConverter
import com.jarvislin.drugstores.widget.OpenTimeView
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_detail.*


class DetailActivity : BaseActivity(),
    OnMapReadyCallback {

    companion object {
        private const val FORM_URL =
            "https://docs.google.com/forms/d/e/1FAIpQLSf1lLV7nNoZMFdOER7jmh735zM8W_0G8TJJKDEC3E0ZBPgEMQ/viewform"
        private const val KEY_INFO = "key_info"
        const val KEY_LOCATION = "key_location"
        fun start(
            context: Context,
            info: DrugstoreInfo,
            location: Location? = null
        ) {
            Intent(context, DetailActivity::class.java).apply {
                putExtra(KEY_INFO, info)
                location?.let { putExtra(KEY_LOCATION, it) }
                context.startActivity(this)
            }
        }
    }

    override val viewModel: BaseViewModel? = null
    private val info by lazy { intent.getSerializableExtra(KEY_INFO) as DrugstoreInfo }
    private val location by lazy { intent.getParcelableExtra(KEY_LOCATION) as? Location }
    private val analytics: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(this) }
    private val modelConverter by lazy { ModelConverter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        toolbar.navigationIcon?.tint(ContextCompat.getColor(this, R.color.secondaryIcons))
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        layoutAdult.background = modelConverter.from(info).toAdultMaskBackground()
        layoutChild.background = modelConverter.from(info).toChildMaskBackground()

        textAdultAmount.text = info.adultMaskAmount.toString()
        textChildAmount.text = info.childMaskAmount.toString()

        textName.text = info.name
        textAddress.text = info.address
        textPhone.text = info.phone
        textUpdate.text = modelConverter.from(info).toUpdateWording()
        info.note.trim().let {
            if (it.isNotEmpty() && it != "-") {
                textOpening.text = info.note
            } else {
                textOpening.hide()
                imageOpening.hide()
            }
        }

        textDistance.text = modelConverter.from(info).toDistance(location)

        if (info.isValidOpenTime()) {
            modelConverter.from(info).toOpenTime()
                .mapIndexed { index: Int, triple: Triple<Boolean, Boolean, Boolean> ->
                    OpenTimeView(this).apply {
                        setOpenTime(InfoConverter.toDayOfWeek(index), triple, index)
                    }
                }.forEach { layoutOpenTime.addView(it) }
            cardOpenTime.show()
        }


        RxView.clicks(imagePhone)
            .throttleClick()
            .subscribe {
                analytics.logEvent("detail_click_phone", null)
                showPhoneDialog()
            }
            .bind(this)

        RxView.clicks(imageLocation)
            .throttleClick()
            .subscribe {
                analytics.logEvent("detail_click_address", null)
                openMap(info.lat, info.lng)
            }
            .bind(this)

        RxView.clicks(textShare)
            .throttleClick()
            .subscribe {
                analytics.logEvent("detail_click_share", null)
                shareText(
                    "防疫資訊",
                    modelConverter.from(info).toShareContentText()
                )
            }
            .bind(this)

        RxView.clicks(textReport)
            .throttleClick()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                analytics.logEvent("detail_click_report", null)
                openWeb(FORM_URL)
            }
            .bind(this)
    }

    private fun showRecordsDialog() {
        analytics.logEvent("detail_show_records_dialog", null)
        AlertDialog.Builder(this)
            .setTitle("庫存量說明")
            .setMessage("收錄早上七點至晚上十點的存量資料，用來推測約略的口罩販售時間；圖表支援水平及垂直的兩指縮放功能。")
            .setPositiveButton(getString(R.string.dismiss)) { _, _ ->
                analytics.logEvent("detail_dismiss_records_dialog", null)
            }
            .show()
    }

    override fun onMapReady(map: GoogleMap) {

        // set map UI
        map.uiSettings.setAllGesturesEnabled(false)
        map.uiSettings.isMapToolbarEnabled = false

        // move camera
        info.let { LatLng(it.lat, it.lng) }
            .also { CameraUpdateFactory.newLatLngZoom(it, 18f).run { map.moveCamera(this) } }

        // add marker
        val markerInfo = MarkerInfoManager.getMarkerInfo(info.adultMaskAmount)
        val option = MarkerOptions().position(LatLng(info.lat, info.lng))

        ContextCompat.getDrawable(this, markerInfo.drawableId)?.getBitmap()
            .let { option.icon(BitmapDescriptorFactory.fromBitmap(it)) }

        map.addMarker(option)

        map.setOnMapClickListener {
            analytics.logEvent("detail_click_map", null)
            openMap(info.lat, info.lng)
        }
    }

    private fun showPhoneDialog() {
        analytics.logEvent("detail_show_dial_dialog", null)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dial_title))
            .setMessage(getString(R.string.dial_message))
            .setNegativeButton(getString(R.string.dismiss)) { _, _ ->
                analytics.logEvent("detail_cancel_dial", null)
            }
            .setPositiveButton(getString(R.string.dial)) { _, _ ->
                analytics.logEvent("detail_click_dial", null)
                call(info.phone)
            }
            .show()
    }
}

