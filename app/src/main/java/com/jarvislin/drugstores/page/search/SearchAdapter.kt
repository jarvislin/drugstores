package com.jarvislin.drugstores.page.search

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.extension.throttleClick
import com.jarvislin.drugstores.extension.toBackground
import com.jarvislin.drugstores.extension.toUpdateWording
import com.jarvislin.drugstores.page.detail.DetailActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

class SearchAdapter : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    private val info = ArrayList<DrugstoreInfo>()
    private val compositeDisposable = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_info, parent, false)
        return SearchViewHolder(view)
    }

    override fun getItemCount(): Int = info.size

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(info[position])
    }

    fun update(info: List<DrugstoreInfo>) {
        this.info.clear()
        this.info.addAll(info)
        notifyDataSetChanged()
    }


    inner class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val layoutAdult: View = itemView.findViewById(R.id.layoutAdult)
        private val layoutChild: View = itemView.findViewById(R.id.layoutChild)
        private val textAdultAmount: TextView = itemView.findViewById(R.id.textAdultAmount)
        private val textChildAmount: TextView = itemView.findViewById(R.id.textChildAmount)
        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textUpdate: TextView = itemView.findViewById(R.id.textUpdate)
        private val textAddress: TextView = itemView.findViewById(R.id.textAddress)
        private val layoutCard: View = itemView.findViewById(R.id.layoutCard)

        fun bind(drugstoreInfo: DrugstoreInfo) {
            layoutAdult.background = drugstoreInfo.adultMaskAmount.toBackground()
            layoutChild.background = drugstoreInfo.childMaskAmount.toBackground()

            textAdultAmount.text = "成人 " + drugstoreInfo.adultMaskAmount.toString()
            textChildAmount.text = "兒童 " + drugstoreInfo.childMaskAmount.toString()

            textName.text = drugstoreInfo.name
            textAddress.text = drugstoreInfo.address
            textUpdate.text = drugstoreInfo.updateAt.toUpdateWording()

            RxView.clicks(layoutCard)
                .throttleClick()
                .subscribe { DetailActivity.start(itemView.context, drugstoreInfo) }
                .addTo(compositeDisposable)
        }

        private fun openMap(drugstoreInfo: DrugstoreInfo) {
            val gmmIntentUri =
                Uri.parse(
                    "geo:${drugstoreInfo.lat},${drugstoreInfo.lng}?q=" + Uri.encode(
                        drugstoreInfo.name
                    )
                )
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            if (mapIntent.resolveActivity(itemView.context.packageManager) != null) {
                itemView.context.startActivity(mapIntent)
            }
        }
    }

    fun release() {
        compositeDisposable.clear()
    }
}