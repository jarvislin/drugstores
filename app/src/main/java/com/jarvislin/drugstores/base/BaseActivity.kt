package com.jarvislin.drugstores.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast


abstract class BaseActivity : AppCompatActivity() {

    protected abstract val viewModel: BaseViewModel?
    protected val compositeDisposable = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel?.toastText?.observe(this, Observer { toast(it) })
        viewModel?.longToastText?.observe(this, Observer { longToast(it) })
    }

    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}