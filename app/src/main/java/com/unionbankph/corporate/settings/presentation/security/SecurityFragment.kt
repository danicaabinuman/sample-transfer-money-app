package com.unionbankph.corporate.settings.presentation.security

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.jakewharton.rxbinding2.view.RxView
import com.mtramin.rxfingerprint.RxFingerprint
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.BiometricSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.FragmentSettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialHasTutorial
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.databinding.FragmentSecurityBinding
import com.unionbankph.corporate.settings.presentation.general.GeneralSettingsViewModel
import com.unionbankph.corporate.settings.presentation.general.ShowGeneralSettingsClearToken
import com.unionbankph.corporate.settings.presentation.general.ShowGeneralSettingsError
import com.unionbankph.corporate.settings.presentation.general.ShowGeneralSettingsGetToken
import com.unionbankph.corporate.settings.presentation.update_password.UpdatePasswordActivity
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class SecurityFragment :
    BaseFragment<FragmentSecurityBinding, GeneralSettingsViewModel>(),
    OnTutorialListener {

    override fun onViewModelBound() {
        super.onViewModelBound()
        initTutorialViewModel()
        initViewModel()
    }

    private fun initViewModel() {

        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralSettingsGetToken -> {
                    binding.switchBiometric.tag = GeneralSettingsViewModel::class.java.simpleName
                    binding.switchBiometric.isChecked = it.token != ""
                }
                is ShowGeneralSettingsClearToken -> {
                    binding.switchBiometric.tag = GeneralSettingsViewModel::class.java.simpleName
                    binding.switchBiometric.isChecked = false
                }
                is ShowGeneralSettingsError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    private fun initTutorialViewModel() {
        tutorialViewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[TutorialViewModel::class.java]
        tutorialViewModel.state.observe(this, Observer {
            when (it) {
                is ShowTutorialHasTutorial -> {
                    if (it.hasTutorial) {
                        startViewTutorial()
                    }
                }
                is ShowTutorialError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        (activity as DashboardActivity).btnHelp().visibility = View.VISIBLE
        (activity as DashboardActivity).setToolbarTitle(
            getString(R.string.title_security),
            hasBackButton = true,
            hasMenuItem = true
        )
        tutorialEngineUtil.setOnTutorialListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (RxFingerprint.isAvailable(applicationContext)) {
            viewModel.getTokenFingerPrint()
            binding.constraintLayoutBiometric.visibility(true)
            binding.viewBorderFingerPrint.visibility(true)
        } else {
            binding.constraintLayoutBiometric.visibility(false)
            binding.viewBorderFingerPrint.visibility(false)
        }
    }

    override fun onClickSkipButtonTutorial(spotlight: Spotlight) {
        isSkipTutorial = true
        tutorialViewModel.skipTutorial()
        spotlight.closeSpotlight()
    }

    override fun onClickOkButtonTutorial(spotlight: Spotlight) {
        spotlight.closeCurrentTarget()
    }

    override fun onStartedTutorial(view: View?, viewTarget: View) {
        // onStartedTutorial
        isSkipTutorial = false
    }

    override fun onEndedTutorial(view: View?, viewTarget: View) {
        if (!isSkipTutorial && view == binding.constraintLayoutOTP &&
            RxFingerprint.isAvailable(getAppCompatActivity())) {
            tutorialEngineUtil.startTutorial(
                getAppCompatActivity(),
                binding.constraintLayoutBiometric,
                R.layout.frame_tutorial_upper_left,
                0f,
                false,
                getString(R.string.msg_tutorial_setting_fingerprint),
                GravityEnum.BOTTOM,
                OverlayAnimationEnum.ANIM_EXPLODE
            )
        }
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        RxView.clicks(binding.constraintLayoutPassword)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                val bundle = Bundle()
                bundle.putString(
                    UpdatePasswordActivity.EXTRA_REQUEST_PAGE,
                    UpdatePasswordActivity.PAGE_UPDATE_PASSWORD
                )
                navigator.navigate(
                    (activity as DashboardActivity),
                    UpdatePasswordActivity::class.java,
                    bundle,
                    isClear = false,
                    isAnimated = true,
                    transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
                )
            }.addTo(disposables)

        RxView.clicks(binding.constraintLayoutOTP)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                eventBus.fragmentSettingsSyncEvent.emmit(
                    BaseEvent(FragmentSettingsSyncEvent.ACTION_CLICK_SECURITY_MANAGE_OTP)
                )
            }.addTo(disposables)

        RxView.clicks(binding.constraintLayoutManageDevices)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                eventBus.fragmentSettingsSyncEvent.emmit(
                    BaseEvent(FragmentSettingsSyncEvent.ACTION_CLICK_MANAGE_DEVICES)
                )
            }.addTo(disposables)

        binding.switchBiometric.tag = GeneralSettingsViewModel::class.java.simpleName
        binding.switchBiometric.setOnCheckedChangeListener { buttonView, isChecked ->
            if (binding.switchBiometric.tag != null) {
                binding.switchBiometric.tag = null
                return@setOnCheckedChangeListener
            }
            if (isChecked) {
                if (RxFingerprint.hasEnrolledFingerprints(activity!!)) {
                    (activity as DashboardActivity).showFingerprintBottomSheet()
                } else {
                    MaterialDialog(getAppCompatActivity()).show {
                        lifecycleOwner(getAppCompatActivity())
                        title(R.string.title_no_fingerprint)
                        message(R.string.msg_no_fingerprint)
                        positiveButton(
                            res = R.string.action_ok,
                            click = {
                                it.dismiss()
                                this@SecurityFragment.binding.switchBiometric.tag =
                                    GeneralSettingsViewModel::class.java.simpleName
                                this@SecurityFragment.binding.switchBiometric.isChecked = false
                            }
                        )
                        negativeButton(
                            res = R.string.action_setting,
                            click = {
                                it.dismiss()
                                startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                            }
                        )
                    }
                }
            } else {
                viewModel.clearTokenFingerPrint()
            }
        }

        binding.switchBiometric.setOnTouchListener { view, motionEvent ->
            binding.switchBiometric.tag = null
            return@setOnTouchListener false
        }
    }

    private fun initEventBus() {
        eventBus.biometricSyncEvent.flowable.subscribe {
            when (it.eventType) {
                BiometricSyncEvent.ACTION_UPDATE_BIOMETRIC -> {
                    binding.switchBiometric.tag = GeneralSettingsViewModel::class.java.simpleName
                    binding.switchBiometric.isChecked = it.payload!!
                }
            }
        }.addTo(disposables)
        eventBus.fragmentSettingsSyncEvent.flowable.subscribe {
            if (it.eventType == FragmentSettingsSyncEvent.ACTION_CLICK_HELP_SECURITY) {
                startViewTutorial()
            }
        }.addTo(disposables)
    }

    private fun startViewTutorial() {
        tutorialEngineUtil.startTutorial(
            getAppCompatActivity(),
            binding.constraintLayoutOTP,
            R.layout.frame_tutorial_upper_left,
            0f,
            false,
            getString(R.string.msg_tutorial_setting_otp),
            GravityEnum.BOTTOM,
            OverlayAnimationEnum.ANIM_EXPLODE
        )
    }

    override val viewModelClassType: Class<GeneralSettingsViewModel>
        get() = GeneralSettingsViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSecurityBinding
        get() = FragmentSecurityBinding::inflate
}
