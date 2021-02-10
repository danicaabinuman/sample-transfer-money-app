package com.unionbankph.corporate.ebilling.presentation.form

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.presentation.account_selection.AccountSelectionActivity
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.AccountSyncEvent
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.edittext.autoformat.currencyedittext.CurrencyEditText
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.common.data.model.ServiceFee
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.ebilling.presentation.confirmation.EBillingConfirmationActivity
import com.unionbankph.corporate.fund_transfer.presentation.ubp.UBPFormActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_ebilling_form.*
import kotlinx.android.synthetic.main.widget_channel_header.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*

/**
 * Created by herald on 10/27/20
 */
class EBillingFormActivity : BaseActivity<EBillingFormViewModel>(R.layout.activity_ebilling_form),
    OnTutorialListener {

    private lateinit var buttonAction: Button

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
        initTutorialViewModel()
        initGeneralViewModel()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        setupViews()
        setupBindings()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        tutorialEngineUtil.setOnTutorialListener(this)
        tie_deposit_to.setOnClickListener {
            navigateAccountSelectionScreen()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        val menuActionButton = menu.findItem(R.id.menu_action_button)
        val menuView = menuActionButton.actionView
        val helpMenu = menu.findItem(R.id.menu_help)
        buttonAction = menuView.findViewById(R.id.buttonAction)
        buttonAction.text = getString(R.string.action_next)
        buttonAction.enableButton(viewModel.selectedAccount.hasValue())
        helpMenu.isVisible = true
        menuActionButton.isVisible = true
        buttonAction.setOnClickListener { onOptionsItemSelected(menuActionButton) }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_help -> {
                isClickedHelpTutorial = true
                viewUtil.dismissKeyboard(this)
                scrollView.post { scrollView.smoothScrollTo(0, 0) }
                runPostDelayed(
                    {
                        startViewTutorial()
                    },
                    resources.getInteger(R.integer.time_enter_tutorial_immediate).toLong()
                )
                true
            }
            R.id.menu_action_button -> {
                clickNext()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
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
        isSkipTutorial = false
    }

    override fun onEndedTutorial(view: View?, viewTarget: View) {
        if (isSkipTutorial) {
            scrollView.post { scrollView.smoothScrollTo(0, 0) }
        } else {
            val radius = resources.getDimension(R.dimen.field_radius)
            if (view == null) {
                tutorialEngineUtil.startTutorial(
                    this,
                    view_tutorial_deposit_to,
                    R.layout.frame_tutorial_upper_left,
                    radius,
                    false,
                    getString(R.string.msg_tutorial_ebilling_deposit_to),
                    GravityEnum.BOTTOM,
                    OverlayAnimationEnum.ANIM_EXPLODE
                )
            } else {
                when (view) {
                    view_tutorial_deposit_to -> {
                        viewUtil.setFocusOnView(scrollView, view_tutorial_amount)
                        tutorialEngineUtil.startTutorial(
                            this,
                            view_tutorial_amount,
                            R.layout.frame_tutorial_upper_left,
                            radius,
                            false,
                            getString(R.string.msg_tutorial_ebilling_amount),
                            GravityEnum.BOTTOM,
                            OverlayAnimationEnum.ANIM_EXPLODE
                        )
                    }
                    view_tutorial_amount -> {
                        val buttonRadius = resources.getDimension(R.dimen.button_radius)
                        tutorialEngineUtil.startTutorial(
                            this,
                            buttonAction,
                            R.layout.frame_tutorial_upper_right,
                            buttonRadius,
                            false,
                            getString(R.string.msg_tutorial_ubp_form_next),
                            GravityEnum.BOTTOM,
                            OverlayAnimationEnum.ANIM_EXPLODE
                        )
                    }
                }
            }
        }
    }

    private fun initViewModel() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[EBillingFormViewModel::class.java]
        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.navigateNext.observe(this, EventObserver {
            navigateEBillingConfirmation(it)
        })
    }

    private fun initTutorialViewModel() {
        tutorialViewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[TutorialViewModel::class.java]
        tutorialViewModel.state.observe(this, Observer {
            when (it) {
                is ShowTutorialError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        textViewTitle,
                        textViewCorporationName,
                        formatString(R.string.title_generate_qr),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    private fun initEventBus() {
        eventBus.accountSyncEvent.flowable.subscribe {
            if (it.eventType == AccountSyncEvent.ACTION_UPDATE_SELECTED_ACCOUNT) {
                it.payload?.let {
                    viewModel.selectedAccount.onNext(it)
                }
            }
        }.addTo(disposables)
    }

    private fun setupViews() {
        et_amount.setEnableAmount(false)
        textViewChannel.isVisible = true
        imageViewChannel.setImageResource(R.drawable.ic_electronic_billing)
        textViewChannel.text = formatString(R.string.title_electronic_billing)
        textViewServiceFee.text = if (intent.getStringExtra(UBPFormActivity.EXTRA_SERVICE_FEE) != null) {
            val serviceFee = JsonHelper.fromJson<ServiceFee>(
                intent.getStringExtra(UBPFormActivity.EXTRA_SERVICE_FEE)
            )
            String.format(
                getString(R.string.value_service),
                autoFormatUtil.formatWithTwoDecimalPlaces(
                    serviceFee.value,
                    serviceFee.currency
                )
            )
        } else {
            getString(R.string.value_service_fee_free)
        }
        initAmountToolTip()
    }

    private fun setupBindings() {
        viewModel.selectedAccount.subscribe {
            et_amount.setEnableAmount(true)
            et_amount.setCurrencySymbol(it.currency, true)
            tie_deposit_to.setText((it.name + "\n" + it.accountNumber.formatAccountNumber()))
            invalidateOptionsMenu()
        }.addTo(disposables)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initAmountToolTip() {
        et_amount.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.ic_warning_circle_orange,
            0
        )
        et_amount.setToolTipListener(object : CurrencyEditText.ToolTipListener{
            override fun onClickedIcon() {
                showToolTip(
                    formatString(R.string.title_tooltip_amount),
                    formatString(R.string.msg_tooltip_amount)
                )
            }
        })
    }

    private fun navigateAccountSelectionScreen() {
        val bundle = Bundle()
        bundle.putString(
            AccountSelectionActivity.EXTRA_PAGE,
            AccountSelectionActivity.PAGE_EBILLING
        )
        if (viewModel.selectedAccount.hasValue()) {
            bundle.putString(
                AccountSelectionActivity.EXTRA_ID,
                viewModel.selectedAccount.value?.id.toString()
            )
        }
        navigator.navigate(
            this,
            AccountSelectionActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun validateForm(isValueChanged: Boolean) {
        val depositToObservable = viewUtil.rxTextChanges(
            true,
            isValueChanged,
            resources.getInteger(R.integer.min_length_field),
            resources.getInteger(R.integer.max_length_field_100),
            tie_deposit_to
        )
        initSetError(depositToObservable)

        RxCombineValidator(
            depositToObservable
        )
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnNext {
                isValidForm = it
                if (!isValueChanged && isValidForm) {
                    clickNext()
                }
                if (!isValueChanged && !isValidForm) {
                    showMissingFieldDialog()
                }
            }
            .doOnComplete {
                if (!isValueChanged) {
                    validateForm(true)
                }
            }
            .subscribe()
            .addTo(disposables)
        initEditTextDefaultValue()
    }

    private fun clickNext() {
        viewUtil.dismissKeyboard(this)
        if (isValidForm) {
            clearFormFocus()
            submitForm()
        } else {
            if (isInitialSubmitForm) {
                isInitialSubmitForm = false
                validateForm(false)
            } else {
                showMissingFieldDialog()
            }
        }
    }

    private fun initEditTextDefaultValue() {
        if (tie_deposit_to.length() != 0) {
            tie_deposit_to.setText(tie_deposit_to.text.toString())
        }
        if (et_amount.length() != 0) {
            et_amount.setText(et_amount.text.toString())
        }
    }

    private fun showMissingFieldDialog() {
        showMaterialDialogError(
            message = formatString(R.string.msg_error_missing_fields)
        )
    }

    private fun submitForm() {
        viewModel.bindFreeFields(et_amount.getNumericValue())
        viewModel.onClickedNext()
    }

    private fun clearFormFocus() {
        cl_parent.requestFocus()
        cl_parent.isFocusableInTouchMode = true
    }

    private fun startViewTutorial() {
        tutorialEngineUtil.startTutorial(
            this,
            R.drawable.ic_tutorial_ebilling_orange,
            getString(R.string.title_tutorial_ebilling),
            getString(R.string.msg_tutorial_ebilling)
        )
    }

    private fun navigateEBillingConfirmation(eBillingFormString: String) {
        navigator.navigate(
            this,
            EBillingConfirmationActivity::class.java,
            Bundle().apply {
                putString(EBillingConfirmationActivity.EXTRA_FORM, eBillingFormString)
            },
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

}