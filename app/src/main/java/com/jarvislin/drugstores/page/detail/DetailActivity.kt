package com.jarvislin.drugstores.page.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.jakewharton.rxbinding2.view.RxView
import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.domain.entity.MaskStatus
import com.jarvislin.domain.entity.Status
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.base.BaseActivity
import com.jarvislin.drugstores.extension.*
import com.jarvislin.drugstores.page.map.MarkerInfoManager
import com.jarvislin.drugstores.widget.InfoConverter
import com.jarvislin.drugstores.widget.ModelConverter
import com.jarvislin.drugstores.widget.OpenTimeView
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_detail.*
import org.jetbrains.anko.toast
import org.koin.android.ext.android.inject
import java.util.*


class DetailActivity : BaseActivity(),
    OnMapReadyCallback {

    companion object {
        private const val FORM_URL =
            "https://docs.google.com/forms/d/e/1FAIpQLSf1lLV7nNoZMFdOER7jmh735zM8W_0G8TJJKDEC3E0ZBPgEMQ/viewform"
        private const val KEY_INFO = "key_info"
        fun start(context: Context, info: DrugstoreInfo) {
            Intent(context, DetailActivity::class.java).apply {
                putExtra(KEY_INFO, info)
                context.startActivity(this)
            }
        }
    }

    override val viewModel: DetailViewModel by inject()
    private val info by lazy { intent.getSerializableExtra(KEY_INFO) as DrugstoreInfo }
    private val modelConverter by lazy { ModelConverter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        toolbar.navigationIcon?.tint(ContextCompat.getColor(this, R.color.secondaryIcons))
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewModel.latestMaskStatus.observe(this, Observer { showMaskStatus(it) })
        viewModel.fetchMaskStatus(info.id)

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

        textDateType.text = modelConverter.from(info).toDateType()

        if(info.isValidOpenTime()){
            modelConverter.from(info).toOpenTime()
                .mapIndexed { index: Int, triple: Triple<Boolean, Boolean, Boolean> ->
                    OpenTimeView(this).apply {
                        setOpenTime(InfoConverter.toDayOfWeek(index), triple)
                    }
                }.forEach { layoutOpenTime.addView(it) }
            cardOpenTime.show()
        }

        RxView.clicks(textInfo)
            .throttleClick()
            .subscribe { showInfoDialog() }
            .bind(this)

        RxView.clicks(imagePhone)
            .throttleClick()
            .subscribe { showPhoneDialog() }
            .bind(this)

        RxView.clicks(imageLocation)
            .throttleClick()
            .subscribe { openMap(info.lat, info.lng) }
            .bind(this)

        RxView.clicks(textShare)
            .throttleClick()
            .subscribe {
                shareText(
                    "口罩資訊地圖",
                    modelConverter.from(info).toShareContentText()
                )
            }
            .bind(this)

        RxView.clicks(textReport)
            .throttleClick()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showReportDialog() }
            .bind(this)
    }

    private fun showMaskStatus(maskStatus: MaskStatus) {
        textMaskStatus.text = modelConverter.from(maskStatus).toAmountWording()
        textMaskStatusTime.text = modelConverter.from(maskStatus).toReportWording()
        cardMaskStatus.show()
    }

    private fun showReportDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_report, null, false)
        val textSeller = view.findViewById<View>(R.id.textSeller)
        val textBuyer = view.findViewById<View>(R.id.textBuyer)

        val dialog = AlertDialog.Builder(this)
            .setTitle("回報相關資訊")
            .setPositiveButton(getString(R.string.dismiss)) { _, _ -> }
            .setView(view)
            .show()

        RxView.clicks(textSeller)
            .throttleClick()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                dialog.dismiss()
                openWeb(FORM_URL)
            }
            .bind(this)

        RxView.clicks(textBuyer)
            .throttleClick()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                dialog.dismiss()
                showBuyerDialog()
            }
            .bind(this)
    }

    private fun showBuyerDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_buyer, null, false)
        val radioSufficient = view.findViewById<RadioButton>(R.id.radioSufficient)
        val radioWarning = view.findViewById<RadioButton>(R.id.radioWarning)
        val radioEmpty = view.findViewById<RadioButton>(R.id.radioEmpty)

        val dialog = AlertDialog.Builder(this)
            .setTitle("還有成人口罩嗎？")
            .setView(view)
            .setPositiveButton(getString(R.string.dismiss)) { _, _ -> }
            .show()

        RxView.clicks(radioSufficient)
            .throttleClick()
            .subscribe {
                dialog.dismiss()
                showConfirmDialog(Status.Sufficient)
            }
            .bind(this)

        RxView.clicks(radioWarning)
            .throttleClick()
            .subscribe {
                dialog.dismiss()
                showConfirmDialog(Status.Warning)
            }
            .bind(this)

        RxView.clicks(radioEmpty)
            .throttleClick()
            .subscribe {
                dialog.dismiss()
                showConfirmDialog(Status.Empty)
            }
            .bind(this)
    }

    private fun showConfirmDialog(status: Status) {
        val option = when (status) {
            Status.Empty -> getString(R.string.option_empty)
            Status.Warning -> getString(R.string.option_warning)
            Status.Sufficient -> getString(R.string.option_sufficient)
        }

        AlertDialog.Builder(this)
            .setMessage("即將送出選項：$option")
            .setPositiveButton("送出") { _, _ -> viewModel.reportMaskStatus(info.id, status) }
            .setNegativeButton("取消") { _, _ -> }
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

        map.setOnMapClickListener { openMap(info.lat, info.lng) }
    }

    private fun showInfoDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.id_note_title))
            .setMessage(getString(R.string.id_note_message))
            .setPositiveButton(getString(R.string.dismiss)) { _, _ -> }
            .show()
    }

    private fun showPhoneDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dial_title))
            .setMessage(getString(R.string.dial_message))
            .setNegativeButton(getString(R.string.dismiss)) { _, _ -> }
            .setPositiveButton(getString(R.string.dial)) { _, _ ->
                Intent(Intent.ACTION_DIAL).apply {
                    try {
                        data = Uri.parse("tel:${info.phone}")
                        startActivity(this)
                    } catch (ex: Exception) {
                        toast(getString(R.string.dial_error))
                    }
                }
            }
            .show()
    }
}

