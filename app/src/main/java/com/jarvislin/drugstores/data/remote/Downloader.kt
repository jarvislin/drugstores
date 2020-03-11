package com.jarvislin.drugstores.data.remote

import com.jarvislin.domain.entity.Progress
import com.jarvislin.drugstores.BuildConfig
import com.jarvislin.drugstores.base.App
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.BufferedSink
import okio.Okio
import okio.buffer
import okio.sink
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class Downloader(
    private val progressPeriodBytes: Long = 5000,
    private val internalBufferBytes: Long = 2048,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().addInterceptor(
        HttpLoggingInterceptor().setLevel(
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS
            else HttpLoggingInterceptor.Level.NONE
        )
    ).build()
) {

    fun download(url: String): Observable<Progress> {
        return Observable.create<Progress> { emitter ->
            val isCanceled = AtomicBoolean(false)

            val request: Request = Request.Builder().url(url).get().build()
            val call = okHttpClient.newCall(request)
            val response: Response

            try {
                response = call.execute()
            } catch (ex: Exception) {
                emitter.onError(ex)
                return@create
            }

            if (response.isSuccessful.not()) {
                emitter.onError(HttpException())
            }

            val originContentLength = response.networkResponse?.header("Content-Length")?.toLong() // because okhttp remove content-length when server uses gzip

            emitter.setCancellable { isCanceled.set(true) }

            val cacheFile = File(App.instance().cacheDir, "temp")
            val sink = cacheFile.sink().buffer()

            handleWrites(
                sink,
                response.body!!,
                emitter,
                cacheFile,
                isCanceled,
                originContentLength
            )

            emitter.onNext(
                Progress.Done(
                    originContentLength ?: response.body!!.contentLength(),
                    cacheFile
                )
            )
            emitter.onComplete()

        }.subscribeOn(Schedulers.io())
    }

    private fun handleWrites(
        fileSink: BufferedSink,
        body: ResponseBody,
        emitter: ObservableEmitter<Progress>,
        file: File,
        isCanceled: AtomicBoolean,
        originContentLength: Long?
    ) {
        val contentLength = body.contentLength()
        var totalBytes = 0L
        var readBytes = 0L
        var progressLimitBytes = 0L

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
                emitter.onNext(
                    Progress.Downloading(
                        totalBytes,
                        originContentLength ?: contentLength,
                        file
                    )
                )
            }
        }

        body.close()
        fileSink.close()
    }
}

class HttpException : Exception()