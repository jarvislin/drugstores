package com.jarvislin.drugstores.page.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.extension.*
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
        private val textNote: TextView = itemView.findViewById(R.id.textNote)
        private val layoutCard: View = itemView.findViewById(R.id.layoutCard)
        private val textShare: View = itemView.findViewById(R.id.textShare)
        private val textNavigate: View = itemView.findViewById(R.id.textNavigate)

        fun bind(drugstoreInfo: DrugstoreInfo) {
            layoutAdult.background = drugstoreInfo.adultMaskAmount.toBackground()
            layoutChild.background = drugstoreInfo.childMaskAmount.toBackground()

            textAdultAmount.text = "成人 " + drugstoreInfo.adultMaskAmount.toString()
            textChildAmount.text = "兒童 " + drugstoreInfo.childMaskAmount.toString()

            textName.text = drugstoreInfo.name
            textAddress.text = drugstoreInfo.address
            textUpdate.text = drugstoreInfo.getUpdateWording()
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
                .subscribe { DetailActivity.start(itemView.context, drugstoreInfo) }
                .addTo(compositeDisposable)

            RxView.clicks(textShare)
                .throttleClick()
                .subscribe {
                    val wording = if (drugstoreInfo.note.isEmpty()) {
                        ""
                    } else {
                        drugstoreInfo.note + "，"
                    }
                    itemView.context.shareText(
                        "口罩資訊地圖",
                        "${drugstoreInfo.name}位於${drugstoreInfo.address}，" +
                                "$wording" +
                                "成人口罩數量為：${drugstoreInfo.adultMaskAmount}個，" +
                                "兒童口罩數量為：${drugstoreInfo.childMaskAmount}個，" +
                                "口罩數量更新時間為：${drugstoreInfo.updateAt}，" +
                                "更多資訊請參考口罩資訊地圖：https://play.google.com/store/apps/details?id=com.jarvislin.drugstores"
                    )
                }
                .addTo(compositeDisposable)

            RxView.clicks(textNavigate)
                .throttleClick()
                .subscribe {
                    itemView.context.openMap(
                        drugstoreInfo.lat,
                        drugstoreInfo.lng
                    )
                }
                .addTo(compositeDisposable)
        }
    }

    fun release() {
        compositeDisposable.clear()
    }
}