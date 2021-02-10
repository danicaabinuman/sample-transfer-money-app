package com.unionbankph.corporate.settings.presentation.country

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class CountryViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val settingsGateway: SettingsGateway
) : BaseViewModel() {

    private val _state = MutableLiveData<CountryState>()

    val state: LiveData<CountryState> get() = _state

    fun getCountries() {
        settingsGateway.getCountryCodes()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _state.value = ShowCountryLoading }
            .doFinally { _state.value = ShowCountryDismissLoading }
            .subscribe(
                {
                    _state.value = ShowCountryGetCountries(it)
                }, {
                    Timber.e(it, "getCountries Failed")
                    _state.value = ShowCountryError(it)
                })
            .addTo(disposables)
    }
}

sealed class CountryState

object ShowCountryLoading : CountryState()

object ShowCountryDismissLoading : CountryState()

data class ShowCountryGetCountries(val data: MutableList<CountryCode>) : CountryState()

data class ShowCountryError(val throwable: Throwable) : CountryState()
