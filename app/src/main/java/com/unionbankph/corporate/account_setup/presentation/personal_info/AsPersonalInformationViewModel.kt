package com.unionbankph.corporate.account_setup.presentation.personal_info

import android.app.Person
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account_setup.data.PersonalInfoInput
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class AsPersonalInformationViewModel @Inject constructor() : BaseViewModel() {

    private var _state = MutableLiveData<Event<PersonalInfoInput>>()
    val state: LiveData<Event<PersonalInfoInput>> get() = _state

    fun hasValidForm() = input.isValidFormInput.value ?: false

    private var personalInfoInput = PersonalInfoInput()

    val input = Input()

    inner class Input {
        val isValidFormInput = BehaviorSubject.create<Boolean>()
        var firstNameInput = BehaviorSubject.create<String>()
        var middleNameInput = BehaviorSubject.create<String>()
        var lastNameInput = BehaviorSubject.create<String>()
        var mobileInput = BehaviorSubject.create<String>()
        var emailInput = BehaviorSubject.create<String>()
        var genderInput = BehaviorSubject.create<Selector>()
        var civilStatusInput = BehaviorSubject.create<Selector>()
        var tinInput = BehaviorSubject.create<String>()
        var dobInput = BehaviorSubject.create<String>()
        var pobInput = BehaviorSubject.create<String>()
        var nationalityInput = BehaviorSubject.create<String>()
        var notUsCitizenInput = BehaviorSubject.createDefault(true)
    }

    fun onClickedNext() {
        _state.value = Event(personalInfoInput)
    }

    fun populateFieldsWithExisting(form: PersonalInfoInput) {
        personalInfoInput = form
        form.let {
            syncInputData(
                firstNameInput = it.firstName,
                middleNameInput = it.middleName,
                lastNameInput = it.lastName,
                mobileInput = it.mobile,
                emailInput = it.email,
                genderInput = it.gender,
                civilStatusInput = it.civilStatus,
                tinInput = it.tin,
                dobInput = it.dateOfBirth,
                pobInput = it.placeOfBirth,
                nationalityInput = it.nationality,
                isNotUsCitizen = it.notUsCitizen
            )
        }
    }

    fun syncInputData(
        firstNameInput: String?,
        middleNameInput: String?,
        lastNameInput: String?,
        mobileInput: String?,
        emailInput: String?,
        genderInput: Selector? = null,
        civilStatusInput: Selector? = null,
        tinInput: String?,
        dobInput: String? = null,
        pobInput: String?,
        nationalityInput: String?,
        isNotUsCitizen: Boolean
    ) {
        firstNameInput?.let {
            personalInfoInput.firstName = it
            input.firstNameInput.onNext(it)
        }
        middleNameInput?.let {
            personalInfoInput.middleName = it
            input.middleNameInput.onNext(it)
        }
        lastNameInput?.let {
            personalInfoInput.lastName = it
            input.lastNameInput.onNext(it)
        }
        mobileInput?.let {
            personalInfoInput.mobile = it
            input.mobileInput.onNext(it)
        }
        emailInput?.let {
            personalInfoInput.email = it
            input.emailInput.onNext(it)
        }
        genderInput?.let {
            personalInfoInput.gender = it
            input.genderInput.onNext(it)
        }
        civilStatusInput?.let {
            personalInfoInput.civilStatus = it
            input.civilStatusInput.onNext(it)
        }
        tinInput?.let {
            personalInfoInput.tin = it
            input.tinInput.onNext(it)
        }
        dobInput?.let {
            personalInfoInput.dateOfBirth = it
            input.dobInput.onNext(it)
        }
        pobInput?.let {
            personalInfoInput.placeOfBirth = it
            input.pobInput.onNext(it)
        }
        nationalityInput?.let {
            personalInfoInput.nationality = it
            input.nationalityInput.onNext(it)
        }
        isNotUsCitizen?.let {
            personalInfoInput.notUsCitizen = it
            input.notUsCitizenInput.onNext(it)
        }
    }
}