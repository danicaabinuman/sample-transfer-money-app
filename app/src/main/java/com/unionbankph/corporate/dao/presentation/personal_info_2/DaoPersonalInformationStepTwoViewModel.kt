package com.unionbankph.corporate.dao.presentation.personal_info_2

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.data.form.DaoForm
import com.unionbankph.corporate.dao.data.model.Government
import com.unionbankph.corporate.dao.data.model.US
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
class DaoPersonalInformationStepTwoViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val context: Context,
    private val submitDao: SubmitDao
) : BaseViewModel() {

    val isLoadedScreen = BehaviorSubject.create<Boolean>()

    val isEditMode = BehaviorSubject.create<Boolean>()

    var daoForm = DaoForm()

    var input: Input = Input()

    private var currentInput: CurrentInput = CurrentInput()

    inner class Input {
        val isValidFormInput = BehaviorSubject.create<Boolean>()
        var governmentIdTypeInput =
            BehaviorSubject.createDefault(
                Selector(
                    id = "1",
                    value = context.formatString(R.string.title_tin)
                )
            )
        var governmentIdNumberInput = BehaviorSubject.create<String>()
        var dateOfBirthInput = BehaviorSubject.create<String>()
        var placeOfBirthInput = BehaviorSubject.create<String>()
        var nationalityInput = BehaviorSubject.create<Selector>()
        var usCitizenshipInput = BehaviorSubject.createDefault(false)
        var recordTypeInput = BehaviorSubject.create<Selector>()
        var usRecordInput = BehaviorSubject.create<String>()
    }

    inner class CurrentInput {
        var governmentIdNumberInput: String? = ""
        var dateOfBirthInput: String? = null
        var placeOfBirthInput: String? = ""
        var nationalityInput: String? = null
        var recordTypeInput: String? = null
        var usCitizenshipInput: Boolean? = input.usCitizenshipInput.value
        var usRecordInput: String? = null
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
        governmentIdNumberInput: String?,
        placeOfBirthInput: String?,
        usRecordInput: String?
    ) {
        governmentIdNumberInput?.let { input.governmentIdNumberInput.onNext(it) }
        placeOfBirthInput?.let { input.placeOfBirthInput.onNext(it) }
        usRecordInput?.let { input.usRecordInput.onNext(it) }
    }

    fun setExistingPersonalInformationStepTwo(input: DaoViewModel.Input2) {
        input.governmentIdTypeInput.value?.let { this.input.governmentIdTypeInput.onNext(it) }
        input.governmentIdNumberInput.value?.let {
            currentInput.governmentIdNumberInput = it
            this.input.governmentIdNumberInput.onNext(it)
        }
        input.dateOfBirthInput.value?.let {
            currentInput.dateOfBirthInput = it
            this.input.dateOfBirthInput.onNext(it)
        }
        input.placeOfBirthInput.value?.let {
            currentInput.placeOfBirthInput = it
            this.input.placeOfBirthInput.onNext(it)
        }
        input.nationalityInput.value?.let {
            currentInput.nationalityInput = it.id
            this.input.nationalityInput.onNext(it)
        }
        input.usCitizenshipInput.value?.let {
            currentInput.usCitizenshipInput = it
            this.input.usCitizenshipInput.onNext(it)
        }
        input.recordTypeInput.value?.let {
            currentInput.recordTypeInput = it.id
            this.input.recordTypeInput.onNext(it)
        }
        input.usRecordInput.value?.let {
            currentInput.usRecordInput = it
            this.input.usRecordInput.onNext(it)
        }
    }


    fun hasFormChanged(): Boolean {
        return input.nationalityInput.value?.id != currentInput.nationalityInput
                || input.governmentIdNumberInput.value != currentInput.governmentIdNumberInput
                || input.dateOfBirthInput.value != currentInput.dateOfBirthInput
                || input.placeOfBirthInput.value != currentInput.placeOfBirthInput
//                || input.recordTypeInput.value?.id != currentInput.recordTypeInput
                || input.usCitizenshipInput.value != currentInput.usCitizenshipInput
                || input.usRecordInput.value != currentInput.usRecordInput
    }

    fun onClickedNext() {
        if (!hasFormChanged()) {
            isLoadedScreen.onNext(true)
            _navigateNextStep.value = Event(input)
        } else {
            val daoForm = daoForm.apply {
                nationality = input.nationalityInput.value?.id
                government = Government(
                    input.governmentIdTypeInput.value?.id?.toInt(),
                    input.governmentIdNumberInput.value
                )
                birthdate = input.dateOfBirthInput.value
                birthplace = input.placeOfBirthInput.value
                us = US(
                    if (input.usCitizenshipInput.value == false) {
                        "0001"
                    } else {
                        input.recordTypeInput.value?.id
                    },
                    input.usCitizenshipInput.value
                )
                addressRecord = if (input.usCitizenshipInput.value == false) {
                    null
                } else {
                    input.usRecordInput.value
                }
                page?.let {
                    page = if (it > 1) it else 2
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
            governmentIdNumberInput = input.governmentIdNumberInput.value
            dateOfBirthInput = input.dateOfBirthInput.value
            placeOfBirthInput = input.placeOfBirthInput.value
            nationalityInput = input.nationalityInput.value?.id
            recordTypeInput = input.recordTypeInput.value?.id
            usCitizenshipInput = input.usCitizenshipInput.value
            usRecordInput = input.usRecordInput.value
        }
    }

}
