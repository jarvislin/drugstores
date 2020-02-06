package com.jarvislin.drugstores.base

import android.annotation.SuppressLint
import android.app.Application
import com.facebook.stetho.Stetho
import com.jarvislin.drugstores.BuildConfig
import com.jarvislin.drugstores.module.*
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.plugins.RxJavaPlugins
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber


class App : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: App? = null

        fun instance() = instance!!

        private const val APP_NAME = "Pet Adoption"
    }

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate() {
        super.onCreate()
        instance = this

        // koin
        startKoin {
            androidContext(this@App)
            modules(
                listOf(
                    adapterModule,
                    localModule,
                    remoteModule,
                    repositoryModule,
                    useCaseModule,
                    viewModelModule,
                    databaseModule
                )
            )
        }

        if (BuildConfig.DEBUG) {
            initLogger()
            Stetho.initializeWithDefaults(this)
        }

        RxJavaPlugins.setErrorHandler { Timber.e(it) }
    }

    private fun initLogger() {
        val formatStrategy = PrettyFormatStrategy.newBuilder()
            .tag(APP_NAME)
            .showThreadInfo(false)
            .build()

        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))
        Timber.plant(DebugTree())
    }

    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }
}

class DebugTree : Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        Logger.log(priority, tag, message, t)
    }

    override fun formatMessage(message: String, args: Array<out Any>): String {
        return super.formatMessage(message.replace("%", "%%"), args)
    }
}