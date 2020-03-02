package com.jarvislin.drugstores.page.detail

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.jarvislin.domain.entity.InvalidReportTimeException
import com.jarvislin.domain.entity.MaskStatus
import com.jarvislin.domain.entity.Status
import com.jarvislin.domain.interactor.DrugstoreUseCase
import com.jarvislin.drugstores.base.App
import com.jarvislin.drugstores.base.BaseViewModel
import com.jarvislin.drugstores.extension.bind
import org.koin.core.inject
import timber.log.Timber
import java.util.*

class DetailViewModel : BaseViewModel() {
    private val useCase: DrugstoreUseCase by inject()
    val latestMaskStatus = MutableLiveData<MaskStatus>()
    val ad = MutableLiveData<UnifiedNativeAd>()
    val usesNumberTicket = MutableLiveData<Boolean>()

    fun reportMaskStatus(id: String, status: Status) {
        useCase.reportMaskStatus(id, status)
            .subscribe({
                longToastText.postValue("回報成功")
                latestMaskStatus.postValue(MaskStatus(status, Date()))
            }, {
                if (it is InvalidReportTimeException) {
                    longToastText.postValue("回報過於頻繁，請稍後再試")
                } else {
                    Timber.e(it)
                }
            })
            .bind(this)
    }

    fun fetchMaskStatus(id: String) {
        useCase.fetchMaskStatus(id)
            .subscribe({ latestMaskStatus.postValue(it) }, { Timber.e(it) })
            .bind(this)
    }

    fun requestAd(adId: String, location: Location?) {
        val videoOptions = VideoOptions.Builder()
            .setClickToExpandRequested(true)
            .build()

        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()

        val adBuilder = AdRequest.Builder()
            .addTestDevice("94AAY0LJFG")

        location?.let { adBuilder.setLocation(it) }
        AdLoader.Builder(App.instance(), adId)
            .forUnifiedNativeAd {
                ad.value?.destroy()
                ad.postValue(it)
            }
            .withNativeAdOptions(adOptions)
            .build()
            .loadAd(adBuilder.build())
    }

    fun reportNumberTicket(id: String) {
        useCase.reportNumberTicket(id)
            .subscribe({
                usesNumberTicket.postValue(true)
                longToastText.postValue("回報成功")
            }, { Timber.e(it) })
            .bind(this)
    }

    fun fetchUsesNumberTicket(id: String) {
        useCase.fetchUsesNumberTicket(id)
            .subscribe({ usesNumberTicket.postValue(it) }, { Timber.e(it) })
            .bind(this)
    }
}