package com.unionbankph.corporate.settings.presentation.fingerprint

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.bus.event.BiometricSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.databinding.ActivityBiometricBinding
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class BiometricActivity :
    BaseActivity<ActivityBiometricBinding, BiometricViewModel>(),
    FingerprintBottomSheet.OnFingerPrintListener {

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowBiometricLoading -> {
                    showProgressAlertDialog(BiometricActivity::class.java.simpleName)
                }
                is ShowBiometricDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowBiometricSuccess -> {
                    val fingerprintBottomSheet = FingerprintBottomSheet.newInstance(
                        it.token,
                        FingerprintBottomSheet.ENCRYPT_TYPE
                    )
                    fingerprintBottomSheet.setOnFingerPrintListener(this)
                    fingerprintBottomSheet.encrypt(
                        this,
                        FingerprintBottomSheet.EXTRA_TOKEN,
                        it.token
                    )
                }
                is ShowBiometricSetToken -> {
                    if (intent.getStringExtra(EXTRA_REQUEST_PAGE) == PAGE_LOGIN) {
                        navigateDashboardScreen()
                    } else {
                        eventBus.biometricSyncEvent.emmit(
                            BaseEvent(
                                BiometricSyncEvent.ACTION_UPDATE_BIOMETRIC,
                                true
                            )
                        )
                        super.onBackPressed()
                    }
                }
                is ShowBiometricError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        RxView.clicks(binding.buttonLink)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                viewModel.setFingerPrint()
            }.addTo(disposables)

        RxView.clicks(binding.buttonNotNow)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                if (intent.getStringExtra(EXTRA_REQUEST_PAGE) == PAGE_LOGIN) {
                    navigateDashboardScreen()
                } else {
                    eventBus.biometricSyncEvent.emmit(
                        BaseEvent(
                            BiometricSyncEvent.ACTION_UPDATE_BIOMETRIC,
                            false
                        )
                    )
                    super.onBackPressed()
                }
            }.addTo(disposables)
    }

    override fun onBackPressed() {
        if (intent.getStringExtra(EXTRA_REQUEST_PAGE) == PAGE_LOGIN) {
            navigateDashboardScreen()
        } else {
            super.onBackPressed()
            eventBus.biometricSyncEvent.emmit(
                BaseEvent(
                    BiometricSyncEvent.ACTION_UPDATE_BIOMETRIC,
                    false
                )
            )
        }
    }

    private fun navigateDashboardScreen() {
        navigator.navigateClearStacks(
            this,
            DashboardActivity::class.java,
            null,
            true,
            Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    override fun setOnEncryptedToken(token: String) {
        viewModel.setTokenFingerPrint(token)
    }

    override fun onCompleteFingerprint(token: String) {
        // onCompleteFingerprint
    }

    override fun onDismissFingerprintDialog() {
        // onDismissFingerprintDialog
    }

    companion object {
        const val EXTRA_REQUEST_PAGE = "page"

        const val PAGE_LOGIN = "login"
        const val PAGE_SETTINGS = "settings"
    }

    override val layoutId: Int
        get() = R.layout.activity_biometric

    override val viewModelClassType: Class<BiometricViewModel>
        get() = BiometricViewModel::class.java
}
