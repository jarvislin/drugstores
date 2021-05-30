package com.jarvislin.drugstores.extension

import android.view.View
import com.jakewharton.rxbinding2.view.RxView
import com.jarvislin.drugstores.base.App
import com.jarvislin.drugstores.base.BaseActivity
import com.jarvislin.drugstores.base.BaseFragment
import com.jarvislin.drugstores.base.BaseViewModel
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

fun Disposable.bind(viewModel: BaseViewModel) {
    viewModel.addDisposable(this)
}

fun Disposable.bind(activity: BaseActivity) {
    activity.addDisposable(this)
}

fun Disposable.bind(fragment: BaseFragment) {
    fragment.addDisposable(this)
}

fun Disposable.bind(app: App) {
    app.addDisposable(this)
}

fun Observable<Any>.throttleClick(): Observable<Any> {
    return this.throttleFirst(1_000, TimeUnit.MILLISECONDS)
}

fun View.click(trigger: () -> Unit): Disposable {
    return RxView.clicks(this)
        .throttleClick()
        .subscribe { trigger.invoke() }
}