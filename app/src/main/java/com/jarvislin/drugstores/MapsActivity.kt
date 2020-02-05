package com.jarvislin.drugstores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.InputStream
import java.nio.charset.Charset


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val DEFAULT_LAT = 25.0393868
        private const val DEFAULT_LNG = 121.5087163
    }

    private lateinit var map: GoogleMap
    private lateinit var stores: List<Drugstore>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val taipei = LatLng(DEFAULT_LAT, DEFAULT_LNG)

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(taipei, 18f))

        stores = getDrugstores()
        stores.forEach {
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(it.lat, it.lng))
                    .title(it.name)
                    .snippet(it.id)
            )
        }


        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoContents(p0: Marker?): View {
                val view = TextView(this@MapsActivity)
                view.text = "getInfoContents"
                return view
            }

            override fun getInfoWindow(p0: Marker?): View {
                val store = stores.first { it.id == p0!!.snippet }
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
        })
    }

    private fun getDrugstores(): List<Drugstore> {
        return try {
            assets.open("info.json").use { stream ->
                val size: Int = stream.available()
                val buffer = ByteArray(size)
                stream.read(buffer)
                String(buffer, Charset.forName("UTF-8")).toList()
            }
        } catch (ex: Exception) {
            emptyList()
        }
    }
}

fun <T> String.toObject(clazz: Class<T>): T {
    return Gson().fromJson(this, clazz)
}

inline fun <reified T> String.toList(): T {
    val gson = GsonBuilder().create()
    val typeToken = object : TypeToken<T>() {}.type
    return gson.fromJson<T>(this, typeToken)
}

class Drugstore(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double,
    @SerializedName("address")
    val address: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("note")
    val note: String
)