package com.jarvislin.drugstores.page.search

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.extension.hide
import com.jarvislin.drugstores.extension.show
import com.jarvislin.drugstores.extension.throttleClick
import com.jarvislin.drugstores.page.map.MapViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_search.*
import org.jetbrains.anko.dip
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit


class SearchDialogFragment : DialogFragment() {

    private val adapter: SearchAdapter by inject()
    private val viewModel: MapViewModel by sharedViewModel()
    private val compositeDisposable = CompositeDisposable()
    private var doNotUpdate = false

    companion object {
        const val KEY_INFO = "key_info"
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            it.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            it.setBackgroundDrawable(ColorDrawable(Color.parseColor("#FAFAFA")))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.statusBarHeight.value?.let { height ->
            (layoutSearch.layoutParams as LinearLayout.LayoutParams).let {
                val param = it
                param.setMargins(it.marginStart, height + view.context.dip(16), it.marginEnd, 0)
                layoutSearch.layoutParams = param
            }
        }

        // init wording
        val calendar = Calendar.getInstance(Locale.getDefault())
        var day = calendar.get(Calendar.DAY_OF_WEEK)
        if (calendar.firstDayOfWeek == Calendar.SUNDAY) {
            day--
        }
        val text = when (day) {
            1, 3, 5 -> "單號"
            2, 4, 6 -> "雙號"
            else -> "無限制"
        }
        textDateType.text = text

        // init recycler view
        recyclerView.layoutManager =
            LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        ContextCompat.getDrawable(view.context, R.drawable.divider)?.let {
            val itemDecorator = DividerItemDecoration(view.context, RecyclerView.VERTICAL)
            itemDecorator.setDrawable(it)
            recyclerView.addItemDecoration(itemDecorator)
        }


        doNotUpdate = viewModel.searchedResult.value != null

        // handle keyword
        RxTextView.afterTextChangeEvents(editSearch)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val info = arguments?.getSerializable(KEY_INFO) as? ArrayList<DrugstoreInfo>
                val keyword = it.view().text.toString()
                if (keyword.isNotEmpty()) {
                    viewModel.searchAddress(it.view().text.toString())
                } else if (info != null) {
                    adapter.update(info)
                }
            }, { Timber.e(it) })
            .addTo(compositeDisposable)

        activity?.let {
            viewModel.searchedResult.observe(it, Observer {
                if (doNotUpdate) {
                    doNotUpdate = false
                    return@Observer
                }
                adapter.update(it)
            })
        }

        RxView.clicks(imageBack)
            .throttleClick()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { dismissAllowingStateLoss() }
            .addTo(compositeDisposable)

        RxView.clicks(textInfo).throttleClick()
            .subscribe { showInfoDialog() }
            .addTo(compositeDisposable)
    }

    private fun showInfoDialog() {
        context?.let {
            AlertDialog.Builder(it)
                .setTitle(getString(R.string.id_note_title))
                .setMessage(getString(R.string.id_note_message))
                .setPositiveButton(getString(R.string.dismiss)) { _, _ -> }
                .show()
        }
    }

    override fun onDestroy() {
        adapter.release()
        compositeDisposable.clear()
        super.onDestroy()
    }
}

