package com.unionbankph.corporate.settings.presentation.selector

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.bus.event.EventBus
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.data.model.StateData
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.form.SelectorData
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.annotation.concurrent.ThreadSafe
import javax.inject.Inject

/**
 * Created by herald25santos on 2020-02-18
 */
class SelectorViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val eventBus: EventBus,
    private val context: Context
) : BaseViewModel() {

    private val _items = MutableLiveData<MutableList<StateData<Selector>>>()

    val items: LiveData<MutableList<StateData<Selector>>> get() = _items

    val selector = BehaviorSubject.create<String>()

    val selectedCount = BehaviorSubject.createDefault(0)

    fun loadData(selectorData: SelectorData) {
        Observable.fromCallable { selectorData }
            .delay(
                context.resources.getInteger(R.integer.time_enter_tutorial).toLong(),
                TimeUnit.MILLISECONDS
            )
            .flatMap {
                if (it.stateData != null) {
                    Observable.fromCallable { it.stateData }
                } else {
                    Observable.fromIterable(it.selectors)
                        .map { StateData(it) }
                        .toList()
                        .toObservable()
                }
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _uiState.value = Event(UiState.Loading) }
            .doFinally { _uiState.value = Event(UiState.Complete) }
            .subscribe(
                {
                    _items.value = it
                    selectedCount.onNext(getTotalSelected())
                }, {
                    Timber.e(it, "loadData")
                    _uiState.value = Event(UiState.Error(it))
                }
            ).addTo(disposables)
    }

    fun onClickedItem(position: Int, isChecked: Boolean) {
        items.value?.let { items ->
            val item = items[position]
            item.state = isChecked
            _items.value = items
            selectedCount.onNext(getTotalSelected())
        }
    }

    private fun getTotalSelected(): Int {
        items.value?.let {
            return Observable.fromIterable(it)
                .filter { it.state }
                .count()
                .blockingGet().toInt()
        }
        return 0
    }

    fun onClickedSelect() {
        items.value?.let {
            val filteredList = it.filter { it.state }
            val selectedStatus = appendSelectedValues(filteredList)
            val valueToDisplay =
                getValueToDisplay(filteredList, it, selector.value.notNullable())
            val selectorData =
                SelectorData(
                    items = selectedStatus,
                    valueToDisplay = valueToDisplay,
                    stateData = it
                )
            val selectorDataString = JsonHelper.toJson(selectorData)
            if (selector.value.notNullable() == SelectorActivity.CHANNEL_SELECTOR) {
                invokeEventBus(selectorDataString, InputSyncEvent.ACTION_INPUT_FILTER_CHANNEL)
            } else {
                invokeEventBus(selectorDataString, InputSyncEvent.ACTION_INPUT_FILTER_STATUS)
            }
            _uiState.value = Event(UiState.Exit)
        }
    }

    private fun invokeEventBus(selectorDataString: String, eventType: String) {
        eventBus.inputSyncEvent.emmit(
            BaseEvent(
                eventType,
                selectorDataString
            )
        )
    }

    private fun appendSelectedValues(
        filteredList: List<StateData<Selector>>
    ): String? {
        val selectedStatus = StringBuilder()
        filteredList
            .forEachIndexed { index, stateData ->
                if (index.plus(1) == filteredList.size) {
                    selectedStatus.append(stateData.data.id.notNullable())
                } else {
                    selectedStatus.append("${stateData.data.id.notNullable()},")
                }
            }
        return if (selectedStatus.toString() == "") {
            null
        } else {
            selectedStatus.toString()
        }
    }

    private fun getValueToDisplay(
        filteredList: List<StateData<Selector>>,
        items: MutableList<StateData<Selector>>,
        selector: String
    ): String {
        return when {
            filteredList.size == 1 -> filteredList.first().data.value.notNullable()
            filteredList.isEmpty() -> ""
            filteredList.size == items.size -> ACTION_ALL
            else -> if (selector == SelectorActivity.CHANNEL_SELECTOR) {
                "${filteredList.size} channels selected"
            } else {
                "${filteredList.size} status selected"
            }
        }
    }

    fun selectAll() {
        items.value?.let {
            Observable.fromIterable(it)
                .filter { !it.state }
                .forEach { it.state = true }
            _items.value = items.value
            selectedCount.onNext(getTotalSelected())
        }
    }

    fun deSelectAll() {
        items.value?.let {
            Observable.fromIterable(it)
                .filter { it.state }
                .forEach { it.state = false }
            _items.value = items.value
            selectedCount.onNext(getTotalSelected())
        }
    }

    fun isAllSelected(): Boolean {
        items.value?.size?.let {
            return Observable.fromIterable(items.value)
                .filter { it.state }
                .count()
                .blockingGet() == it.toLong()
        }
        return false
    }

    @ThreadSafe
    companion object {
        const val ACTION_ALL = "All"
    }
}
