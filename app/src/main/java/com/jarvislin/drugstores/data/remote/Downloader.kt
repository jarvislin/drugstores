package com.jarvislin.drugstores.data.remote

import com.jarvislin.domain.entity.DownloadResult
import com.jarvislin.drugstores.BuildConfig
import com.jarvislin.drugstores.base.App
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.BufferedSink
import okio.buffer
import okio.sink
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class Downloader(
    private val internalBufferBytes: Long = 2048,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().addInterceptor(
        HttpLoggingInterceptor().setLevel(
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS
            else HttpLoggingInterceptor.Level.NONE
        )
    ).build()
) {

    fun download(url: String): Single<DownloadResult> {
        return Single.create<DownloadResult> { emitter ->
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


            emitter.setCancellable { isCanceled.set(true) }

            val cacheFile = File(App.instance().cacheDir, "temp")
            val sink = cacheFile.sink().buffer()

            handleWrites(sink, response.body!!, isCanceled)

            emitter.onSuccess(DownloadResult(cacheFile))
        }.subscribeOn(Schedulers.io())
    }

    private fun handleWrites(
        fileSink: BufferedSink,
        body: ResponseBody,
        isCanceled: AtomicBoolean
    ) {
        body.contentLength()
        var totalBytes = 0L
        var readBytes = 0L

        while (readBytes != -1L) {
            if (isCanceled.get()) {
                body.close()
                fileSink.close()
                return
            }

            readBytes = body.source().read(fileSink.buffer, internalBufferBytes)
            totalBytes += readBytes
        }

        body.close()
        fileSink.close()
    }
}

class HttpException : Exception()