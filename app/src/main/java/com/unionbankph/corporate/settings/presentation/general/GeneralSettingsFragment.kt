package com.unionbankph.corporate.settings.presentation.general

import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.RxView
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.FragmentSettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.SettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.constant.TutorialScreenEnum
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialHasTutorial
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.settings.presentation.learn_more.LearnMoreActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_general_settings.*
import java.util.concurrent.TimeUnit

class GeneralSettingsFragment :
    BaseFragment<GeneralSettingsViewModel>(R.layout.fragment_general_settings),
    OnTutorialListener {

    override fun onViewModelBound() {
        super.onViewModelBound()
        initTutorialViewModel()
        initViewModel()
    }

    override fun onResume() {
        super.onResume()
        if (hasInitialLoad) {
            hasInitialLoad = false
            eventBus.settingsSyncEvent.emmit(
                BaseEvent(SettingsSyncEvent.ACTION_DISABLE_NAVIGATION_BOTTOM)
            )
            tutorialViewModel.hasTutorial(TutorialScreenEnum.SETTINGS)
        }
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
                        startSettingsTutorial()
                    } else {
                        eventBus.settingsSyncEvent.emmit(
                            BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
                        )
                    }
                }
                is ShowTutorialError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    private fun initViewModel() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[GeneralSettingsViewModel::class.java]

        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralSettingsResetTutorial -> {
                    eventBus.settingsSyncEvent.emmit(BaseEvent(SettingsSyncEvent.ACTION_RESET_TUTORIAL))
                }
                is ShowGeneralSettingsResetDemo -> {
                    eventBus.settingsSyncEvent.emmit(BaseEvent(SettingsSyncEvent.ACTION_RESET_DEMO))
                }
                is ShowGeneralSettingsError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        initListener()
    }

    private fun initEventBus() {
        eventBus.fragmentSettingsSyncEvent.flowable.subscribe {
            if (it.eventType == FragmentSettingsSyncEvent.ACTION_CLICK_HELP_GENERAL) {
                isClickedHelpTutorial = true
                eventBus.settingsSyncEvent.emmit(
                    BaseEvent(SettingsSyncEvent.ACTION_DISABLE_NAVIGATION_BOTTOM)
                )
                startSettingsTutorial()
            }
        }.addTo(disposables)
    }

    private fun initListener() {
        tutorialEngineUtil.setOnTutorialListener(this)
        RxView.clicks(constraintLayoutProfile)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                eventBus.fragmentSettingsSyncEvent.emmit(
                    BaseEvent(FragmentSettingsSyncEvent.ACTION_CLICK_PROFILE)
                )
            }.addTo(disposables)
        RxView.clicks(constraintLayoutSecurity)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                eventBus.fragmentSettingsSyncEvent.emmit(
                    BaseEvent(FragmentSettingsSyncEvent.ACTION_CLICK_SECURITY)
                )
            }.addTo(disposables)
        RxView.clicks(constraintLayoutNotification)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                eventBus.fragmentSettingsSyncEvent.emmit(
                    BaseEvent(FragmentSettingsSyncEvent.ACTION_CLICK_NOTIFICATION)
                )
            }.addTo(disposables)
        RxView.clicks(constraintLayoutLearnMore)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                navigator.navigate(
                    getAppCompatActivity(),
                    LearnMoreActivity::class.java,
                    null,
                    isClear = false,
                    isAnimated = true,
                    transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
                )
            }.addTo(disposables)
        RxView.clicks(constraintLayoutDisplay)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                eventBus.fragmentSettingsSyncEvent.emmit(
                    BaseEvent(FragmentSettingsSyncEvent.ACTION_CLICK_DISPLAY)
                )
            }.addTo(disposables)
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
        if (!isSkipTutorial) {
            if (view != null) {
                when (view) {
                    constraintLayoutProfile -> {
                        startViewTutorial(
                            constraintLayoutSecurity,
                            R.string.msg_tutorial_setting_security
                        )
                    }
                    constraintLayoutSecurity -> {
                        startViewTutorial(
                            constraintLayoutNotification,
                            R.string.msg_tutorial_setting_notification
                        )
                    }
                    constraintLayoutNotification -> {
                        startViewTutorial(
                            constraintLayoutLearnMore,
                            R.string.msg_tutorial_setting_learn_more
                        )
                    }
                    constraintLayoutLearnMore -> {
                        eventBus.settingsSyncEvent.emmit(
                            BaseEvent(SettingsSyncEvent.ACTION_TUTORIAL_SETTINGS_DONE)
                        )
                    }
                }
            } else {
                if (isClickedHelpTutorial) {
                    isClickedHelpTutorial = false
                    startViewTutorial(
                        constraintLayoutProfile,
                        R.string.msg_tutorial_setting_profile
                    )
                } else {
                    tutorialViewModel.setTutorial(TutorialScreenEnum.SETTINGS, false)
                    eventBus.settingsSyncEvent.emmit(
                        BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
                    )
                }
            }
        } else {
            eventBus.settingsSyncEvent.emmit(
                BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
            )
        }
    }

    private fun startSettingsTutorial() {
        tutorialEngineUtil.startTutorial(
            getAppCompatActivity(),
            R.drawable.ic_tutorial_settings_orange,
            getString(R.string.title_tab_settings),
            getString(R.string.msg_tutorial_settings)
        )
    }

    private fun startViewTutorial(view: View, message: Int) {
        tutorialEngineUtil.startTutorial(
            (activity as DashboardActivity),
            view,
            R.layout.frame_tutorial_upper_left,
            0f,
            false,
            formatString(message),
            GravityEnum.BOTTOM,
            OverlayAnimationEnum.ANIM_EXPLODE
        )
    }

    private fun init() {
        constraintLayoutDisplay.visibility(!App.isSME())
        viewBorderDisplay.visibility(!App.isSME())
    }
}
