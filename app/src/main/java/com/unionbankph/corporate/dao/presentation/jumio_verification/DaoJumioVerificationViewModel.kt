package com.unionbankph.corporate.dao.presentation.jumio_verification

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.data.form.DaoForm
import com.unionbankph.corporate.dao.domain.interactor.SubmitDao
import com.unionbankph.corporate.dao.domain.model.DaoHit
import com.unionbankph.corporate.dao.presentation.DaoViewModel
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * Created by herald25santos on 3/17/20
 */
class DaoJumioVerificationViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val context: Context,
    private val submitDao: SubmitDao
) : BaseViewModel() {

    val isLoadedScreen = BehaviorSubject.create<Boolean>()

    val isEditMode = BehaviorSubject.create<Boolean>()

    var input: Input = Input()

    var daoForm = DaoForm()

    inner class Input {
        val isValidFormInput = BehaviorSubject.create<Boolean>()
        var tempIdTypeInput = BehaviorSubject.create<String>()
        var idTypeInput = BehaviorSubject.create<String>()
        var scanReferenceInput = BehaviorSubject.create<String>()
    }

    private val _navigateNextStep = MutableLiveData<Event<Input>>()

    val navigateNextStep: LiveData<Event<Input>> get() = _navigateNextStep

    private val _navigateResult = MutableLiveData<Event<DaoHit>>()

    val navigateResult: LiveData<Event<DaoHit>> get() = _navigateResult

    fun hasValidForm() = input.isValidFormInput.value ?: false

    fun loadDaoForm(daoForm: DaoForm) {
        this.daoForm = daoForm
    }

    fun setExistingJumioVerification(input: DaoViewModel.Input5) {
        input.idTypeInput.value?.let { this.input.idTypeInput.onNext(it) }
        input.scanReferenceInput.value?.let { this.input.scanReferenceInput.onNext(it) }
    }

    fun onClickedNext() {
        val daoForm = daoForm.apply {
            scanReferenceId = input.scanReferenceInput.value
            jumioIdType = input.tempIdTypeInput.value
            page?.let {
                page = if (it > 4) it else 5
            }
        }
        submitDao.execute(
            getDisposableSingleObserver(
                {
                    isLoadedScreen.onNext(true)
                    input.idTypeInput.onNext(input.tempIdTypeInput.value.notNullable())
                    if (it.isHit) {
                        _navigateResult.value = Event(it)
                    } else {
                        _navigateNextStep.value = Event(input)
                    }
                }, {
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            params = daoForm,
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            }
        ).addTo(disposables)
    }
}
