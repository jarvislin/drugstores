package com.jarvislin.drugstores

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.jarvislin.domain.entity.Drugstore
import com.jarvislin.drugstores.base.BaseActivity
import com.jarvislin.drugstores.extension.bind
import com.jarvislin.drugstores.extension.tint
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
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
    private var myLocation: LatLng? = null

    companion object {
        private const val DELAY_MILLISECONDS = 100L
        private const val REQUEST_LOCATION = 5566
        private const val DEFAULT_ZOOM_LEVEL = 15f
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

        viewModel.initData()
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

        viewModel.isInitialized.observe(this, Observer { done ->
            if (done) {
                viewModel.fetchNearDrugstoreInfo(positionLatitude, positionLongitude)
                viewModel.stores.observe(this, Observer { addMarkers(it) })
            }
        })

        map.uiSettings.isMapToolbarEnabled = false

        map.setOnInfoWindowClickListener { /** todo **/ }
        map.setOnCameraIdleListener {
            viewModel.fetchNearDrugstoreInfo(positionLatitude, positionLongitude)
        }

        map.setOnCameraMoveStartedListener { updateFabColor(R.color.secondaryIcons) }

        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoContents(marker: Marker): View? {
                bindView(infoWindowView, cacheManager.getDrugstore(marker))
                return infoWindowView
            }

            override fun getInfoWindow(p0: Marker): View? {
                return null
            }
        })

        fab.setOnClickListener { checkPermission() }
    }

    private fun updateFabColor(colorId: Int) {
        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_my_location)
        drawable?.tint(ContextCompat.getColor(this, colorId))
        fab.setImageDrawable(drawable)
    }

    private fun bindView(view: View, store: Drugstore) {
        view.findViewById<TextView>(R.id.textName).text = store.name
        view.findViewById<TextView>(R.id.textPhone).text = store.phone
        view.findViewById<TextView>(R.id.textAddress).text = store.address
        if (store.note.isNotEmpty()) {
            view.findViewById<TextView>(R.id.textNote).text = store.note
            view.findViewById<TextView>(R.id.textNote).visibility = VISIBLE
        }
    }

    private fun addMarkers(drugstores: List<Drugstore>) {
        Flowable.fromIterable(drugstores)
            .subscribeOn(Schedulers.computation())
            .filter { cacheManager.isCached(it).not() }
            .delay(DELAY_MILLISECONDS, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ drugstore ->
                LatLng(drugstore.lat, drugstore.lng)
                    .let { map.addMarker(MarkerOptions().position(it).snippet(drugstore.id)) }
                    .also { cacheManager.add(drugstore, it) }
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
                , REQUEST_LOCATION
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
}

fun Context.hasPermission(vararg permission: String): Boolean {
    return permission.all {
        ActivityCompat.checkSelfPermission(
            this,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }
}