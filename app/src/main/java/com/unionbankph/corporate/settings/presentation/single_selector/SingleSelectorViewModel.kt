package com.unionbankph.corporate.settings.presentation.single_selector

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.bus.event.EventBus
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.domain.interactor.GetCities
import com.unionbankph.corporate.dao.domain.interactor.GetCountries
import com.unionbankph.corporate.dao.domain.interactor.GetOccupations
import com.unionbankph.corporate.dao.domain.interactor.GetProvinces
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum.CITY
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum.CITY_PERMANENT
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum.CIVIL_STATUS
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum.COUNTRY
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum.COUNTRY_PERMANENT
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum.GENDER
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum.GOVERNMENT_ID
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum.NATIONALITY
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum.OCCUPATION
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum.PROVINCE
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum.PROVINCE_PERMANENT
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum.PURPOSE
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum.RECORD_TYPE
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum.SALUTATION
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum.SOURCE_OF_FUNDS
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by herald25santos on 2020-02-18
 */
class SingleSelectorViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val context: Context,
    private val eventBus: EventBus,
    private val settingsGateway: SettingsGateway,
    private val getCities: GetCities,
    private val getProvinces: GetProvinces,
    private val getOccupations: GetOccupations,
    private val getCountries: GetCountries
) : BaseViewModel() {

    private val _items = MutableLiveData<MutableList<Selector>>()

    val items: LiveData<MutableList<Selector>> get() = _items

    var searchItems: MutableList<Selector> = mutableListOf()

    val selector = BehaviorSubject.create<String>()

    val hasAPIRequest = BehaviorSubject.createDefault(false)

    val isPaginated = BehaviorSubject.createDefault(false)

    val pageable = Pageable().apply { size = 20 }

    private var getOccupationsDisposable: Disposable? = null

    fun setSelector(selector: String) {
        this.selector.onNext(selector)
        when (enumValueOf<SingleSelectorTypeEnum>(selector)) {
            OCCUPATION,
            PROVINCE,
            PROVINCE_PERMANENT,
            CITY,
            CITY_PERMANENT,
            COUNTRY,
            COUNTRY_PERMANENT -> {
                hasAPIRequest.onNext(true)
            }
            else -> {
                hasAPIRequest.onNext(false)
            }
        }
    }

    fun getSingleSelector(selector: String, isInitialLoading: Boolean, param: String?) {
        when (val singleSelectorEnum = enumValueOf<SingleSelectorTypeEnum>(selector)) {
            OCCUPATION -> {
                requestPaginatedRemotes(singleSelectorEnum, isInitialLoading)
            }
            PROVINCE,
            PROVINCE_PERMANENT,
            CITY,
            CITY_PERMANENT,
            COUNTRY,
            COUNTRY_PERMANENT -> {
                requestRemotes(singleSelectorEnum, param)
            }
            else -> {
                requestLocals(singleSelectorEnum)
            }
        }
    }

    private fun requestLocals(selector: SingleSelectorTypeEnum) {
        mapSingleSelector(selector)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    searchItems = it
                    _items.value = it
                }, {
                    Timber.e(it, "loadData")
                    _uiState.value = Event(UiState.Error(it))
                }
            ).addTo(disposables)
    }

    private fun requestRemotes(singleSelectorEnum: SingleSelectorTypeEnum, param: String?) {
        val interactor =
            if (singleSelectorEnum == PROVINCE_PERMANENT || singleSelectorEnum == PROVINCE) {
                getProvinces
            } else if (singleSelectorEnum == CITY || singleSelectorEnum == CITY_PERMANENT) {
                getCities
            } else {
                getCountries
            }
        interactor.execute(
            getDisposableSingleObserver(
                {
                    searchItems = it
                    _items.value = it
                }, {
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            },
            params = param
        ).addTo(disposables)
    }

    private fun requestPaginatedRemotes(
        singleSelectorEnum: SingleSelectorTypeEnum,
        isInitialLoading: Boolean
    ) {
        getOccupationsDisposable?.dispose()
        getOccupationsDisposable = getOccupations.execute(
            getDisposableSingleObserver(
                {
                    pageable.apply {
                        totalPageCount = it.totalPages
                        isLastPage = !it.hasNextPage
                        increasePage()
                    }
                    val combineList = pageable.combineList(items.value, it.results)
                    _items.value = combineList
                }, {
                    if (!isInitialLoading) {
                        pageable.errorPagination(it.message)
                    } else {
                        _uiState.value = Event(UiState.Error(it))
                    }
                }
            ),
            doOnSubscribeEvent = {
                if (!isInitialLoading) {
                    pageable.isLoadingPagination = true
                }
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                if (!isInitialLoading) {
                    pageable.isLoadingPagination = false
                }
                _uiState.value = Event(UiState.Complete)
            },
            params = pageable
        )
        getOccupationsDisposable?.addTo(disposables)
    }

    private fun mapSingleSelector(selector: SingleSelectorTypeEnum): Single<MutableList<Selector>> {
        return when (selector) {
            SALUTATION -> {
                settingsGateway.getSalutations()
            }
            GOVERNMENT_ID -> {
                settingsGateway.getGovernmentIds()
            }
            SOURCE_OF_FUNDS -> {
                settingsGateway.getSourceOfFunds()
            }
            GENDER -> {
                settingsGateway.getGenders()
            }
            CIVIL_STATUS -> {
                settingsGateway.getCivilStatuses()
            }
            RECORD_TYPE -> {
                settingsGateway.getRecordTypes()
            }
            NATIONALITY -> {
                settingsGateway.getNationality()
            }
            PURPOSE -> {
                val items = context.resources.getStringArray(R.array.array_purpose)
                val selectors = items.map { Selector(it, it, it) }
                Single.fromCallable { selectors.toMutableList() }
            }
            else -> {
                settingsGateway.getOccupations()
            }
        }
    }

    fun filterSearch(filter: String) {
        pageable.isInitialLoad = true
        if (filter != "") {
            val filteredList = searchItems.asSequence()
                .filter { it.value?.contains(filter, true) == true }
                .toMutableList()
            _items.value = filteredList
        } else {
            _items.value = searchItems
        }
    }

    fun onClickedItem(position: Int) {
        items.value?.let { items ->
            val item = items[position]
            invokeEventBus(JsonHelper.toJson(item))
        }
    }

    private fun invokeEventBus(selectorDataString: String) {
        _uiState.value = Event(UiState.Exit)
        eventBus.inputSyncEvent.emmit(
            BaseEvent(
                selector.value.notNullable(),
                selectorDataString
            )
        )
    }
}
