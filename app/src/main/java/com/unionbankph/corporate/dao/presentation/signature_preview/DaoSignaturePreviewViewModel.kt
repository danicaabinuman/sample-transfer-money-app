package com.unionbankph.corporate.dao.presentation.signature_preview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.data.form.DaoForm
import com.unionbankph.corporate.dao.domain.form.SignatureForm
import com.unionbankph.corporate.dao.domain.interactor.SubmitSignature
import com.unionbankph.corporate.dao.domain.model.DaoHit
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import java.io.File
import javax.inject.Inject

/**
 * Created by herald25santos on 3/17/20
 */
class DaoSignaturePreviewViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val submitSignature: SubmitSignature
) : BaseViewModel() {

    var daoForm = DaoForm()

    var isSuccessUpload = BehaviorSubject.createDefault(false)

    private val _navigateNextStep = MutableLiveData<Event<File>>()

    val navigateNextStep: LiveData<Event<File>> get() = _navigateNextStep

    private val _navigateResult = MutableLiveData<Event<DaoHit>>()

    val navigateResult: LiveData<Event<DaoHit>> get() = _navigateResult

    fun loadDaoForm(daoFormString: String?) {
        this.daoForm = JsonHelper.fromJson(daoFormString)
    }

    fun onSubmitSignatureFile(file: File) {
        val daoForm = daoForm.apply {
            page?.let {
                page = if (it > 5) page else 6
            }
        }
        val signatureForm = SignatureForm().apply {
            this.file = file
            this.daoForm = daoForm
        }
        submitSignature.execute(
            getDisposableSingleObserver(
                {
                    isSuccessUpload.onNext(true)
                    _uiState.value = Event(UiState.Complete)
                    if (it.isHit) {
                        _navigateResult.value = Event(it)
                    } else {
                        _navigateNextStep.value = Event(file)
                    }
                }, {
                    _uiState.value = Event(UiState.Complete)
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            params = signatureForm
        ).addTo(disposables)
    }
}
