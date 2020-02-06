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
import com.jarvislin.petme.base.BaseActivity
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.concurrent.TimeUnit


class MapsActivity : BaseActivity(), OnMapReadyCallback {
    override val viewModel: MapViewModel by viewModel()

    companion object {
        private const val DEFAULT_LAT = 25.0393868
        private const val DEFAULT_LNG = 121.5087163
    }

    private lateinit var map: GoogleMap

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
                viewModel.fetchNearStores(
                    map.cameraPosition.target.latitude,
                    map.cameraPosition.target.longitude

                )
                viewModel.stores.observe(this, Observer {
                    Flowable.fromIterable(it)
                        .subscribeOn(Schedulers.computation())
                        .delay(50, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            map.addMarker(
                                MarkerOptions()
                                    .position(LatLng(it.lat, it.lng))
                                    .title(it.name)
                                    .snippet(it.id)
                            )
                        }, { Timber.e(it) })
                })
            }

        })

        val taipei = LatLng(DEFAULT_LAT, DEFAULT_LNG)

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(taipei, 15f))
        map.setOnCameraMoveStartedListener { map.clear() }
        map.setOnInfoWindowClickListener {  }

        map.setOnCameraIdleListener {
            viewModel.fetchNearStores(
                map.cameraPosition.target.latitude,
                map.cameraPosition.target.longitude
            )
        }


        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoContents(marker: Marker): View {
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
}

