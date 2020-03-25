package com.jarvislin.drugstores.page.detail

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
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
import com.jarvislin.drugstores.BuildConfig
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
        viewModel.usesNumberTicket.observe(this, Observer { cardNumberTicket.show() })
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

        textDateType.text = modelConverter.from(info).toDateType()
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

        viewModel.fetchRecords(info.id)
        viewModel.records.observe(this, Observer {
            populateChartView(it)
            Timber.e(it.toJson())
        })

        RxView.clicks(textInfo)
            .throttleClick()
            .subscribe {
                analytics.logEvent("detail_click_info", null)
                showInfoDialog()
            }
            .bind(this)

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
                    "口罩資訊地圖",
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

    private fun showRecordsDialog() {
        analytics.logEvent("detail_show_records_dialog", null)
        AlertDialog.Builder(this)
            .setTitle("庫存量說明")
            .setMessage("收錄早上七點至晚上十點的存量資料，用來推測約略的口罩販售時間；圖表支援水平及垂直的兩指縮放功能。")
            .setPositiveButton(getString(R.string.dismiss)) { _, _ ->
                analytics.logEvent(
                    "detail_dismiss_records_dialog",
                    null
                )
            }
            .show()
    }

    private fun populateChartView(records: List<MaskRecord>) {

        val rightAxis = chartView.axisRight
        rightAxis.setDrawAxisLine(false)
        rightAxis.setDrawGridLines(false)
        rightAxis.setDrawLabels(false)

        val leftAxis = chartView.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.axisMinimum = 0f
        leftAxis.textSize = 14.5f
        leftAxis.granularity = 1f
        leftAxis.textColor = ContextCompat.getColor(this, R.color.primaryText)

        val xAxis = chartView.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.labelRotationAngle = -60f
        xAxis.textSize = 13.5f
        xAxis.textColor = ContextCompat.getColor(this, R.color.secondaryText)
        xAxis.granularity = 1f
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        val adults = ArrayList<Entry>()
        val children = ArrayList<Entry>()
        val dates = ArrayList<Date>()

        records.forEachIndexed { index, maskRecord ->
            adults.add(Entry(index.toFloat(), maskRecord.adultAmount.toFloat()))
            children.add(Entry(index.toFloat(), maskRecord.childAmount.toFloat()))
            dates.add(maskRecord.date)
        }

        val adultsDataSet =
            initLineDataSetSettings(LineDataSet(adults, "成人"), ColorTemplate.JOYFUL_COLORS[0])
        val childrenDataSet =
            initLineDataSetSettings(LineDataSet(children, "兒童"), ColorTemplate.JOYFUL_COLORS[3])

        chartView.data = LineData(adultsDataSet, childrenDataSet)
        chartView.setExtraOffsets(12f, 0f, 24f, 16f)
        chartView.xAxis.valueFormatter = ChartDateFormatter(dates, "HH:mm")
        chartView.description.text = ""
        chartView.legend.apply {
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            yOffset = 12f
            formSize = 14f
            textSize = 14f
            form = Legend.LegendForm.LINE
        }

        chartView.invalidate()
        cardChart.show()
    }

    private fun initLineDataSetSettings(lineDataSet: LineDataSet, color: Int): LineDataSet {
        lineDataSet.color = color
        lineDataSet.lineWidth = 1f
        lineDataSet.setCircleColor(color)
        lineDataSet.circleRadius = 1.5f
        lineDataSet.fillColor = color
        lineDataSet.mode = LineDataSet.Mode.LINEAR
        lineDataSet.setDrawValues(false)
        lineDataSet.setDrawCircles(false)
        lineDataSet.valueTextSize = 14f
        lineDataSet.valueTextColor = color
        lineDataSet.axisDependency = YAxis.AxisDependency.LEFT

        return lineDataSet
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
                showNumberDialog()
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

    private fun showNumberDialog() {
        analytics.logEvent("detail_report_number_ticket_dialog", null)
        AlertDialog.Builder(this)
            .setTitle("採用號碼牌制度？")
            .setMessage("即將回報此藥局採用號碼牌制度")
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                analytics.logEvent("detail_report_use_number_ticket_cancel", null)
            }
            .setPositiveButton(getString(R.string.submit)) { _, _ ->
                viewModel.reportNumberTicket(info.id)
                analytics.logEvent("detail_report_use_number_ticket_success", null)
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

    private fun showInfoDialog() {
        analytics.logEvent("detail_show_info_dialog", null)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.id_note_title))
            .setMessage(getString(R.string.id_note_message))
            .setPositiveButton(getString(R.string.dismiss)) { _, _ ->
                analytics.logEvent("detail_dismiss_info_dialog", null)
            }
            .show()
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

class ChartDateFormatter(
    private val dates: List<Date>,
    private val pattern: String = "MM.dd HH:mm"
) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return value.toInt().let {
            if (it >= 0 && it < dates.size)
                SimpleDateFormat(pattern, Locale.getDefault()).format(dates[it])
            else {
                FirebaseCrashlytics.getInstance().log("array size: ${dates.size}, value: $it")
                ""
            }
        }
    }
}
