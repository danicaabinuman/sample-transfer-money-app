package com.unionbankph.corporate.dao.presentation.checking_account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.domain.interactor.GenerateDaoAccessToken
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by herald25santos on 3/17/20
 */
class DaoCheckingAccountTypeViewModel
@Inject
constructor(
    private val schedulerProvider: SchedulerProvider,
    private val generateDaoAccessToken: GenerateDaoAccessToken
) : BaseViewModel() {

    private val _token = MutableLiveData<String>()

    val token: LiveData<String> get() = _token

    val isValidFormInput = BehaviorSubject.create<Boolean>()

    val checkingAccountType = BehaviorSubject.create<String>()

    fun generateDaoToken() {
        generateDaoAccessToken.execute(
            getDisposableSingleObserver(
                {
                    _uiState.value = Event(UiState.Success)
                }, {
                    Timber.d(it, "getDaoToken")
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            }
        )
    }

}
