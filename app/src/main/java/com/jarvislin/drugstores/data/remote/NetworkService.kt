package com.jarvislin.drugstores.data.remote

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.jarvislin.drugstores.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.KoinComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NetworkService : KoinComponent {
    companion object {
        const val DATA_ADAPTION_ID = "QcbUEzN6E6DL"
        const val DATA_SHELTER_INFO1_ID = "DyplMIk3U1hf"
        const val DATA_SHELTER_INFO2_ID = "p9yPwrCs2OtC"
        const val BASE_URL = "https://data.nhi.gov.tw/Datasets/"
        const val TIMEOUT_SECOND = 60L
    }

    private val retrofit: Retrofit

    init {
        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECOND, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECOND, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECOND, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().setLevel(
                    if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
                )
            )

        if (BuildConfig.DEBUG) {
            builder.addNetworkInterceptor(StethoInterceptor())
        }

        retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .baseUrl(BASE_URL)
            .client(builder.build())
            .build()
    }

    fun <T> create(clazz: Class<T>): T = retrofit.create(clazz)
}