package com.jarvislin.drugstores.page.search

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakewharton.rxbinding2.view.RxView
import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.extension.*
import com.jarvislin.drugstores.page.detail.DetailActivity
import com.jarvislin.drugstores.widget.ModelConverter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

class SearchAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var location: Location? = null
    private val info = ArrayList<DrugstoreInfo>()
    private val compositeDisposable = CompositeDisposable()
    private val modelConverter by lazy { ModelConverter() }
    private lateinit var analytics: FirebaseAnalytics

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        analytics = FirebaseAnalytics.getInstance(recyclerView.context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_info, parent, false)
        return SearchViewHolder(view)
    }

    override fun getItemCount(): Int = info.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SearchViewHolder -> holder.bind(info[position])
        }

    }

    fun update(info: List<DrugstoreInfo>, location: Location?) {
        this.info.clear()
        this.info.addAll(info)
        this.location = location
        notifyDataSetChanged()
    }

    fun release() {
        compositeDisposable.clear()
    }

    inner class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val layoutAdult: View = itemView.findViewById(R.id.layoutAdult)
        private val layoutChild: View = itemView.findViewById(R.id.layoutChild)
        private val textAdultAmount: TextView = itemView.findViewById(R.id.textAdultAmount)
        private val textChildAmount: TextView = itemView.findViewById(R.id.textChildAmount)
        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textUpdate: TextView = itemView.findViewById(R.id.textUpdate)
        private val textDistance: TextView = itemView.findViewById(R.id.textDistance)
        private val textAddress: TextView = itemView.findViewById(R.id.textAddress)
        private val textNote: TextView = itemView.findViewById(R.id.textNote)
        private val layoutCard: View = itemView.findViewById(R.id.layoutCard)
        private val textShare: View = itemView.findViewById(R.id.textShare)
        private val textNavigate: View = itemView.findViewById(R.id.textNavigate)

        fun bind(drugstoreInfo: DrugstoreInfo) {
            layoutAdult.background = modelConverter.from(drugstoreInfo).toAdultMaskBackground()
            layoutChild.background = modelConverter.from(drugstoreInfo).toChildMaskBackground()

            textAdultAmount.text = modelConverter.from(drugstoreInfo).toAdultMaskAmountWording()
            textChildAmount.text = modelConverter.from(drugstoreInfo).toChildMaskAmountWording()

            textName.text = drugstoreInfo.name
            textAddress.text = drugstoreInfo.address
            textUpdate.text = modelConverter.from(drugstoreInfo).toUpdateWording()
            textDistance.text = modelConverter.from(drugstoreInfo).toDistance(location)
            drugstoreInfo.note.trim().let {
                if (it.isNotEmpty() && it != "-") {
                    textNote.text = drugstoreInfo.note
                    textNote.show()
                } else {
                    textNote.hide()
                }
            }

            RxView.clicks(layoutCard)
                .throttleClick()
                .subscribe {
                    analytics.logEvent("search_click_card_info", null)
                    DetailActivity.start(itemView.context, drugstoreInfo, location) }
                .addTo(compositeDisposable)

            RxView.clicks(textShare)
                .throttleClick()
                .subscribe {
                    analytics.logEvent("search_click_share", null)
                    itemView.context.shareText(
                        "口罩資訊地圖",
                        modelConverter.from(drugstoreInfo).toShareContentText()
                    )
                }
                .addTo(compositeDisposable)

            RxView.clicks(textNavigate)
                .throttleClick()
                .subscribe {
                    analytics.logEvent("search_click_navigate", null)
                    itemView.context.openMap(
                        drugstoreInfo.lat,
                        drugstoreInfo.lng
                    )
                }
                .addTo(compositeDisposable)
        }
    }
}

