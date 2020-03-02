package com.jarvislin.drugstores.page.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.jakewharton.rxbinding2.view.RxView
import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.domain.entity.Item
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.extension.*
import com.jarvislin.drugstores.page.detail.DetailActivity
import com.jarvislin.drugstores.widget.ModelConverter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

class SearchAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var ad: UnifiedNativeAd? = null
    private val info = ArrayList<Item>()
    private val compositeDisposable = CompositeDisposable()
    private val modelConverter by lazy { ModelConverter() }

    companion object {
        private const val TYPE_SEARCH = 1
        private const val TYPE_AD = 2
        private const val POSITION_AD = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutId = if (viewType == TYPE_SEARCH) R.layout.item_info else R.layout.item_empty_card
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return if (viewType == TYPE_SEARCH) SearchViewHolder(view) else AdViewHolder(view)
    }

    override fun getItemCount(): Int = info.size

    override fun getItemViewType(position: Int): Int {
        return if (position == POSITION_AD && ad != null) {
            TYPE_AD
        } else {
            TYPE_SEARCH
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SearchViewHolder -> holder.bind(info[position] as DrugstoreInfo)
            is AdViewHolder -> holder.bind()
        }

    }

    fun update(info: List<DrugstoreInfo>) {
        this.info.clear()
        this.info.addAll(info)
        notifyDataSetChanged()
    }

    fun setAd(ad: UnifiedNativeAd) {
        this.ad = ad
        if (info.size > 3 && info[POSITION_AD] !is AdItem) {
            info.add(POSITION_AD, AdItem())
            notifyDataSetChanged()
        }
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
                    itemView.context.shareText(
                        "口罩資訊地圖",
                        modelConverter.from(drugstoreInfo).toShareContentText()
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

    inner class AdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            val adView = LayoutInflater.from(itemView.context).inflate(
                R.layout.view_ad_large,
                null
            ) as UnifiedNativeAdView
            adView.bodyView = adView.findViewById<TextView>(R.id.ad_body)
            adView.starRatingView = adView.findViewById<RatingBar>(R.id.ad_stars)
            adView.mediaView = adView.findViewById(R.id.ad_media)
            adView.callToActionView = adView.findViewById<TextView>(R.id.ad_call_to_action)
            adView.priceView = adView.findViewById<TextView>(R.id.ad_price)
            adView.storeView = adView.findViewById<TextView>(R.id.ad_store)
            adView.iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
            adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
            adView.mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP)

            if (ad!!.body == null) {
                adView.bodyView.hide()
            } else {
                adView.bodyView.show()
                (adView.bodyView as TextView).text = ad!!.body
            }

            if (ad!!.callToAction == null) {
                adView.callToActionView.hide()
            } else {
                adView.callToActionView.show()
                (adView.callToActionView as TextView).text = ad!!.callToAction
            }

            if (ad!!.icon == null) {
                adView.iconView.hide()
            } else {
                (adView.iconView as ImageView).setImageDrawable(
                    ad!!.icon.drawable
                )
                adView.iconView.show()
            }

            if (ad!!.price == null) {
                adView.priceView.hide()
            } else {
                adView.priceView.show()
                (adView.priceView as TextView).text = ad!!.price
            }

            if (ad!!.store == null) {
                adView.storeView.hide()
            } else {
                adView.storeView.show()
                (adView.storeView as TextView).text = ad!!.store
            }

            if (ad!!.starRating == null) {
                adView.starRatingView.hide()
            } else {
                (adView.starRatingView as RatingBar).rating = ad!!.starRating!!.toFloat()
                adView.starRatingView.show()
            }

            if (ad!!.advertiser == null) {
                adView.advertiserView.hide()
            } else {
                (adView.advertiserView as TextView).text = ad!!.advertiser
                adView.advertiserView.show()
            }

            adView.setNativeAd(ad!!)

            (itemView as ViewGroup).apply {
                removeAllViews()
                addView(adView)
            }
        }

    }

    inner class AdItem : Item
}

