package com.jarvislin.drugstores.page.testing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jarvislin.domain.entity.RapidTestLocation
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.extension.*
import com.jarvislin.drugstores.widget.TitleTextView
import org.jetbrains.anko.toast

class LocationAdapter : RecyclerView.Adapter<LocationViewHolder>() {

    private val locations = ArrayList<RapidTestLocation>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location, parent, false)
            .let { LocationViewHolder(it) }
    }

    override fun getItemCount() = locations.size

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        locations[position].let { holder.bind(it) }
    }

    fun update(data: List<RapidTestLocation>) {
        locations.clear()
        locations.addAll(data)
        notifyDataSetChanged()
    }
}

class LocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val context = itemView.context
    private val textName = itemView.findViewById<TextView>(R.id.textName)
    private val textReserve = itemView.findViewById<TextView>(R.id.textReserve)
    private val textSource = itemView.findViewById<TextView>(R.id.textSource)
    private val textNavigate = itemView.findViewById<TextView>(R.id.textNavigate)
    private val textShare = itemView.findViewById<TextView>(R.id.textShare)
    private val titleQuota = itemView.findViewById<TitleTextView>(R.id.titleQuota)
    private val titleOpening = itemView.findViewById<TitleTextView>(R.id.titleOpening)
    private val titleMethod = itemView.findViewById<TitleTextView>(R.id.titleMethod)
    private val titlePhone = itemView.findViewById<TitleTextView>(R.id.titlePhone)
    private val titleAddress = itemView.findViewById<TitleTextView>(R.id.titleAddress)
    private val titleLocationDescription =
        itemView.findViewById<TitleTextView>(R.id.titleLocationDescription)
    private val titleLimit = itemView.findViewById<TitleTextView>(R.id.titleLimit)
    private val titleNote = itemView.findViewById<TitleTextView>(R.id.titleNote)

    fun bind(location: RapidTestLocation) {
        handleVisibility(location.name, textName)
        handleVisibility(location.quotaOfPeople, titleQuota)
        handleVisibility(location.openingHours, titleOpening)
        handleVisibility(location.method, titleMethod)
        handleVisibility(location.phone, titlePhone)
        handleVisibility(location.address, titleAddress)
        handleVisibility(location.locationDescription, titleLocationDescription)
        handleVisibility(location.limit, titleLimit)
        handleVisibility(location.note, titleNote)

        // handle clicks
        if (location.reservationUrl == null) {
            textReserve.hide()
        } else {
            textReserve.show()
            textReserve.setOnClickListener { context.openWeb(location.reservationUrl!!) }
        }

        if (location.dataSourceUrl == null) {
            textSource.hide()
        } else {
            textSource.show()
            textSource.setOnClickListener { context.openWeb(location.dataSourceUrl!!) }
        }

        if (location.hasLocation()) {
            textNavigate.show()
            textNavigate.setOnClickListener {
                location.getLocation().let { context.openMap(it.first, it.second) }
            }
        }

        textShare.setOnClickListener { context.shareText("防疫資訊", location.getShareText()) }

        location.phone?.let { phone ->
            titlePhone.setOnClickListener {
                context.call(phone)
            }
        }

        location.address?.let { address ->
            titleAddress.setOnClickListener {
                context.toast("已複製地址到剪貼簿")
                context.copyToClipboard(address)
            }
        }
    }

    private fun handleVisibility(data: String?, view: View) {
        if (data == null) {
            view.hide()
        } else {
            when (view) {
                is TextView -> view.text = data
                is TitleTextView -> view.setContent(data)
            }
            view.show()
        }
    }
}