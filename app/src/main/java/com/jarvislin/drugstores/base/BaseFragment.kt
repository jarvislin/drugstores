package com.jarvislin.petme.base

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseFragment : Fragment() {
    protected abstract val viewModel: BaseViewModel?
    private lateinit var loadingDialog: Dialog
    private val compositeDisposable = CompositeDisposable()

    protected fun showToast(text: String) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadingDialog = AlertDialog.Builder(activity)
            .setCancelable(false)
//            .setView(LayoutInflater.from(activity).inflate(R.layout.dialog_progress, null))
            .create()

        viewModel?.showLoading?.observe(this, Observer {
            if (it) {
                showLoading()
            } else {
                hideLoading()
            }
        })
    }

    private fun showLoading() {
        if (isAdded) {
            loadingDialog.show()
        }
    }

    private fun hideLoading() {
        if (isAdded) {
            loadingDialog.dismiss()
        }
    }

    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}