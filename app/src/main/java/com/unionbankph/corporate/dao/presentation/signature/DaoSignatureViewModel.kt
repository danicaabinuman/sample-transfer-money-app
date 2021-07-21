package com.unionbankph.corporate.dao.presentation.signature

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.data.form.DaoForm
import com.unionbankph.corporate.dao.presentation.DaoViewModel
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Created by herald25santos on 3/17/20
 */
class DaoSignatureViewModel @Inject constructor(
    private val context: Context,
    private val schedulerProvider: SchedulerProvider
) : BaseViewModel() {

    private val _navigateSignaturePreview = MutableLiveData<Event<File>>()

    val navigateSignaturePreview: LiveData<Event<File>> get() = _navigateSignaturePreview

    val isLoadedScreen = BehaviorSubject.create<Boolean>()

    val isEditMode = BehaviorSubject.create<Boolean>()

    val isFlashOn = BehaviorSubject.createDefault(false)

    var input: Input = Input()

    var daoForm = DaoForm()

    inner class Input {
        val isValidFormInput = BehaviorSubject.create<Boolean>()
        var fileInput = BehaviorSubject.create<File>()
        var imagePath = BehaviorSubject.create<String>()
    }

    private val _navigateNextStep = MutableLiveData<Event<Input>>()

    val navigateNextStep: LiveData<Event<Input>> get() = _navigateNextStep

    fun hasValidForm() = input.isValidFormInput.value ?: false

    fun loadDaoForm(daoForm: DaoForm) {
        this.daoForm = daoForm
    }

    fun setExistingSignature(input: DaoViewModel.Input6) {
        input.file.value?.let { this.input.fileInput.onNext(it) }
        input.imagePath.value?.let { this.input.imagePath.onNext(it) }
    }

    fun onTakenSignature(file: File?) {
        Compressor(context)
            .setQuality(100)
            .compressToFileAsFlowable(file)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _uiState.value = Event(UiState.Loading) }
            .doFinally { _uiState.value = Event(UiState.Complete) }
            .subscribe(
                {
                    _navigateSignaturePreview.value = Event(it)
                }, {
                    Timber.e(it, "onPictureTaken")
                }
            ).addTo(disposables)
    }

    fun onSubmitSignatureFile(file: File) {
        daoForm.page?.let {
            if (it > 5) it else 6
        }
        isLoadedScreen.onNext(true)
        input.fileInput.onNext(file)
        input.imagePath.onNext("")
        _navigateNextStep.value = Event(input)
    }
}
