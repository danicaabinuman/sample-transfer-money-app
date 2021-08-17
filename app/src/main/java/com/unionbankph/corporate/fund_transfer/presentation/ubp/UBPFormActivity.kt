package com.unionbankph.corporate.fund_transfer.presentation.ubp

import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.*
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.presentation.account_selection.AccountSelectionActivity
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.AccountSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.ProposedTransferDateSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.edittext.ImeOptionEditText
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.model.PermissionCollection
import com.unionbankph.corporate.common.data.model.ProposedTransferDate
import com.unionbankph.corporate.common.data.model.ServiceFee
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialHasTutorial
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.corporate.data.model.Channel
import com.unionbankph.corporate.databinding.ActivityFundTransferFormUbpBinding
import com.unionbankph.corporate.fund_transfer.data.form.FundTransferUBPForm
import com.unionbankph.corporate.fund_transfer.data.model.Beneficiary
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_selection.BeneficiaryActivity
import com.unionbankph.corporate.fund_transfer.presentation.proposed_transfer.ProposedTransferDateActivity
import io.reactivex.rxkotlin.addTo
import java.util.regex.Pattern

class UBPFormActivity :
    BaseActivity<ActivityFundTransferFormUbpBinding, UBPViewModel>(),
    View.OnClickListener,
    OnTutorialListener,
    OnConfirmationPageCallBack,
    ImeOptionEditText.OnImeOptionListener {

    private lateinit var buttonAction: Button

    private val channel by lazyFast {
        JsonHelper.fromJson<Channel>(intent.getStringExtra(EXTRA_CHANNEL))
    }

    private lateinit var imeOptionEditText: ImeOptionEditText

    private var validateTimeZoneBottomSheet: ConfirmationBottomSheet? = null

    private var permissionCollection: PermissionCollection? = null

    private var proposedTransferDate: ProposedTransferDate? = null

    private var selectedAccount: Account? = null

    private var destinationAccount: Account? = null

    private var beneficiaryMaster: Beneficiary? = null

    private var isEnableButton: Boolean = false

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initTutorialViewModel()
        initGeneralViewModel()
        initViewModel()
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
        // tutorialViewModel.hasTutorial(TutorialScreenEnum.UBP_FORM)
    }

    private fun initViewModel() {
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowUBPLoading -> {
                    showProgressAlertDialog(UBPFormActivity::class.java.simpleName)
                }
                is ShowUBPDismissLoading -> dismissProgressAlertDialog()
                is ShowUBPError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.uiState.observe(this, Observer {
            it.getContentIfNotHandled().let { event ->
                when (event) {
                    is UiState.Loading -> {
                        showProgressAlertDialog(this::class.java.simpleName)
                    }
                    is UiState.Complete -> {
                        dismissProgressAlertDialog()
                    }
                }
            }
        })
        viewModel.account.observe(this, Observer {
            showTransferFrom(it)
        })
        viewModel.getAccountsPermission(
            channelId = channel.id.toString(),
            permissionId = intent.getStringExtra(EXTRA_ID)
        )
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        binding.viewToolbar.textViewTitle,
                        binding.viewToolbar.textViewCorporationName,
                        formatString(R.string.title_ubp_channel),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        binding.viewTransferToForm.textInputLayoutTransferTo.hint = getString(R.string.hint_transfer_to)
        binding.etAmount.setEnableAmount(false)
        initChannelView(channel)
        tutorialEngineUtil.setOnTutorialListener(this)
    }

    override fun onBackPressed() {
        showCancelTransactionBottomSheet()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        val menuActionButton = menu.findItem(R.id.menu_action_button)
        val menuView = menuActionButton.actionView
        val helpMenu = menu.findItem(R.id.menu_help)
        buttonAction = menuView.findViewById(R.id.buttonAction)
        buttonAction.text = getString(R.string.action_next)
        buttonAction.enableButton(isEnableButton)
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
                binding.scrollView.post { binding.scrollView.smoothScrollTo(0, 0) }
                Handler().postDelayed(
                    {
                        startViewTutorial()
                    }, resources.getInteger(R.integer.time_enter_tutorial_immediate).toLong()
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

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        viewUtil.setEditTextMaskListener(
            binding.viewTransferToForm.textInputEditTextTransferTo,
            getString(R.string.hint_account_number_format)
        )
        viewUtil.setEditTextMaxLength(
            binding.viewTransferToForm.textInputEditTextTransferTo,
            resources.getInteger(R.integer.max_length_account_number_ubp)
        )
        binding.textInputEditTextTransferFrom.setOnClickListener(this)
        binding.viewTransferToForm.imageViewBeneficiaryClose.setOnClickListener(this)
        binding.viewProposedTransactionDate.constraintLayoutProposedTransactionDate.setOnClickListener(this)
        binding.viewProposedTransactionDate.imageViewSelectedProposedTransactionDate.setOnClickListener(this)
        binding.viewTransferToForm.constraintLayoutBeneficiary.setOnClickListener(this)
        imeOptionEditText =
            ImeOptionEditText()
        imeOptionEditText.addEditText(
            binding.viewTransferToForm.textInputEditTextTransferTo,
            binding.etAmount,
            binding.textInputEditTextRemarks
        )
        imeOptionEditText.setOnImeOptionListener(this)
        imeOptionEditText.startListener()
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            if (it.eventType == InputSyncEvent.ACTION_INPUT_BENEFICIARY) {
                showBeneficiaryDetails(it)
            } else if (it.eventType == InputSyncEvent.ACTION_INPUT_OWN_ACCOUNT) {
                showBeneficiaryOwnAccount(it)
            }
        }.addTo(disposables)
        eventBus.accountSyncEvent.flowable.subscribe {
            if (it.eventType == AccountSyncEvent.ACTION_UPDATE_SELECTED_ACCOUNT) {
                showTransferFrom(it.payload)
            }
        }.addTo(disposables)
        eventBus.proposedTransferDateSyncEvent.flowable.subscribe {
            if (it.eventType == ProposedTransferDateSyncEvent.ACTION_SET_PROPOSED_TRANSFER_DATE) {
                showProposedTransactionDate(it)
            }
        }.addTo(disposables)
    }

    private fun showBeneficiaryDetails(it: BaseEvent<String>) {
        this.destinationAccount = null
        beneficiaryMaster = JsonHelper.fromJson(it.payload)
        binding.viewTransferToForm.textViewBeneficiaryCodeTitle.visibility = View.VISIBLE
        binding.viewTransferToForm.textViewBeneficiaryNameTitle.visibility = View.VISIBLE
        binding.viewTransferToForm.textViewBeneficiaryBankAccountTitle.visibility = View.VISIBLE
        binding.viewTransferToForm.textViewBeneficiaryBankAccount.visibility = View.VISIBLE
        binding.viewTransferToForm.textInputEditTextTransferTo.setText(getString(R.string.value_empty_account_number))
        binding.viewTransferToForm.textViewBeneficiaryCode.text = beneficiaryMaster?.code
        binding.viewTransferToForm.textViewBeneficiaryName.text = beneficiaryMaster?.name
        binding.viewTransferToForm.textViewBeneficiaryBankAccount.text = beneficiaryMaster?.accountNumber.formatAccountNumber()
        showBeneficiaryMasterField(true)
    }

    private fun showBeneficiaryOwnAccount(it: BaseEvent<String>) {
        this.beneficiaryMaster = null
        this.destinationAccount = JsonHelper.fromJson(it.payload)
        binding.viewTransferToForm.textInputEditTextTransferTo.setText(getString(R.string.value_empty_account_number))
        binding.viewTransferToForm.textInputLayoutTransferTo.visibility = View.GONE
        binding.viewTransferToForm.viewBorderTransferTo.visibility = View.GONE
        binding.viewTransferToForm.imageViewTransferTo.visibility = View.GONE
        binding.viewTransferToForm.constraintLayoutBeneficiary.visibility = View.VISIBLE
        binding.viewTransferToForm.textViewBeneficiaryBankAccountTitle.visibility = View.VISIBLE
        binding.viewTransferToForm.textViewBeneficiaryBankAccount.visibility = View.VISIBLE
        binding.viewTransferToForm.textViewBeneficiaryCodeTitle.visibility = View.GONE
        binding.viewTransferToForm.textViewBeneficiaryNameTitle.visibility = View.GONE
        binding.viewTransferToForm.textViewBeneficiaryBankAccountTitle.visibility = View.GONE
        binding.viewTransferToForm.textViewBeneficiaryBankAccount.visibility = View.GONE
        binding.viewTransferToForm.textViewBeneficiaryCode.text = destinationAccount?.name
        binding.viewTransferToForm.textViewBeneficiaryName.text = destinationAccount?.accountNumber.formatAccountNumber()
    }

    private fun showCancelTransactionBottomSheet() {
        val leavePageBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_leave_page),
            formatString(R.string.msg_leave_page),
            formatString(R.string.action_confirm),
            formatString(R.string.action_no)
        )
        leavePageBottomSheet.setOnConfirmationPageCallBack(object : OnConfirmationPageCallBack {
            override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
                super.onClickPositiveButtonDialog(data, tag)
                leavePageBottomSheet.dismiss()
                this@UBPFormActivity.onBackPressed(true)
            }

            override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
                super.onClickNegativeButtonDialog(data, tag)
                leavePageBottomSheet.dismiss()
            }
        })
        leavePageBottomSheet.show(
            supportFragmentManager,
            this::class.java.simpleName
        )
    }

    private fun showValidateTimeZoneBottomSheet() {
        validateTimeZoneBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_different_time_zone),
            formatString(R.string.msg_different_time_zone),
            actionNegative = formatString(R.string.action_close)
        )
        validateTimeZoneBottomSheet?.setOnConfirmationPageCallBack(this)
        validateTimeZoneBottomSheet?.show(
            supportFragmentManager,
            TAG_VALIDATE_TIME_ZONE_DIALOG
        )
    }

    private fun showProposedTransactionDate(it: BaseEvent<ProposedTransferDate>) {
        proposedTransferDate = it.payload
        if (proposedTransferDate?.immediately!!) {
            showProposedTransferDate(false)
        } else {
            showProposedTransferDate(true)
        }
    }

    private fun showTransferFrom(account: Account?) {
        account?.let {
            if (beneficiaryMaster != null) showBeneficiaryMasterField(false)
            selectedAccount = it
            permissionCollection = it.permissionCollection
            isEnableButton = true
            binding.etAmount.setCurrencySymbol(it.currency.notNullable(), true)
            binding.textInputEditTextTransferFrom.setText(
                (it.name + "\n" + it.accountNumber.formatAccountNumber())
            )
            binding.viewTransferToForm.textInputEditTextTransferTo.text?.clear()
            invalidateOptionsMenu()
            setPermission(permissionCollection!!)
        }
    }

    override fun onClick(view: View?) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return
        mLastClickTime = SystemClock.elapsedRealtime()
        when (view?.id) {
            R.id.textInputEditTextTransferFrom -> {
                navigateAccountSelectionScreen()
            }
            R.id.textInputEditTextProposedTransactionDate,
            R.id.constraintLayoutProposedTransactionDate,
            R.id.imageViewSelectedProposedTransactionDate,
            R.id.imageViewProposedTransactionDate -> {
                if (!viewUtil.isGMTPlus8()) {
                    showValidateTimeZoneBottomSheet()
                } else {
                    navigateProposedTransactionDate()
                }
            }
            R.id.imageViewTransferTo,
            R.id.constraintLayoutBeneficiary -> {
                navigateToBeneficiaryScreen()
            }
            R.id.imageViewBeneficiaryClose -> {
                showBeneficiaryMasterField(false)
            }
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
            setDefaultViewTutorial(false)
            binding.scrollView.post { binding.scrollView.smoothScrollTo(0, 0) }
        } else {
            val radius = resources.getDimension(R.dimen.field_radius)
            when (view) {
                binding.viewTutorialTransferFrom -> {
                    viewUtil.setFocusOnView(binding.scrollView, binding.viewTutorialTransferTo)
                    tutorialEngineUtil.startTutorial(
                        this,
                        binding.viewTutorialTransferTo,
                        R.layout.frame_tutorial_upper_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_ubp_form_transfer_to),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                binding.viewTutorialTransferTo -> {
                    viewUtil.setFocusOnView(binding.scrollView, binding.viewTutorialAmount)
                    tutorialEngineUtil.startTutorial(
                        this,
                        binding.viewTutorialAmount,
                        R.layout.frame_tutorial_upper_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_ubp_form_amount),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                binding.viewTutorialAmount -> {
                    viewUtil.setFocusOnView(binding.scrollView, binding.viewTutorialProposedTransactionDate)
                    tutorialEngineUtil.startTutorial(
                        this,
                        binding.viewTutorialProposedTransactionDate,
                        R.layout.frame_tutorial_lower_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_ft_form_transaction_date),
                        GravityEnum.TOP,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                binding.viewTutorialProposedTransactionDate -> {
                    viewUtil.setFocusOnView(binding.scrollView, binding.viewTutorialRemarks)
                    tutorialEngineUtil.startTutorial(
                        this,
                        binding.viewTutorialRemarks,
                        R.layout.frame_tutorial_lower_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_ft_form_remarks),
                        GravityEnum.TOP,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                binding.viewTutorialRemarks -> {
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
                else -> {
                    setDefaultViewTutorial(false)
                    binding.scrollView.post { binding.scrollView.smoothScrollTo(0, 0) }
                    // tutorialViewModel.setTutorial(TutorialScreenEnum.UBP_FORM, false)
                }
            }
        }
    }

    override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
        if (tag == TAG_VALIDATE_TIME_ZONE_DIALOG) {
            validateTimeZoneBottomSheet?.dismiss()
            navigateProposedTransactionDate()
        }
    }

    private fun setDefaultViewTutorial(isShown: Boolean) {
        if (!isShown && proposedTransferDate != null &&
            proposedTransferDate?.immediately == false
        ) {
            binding.viewProposedTransactionDate.textInputLayoutProposedTransactionDate.visibility = View.GONE
            binding.viewProposedTransactionDate.viewBorderProposedTransactionDate.visibility = View.GONE
            binding.viewProposedTransactionDate.imageViewProposedTransactionDate.visibility = View.GONE
            binding.viewProposedTransactionDate.constraintLayoutProposedTransactionDate.visibility = View.VISIBLE
            if (proposedTransferDate?.frequency == getString(R.string.title_one_time)) {
                binding.viewProposedTransactionDate.textViewEndDateTitle.visibility = View.GONE
                binding.viewProposedTransactionDate.textViewEndDate.visibility = View.GONE
            } else {
                binding.viewProposedTransactionDate.textViewEndDateTitle.visibility = View.VISIBLE
                binding.viewProposedTransactionDate.textViewEndDate.visibility = View.VISIBLE
            }
        } else {
            binding.viewProposedTransactionDate.textInputEditTextProposedTransactionDate.setText(R.string.title_immediately)
            binding.viewProposedTransactionDate.textInputLayoutProposedTransactionDate.visibility = View.VISIBLE
            binding.viewProposedTransactionDate.viewBorderProposedTransactionDate.visibility = View.VISIBLE
            binding.viewProposedTransactionDate.imageViewProposedTransactionDate.visibility = View.VISIBLE
            binding.viewProposedTransactionDate.constraintLayoutProposedTransactionDate.visibility = View.GONE
        }
    }

    private fun validateForm(isValueChanged: Boolean) {
        val transferFromObservable = viewUtil.rxTextChanges(
            true,
            isValueChanged,
            resources.getInteger(R.integer.min_length_field),
            resources.getInteger(R.integer.max_length_field_100),
            binding.textInputEditTextTransferFrom
        )
        val transferToObservable = viewUtil.rxTextChanges(
            true,
            isValueChanged,
            resources.getInteger(R.integer.max_length_account_number_ubp),
            resources.getInteger(R.integer.max_length_account_number_ubp),
            binding.viewTransferToForm.textInputEditTextTransferTo,
            customErrorMessage = formatString(
                R.string.error_account_number_length,
                resources.getInteger(R.integer.max_length_account_number_ubp_without_mask)
            )
        )
        val amountObservable = viewUtil.rxTextChangesAmount(
            true,
            isValueChanged,
            binding.etAmount,
            RxValidator.PatternMatch(
                formatString(R.string.error_input_valid_amount),
                Pattern.compile(ViewUtil.REGEX_FORMAT_VALID_AMOUNT)
            ),
            RxValidator.PatternMatch(
                formatString(R.string.error_specific_field, formatString(R.string.title_amount)),
                Pattern.compile(ViewUtil.REGEX_FORMAT_AMOUNT)
            ),
            RxValidator.PatternMatch(
                formatString(R.string.error_input_valid_amount),
                Pattern.compile(ViewUtil.REGEX_FORMAT_AMOUNT_OPTIONAL)
            )
        )
        initSetError(transferFromObservable)
        initSetError(transferToObservable)
        initSetError(amountObservable)

        RxCombineValidator(
            transferToObservable,
            amountObservable
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

    private fun initEditTextDefaultValue() {
        if (binding.textInputEditTextTransferFrom.length() != 0) {
            binding.textInputEditTextTransferFrom.setText(
                binding.textInputEditTextTransferFrom.text.toString()
            )
        }
        if (binding.viewTransferToForm.textInputEditTextTransferTo.length() != 0) {
            binding.viewTransferToForm.textInputEditTextTransferTo.setText(
                binding.viewTransferToForm.textInputEditTextTransferTo.text.toString()
            )
        }
        if (binding.etAmount.length() != 0) {
            binding.etAmount.setText(binding.etAmount.text.toString())
        }
    }

    private fun showBeneficiaryMasterField(isShown: Boolean) {
        if (isShown) {
            binding.viewTransferToForm.textInputLayoutTransferTo.visibility = View.GONE
            binding.viewTransferToForm.imageViewTransferTo.visibility = View.GONE
            binding.viewTransferToForm.viewBorderTransferTo.visibility = View.GONE
            binding.viewTransferToForm.constraintLayoutBeneficiary.visibility = View.VISIBLE
            binding.viewTransferToForm.constraintLayoutBeneficiary.setOnClickListener {
                navigateToBeneficiaryScreen()
            }
        } else {
            this.destinationAccount = null
            this.beneficiaryMaster = null
            binding.viewTransferToForm.textInputEditTextTransferTo.text?.clear()
            binding.viewTransferToForm.textInputLayoutTransferTo.visibility = View.VISIBLE
            binding.viewTransferToForm.viewBorderTransferTo.visibility = View.VISIBLE
            binding.viewTransferToForm.imageViewTransferTo.visibility = View.VISIBLE
            binding.viewTransferToForm.constraintLayoutBeneficiary.visibility = View.GONE
        }
    }

    private fun showProposedTransferDate(isShown: Boolean) {
        if (isShown) {
            binding.viewProposedTransactionDate.textInputLayoutProposedTransactionDate.visibility = View.GONE
            binding.viewProposedTransactionDate.viewBorderProposedTransactionDate.visibility = View.GONE
            binding.viewProposedTransactionDate.imageViewProposedTransactionDate.visibility = View.GONE
            binding.viewProposedTransactionDate.constraintLayoutProposedTransactionDate.visibility = View.VISIBLE
            if (proposedTransferDate?.frequency == getString(R.string.title_one_time)) {
                binding.viewProposedTransactionDate.textViewEndDateTitle.visibility = View.GONE
                binding.viewProposedTransactionDate.textViewEndDate.visibility = View.GONE
            } else {
                binding.viewProposedTransactionDate.root.visibility = View.VISIBLE
                binding.viewProposedTransactionDate.textViewEndDate.visibility = View.VISIBLE
            }
            binding.viewProposedTransactionDate.textViewStartDate.text =
                viewUtil.getDateFormatByDateString(
                    proposedTransferDate?.startDate,
                    ViewUtil.DATE_FORMAT_ISO,
                    ViewUtil.DATE_FORMAT_DEFAULT
                )
            binding.viewProposedTransactionDate.textViewFrequency.text = proposedTransferDate?.frequency
            if (proposedTransferDate?.endDate != null) {
                val endDateDesc = viewUtil.getDateFormatByDateString(
                    proposedTransferDate?.endDate,
                    ViewUtil.DATE_FORMAT_ISO,
                    ViewUtil.DATE_FORMAT_DATE
                )
                binding.viewProposedTransactionDate.textViewEndDate.text =
                    ("${proposedTransferDate?.occurrencesText}\n(Until $endDateDesc)")
            } else {
                binding.viewProposedTransactionDate.textViewEndDate.text = proposedTransferDate?.occurrencesText
            }
        } else {
            binding.viewProposedTransactionDate.textInputEditTextProposedTransactionDate.setText(R.string.title_immediately)
            binding.viewProposedTransactionDate.textInputLayoutProposedTransactionDate.visibility = View.VISIBLE
            binding.viewProposedTransactionDate.viewBorderProposedTransactionDate.visibility = View.VISIBLE
            binding.viewProposedTransactionDate.imageViewProposedTransactionDate.visibility = View.VISIBLE
            binding.viewProposedTransactionDate.constraintLayoutProposedTransactionDate.visibility = View.GONE
        }
    }

    private fun setPermission(permissionCollection: PermissionCollection) {
        if (permissionCollection.hasAllowToCreateTransactionAdhoc) {
            binding.viewTransferToForm.textInputLayoutTransferTo.setBoxBackgroundColorResource(R.color.colorTransparent)
            binding.viewTransferToForm.textInputEditTextTransferTo.isEnabled = true
            binding.viewTransferToForm.viewImageViewBackground.background = null
        } else {
            binding.viewTransferToForm.textInputLayoutTransferTo.setBoxBackgroundColorResource(R.color.colorDisableTextInputEditText)
            binding.viewTransferToForm.textInputEditTextTransferTo.isEnabled = false
            binding.viewTransferToForm.viewImageViewBackground.background =
                ContextCompat.getDrawable(this, R.drawable.bg_edittext_right_form)
        }

        if (permissionCollection.hasAllowToCreateTransactionOwn ||
            permissionCollection.hasAllowToCreateTransactionBeneficiaryMaster
        ) {
            binding.viewTransferToForm.imageViewTransferTo.setOnClickListener(this)
        } else {
            binding.viewTransferToForm.imageViewTransferTo.setOnClickListener(null)
        }

        if (selectedAccount != null) {
            binding.viewProposedTransactionDate.textInputEditTextProposedTransactionDate.setOnClickListener(this)
            binding.viewProposedTransactionDate.imageViewProposedTransactionDate.setOnClickListener(this)
            binding.viewProposedTransactionDate.textInputLayoutProposedTransactionDate.setBoxBackgroundColorResource(R.color.colorTransparent)
            binding.textInputLayoutRemarks.setBoxBackgroundColorResource(R.color.colorTransparent)
            binding.etAmount.setEnableAmount(true)
            binding.viewProposedTransactionDate.textInputEditTextProposedTransactionDate.isEnabled = true
            binding.textInputEditTextRemarks.isEnabled = true
        }
    }

    private fun navigateToBeneficiaryScreen() {
        val bundle = Bundle()
        bundle.putString(
            BeneficiaryActivity.EXTRA_PAGE,
            BeneficiaryActivity.PAGE_UBP_FORM
        )
        bundle.putString(
            BeneficiaryActivity.EXTRA_ID,
            channel.id.toString()
        )
        bundle.putString(BeneficiaryActivity.EXTRA_CURRENCY, selectedAccount?.currency)
        bundle.putString(
            BeneficiaryActivity.EXTRA_SOURCE_ACCOUNT_ID,
            selectedAccount?.id.toString()
        )
        if (destinationAccount != null)
            bundle.putString(
                BeneficiaryActivity.EXTRA_DESTINATION_ACCOUNT_ID,
                destinationAccount?.id?.toString()
            )
        bundle.putString(
            BeneficiaryActivity.EXTRA_CHANNEL_ID,
            channel.id.toString()
        )
        bundle.putString(
            BeneficiaryActivity.EXTRA_PERMISSION_ID,
            intent.getStringExtra(EXTRA_ID)
        )
        bundle.putString(
            BeneficiaryActivity.EXTRA_PERMISSION,
            JsonHelper.toJson(permissionCollection)
        )
        navigator.navigate(
            this,
            BeneficiaryActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateAccountSelectionScreen() {
        val bundle = Bundle()
        bundle.putString(
            AccountSelectionActivity.EXTRA_PAGE,
            AccountSelectionActivity.PAGE_FUND_TRANSFER
        )
        if (selectedAccount != null)
            bundle.putString(
                AccountSelectionActivity.EXTRA_ID,
                selectedAccount?.id?.toString()
            )
        bundle.putString(
            AccountSelectionActivity.EXTRA_CHANNEL_ID,
            channel.id.toString()
        )
        bundle.putString(
            AccountSelectionActivity.EXTRA_PERMISSION_ID,
            intent.getStringExtra(EXTRA_ID)
        )
        navigator.navigate(
            this,
            AccountSelectionActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateProposedTransactionDate() {
        val bundle = Bundle()
        if (proposedTransferDate != null && !proposedTransferDate?.immediately!!)
            bundle.putString(
                ProposedTransferDateActivity.EXTRA_PTD,
                JsonHelper.toJson(proposedTransferDate)
            )
        bundle.putBoolean(
            ProposedTransferDateActivity.EXTRA_HAS_VALID_FORM,
            selectedAccount != null
        )
        bundle.putString(
            ProposedTransferDateActivity.EXTRA_PAGE,
            ProposedTransferDateActivity.PAGE_FUND_TRANSFER
        )
        if (permissionCollection != null)
            bundle.putBoolean(
                ProposedTransferDateActivity.EXTRA_SCHEDULED_PERMISSION,
                permissionCollection?.hasAllowToCreateTransactionScheduled!!
            )
        navigator.navigate(
            this,
            ProposedTransferDateActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
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

    private fun showMissingFieldDialog() {
        showMaterialDialogError(
            message = formatString(R.string.msg_error_missing_fields)
        )
    }

    private fun submitForm() {
        if (proposedTransferDate == null) {
            proposedTransferDate = ProposedTransferDate()
            proposedTransferDate?.immediately = true
            proposedTransferDate?.startDate = viewUtil.getDateFormatByTimeMilliSeconds(
                viewUtil.getCurrentDate(),
                ViewUtil.DATE_FORMAT_ISO_Z
            )
        }
        val fundTransferUBPForm =
            FundTransferUBPForm()
        fundTransferUBPForm.channelId = channel.id.toString()
        fundTransferUBPForm.immediate = proposedTransferDate?.immediately
        fundTransferUBPForm.amount = binding.etAmount.getNumericValue()
        fundTransferUBPForm.senderAccountNumber = selectedAccount?.accountNumber
        fundTransferUBPForm.referenceNumber = viewModel.uuid.value
        fundTransferUBPForm.receiverAccountNumber =
            if (destinationAccount != null) {
                destinationAccount?.accountNumber
            } else {
                if (binding.viewTransferToForm.textInputEditTextTransferTo.text.toString() !=
                    getString(R.string.value_empty_account_number)
                )
                    binding.viewTransferToForm.textInputEditTextTransferTo.text.toString().replace(" ", "")
                else null
            }
        fundTransferUBPForm.remarks = binding.textInputEditTextRemarks.text.toString()
        fundTransferUBPForm.beneficiaryMasterForm =
            if (beneficiaryMaster != null)
                ConstantHelper.Object.getBeneficiaryForm(this, beneficiaryMaster!!)
            else null
        fundTransferUBPForm.beneficiaryMasterId = beneficiaryMaster?.id
        fundTransferUBPForm.transferDate = proposedTransferDate?.startDate
        fundTransferUBPForm.frequency = proposedTransferDate?.frequency
        fundTransferUBPForm.occurrencesText = proposedTransferDate?.occurrencesText
        fundTransferUBPForm.occurrences =
            if (proposedTransferDate?.occurrences == 0) null
            else proposedTransferDate?.occurrences
        fundTransferUBPForm.recurrenceTypeId = proposedTransferDate?.recurrenceTypeId ?: "1"
        if (proposedTransferDate?.immediately == false) {
            fundTransferUBPForm.recurrenceEndDate = proposedTransferDate?.endDate
        }
        val bundle = Bundle()
        bundle.putString(
            UBPConfirmationActivity.EXTRA_ACCOUNT,
            JsonHelper.toJson(selectedAccount)
        )
        bundle.putString(
            UBPConfirmationActivity.EXTRA_ACCOUNT_TYPE,
            getString(R.string.title_ubp_channel)
        )
        bundle.putString(
            UBPConfirmationActivity.EXTRA_FUND_TRANSFER,
            JsonHelper.toJson(fundTransferUBPForm)
        )
        bundle.putString(
            UBPConfirmationActivity.EXTRA_SERVICE_FEE,
            intent.getStringExtra(EXTRA_SERVICE_FEE)
        )
        bundle.putString(
            UBPConfirmationActivity.EXTRA_REMINDERS,
            JsonHelper.toJson(channel.formattedReminders)
        )
        navigator.navigate(
            this,
            UBPConfirmationActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun initChannelView(channel: Channel) {
        binding.viewChannelHeader.textViewChannel.visibility = View.GONE
        binding.viewChannelHeader.imageViewChannel.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.viewChannelHeader.imageViewChannel.setImageResource(
            when {
                channel.id == ChannelBankEnum.BILLS_PAYMENT.getChannelId() ->
                    R.drawable.ic_channel_bills_payment
                channel.id == ChannelBankEnum.UBP_TO_UBP.getChannelId() ->
                    R.drawable.ic_channel_ubp
                channel.id == ChannelBankEnum.PESONET.getChannelId() ->
                    R.drawable.ic_channel_pesonet
                channel.id == ChannelBankEnum.SWIFT.getChannelId() ->
                    R.drawable.ic_channel_swift
                channel.id == ChannelBankEnum.INSTAPAY.getChannelId() ->
                    R.drawable.ic_channel_instapay
                channel.id == ChannelBankEnum.PDDTS.getChannelId() ->
                    R.drawable.ic_channel_pddts
                else -> R.drawable.ic_fund_transfer_other_banks_orange
            }
        )
        binding.viewChannelHeader.textViewChannel.text = channel.contextChannel?.displayName
        binding.viewChannelHeader.textViewServiceFee.text = if (intent.getStringExtra(EXTRA_SERVICE_FEE) != null) {
            val serviceFee = JsonHelper.fromJson<ServiceFee>(
                intent.getStringExtra(EXTRA_SERVICE_FEE)
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
    }

    private fun startViewTutorial() {
        val radius = resources.getDimension(R.dimen.field_radius)
        setDefaultViewTutorial(true)
        tutorialEngineUtil.startTutorial(
            this,
            binding.viewTutorialTransferFrom,
            R.layout.frame_tutorial_upper_left,
            radius,
            false,
            getString(R.string.msg_tutorial_ubp_form_transfer_from),
            GravityEnum.BOTTOM,
            OverlayAnimationEnum.ANIM_EXPLODE
        )
    }

    private fun clearFormFocus() {
        binding.constraintLayoutParent.requestFocus()
        binding.constraintLayoutParent.isFocusableInTouchMode = true
    }

    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_CHANNEL = "channel"
        const val EXTRA_SERVICE_FEE = "service_fee"

        const val TAG_VALIDATE_TIME_ZONE_DIALOG = "validate_time_zone_dialog"
    }

    override val viewModelClassType: Class<UBPViewModel>
        get() = UBPViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityFundTransferFormUbpBinding
        get() = ActivityFundTransferFormUbpBinding::inflate
}
