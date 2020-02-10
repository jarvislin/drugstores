package com.jarvislin.drugstores.page.search

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.domain.entity.EntireInfo
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.extension.throttleClick
import com.jarvislin.drugstores.extension.toBackground
import com.jarvislin.drugstores.extension.toUpdateWording
import com.jarvislin.drugstores.page.detail.DetailActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import org.jetbrains.anko.toast
import kotlin.collections.ArrayList

class SearchAdapter : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    private val info = ArrayList<EntireInfo>()
    private val compositeDisposable = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_info, parent, false)
        return SearchViewHolder(view)
    }

    override fun getItemCount(): Int = info.size

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(info[position])
    }

    fun update(info: List<EntireInfo>) {
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
        private val textPhone: TextView = itemView.findViewById(R.id.textPhone)
        private val imagePhone: ImageView = itemView.findViewById(R.id.imagePhone)
        private val imageLocation: ImageView = itemView.findViewById(R.id.imageLocation)
        private val layoutCard: View = itemView.findViewById(R.id.layoutCard)

        fun bind(drugstoreInfo: EntireInfo) {
            layoutAdult.background = drugstoreInfo.getAdultMaskAmount().toBackground()
            layoutChild.background = drugstoreInfo.getChildMaskAmount().toBackground()

            textAdultAmount.text = drugstoreInfo.getAdultMaskAmount().toString()
            textChildAmount.text = drugstoreInfo.getChildMaskAmount().toString()

            textName.text = drugstoreInfo.getName()
            textAddress.text = drugstoreInfo.getAddress()
            textPhone.text = "電話  " + drugstoreInfo.getPhone()
            textUpdate.text = drugstoreInfo.getUpdateAt().toUpdateWording()

            RxView.clicks(imagePhone)
                .throttleClick()
                .subscribe { showPhoneDialog(drugstoreInfo) }
                .addTo(compositeDisposable)

            RxView.clicks(imageLocation)
                .throttleClick()
                .subscribe { openMap(drugstoreInfo) }
                .addTo(compositeDisposable)

            RxView.clicks(layoutCard)
                .throttleClick()
                .subscribe { DetailActivity.start(itemView.context, drugstoreInfo) }
                .addTo(compositeDisposable)
        }

        private fun showPhoneDialog(drugstoreInfo: EntireInfo) {
            AlertDialog.Builder(itemView.context)
                .setTitle(itemView.context.getString(R.string.dial_title))
                .setMessage(itemView.context.getString(R.string.dial_message))
                .setNegativeButton(itemView.context.getString(R.string.dial)) { _, _ -> }
                .setPositiveButton(itemView.context.getString(R.string.dismiss)) { _, _ ->
                    Intent(Intent.ACTION_DIAL).apply {
                        try {
                            data = Uri.parse("tel:${drugstoreInfo.getPhone()}")
                            itemView.context.startActivity(this)
                        } catch (ex: Exception) {
                            itemView.context.toast(itemView.context.getString(R.string.dial_error))
                        }
                    }
                }
                .show()
        }

        private fun openMap(drugstoreInfo: EntireInfo) {
            val gmmIntentUri =
                Uri.parse(
                    "geo:${drugstoreInfo.getLng()},${drugstoreInfo.getLng()}?q=" + Uri.encode(
                        drugstoreInfo.getName()
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