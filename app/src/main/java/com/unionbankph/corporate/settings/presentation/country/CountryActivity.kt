package com.unionbankph.corporate.settings.presentation.country

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_countries.*
import kotlinx.android.synthetic.main.widget_search_layout.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*
import java.util.concurrent.TimeUnit

class CountryActivity : BaseActivity<CountryViewModel>(R.layout.activity_countries),
                        EpoxyAdapterCallback<CountryCode> {

    private val controller by lazyFast { CountryController(this, viewUtil) }

    private var countries: MutableList<CountryCode> = mutableListOf()

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setToolbarTitle(tvToolbar, getString(R.string.title_mobile_country_code))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        textViewState.text = getString(R.string.title_no_country)
        initRecyclerView()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
        getCountries()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initRxSearchEventListener()
        RxView.clicks(imageViewClearText)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                editTextSearch.text?.clear()
            }.addTo(disposables)
        swipeRefreshLayoutCountries.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                viewModel.getCountries()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClickItem(view: View, data: CountryCode, position: Int) {
        onBackPressed()
        eventBus.inputSyncEvent.emmit(
            BaseEvent(
                InputSyncEvent.ACTION_INPUT_COUNTRY,
                JsonHelper.toJson(data)
            )
        )
    }

    private fun updateController(data: MutableList<CountryCode>) {
        controller.setData(data)
    }

    private fun showEmptyState(data: MutableList<CountryCode>) {
        if (data.size > 0) {
            if (textViewState.visibility == View.VISIBLE) textViewState.visibility = View.GONE
        } else {
            textViewState.visibility = View.VISIBLE
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[CountryViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowCountryLoading -> {
                    showLoading(
                        viewLoadingState,
                        swipeRefreshLayoutCountries,
                        recyclerViewCountries,
                        textViewState
                    )
                }
                is ShowCountryDismissLoading -> {
                    dismissLoading(
                        viewLoadingState,
                        swipeRefreshLayoutCountries,
                        recyclerViewCountries
                    )
                }
                is ShowCountryGetCountries -> {
                    countries = it.data
                    updateController(it.data)
                    showEmptyState(it.data)
                }
                is ShowCountryError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    private fun initRecyclerView() {
        controller.setAdapterCallbacks(this)
        recyclerViewCountries.setController(controller)
    }

    private fun getCountries() {
        viewModel.getCountries()
    }

    private fun initRxSearchEventListener() {
        editTextSearch.setOnEditorActionListener(
            TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    filterList(editTextSearch.text.toString())
                    editTextSearch.clearFocus()
                    viewUtil.dismissKeyboard(this)
                    return@OnEditorActionListener true
                }
                false
            })
        RxTextView.textChangeEvents(editTextSearch)
            .debounce(
                resources.getInteger(R.integer.time_edit_text_search_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe { filter ->
                imageViewClearText.visibility(filter.text().isNotEmpty())
                if (filter.view().isFocused && countries.isNotEmpty()) {
                    filterList(filter.text().toString())
                }
            }.addTo(disposables)
    }

    private fun filterList(filter: String) {
        if (filter != "") {
            val searchCountries = countries
                .filter {
                    it.callingCode!!.contains(filter, true) || it.name!!.contains(filter, true)
                }.toMutableList()
            updateController(searchCountries)
            showEmptyState(searchCountries)
        } else {
            updateController(countries)
            showEmptyState(countries)
        }
    }

    companion object {
        const val EXTRA_PAGE = "page"
    }

    enum class CountryScreen {
        BENEFICIARY_SCREEN,
        SETTINGS_SCREEN,
        MIGRATION_SCREEN
    }
}
