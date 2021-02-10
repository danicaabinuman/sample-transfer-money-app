package com.unionbankph.corporate.dao.presentation.personal_info_4

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.extension.notEmpty
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.data.form.DaoForm
import com.unionbankph.corporate.dao.data.model.Other
import com.unionbankph.corporate.dao.domain.interactor.SubmitDao
import com.unionbankph.corporate.dao.domain.model.DaoHit
import com.unionbankph.corporate.dao.presentation.DaoViewModel
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * Created by herald25santos on 3/17/20
 */
class DaoPersonalInformationStepFourViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val context: Context,
    private val submitDao: SubmitDao
) : BaseViewModel() {

    val isLoadedScreen = BehaviorSubject.create<Boolean>()

    val isEditMode = BehaviorSubject.create<Boolean>()

    private var currentInput: CurrentInput = CurrentInput()

    var daoForm = DaoForm()

    var input: Input = Input()

    inner class Input {
        var isValidFormInput = BehaviorSubject.create<Boolean>()
        var occupationInput = BehaviorSubject.create<Selector>()
        var otherOccupationInput = BehaviorSubject.create<String>()
        var sourceOfFundsInput = BehaviorSubject.create<Selector>()
        var percentOwnershipInput = BehaviorSubject.create<String>()
    }

    inner class CurrentInput {
        var occupationInput: String? = null
        var otherOccupationInput: String? = ""
        var sourceOfFundsInput: String? = null
        var percentOwnershipInput: String? = ""
    }

    private val _navigateNextStep = MutableLiveData<Event<Input>>()

    val navigateNextStep: LiveData<Event<Input>> get() = _navigateNextStep

    private val _navigateResult = MutableLiveData<Event<DaoHit>>()

    val navigateResult: LiveData<Event<DaoHit>> get() = _navigateResult

    fun hasValidForm() = input.isValidFormInput.value ?: false

    fun loadDaoForm(daoForm: DaoForm) {
        this.daoForm = daoForm
    }

    fun setPreTextValues(
        otherOccupationInput: String?,
        percentOwnershipInput: String?
    ) {
        otherOccupationInput?.let { input.otherOccupationInput.onNext(it) }
        percentOwnershipInput?.let { input.percentOwnershipInput.onNext(it) }
    }

    fun setExistingPersonalInformationStepFour(input: DaoViewModel.Input4) {
        input.occupationInput.value?.let {
            currentInput.occupationInput = it.id
            this.input.occupationInput.onNext(it)
        }
        input.otherOccupationInput.value?.let {
            currentInput.otherOccupationInput = it
            this.input.otherOccupationInput.onNext(it)
        }
        input.sourceOfFundsInput.value?.let {
            currentInput.sourceOfFundsInput = it.id
            this.input.sourceOfFundsInput.onNext(it)
        }
        input.percentOwnershipInput.value?.let {
            currentInput.percentOwnershipInput = it
            this.input.percentOwnershipInput.onNext(it)
        }
    }

    fun hasFormChanged(): Boolean {
        return input.occupationInput.value?.id != currentInput.occupationInput
//                || input.otherOccupationInput.value != currentInput.otherOccupationInput
                || input.sourceOfFundsInput.value?.id != currentInput.sourceOfFundsInput
                || input.percentOwnershipInput.value != currentInput.percentOwnershipInput
    }

    fun onClickedNext() {
        if (!hasFormChanged()) {
            isLoadedScreen.onNext(true)
            _navigateNextStep.value = Event(input)
        } else {
            val daoForm = daoForm.apply {
                occupation = input.occupationInput.value?.id
                other = Other(occupation = input.otherOccupationInput.value.notEmpty())
                fundsSource = input.sourceOfFundsInput.value?.id
                ownershipPercentage = input.percentOwnershipInput.value
                page?.let {
                    page = if (it > 3) it else 4
                }
            }
            submitDao.execute(
                getDisposableSingleObserver(
                    {
                        updateCurrentInputs()
                        isLoadedScreen.onNext(true)
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

    private fun updateCurrentInputs() {
        currentInput.apply {
            occupationInput = input.occupationInput.value?.id
            otherOccupationInput = input.otherOccupationInput.value
            sourceOfFundsInput = input.sourceOfFundsInput.value?.id
            percentOwnershipInput = input.percentOwnershipInput.value
        }
    }
}
