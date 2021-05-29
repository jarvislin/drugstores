package com.jarvislin.drugstores.page.detail

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.jakewharton.rxbinding2.view.RxView
import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.domain.entity.MaskRecord
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
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DetailActivity : BaseActivity(),
    OnMapReadyCallback {

    companion object {
        private const val FORM_URL =
            "https://docs.google.com/forms/d/e/1FAIpQLSf1lLV7nNoZMFdOER7jmh735zM8W_0G8TJJKDEC3E0ZBPgEMQ/viewform"
        private const val KEY_INFO = "key_info"
        const val KEY_LOCATION = "key_location"
        const val TARGET_STRING = "更新資訊"
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

    override val viewModel: DetailViewModel by inject()
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

        viewModel.latestMaskStatus.observe(this, Observer { showMaskStatus(it) })
        viewModel.usesNumberTicket.observe(this, Observer { showNumberTicketCard(it) })
        viewModel.fetchMaskStatus(info.id)
        viewModel.fetchUsesNumberTicket(info.id)

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
                showReportDialog()
            }
            .bind(this)

        RxView.clicks(textRecords)
            .throttleClick()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                analytics.logEvent("detail_click_records_info", null)
                showRecordsDialog()
            }
            .bind(this)
    }

    private fun showNumberTicketCard(usesNumberTicket: Boolean) {
        textNumberTicket.highlightColor = ContextCompat.getColor(this, android.R.color.transparent)
        textNumberTicket.movementMethod = LinkMovementMethod.getInstance()

        val text = if (usesNumberTicket) {
            getString(R.string.is_number_ticket)
        } else {
            getString(R.string.is_not_number_ticket)
        }
        SpannableString(text).apply {
            setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        showNumberTicketDialog()
                    }
                },
                text.indexOf(TARGET_STRING),
                text.indexOf(TARGET_STRING) + TARGET_STRING.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }.let { textNumberTicket.text = it }

        cardNumberTicket.show()
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

    private fun showMaskStatus(maskStatus: MaskStatus) {
        textMaskStatus.text = modelConverter.from(maskStatus).toAmountWording()
        textMaskStatusTime.text = modelConverter.from(maskStatus).toReportWording()
        cardMaskStatus.show()
    }

    private fun showReportDialog() {
        analytics.logEvent("detail_show_report_dialog", null)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_report, null, false)
        val textSeller = view.findViewById<View>(R.id.textSeller)
        val textBuyer = view.findViewById<View>(R.id.textBuyer)
        val textNumber = view.findViewById<View>(R.id.textNumber)

        val dialog = AlertDialog.Builder(this)
            .setTitle("回報相關資訊")
            .setPositiveButton(getString(R.string.dismiss)) { _, _ -> }
            .setView(view)
            .show()

        RxView.clicks(textNumber)
            .throttleClick()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                analytics.logEvent("detail_click_report_number_ticket", null)
                dialog.dismiss()
                showNumberTicketDialog()
            }
            .bind(this)

        RxView.clicks(textSeller)
            .throttleClick()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                analytics.logEvent("detail_click_report_drugstore", null)
                dialog.dismiss()
                openWeb(FORM_URL)
            }
            .bind(this)

        RxView.clicks(textBuyer)
            .throttleClick()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                analytics.logEvent("detail_click_report_mask_status", null)
                dialog.dismiss()
                showBuyerDialog()
            }
            .bind(this)
    }

    private fun showNumberTicketDialog() {
        analytics.logEvent("detail_report_number_ticket_dialog", null)

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_number_ticket, null, false)
        val radioYes = view.findViewById<RadioButton>(R.id.radioYes)
        val radioNo = view.findViewById<RadioButton>(R.id.radioNo)

        val dialog = AlertDialog.Builder(this)
            .setTitle("是否採用號碼牌制度？")
            .setView(view)
            .setPositiveButton(getString(R.string.dismiss)) { _, _ ->
                analytics.logEvent("detail_dismiss_report_use_number_ticket", null)
            }
            .show()

        RxView.clicks(radioYes)
            .throttleClick()
            .subscribe {
                dialog.dismiss()
                showConfirmNumberTicketDialog(true)
            }
            .bind(this)

        RxView.clicks(radioNo)
            .throttleClick()
            .subscribe {
                dialog.dismiss()
                showConfirmNumberTicketDialog(false)
            }
            .bind(this)
    }


    private fun showConfirmNumberTicketDialog(isNumberTicket: Boolean) {
        analytics.logEvent("detail_show_number_ticket_confirm_dialog", null)
        val option = if (isNumberTicket) getString(R.string.option_number_ticket)
        else getString(R.string.option_not_number_ticket)

        AlertDialog.Builder(this)
            .setMessage("即將送出選項：$option")
            .setPositiveButton("送出") { _, _ ->
                viewModel.reportNumberTicket(info.id, isNumberTicket)
                analytics.logEvent("detail_submit_report_use_number_ticket", null)
            }
            .setNegativeButton("取消") { _, _ ->
                analytics.logEvent("detail_cancel_report_use_number_ticket", null)
            }
            .show()
    }

    private fun showBuyerDialog() {
        analytics.logEvent("detail_show_buyer_dialog", null)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_buyer, null, false)
        val radioSufficient = view.findViewById<RadioButton>(R.id.radioSufficient)
        val radioWarning = view.findViewById<RadioButton>(R.id.radioWarning)
        val radioEmpty = view.findViewById<RadioButton>(R.id.radioEmpty)

        val dialog = AlertDialog.Builder(this)
            .setTitle("還有成人口罩嗎？")
            .setView(view)
            .setPositiveButton(getString(R.string.dismiss)) { _, _ ->
                analytics.logEvent(
                    "detail_dismiss_buyer_dialog",
                    null
                )
            }
            .show()

        RxView.clicks(radioSufficient)
            .throttleClick()
            .subscribe {
                dialog.dismiss()
                showConfirmMaskDialog(Status.Sufficient)
            }
            .bind(this)

        RxView.clicks(radioWarning)
            .throttleClick()
            .subscribe {
                dialog.dismiss()
                showConfirmMaskDialog(Status.Warning)
            }
            .bind(this)

        RxView.clicks(radioEmpty)
            .throttleClick()
            .subscribe {
                dialog.dismiss()
                showConfirmMaskDialog(Status.Empty)
            }
            .bind(this)
    }

    private fun showConfirmMaskDialog(status: Status) {
        analytics.logEvent("detail_show_mask_status_confirm_dialog", null)
        val option = when (status) {
            Status.Empty -> getString(R.string.option_empty)
            Status.Warning -> getString(R.string.option_warning)
            Status.Sufficient -> getString(R.string.option_sufficient)
        }

        AlertDialog.Builder(this)
            .setMessage("即將送出選項：$option")
            .setPositiveButton("送出") { _, _ ->
                viewModel.reportMaskStatus(info.id, status)
                analytics.logEvent("detail_submit_report_mask_status", null)
            }
            .setNegativeButton("取消") { _, _ ->
                analytics.logEvent("detail_cancel_report_mask_status", null)
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

