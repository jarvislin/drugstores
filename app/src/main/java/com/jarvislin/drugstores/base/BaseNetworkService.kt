package com.jarvislin.petme.base

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

abstract class BaseNetworkService {

    companion object {
        private const val TIMEOUT_SECOND = 60L
    }

    private val retrofit: Retrofit

    abstract fun baseUrl(): String

    protected abstract fun isDebug(): Boolean

    protected fun httpLogLevel(): HttpLoggingInterceptor.Level =
        if (isDebug()) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE

    init {
        retrofit = createRetrofit()
    }

    protected fun createRetrofit(url: String = this.baseUrl()): Retrofit {
        return Retrofit.Builder()
            .addCallAdapterFactory(this.createCallAdapter())
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .addConverterFactory(ToStringConverterFactory())
            .baseUrl(url)
            .client(createClient())
            .build()
    }

    protected abstract fun createCallAdapter(): CallAdapter.Factory

    fun <T> create(clazz: Class<T>): T = retrofit.create(clazz)

    private fun createClient(): OkHttpClient {

        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECOND, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECOND, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECOND, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().setLevel(httpLogLevel()))

        if (isDebug()) {
            builder.addNetworkInterceptor(StethoInterceptor())
        }

        return builder.build()
    }

}

internal class ToStringConverterFactory : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type?,
        annotations: Array<Annotation>?,
        retrofit: Retrofit?
    ): Converter<ResponseBody, *>? {
        return if (String::class.java == type) {
            Converter<ResponseBody, String> { value -> value.string() }
        } else null
    }

    override fun requestBodyConverter(
        type: Type?,
        parameterAnnotations: Array<Annotation>?,
        methodAnnotations: Array<Annotation>?,
        retrofit: Retrofit?
    ): Converter<*, RequestBody>? {
        return if (String::class.java == type) {
            Converter<String, RequestBody> { value -> RequestBody.create(MEDIA_TYPE, value) }
        } else null
    }

    companion object {
        private val MEDIA_TYPE = MediaType.parse("text/plain")
    }
}
