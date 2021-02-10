package com.unionbankph.corporate.settings.presentation.profile

import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.setVisible
import com.unionbankph.corporate.app.common.platform.bus.event.SettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.auth.data.model.CorporateUser
import com.unionbankph.corporate.settings.presentation.SettingsViewModel
import com.unionbankph.corporate.settings.presentation.ShowSettingsDismissLoading
import com.unionbankph.corporate.settings.presentation.ShowSettingsError
import com.unionbankph.corporate.settings.presentation.ShowSettingsGetCorporateUser
import com.unionbankph.corporate.settings.presentation.ShowSettingsHasPendingChangeEmail
import com.unionbankph.corporate.settings.presentation.ShowSettingsLoading
import com.unionbankph.corporate.settings.presentation.email.UpdateEmailActivity
import com.unionbankph.corporate.settings.presentation.mobile.ChangeMobileNumberActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_profile_settings.*
import java.util.concurrent.TimeUnit

class ProfileSettingsFragment : BaseFragment<SettingsViewModel>(R.layout.fragment_profile_settings) {

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(this, viewModelFactory)[SettingsViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowSettingsLoading -> {
                    showProgressAlertDialog(
                        ProfileSettingsFragment::class.java.simpleName
                    )
                }
                is ShowSettingsDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowSettingsGetCorporateUser -> {
                    setCorporateUserDetails(it.corporateUser)
                }
                is ShowSettingsHasPendingChangeEmail -> {
                    imageViewPendingEmail.setVisible(it.hasPending)
                }
                is ShowSettingsError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.hasPendingChangeEmail()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        RxView.clicks(textViewEditEmail)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                navigator.navigate(
                    activity as DashboardActivity,
                    UpdateEmailActivity::class.java,
                    null,
                    isClear = false,
                    isAnimated = true,
                    transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
                )
            }.addTo(disposables)

        RxView.clicks(textViewEditMobileNumber)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                navigator.navigate(
                    activity as DashboardActivity,
                    ChangeMobileNumberActivity::class.java,
                    null,
                    isClear = false,
                    isAnimated = true,
                    transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
                )
            }.addTo(disposables)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        (activity as DashboardActivity).imageViewHelp().visibility = View.GONE
        (activity as DashboardActivity).setToolbarTitle(
            getString(R.string.title_profile),
            hasBackButton = true,
            hasMenuItem = false
        )
        viewModel.getCorporateUser()
    }

    private fun initEventBus() {
        eventBus.settingsSyncEvent.flowable.subscribe {
            when (it.eventType) {
                SettingsSyncEvent.ACTION_UPDATE_MOBILE_NUMBER -> {
                    viewModel.getCorporateUser()
                }
                SettingsSyncEvent.ACTION_UPDATE_EMAIL_ADDRESS -> {
                    viewModel.hasPendingChangeEmail()
                }
            }
        }.addTo(disposables)
    }

    private fun setCorporateUserDetails(corporateUser: CorporateUser) {
        textViewEmail.text = corporateUser.emailAddress
        textViewMobileNumber.text =
            ("+${corporateUser.countryCode?.callingCode} ${corporateUser.mobileNumber}")
        imageViewCountry.setImageResource(
            viewUtil.getDrawableById("ic_flag_${corporateUser.countryCode?.code?.toLowerCase()}")
        )
    }
}
