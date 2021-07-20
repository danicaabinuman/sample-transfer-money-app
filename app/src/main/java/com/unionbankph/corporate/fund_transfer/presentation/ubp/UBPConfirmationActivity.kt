package com.unionbankph.corporate.fund_transfer.presentation.ubp

import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.jakewharton.rxbinding2.view.RxView
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.auth.presentation.otp.OTPActivity
import com.unionbankph.corporate.common.data.model.ServiceFee
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialHasTutorial
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.databinding.ActivityFundTransferConfirmationUbpBinding
import com.unionbankph.corporate.fund_transfer.data.form.FundTransferUBPForm
import com.unionbankph.corporate.fund_transfer.presentation.organization_transfer.OrganizationTransferActivity
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class UBPConfirmationActivity :
    BaseActivity<ActivityFundTransferConfirmationUbpBinding, UBPViewModel>(),
    OnTutorialListener {

    private var fundTransferUBPForm: FundTransferUBPForm? = null

    private var selectedAccount: Account? = null

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        tutorialEngineUtil.setOnTutorialListener(this)
        if (intent.getStringExtra(EXTRA_FUND_TRANSFER) != null &&
            intent.getStringExtra(EXTRA_ACCOUNT) != null
        ) {
            fundTransferUBPForm = JsonHelper.fromJson(intent.getStringExtra(EXTRA_FUND_TRANSFER))
            selectedAccount = JsonHelper.fromJson(intent.getStringExtra(EXTRA_ACCOUNT))
            setFundTransferDetails(fundTransferUBPForm!!, selectedAccount!!)
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initTutorialViewModel()
        initGeneralViewModel()
        initViewModel()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        RxView.clicks(binding.buttonEdit)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                onBackPressed()
            }
            .addTo(disposables)

        RxView.clicks(binding.buttonSubmit)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                viewModel.fundTransferUBP(fundTransferUBPForm!!)
            }
            .addTo(disposables)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        val helpMenu = menu.findItem(R.id.menu_help)
        helpMenu.isVisible = true
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
                startViewTutorial()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
        if (isSkipTutorial) {
            binding.scrollView.post { binding.scrollView.smoothScrollTo(0, 0) }
        } else {
            val radius = resources.getDimension(R.dimen.button_radius)
            when (view) {
                binding.buttonEdit -> {
                    viewUtil.setFocusOnView(binding.scrollView, binding.buttonSubmit)
                    tutorialEngineUtil.startTutorial(
                        this,
                        binding.buttonSubmit,
                        R.layout.frame_tutorial_lower_right,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_ubp_confirmation_submit),
                        GravityEnum.TOP,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                else -> {
                    binding.scrollView.post { binding.scrollView.smoothScrollTo(0, 0) }
                    // tutorialViewModel.setTutorial(TutorialScreenEnum.UBP_CONFIRMATION, false)
                }
            }
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
                        startViewTutorial()
                    }
                }
                is ShowTutorialError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        // tutorialViewModel.hasTutorial(TutorialScreenEnum.UBP_CONFIRMATION)
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        binding.viewToolbar.textViewTitle,
                        binding.viewToolbar.textViewCorporationName,
                        formatString(R.string.title_transfer_confirmation),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    private fun initViewModel() {
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowUBPLoading -> {
                    showProgressAlertDialog(UBPConfirmationActivity::class.java.simpleName)
                }
                is ShowUBPDismissLoading -> dismissProgressAlertDialog()

                is ShowUBPSuccess -> {
                    navigateVerifyAccountScreen(it)
                }
                is ShowUBPSkipOTPSuccess -> {
                    navigateUBPSummaryScreen(it)
                }
                is ShowUBPError -> {
                    if (it.throwable.message.equals(
                            formatString(R.string.error_multiple_posting),
                            true
                        )
                    ) {
                        showMultiplePostingDialog()
                    } else {
                        handleOnError(it.throwable)
                    }
                }
            }
        })
    }

    private fun showMultiplePostingDialog() {
        MaterialDialog(this).show {
            lifecycleOwner(this@UBPConfirmationActivity)
            cancelOnTouchOutside(false)
            title(R.string.title_error)
            message(R.string.error_multiple_posting)
            positiveButton(
                res = R.string.action_close,
                click = {
                    navigateFTDashboard()
                }
            )
        }
    }

    private fun navigateUBPSummaryScreen(it: ShowUBPSkipOTPSuccess) {
        val bundle = Bundle()
        bundle.putString(
            UBPSummaryActivity.EXTRA_FUND_TRANSFER,
            JsonHelper.toJson(fundTransferUBPForm)
        )
        bundle.putString(
            UBPSummaryActivity.EXTRA_FUND_TRANSFER_DTO,
            JsonHelper.toJson(it.fundTransferVerify)
        )
        bundle.putString(
            UBPSummaryActivity.EXTRA_ACCOUNT,
            JsonHelper.toJson(selectedAccount)
        )
        bundle.putString(
            UBPSummaryActivity.EXTRA_ACCOUNT_TYPE,
            intent.getStringExtra(EXTRA_ACCOUNT_TYPE)
        )
        bundle.putString(
            UBPSummaryActivity.EXTRA_SERVICE_FEE,
            intent.getStringExtra(EXTRA_SERVICE_FEE)
        )
        navigator.navigate(
            this,
            UBPSummaryActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateVerifyAccountScreen(it: ShowUBPSuccess) {
        val bundle = Bundle()
        bundle.putString(
            OTPActivity.EXTRA_FUND_TRANSFER_REQUEST,
            JsonHelper.toJson(fundTransferUBPForm)
        )
        bundle.putString(
            OTPActivity.EXTRA_SELECTED_ACCOUNT,
            JsonHelper.toJson(selectedAccount)
        )
        bundle.putString(
            OTPActivity.EXTRA_ACCOUNT_TYPE,
            intent.getStringExtra(EXTRA_ACCOUNT_TYPE)
        )
        bundle.putString(
            OTPActivity.EXTRA_REQUEST_PAGE,
            OTPActivity.PAGE_FUND_TRANSFER_UBP
        )
        bundle.putString(
            OTPActivity.EXTRA_REQUEST,
            JsonHelper.toJson(it.auth)
        )
        bundle.putString(
            OTPActivity.EXTRA_SERVICE_FEE,
            intent.getStringExtra(EXTRA_SERVICE_FEE)
        )
        navigator.navigate(
            this,
            OTPActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateFTDashboard() {
        navigator.navigateClearUpStack(
            this,
            OrganizationTransferActivity::class.java,
            null,
            isClear = true,
            isAnimated = true
        )
        eventBus.actionSyncEvent.emmit(
            BaseEvent(ActionSyncEvent.ACTION_UPDATE_TRANSACTION_LIST)
        )
    }

    private fun setFundTransferDetails(
        fundTransferUBPForm: FundTransferUBPForm,
        account: Account
    ) {
        binding.textViewTransferFrom.text = Html.fromHtml(
            String.format(
                getString(R.string.params_account_detail),
                account.name,
                viewUtil.getAccountNumberFormat(account.accountNumber),
                viewUtil.getStringOrEmpty(account.productCodeDesc)
            )
        )

        binding.textViewTransferTo.text = if (fundTransferUBPForm.beneficiaryMasterForm == null) {
            viewUtil.getAccountNumberFormat(fundTransferUBPForm.receiverAccountNumber)
        } else {
            Html.fromHtml(
                String.format(
                    getString(R.string.params_three_format),
                    fundTransferUBPForm.beneficiaryMasterForm?.beneficiaryName,
                    viewUtil.getAccountNumberFormat(
                        fundTransferUBPForm.beneficiaryMasterForm?.beneficiaryBankAccountNumber
                    ),
                    fundTransferUBPForm.beneficiaryMasterForm?.beneficiaryBankName
                )
            )
        }

        binding.textViewProposedTransferDate.text =
            if (fundTransferUBPForm.immediate!!)
                getString(
                    R.string.title_immediately
                )
            else
                viewUtil.getDateFormatByDateString(
                    fundTransferUBPForm.transferDate, ViewUtil.DATE_FORMAT_ISO,
                    ViewUtil.DATE_FORMAT_DEFAULT
                )
        binding.textViewAmount.text = AutoFormatUtil().formatWithTwoDecimalPlaces(
            fundTransferUBPForm.amount.toString(), account.currency
        )

        binding.textViewServiceFee.text = if (intent.getStringExtra(EXTRA_SERVICE_FEE) != null) {
            val serviceFee =
                JsonHelper.fromJson<ServiceFee>(intent.getStringExtra(EXTRA_SERVICE_FEE))
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

        binding.textViewChannel.text = intent.getStringExtra(EXTRA_ACCOUNT_TYPE)
        binding.textViewRemarks.text = viewUtil.getStringOrEmpty(fundTransferUBPForm.remarks)

        val reminders = JsonHelper.fromListJson<String>(intent.getStringExtra(EXTRA_REMINDERS))
        if (reminders.isNotEmpty()) {
            val remindersContent = StringBuilder()
            reminders.forEach {
                remindersContent.append("$it\n\n")
            }
            binding.tvReminders.text = remindersContent
        } else {
            binding.borderReminders.isInvisible = true
            binding.tvRemindersTitle.isInvisible = true
            binding.tvReminders.isVisible = false
        }
        if (fundTransferUBPForm.immediate!!) {
            binding.textViewStartDateTitle.visibility = View.GONE
            binding.textViewStartDate.visibility = View.GONE
            binding.view5.visibility = View.GONE
            binding.textViewFrequencyTitle.visibility = View.GONE
            binding.textViewFrequency.visibility = View.GONE
            binding.view6.visibility = View.GONE
            binding.textViewEndDateTitle.visibility = View.GONE
            binding.textViewEndDate.visibility = View.GONE
            binding.view7.visibility = View.GONE
            binding.textViewProposedTransferDateTitle.visibility = View.VISIBLE
            binding.textViewProposedTransferDate.visibility = View.VISIBLE
            binding.view4.visibility = View.VISIBLE
        } else {
            binding.textViewStartDate.text = viewUtil.getDateFormatByDateString(
                fundTransferUBPForm.transferDate, ViewUtil.DATE_FORMAT_ISO,
                ViewUtil.DATE_FORMAT_DEFAULT
            )
            if (fundTransferUBPForm.recurrenceEndDate != null) {
                val endDate = viewUtil.getDateFormatByDateString(
                    fundTransferUBPForm.recurrenceEndDate,
                    ViewUtil.DATE_FORMAT_ISO,
                    ViewUtil.DATE_FORMAT_DATE
                )
                binding.textViewEndDate.text = ("${fundTransferUBPForm.occurrencesText}\n(Until $endDate)")
            } else {
                binding.textViewEndDate.text = fundTransferUBPForm.occurrencesText
            }
            binding.textViewFrequency.text = fundTransferUBPForm.frequency
            if (fundTransferUBPForm.frequency == getString(R.string.title_one_time)) {
                binding.textViewEndDate.visibility = View.GONE
                binding.textViewEndDateTitle.visibility = View.GONE
                binding.view7.visibility = View.GONE
                binding.textViewFrequencyTitle.visibility = View.GONE
                binding.textViewFrequency.visibility = View.GONE
                binding.view6.visibility = View.GONE
                binding.textViewStartDateTitle.visibility = View.GONE
                binding.textViewStartDate.visibility = View.GONE
                binding.view5.visibility = View.GONE
            } else {
                binding.textViewProposedTransferDateTitle.visibility = View.GONE
                binding.textViewProposedTransferDate.visibility = View.GONE
                binding.view4.visibility = View.GONE
            }
        }
    }

    private fun startViewTutorial() {
        viewUtil.setFocusOnView(binding.scrollView, binding.buttonEdit)
        val radius = resources.getDimension(R.dimen.button_radius)
        tutorialEngineUtil.startTutorial(
            this,
            binding.buttonEdit,
            R.layout.frame_tutorial_lower_left,
            radius,
            false,
            getString(R.string.msg_tutorial_ubp_confirmation_edit),
            GravityEnum.TOP,
            OverlayAnimationEnum.ANIM_EXPLODE
        )
    }

    companion object {
        const val EXTRA_FUND_TRANSFER = "fund_transfer"
        const val EXTRA_ACCOUNT = "account"
        const val EXTRA_ACCOUNT_TYPE = "account_type"
        const val EXTRA_SERVICE_FEE = "service_fee"
        const val EXTRA_REMINDERS = "reminders"
    }

    override val layoutId: Int
        get() = R.layout.activity_fund_transfer_confirmation_ubp

    override val viewModelClassType: Class<UBPViewModel>
        get() = UBPViewModel::class.java
}
