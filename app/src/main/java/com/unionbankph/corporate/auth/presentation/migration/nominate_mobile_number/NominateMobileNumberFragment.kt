package com.unionbankph.corporate.auth.presentation.migration.nominate_mobile_number

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.enableButton
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.auth.data.form.ECredForm
import com.unionbankph.corporate.auth.data.form.MigrationNominateMobileNumberForm
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.auth.presentation.migration.MigrationMainActivity
import com.unionbankph.corporate.auth.presentation.migration.MigrationViewModel
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationDismissLoading
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationECredNominateMobileNumber
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationError
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationLoading
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationNominateMobileNumber
import com.unionbankph.corporate.auth.presentation.migration.ShowMigrationSaveECredPayload
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.settings.presentation.country.CountryActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_nominate_mobile_number.*
import kotlinx.android.synthetic.main.widget_country_picker.*

class NominateMobileNumberFragment : BaseFragment<MigrationViewModel>(R.layout.fragment_nominate_mobile_number) {

    private val migrationMainActivity by lazyFast { (activity as MigrationMainActivity) }

    private val loginMigrationDto by lazyFast { migrationMainActivity.getLoginMigrationInfo() }

    private var countryCode = Constant.getDefaultCountryCode()

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(this, viewModelFactory)[MigrationViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowMigrationLoading -> {
                    showProgressAlertDialog(
                        NominateMobileNumberFragment::class.java.simpleName
                    )
                }
                is ShowMigrationDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowMigrationNominateMobileNumber -> {
                    navigateOTPScreen()
                    eventBus.actionSyncEvent.emmit(
                        BaseEvent(
                            ActionSyncEvent.ACTION_UPDATE_AUTH_MIGRATION,
                            JsonHelper.toJson(it.auth)
                        )
                    )
                }
                is ShowMigrationSaveECredPayload -> {
                    viewModel.nominateECredForm(migrationMainActivity.getAccessToken())
                }
                is ShowMigrationECredNominateMobileNumber -> {
                    navigateOTPScreen()
                    eventBus.actionSyncEvent.emmit(
                        BaseEvent(
                            ActionSyncEvent.ACTION_UPDATE_AUTH_MIGRATION,
                            JsonHelper.toJson(
                                Auth(
                                    requestId = it.eCredSubmitDto.requestId,
                                    mobileNumber = it.eCredSubmitDto.mobileNumber,
                                    validity = it.eCredSubmitDto.validity.toString()
                                )
                            )
                        )
                    )
                }
                is ShowMigrationError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initCountryDefault(countryCode)
        validateForm()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        countryCodePicker.setOnClickListener {
            navigateCountryScreen()
        }
        buttonSubmit.setOnClickListener {
            if (migrationMainActivity.getType() == MigrationMainActivity.TYPE_ECREDITING) {
                viewModel.saveECredPayload(
                    ECredForm(
                        mobileNumber = editTextMobileNumber.text.toString().trim(),
                        countryCodeId = countryCode.id
                    )
                )
            } else {
                viewModel.nominateMobileNumberMigration(
                    loginMigrationDto.temporaryCorporateUserId!!,
                    MigrationNominateMobileNumberForm(
                        countryCode.id,
                        editTextMobileNumber.text.toString().trim(),
                        loginMigrationDto.migrationToken
                    )
                )
            }
        }
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            if (it.eventType == InputSyncEvent.ACTION_INPUT_COUNTRY) {
                countryCode = JsonHelper.fromJson(it.payload)
                initCountryDefault(countryCode)
            }
        }.addTo(disposables)
    }

    private fun initCountryDefault(selectedCountryCode: CountryCode?) {
        textViewCallingCode.text = selectedCountryCode?.callingCode
        imageViewFlag.setImageResource(
            viewUtil.getDrawableById("ic_flag_${selectedCountryCode?.code?.toLowerCase()}")
        )
    }

    private fun validateForm() {
        val mobileNumberObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = editTextMobileNumber
        )
        RxCombineValidator(mobileNumberObservable)
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                buttonSubmit.enableButton(it)
            }.addTo(disposables)
    }

    private fun navigateCountryScreen() {
        val bundle = Bundle()
        bundle.putSerializable(
            CountryActivity.EXTRA_PAGE,
            CountryActivity.CountryScreen.MIGRATION_SCREEN
        )
        countryCode.id.toString()
        navigator.navigate(
            (activity as MigrationMainActivity),
            CountryActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT

        )
    }

    private fun navigateOTPScreen() {
        migrationMainActivity.getViewPager().currentItem =
            migrationMainActivity.getViewPager().currentItem.plus(1)
    }

    companion object {
        fun newInstance(): NominateMobileNumberFragment {
            val fragment =
                NominateMobileNumberFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }
}
