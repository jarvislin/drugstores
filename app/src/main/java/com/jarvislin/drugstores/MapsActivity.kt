package com.jarvislin.drugstores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.lifecycle.Observer
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
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.concurrent.TimeUnit


class MapsActivity : BaseActivity(), OnMapReadyCallback {
    override val viewModel: MapViewModel by viewModel()
    private val cacheManager: MarkerCacheManager by inject()

    companion object {
        private const val DEFAULT_LAT = 25.0393868
        private const val DEFAULT_LNG = 121.5087163
        private const val DELAY_MILLISECONDS = 100L
    }

    private lateinit var map: GoogleMap
    private val positionLatitude
        get() = map.cameraPosition.target.latitude

    private val positionLongitude
        get() = map.cameraPosition.target.longitude

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewModel.initData()
        viewModel.fetchOpenData()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        viewModel.isInitialized.observe(this, Observer { done ->
            if (done) {
                viewModel.fetchNearDrugstoreInfo(positionLatitude, positionLongitude)
                viewModel.stores.observe(this, Observer { addMarkers(it) })
            }
        })

        val taipei = LatLng(DEFAULT_LAT, DEFAULT_LNG)

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(taipei, 15f))
        map.setOnInfoWindowClickListener { /** todo **/ }

        map.setOnCameraIdleListener {
            viewModel.fetchNearDrugstoreInfo(positionLatitude, positionLongitude)
        }

        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoContents(marker: Marker): View? {
                val store = viewModel.stores.value!!.first { it.id == marker.snippet }
                val view =
                    LayoutInflater.from(this@MapsActivity).inflate(R.layout.window, null, false)
                view.findViewById<TextView>(R.id.textName).text = store.name
                view.findViewById<TextView>(R.id.textPhone).text = store.phone
                view.findViewById<TextView>(R.id.textAddress).text = store.address
                if (store.note.isNotEmpty()) {
                    view.findViewById<TextView>(R.id.textNote).text = store.note
                    view.findViewById<TextView>(R.id.textNote).visibility = VISIBLE
                }
                return view
            }

            override fun getInfoWindow(p0: Marker): View? {
                return null
            }
        })
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
}

