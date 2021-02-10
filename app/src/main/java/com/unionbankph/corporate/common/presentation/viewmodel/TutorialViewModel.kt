package com.unionbankph.corporate.common.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.TutorialScreenEnum
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TutorialViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val settingsGateway: SettingsGateway,
    private val context: Context
) : BaseViewModel() {

    private val _tutorialState = MutableLiveData<TutorialState>()

    val state: LiveData<TutorialState> get() = _tutorialState

    fun skipTutorial() {
        settingsGateway.skipTutorial()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _tutorialState.value = ShowTutorialSkipTutorial
                }, {
                    Timber.e(it, "skipTutorial failed")
                    _tutorialState.value = ShowTutorialError(it)
                })
            .addTo(disposables)
    }

    fun hasTutorial(key: TutorialScreenEnum) {
        settingsGateway.hasTutorial(key)
            .delay(
                context.resources.getInteger(R.integer.time_enter_tutorial).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _tutorialState.value = ShowTutorialHasTutorial(it)
                }, {
                    Timber.e(it, "hasTutorial failed")
                    _tutorialState.value = ShowTutorialError(it)
                })
            .addTo(disposables)
    }

    fun setTutorial(key: TutorialScreenEnum, boolean: Boolean) {
        settingsGateway.setTutorial(key, boolean)
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _tutorialState.value = ShowTutorialSetTutorial
                }, {
                    Timber.e(it, "setTutorialBottom failed")
                    _tutorialState.value = ShowTutorialError(it)
                })
            .addTo(disposables)
    }
}

sealed class TutorialState

object ShowTutorialSkipTutorial : TutorialState()

object ShowTutorialSetTutorial : TutorialState()

data class ShowTutorialHasTutorial(val hasTutorial: Boolean) : TutorialState()

data class ShowTutorialError(val throwable: Throwable) : TutorialState()
