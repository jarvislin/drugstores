package com.jarvislin.drugstores.data.remote

import com.jarvislin.domain.entity.Progress
import com.jarvislin.drugstores.base.App
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import okio.BufferedSink
import okio.Okio
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class Downloader(
    private val okHttpClient: OkHttpClient = OkHttpClient(),
    private val progressPeriodBytes: Long = 10000,
    private val internalBufferBytes: Long = 2048
) {

    fun download(url: String = "https://data.nhi.gov.tw/Datasets/Download.ashx?rid=A21030000I-D50001-001&l=https://data.nhi.gov.tw/resource/mask/maskdata.csv"): Observable<Progress> {
        return Observable.create<Progress> { emitter ->
            val isCanceled = AtomicBoolean(false)

            val request: Request = Request.Builder().url(url).get().build()
            val call = okHttpClient.newCall(request)

            val response = call.execute()

            if (!response.isSuccessful) {
                emitter.onError(IOException("Unexpected code " + response.message()))
            }

            emitter.setCancellable { isCanceled.set(true) }

            val cacheFile = File(App.instance().cacheDir, "temp")
            val sink = Okio.buffer(Okio.sink(cacheFile))

            handleWrites(sink, response.body()!!, emitter, cacheFile, isCanceled)

            emitter.onNext(Progress.Done(response.body()!!.contentLength(), cacheFile))
            emitter.onComplete()

        }.subscribeOn(Schedulers.io())
    }

    private fun handleWrites(
        fileSink: BufferedSink,
        body: ResponseBody,
        emitter: ObservableEmitter<Progress>,
        file: File,
        isCanceled: AtomicBoolean
    ) {
        val contentLength = body.contentLength()
        var totalBytes = 0L
        var readBytes = 0L
        var progressLimitBytes = 0L

        emitter.onNext(Progress.Downloading(totalBytes, contentLength, file)) // init

        while (readBytes != -1L) {
            if (isCanceled.get()) {
                body.close()
                fileSink.close()
                return
            }

            readBytes = body.source().read(fileSink.buffer(), internalBufferBytes)
            totalBytes += readBytes
            if (totalBytes > progressLimitBytes) {
                progressLimitBytes += progressPeriodBytes
                emitter.onNext(Progress.Downloading(totalBytes, contentLength, file))
            }
        }

        body.close()
        fileSink.close()
    }

}