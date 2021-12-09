package com.unionbankph.corporate.user_creation.presentation.personalise_settings

import android.app.Notification
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.PromptTypeEnum
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.user_creation.data.param.PersonalizeSettings
import com.unionbankph.corporate.user_creation.domain.SetPersonalSettingsUseCase
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class UcPersonaliseSettingsViewModel @Inject constructor(
    private val settingsUseCase: SetPersonalSettingsUseCase
) :
    BaseViewModel() {

    private val _settingsState = MutableLiveData<SettingsState>()

    val settingsState: LiveData<SettingsState> get() = _settingsState

    private val _navigateToLocalSettings = MutableLiveData<Event<SettingsState>>()

    val navigateToLocalSettings: LiveData<Event<SettingsState>> get() = _navigateToLocalSettings

    fun saveSettings(isChecked: Boolean, isCheckedTOTP: Boolean, promptType: PromptTypeEnum) {
        if(!isChecked){
            Notification()
        }else{

        }
        val param = PersonalizeSettings().apply {
            notification = isChecked
            totp = isCheckedTOTP
            promptTypeEnum = promptType
        }

        settingsUseCase.execute(
            getDisposableSingleObserver(
                {
                    it.let {
                        _navigateToLocalSettings.value = Event(SetSettingsSuccess)
                    }
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

    sealed class SettingsState
    object SetSettingsSuccess : SettingsState()

}