package com.jarvislin.drugstores.page.map

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.domain.entity.Progress
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.base.BaseActivity
import com.jarvislin.drugstores.extension.bind
import com.jarvislin.drugstores.extension.getBitmap
import com.jarvislin.drugstores.extension.tint
import com.jarvislin.drugstores.extension.toBackground
import com.jarvislin.drugstores.page.detail.DetailActivity
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_maps.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.concurrent.TimeUnit


class MapsActivity : BaseActivity(), OnMapReadyCallback {

    override val viewModel: MapViewModel by viewModel()
    private val cacheManager: MarkerCacheManager by inject()
    private val infoWindowView by lazy {
        LayoutInflater.from(this).inflate(R.layout.window, null, false)
    }

    private val positionLatitude get() = map.cameraPosition.target.latitude
    private val positionLongitude get() = map.cameraPosition.target.longitude
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var dots: Disposable
    private var myLocation: LatLng? = null
    private var lastClickedMarker: Marker? = null

    companion object {
        private const val DELAY_MILLISECONDS = 100L
        private const val REQUEST_LOCATION = 5566
        private const val DEFAULT_ZOOM_LEVEL = 15f
        fun start(context: Context) {
            Intent(context, MapsActivity::class.java).apply {
                context.startActivity(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { viewModel.saveLastLocation(it) }
        requestLocation()

        // download open data
        viewModel.downloadProgress.observe(this, Observer { progress ->
            if (progress.bytesDownloaded == 0L) {
                dots.dispose()
                textProgressHint.text = ""
            }
            progressBar.progress = (100 * progress.bytesDownloaded / progress.contentLength).toInt()
            if (progress is Progress.Done) {
                viewModel.handleLatestOpenData(progress.file)
                map.clear()
                layoutDownloadHint.animate().alpha(0f).start()
                viewModel.countDown()
            }
        })

        viewModel.autoUpdate.observe(this, Observer {
            startDownload()
        })

        startDownload()

        // init asset data
        viewModel.initDrugstores()
    }

    private fun startDownload() {
        progressBar.progress = 0
        layoutDownloadHint.animate().alpha(1f).start()
        dots = Flowable.interval(300, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.computation())
            .take(100)
            .map {
                when (it % 6) {
                    1L -> "."
                    2L -> ".."
                    3L -> "..."
                    4L -> ".."
                    5L -> "."
                    else -> ""
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                textProgressHint.text = "準備連線" + it
            }, { Timber.e(it) })
            .addTo(compositeDisposable)

        viewModel.fetchOpenData()
    }

    private fun requestLocation() {
        fusedLocationClient.requestLocationUpdates(
            LocationRequest().setInterval(30_000),
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult?) {
                    super.onLocationResult(result)
                    result?.let {
                        it.locations.firstOrNull()?.let {
                            myLocation = LatLng(it.latitude, it.longitude)
                            viewModel.saveLastLocation(it)
                        }
                    }
                }
            },
            Looper.getMainLooper()
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        if ((hasPermission(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))) {
            enableMyLocation()
        }

        moveTo(myLocation ?: viewModel.getLastLocation())

        viewModel.allDataPrepared.observe(this, Observer { done ->
            if (done) {
                viewModel.fetchNearDrugstoreInfo(positionLatitude, positionLongitude)
                viewModel.drugstoreInfo.observe(this, Observer { addMarkers(it) })
            }
        })

        map.uiSettings.isMapToolbarEnabled = false

        map.setOnInfoWindowClickListener {
            DetailActivity.start(
                this,
                cacheManager.getDrugstoreInfo(it)
            )
        }
        map.setOnCameraIdleListener {
            viewModel.fetchNearDrugstoreInfo(positionLatitude, positionLongitude)
        }

        map.setOnCameraMoveStartedListener { updateFabColor(R.color.secondaryIcons) }

        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoContents(marker: Marker): View? {
                lastClickedMarker = marker
                bindView(infoWindowView, cacheManager.getDrugstoreInfo(marker))
                return infoWindowView
            }

            override fun getInfoWindow(p0: Marker): View? {
                return null
            }
        })

        fab.setOnClickListener { checkPermission() }
    }

    private fun updateFabColor(colorId: Int) {
        val drawable = ContextCompat.getDrawable(
            this,
            R.drawable.ic_my_location
        )
        drawable?.tint(ContextCompat.getColor(this, colorId))
        fab.setImageDrawable(drawable)
    }

    private fun bindView(view: View, info: DrugstoreInfo) {
        view.findViewById<TextView>(R.id.textName).text = info.drugstore.name
        view.findViewById<TextView>(R.id.textUpdate).text = info.openData.getUpdateText()
        view.findViewById<TextView>(R.id.textAdultAmount).text =
            "成人：" + info.openData.adultMaskAmount.toString()
        view.findViewById<TextView>(R.id.textChildAmount).text =
            "兒童：" + info.openData.childMaskAmount.toString()
        view.findViewById<View>(R.id.layoutAdult).background =
            info.openData.adultMaskAmount.toBackground()
        view.findViewById<View>(R.id.layoutChild).background =
            info.openData.childMaskAmount.toBackground()
    }


    private fun addMarkers(drugstores: List<DrugstoreInfo>) {
        Flowable.fromIterable(drugstores)
            .subscribeOn(Schedulers.computation())
            .filter { cacheManager.isCached(it).not() }
            .map {
                val markerInfo =
                    MarkerInfoManager.getMarkerInfo(
                        it.openData.adultMaskAmount
                    )

                val option = MarkerOptions()
                    .position(LatLng(it.drugstore.lat, it.drugstore.lng))
                    .snippet(it.drugstore.id)
                    .zIndex(markerInfo.zIndex)

                ContextCompat.getDrawable(this, markerInfo.drawableId)?.getBitmap()
                    .let { option.icon(BitmapDescriptorFactory.fromBitmap(it)) }

                Pair(it, option)
            }
            .delay(DELAY_MILLISECONDS, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ pair ->
                pair.let { map.addMarker(it.second) }
                    .also { cacheManager.add(pair.first, it) }
            }, { Timber.e(it) })
            .bind(this)
    }


    private fun checkPermission() {
        if (hasPermission(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) {
            enableMyLocation()
            animateTo(
                myLocation ?: viewModel.getLastLocation(),
                callback = { updateFabColor(R.color.colorAccent) })
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    ACCESS_COARSE_LOCATION,
                    ACCESS_FINE_LOCATION
                )
                ,
                REQUEST_LOCATION
            )
        }
    }


    private fun moveTo(latLng: LatLng) {
        CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL).let {
            map.moveCamera(it)
        }
    }

    private fun animateTo(latLng: LatLng, callback: () -> Unit = {}) {
        CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL).let {
            map.animateCamera(it, object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    callback.invoke()
                }

                override fun onCancel() {}
            })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (hasPermission(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) {
            enableMyLocation()
            requestLocation()
        }
    }

    private fun enableMyLocation() {
        map.uiSettings.isMyLocationButtonEnabled = false
        map.isMyLocationEnabled = true
    }

    override fun onBackPressed() {
        if (lastClickedMarker?.isInfoWindowShown == true) {
            lastClickedMarker?.hideInfoWindow()
        } else {
            super.onBackPressed()
        }
    }
}

fun Context.hasPermission(vararg permission: String): Boolean {
    return permission.all {
        ActivityCompat.checkSelfPermission(
            this,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }
}