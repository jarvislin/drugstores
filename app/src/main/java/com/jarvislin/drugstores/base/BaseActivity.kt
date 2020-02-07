package com.jarvislin.drugstores.base

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.toast


abstract class BaseActivity : AppCompatActivity() {

    protected abstract val viewModel: BaseViewModel?
    protected var isActive = false

    private val compositeDisposable = CompositeDisposable()
    private lateinit var loadingDialog: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadingDialog = AlertDialog.Builder(this)
            .setCancelable(false)
//            .setView(LayoutInflater.from(this).inflate(R.layout.dialog_progress, null))
            .create()

        viewModel?.showLoading?.observe(this, Observer { show ->
            if (show) {
                showLoading()
            } else {
                hideLoading()
            }
        })

        viewModel?.toastText?.observe(this, Observer { toast(it) })
    }


    fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    fun showLoading() {
        if (isActive) {
            loadingDialog.show()
        }
    }

    fun hideLoading() {
        if (isActive) {
            loadingDialog.dismiss()
        }
    }

    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onResume() {
        super.onResume()
        isActive = true
    }

    override fun onPause() {
        isActive = false
        super.onPause()
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