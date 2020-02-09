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
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.base.BaseActivity
import com.jarvislin.drugstores.extension.bind
import com.jarvislin.drugstores.extension.throttleClick
import com.jarvislin.drugstores.extension.toBackground
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import org.jetbrains.anko.toast
import java.util.*
import kotlin.collections.ArrayList

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
        private val textAddress: TextView = itemView.findViewById(R.id.textAddress)
        private val textPhone: TextView = itemView.findViewById(R.id.textPhone)
        private val imagePhone: ImageView = itemView.findViewById(R.id.imagePhone)
        private val imageLocation: ImageView = itemView.findViewById(R.id.imageLocation)

        fun bind(drugstoreInfo: DrugstoreInfo) {
            layoutAdult.background = drugstoreInfo.openData.adultMaskAmount.toBackground()
            layoutChild.background = drugstoreInfo.openData.childMaskAmount.toBackground()

            textAdultAmount.text = drugstoreInfo.openData.adultMaskAmount.toString()
            textChildAmount.text = drugstoreInfo.openData.childMaskAmount.toString()

            textName.text = drugstoreInfo.drugstore.name
            textAddress.text = drugstoreInfo.drugstore.address
            textPhone.text = "電話  " + drugstoreInfo.drugstore.phone

            RxView.clicks(imagePhone)
                .throttleClick()
                .subscribe { showPhoneDialog(drugstoreInfo) }
                .addTo(compositeDisposable)

            RxView.clicks(imageLocation)
                .throttleClick()
                .subscribe { openMap(drugstoreInfo) }
                .addTo(compositeDisposable)
        }

        private fun showInfoDialog() {
            AlertDialog.Builder(itemView.context)
                .setTitle("身分證字號末碼")
                .setMessage("雙號者（0、2、4、6、8）於每週二、四、六購買；單號者（1、3、5、7、9）可於每週一、三、五購買；週日則開放全民皆可購買。")
                .setPositiveButton("關閉") { _, _ -> }
                .show()
        }

        private fun showPhoneDialog(drugstoreInfo: DrugstoreInfo) {
            AlertDialog.Builder(itemView.context)
                .setTitle("請詳讀以下資訊")
                .setMessage("為避免增加藥局作業量，建議必要時再撥打。撥打前也請確認此時為營業時間。")
                .setNegativeButton("關閉") { _, _ -> }
                .setPositiveButton("撥打") { _, _ ->
                    Intent(Intent.ACTION_DIAL).apply {
                        try {
                            data = Uri.parse("tel:${drugstoreInfo.drugstore.phone}")
                            itemView.context.startActivity(this)
                        } catch (ex: Exception) {
                            itemView.context.toast("無法開啟電話簿")
                        }
                    }
                }
                .show()
        }

        private fun openMap(drugstoreInfo: DrugstoreInfo) {
            val gmmIntentUri =
                Uri.parse(
                    "geo:${drugstoreInfo.drugstore.lat},${drugstoreInfo.drugstore.lng}?q=" + Uri.encode(
                        "${drugstoreInfo.drugstore.name}"
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