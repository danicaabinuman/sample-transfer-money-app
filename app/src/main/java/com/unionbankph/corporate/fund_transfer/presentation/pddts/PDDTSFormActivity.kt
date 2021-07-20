package com.unionbankph.corporate.fund_transfer.presentation.pddts

import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItems
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
import com.unionbankph.corporate.databinding.ActivityFundTransferFormPesonetBinding
import com.unionbankph.corporate.fund_transfer.data.form.FundTransferPesoNetForm
import com.unionbankph.corporate.fund_transfer.data.model.Bank
import com.unionbankph.corporate.fund_transfer.data.model.Beneficiary
import com.unionbankph.corporate.fund_transfer.data.model.Purpose
import com.unionbankph.corporate.fund_transfer.presentation.bank.BankActivity
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_selection.BeneficiaryActivity
import com.unionbankph.corporate.fund_transfer.presentation.proposed_transfer.ProposedTransferDateActivity
import io.reactivex.rxkotlin.addTo
import java.util.regex.Pattern

class PDDTSFormActivity :
    BaseActivity<ActivityFundTransferFormPesonetBinding, PDDTSViewModel>(),
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

    private var purposes: MutableList<Purpose> = mutableListOf()

    private var selectedAccount: Account? = null

    private var selectedBank: Bank? = null

    private var beneficiaryMaster: Beneficiary? = null

    private var purpose: Purpose? = null

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
        // tutorialViewModel.hasTutorial(TutorialScreenEnum.PDDTS_FORM)
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        binding.viewToolbar.textViewTitle,
                        binding.viewToolbar.textViewCorporationName,
                        formatString(R.string.title_pddts),
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
                is ShowPDDTSLoading -> {
                    showProgressAlertDialog(PDDTSFormActivity::class.java.simpleName)
                }
                is ShowPDDTSDismissLoading -> dismissProgressAlertDialog()

                is ShowPDDTSGetPurposes -> {
                    purposes = it.list
                    showPurposesList()
                }
                is ShowPDDTSError -> {
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
            permissionId = intent.getStringExtra(EXTRA_ID),
            destinationId = selectedAccount?.id?.toString()
        )
    }

    override fun onViewsBound() {
        super.onViewsBound()
        binding.etAmount.setEnableAmount(false)
        binding.etAmount.setCurrencySymbol(formatString(R.string.title_usd), true)
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
        eventBus.accountSyncEvent.flowable.subscribe {
            if (it.eventType == AccountSyncEvent.ACTION_UPDATE_SELECTED_ACCOUNT) {
                showTransferFrom(it.payload)
            }
        }.addTo(disposables)
        eventBus.inputSyncEvent.flowable.subscribe {
            if (it.eventType == InputSyncEvent.ACTION_INPUT_BANK_RECEIVER) {
                showReceivingBank(it)
            } else if (it.eventType == InputSyncEvent.ACTION_INPUT_BENEFICIARY) {
                showBeneficiaryDetails(it)
            }
        }.addTo(disposables)
        eventBus.proposedTransferDateSyncEvent.flowable.subscribe {
            if (it.eventType == ProposedTransferDateSyncEvent.ACTION_SET_PROPOSED_TRANSFER_DATE) {
                proposedTransferDate = it.payload
                if (proposedTransferDate?.immediately!!) {
                    showProposedTransferDate(false)
                } else {
                    showProposedTransferDate(true)
                }
            }
        }.addTo(disposables)
        viewUtil.setEditTextMaskListener(
            binding.textInputEditTextAccountNumber,
            getString(R.string.hint_other_bank_account_number_format)
        )
        binding.textInputEditTextTransferFrom.setOnClickListener(this)
        binding.viewReceivingBankForm.textInputEditTextReceivingBank.setOnClickListener(this)
        binding.viewPurpose.textInputEditTextPurpose.setOnClickListener(this)
        binding.viewReceivingBankForm.imageViewBeneficiaryClose.setOnClickListener(this)
        binding.viewProposedTransactionDate.imageViewSelectedProposedTransactionDate.setOnClickListener(this)
        imeOptionEditText =
            ImeOptionEditText()
        imeOptionEditText.addEditText(
            binding.viewTransferToForm.textInputEditTextTransferTo,
            binding.textInputEditTextAccountNumber,
            binding.etAmount,
            binding.textInputEditTextRemarks
        )
        imeOptionEditText.setOnImeOptionListener(this)
        imeOptionEditText.startListener()
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
                this@PDDTSFormActivity.onBackPressed(true)
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

    private fun showBeneficiaryDetails(it: BaseEvent<String>) {
        beneficiaryMaster = JsonHelper.fromJson(it.payload)
        binding.viewReceivingBankForm.textViewBeneficiaryAddressTitle.visibility = View.GONE
        binding.viewReceivingBankForm.textViewBeneficiaryAddress.visibility = View.GONE
        binding.viewTransferToForm.textInputEditTextTransferTo.setText(beneficiaryMaster?.name)
        binding.textInputEditTextAccountNumber.setText(beneficiaryMaster?.accountNumber.formatAccountNumber())
        binding.viewReceivingBankForm.textInputEditTextReceivingBank.setText(beneficiaryMaster?.bankDetails?.name)
        binding.viewReceivingBankForm.textViewBeneficiaryCode.text = beneficiaryMaster?.code
        binding.viewReceivingBankForm.textViewBeneficiaryName.text = beneficiaryMaster?.name
        binding.viewReceivingBankForm.textViewBeneficiaryBankAccount.text = beneficiaryMaster?.accountNumber.formatAccountNumber()
        binding.viewReceivingBankForm.textViewReceivingBank.text = beneficiaryMaster?.bankDetails?.name
        showBeneficiaryMasterField(true)
    }

    private fun showReceivingBank(it: BaseEvent<String>) {
        selectedBank = JsonHelper.fromJson(it.payload)
        binding.viewReceivingBankForm.textInputEditTextReceivingBank.setText(selectedBank?.bank)
    }

    private fun showTransferFrom(account: Account?) {
        account?.let {
            if (beneficiaryMaster != null) showBeneficiaryMasterField(false)
            selectedAccount = it
            permissionCollection = it.permissionCollection
            isEnableButton = true
            binding.etAmount.setCurrencySymbol(selectedAccount?.currency.notNullable(), true)
            binding.viewTransferToForm.textInputEditTextTransferTo.text?.clear()
            binding.textInputEditTextAccountNumber.text?.clear()
            binding.viewReceivingBankForm.textInputEditTextReceivingBank.text?.clear()
            binding.textInputEditTextTransferFrom.setText(
                (it.name + "\n" + it.accountNumber.formatAccountNumber())
            )
            invalidateOptionsMenu()
            setPermission(permissionCollection!!)
        }
    }

    override fun onClick(view: View?) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return
        mLastClickTime = SystemClock.elapsedRealtime()
        when (view?.id) {
            R.id.textInputEditTextTransferFrom -> {
                navigateAccountSelection()
            }
            R.id.textInputEditTextProposedTransactionDate,
            R.id.imageViewProposedTransactionDate,
            R.id.constraintLayoutProposedTransactionDate,
            R.id.imageViewSelectedProposedTransactionDate -> {
                if (!viewUtil.isGMTPlus8()) {
                    showValidateTimeZoneBottomSheet()
                } else {
                    navigateProposedTransactionDate()
                }
            }
            R.id.textInputEditTextReceivingBank -> {
                navigateBanks()
            }
            R.id.textInputEditTextPurpose -> {
                if (purposes.isNotEmpty()) {
                    showPurposesList()
                } else {
                    viewModel.getPurposes()
                }
            }
            R.id.imageViewTransferTo -> {
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
                    tutorialEngineUtil.startTutorial(
                        this,
                        binding.viewTutorialTransferTo,
                        R.layout.frame_tutorial_upper_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_pesonet_form_transfer_to),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                binding.viewTutorialTransferTo -> {
                    tutorialEngineUtil.startTutorial(
                        this,
                        binding.textInputLayoutAccountNumber,
                        R.layout.frame_tutorial_upper_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_pesonet_form_account_number),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                binding.textInputLayoutAccountNumber -> {
                    viewUtil.setFocusOnView(binding.scrollView, binding.viewReceivingBankForm.root)
                    tutorialEngineUtil.startTutorial(
                        this,
                        binding.viewReceivingBankForm.root,
                        R.layout.frame_tutorial_upper_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_pesonet_form_receiving_bank),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                binding.viewReceivingBankForm.root -> {
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
                    viewUtil.setFocusOnView(binding.scrollView, binding.viewTutorialPurpose)
                    tutorialEngineUtil.startTutorial(
                        this,
                        binding.viewTutorialPurpose,
                        R.layout.frame_tutorial_lower_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_pesonet_form_purpose),
                        GravityEnum.TOP,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                binding.viewTutorialPurpose -> {
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
                    // tutorialViewModel.setTutorial(TutorialScreenEnum.PDDTS_FORM, false)
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

    private fun setDefaultProposedDate(isShown: Boolean) {
        if (isShown && proposedTransferDate != null) {
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

    private fun setDefaultViewTutorial(isShown: Boolean) {
        if (!isShown && beneficiaryMaster != null) {
            binding.viewTransferToForm.textInputLayoutTransferTo.visibility = View.GONE
            binding.viewTransferToForm.imageViewTransferTo.visibility = View.GONE
            binding.textInputLayoutAccountNumber.visibility = View.GONE
            binding.viewReceivingBankForm.textInputLayoutReceivingBank.visibility = View.GONE
            binding.viewReceivingBankForm.constraintLayoutBeneficiary.visibility = View.VISIBLE
        } else {
            binding.viewTransferToForm.textInputLayoutTransferTo.visibility = View.VISIBLE
            binding.viewTransferToForm.imageViewTransferTo.visibility = View.VISIBLE
            binding.textInputLayoutAccountNumber.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textInputLayoutReceivingBank.visibility = View.VISIBLE
            binding.viewReceivingBankForm.constraintLayoutBeneficiary.visibility = View.GONE
        }
        setDefaultProposedDate(!isShown)
    }

    private fun showPurposesList() {
        val purposesDesc = purposes
            .asSequence()
            .map { purposeDesc -> purposeDesc.description.notNullable() }
            .toMutableList()
        MaterialDialog(this).show {
            lifecycleOwner(this@PDDTSFormActivity)
            title(R.string.hint_select_purpose)
            listItems(
                items = purposesDesc,
                selection = { _, index, text ->
                    purpose = purposes[index]
                    if (purpose?.description.equals("Others", true)) {
                        this@PDDTSFormActivity.binding.viewPurpose.textInputLayoutLeavePurpose.visibility = View.VISIBLE
                        this@PDDTSFormActivity.binding.viewPurpose.textInputEditTextLeavePurpose.text?.clear()
                    } else {
                        this@PDDTSFormActivity.binding.viewPurpose.textInputLayoutLeavePurpose.visibility = View.GONE
                        this@PDDTSFormActivity.binding.viewPurpose.textInputEditTextLeavePurpose.setText("-")
                    }
                    this@PDDTSFormActivity.binding.viewPurpose.textInputEditTextPurpose.setText(text.toString())
                })
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
            resources.getInteger(R.integer.min_length_field),
            resources.getInteger(R.integer.max_length_field_100),
            binding.viewTransferToForm.textInputEditTextTransferTo
        )
        val accountNumberObservable =
            viewUtil.rxTextChanges(
                true,
                isValueChanged,
                resources.getInteger(R.integer.min_length_account_number),
                resources.getInteger(R.integer.max_length_field_100),
                binding.textInputEditTextAccountNumber,
                customErrorMessage = formatString(
                    R.string.error_validation_custom_min_length,
                    formatString(R.string.title_account_number),
                    resources.getInteger(R.integer.min_length_account_number).minus(1)
                )
            )
        val receivingBankObservable =
            viewUtil.rxTextChanges(
                true,
                isValueChanged,
                resources.getInteger(R.integer.min_length_field),
                resources.getInteger(R.integer.max_length_field_100),
                binding.viewReceivingBankForm.textInputEditTextReceivingBank
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
        val purposeOservable =
            viewUtil.rxTextChanges(
                true,
                isValueChanged,
                resources.getInteger(R.integer.min_length_field),
                resources.getInteger(R.integer.max_length_field_100),
                binding.viewPurpose.textInputEditTextPurpose
            )

        val leavePurposeOservable =
            viewUtil.rxTextChanges(
                true,
                isValueChanged,
                resources.getInteger(R.integer.min_length_field),
                resources.getInteger(R.integer.max_length_field_100),
                binding.viewPurpose.textInputEditTextLeavePurpose
            )

        initSetError(transferFromObservable)
        initSetError(transferToObservable)
        initSetError(accountNumberObservable)
        initSetError(receivingBankObservable)
        initSetError(amountObservable)
        initSetError(purposeOservable)
        initSetError(leavePurposeOservable)

        RxCombineValidator(
            transferFromObservable,
            transferToObservable,
            accountNumberObservable,
            receivingBankObservable,
            amountObservable,
            purposeOservable,
            leavePurposeOservable
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
        if (binding.textInputEditTextAccountNumber.length() != 0) {
            binding.textInputEditTextAccountNumber.setText(
                binding.textInputEditTextAccountNumber.text.toString()
            )
        }
        if (binding.viewReceivingBankForm.textInputEditTextReceivingBank.length() != 0) {
            binding.viewReceivingBankForm.textInputEditTextReceivingBank.setText(
                binding.viewReceivingBankForm.textInputEditTextReceivingBank.text.toString()
            )
        }
        if (binding.etAmount.length() != 0) {
            binding.etAmount.setText(binding.etAmount.text.toString())
        }
        if (binding.viewPurpose.textInputEditTextPurpose.length() != 0) {
            binding.viewPurpose.textInputEditTextPurpose.setText(binding.viewPurpose.textInputEditTextPurpose.text.toString())
        }
        if (binding.viewPurpose.textInputEditTextLeavePurpose.length() != 0) {
            binding.viewPurpose.textInputEditTextLeavePurpose.setText(
                binding.viewPurpose.textInputEditTextLeavePurpose.text.toString()
            )
        }
    }

    private fun showBeneficiaryMasterField(isShown: Boolean) {
        if (isShown) {
            binding.viewTransferToForm.textInputLayoutTransferTo.visibility = View.GONE
            binding.viewTransferToForm.viewBorderTransferTo.visibility = View.GONE
            binding.viewTransferToForm.imageViewTransferTo.visibility = View.GONE
            binding.textInputLayoutAccountNumber.visibility = View.GONE
            binding.viewReceivingBankForm.textInputLayoutReceivingBank.visibility = View.GONE
            binding.viewReceivingBankForm.constraintLayoutBeneficiary.visibility = View.VISIBLE
            binding.viewReceivingBankForm.constraintLayoutBeneficiary.setOnClickListener {
                navigateToBeneficiaryScreen()
            }
        } else {
            this.beneficiaryMaster = null
            this.selectedBank = null
            binding.viewTransferToForm.textInputEditTextTransferTo.text?.clear()
            binding.textInputEditTextAccountNumber.text?.clear()
            binding.viewReceivingBankForm.textInputEditTextReceivingBank.text?.clear()
            binding.viewTransferToForm.textInputLayoutTransferTo.visibility = View.VISIBLE
            binding.viewTransferToForm.viewBorderTransferTo.visibility = View.VISIBLE
            binding.viewTransferToForm.imageViewTransferTo.visibility = View.VISIBLE
            binding.textInputLayoutAccountNumber.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textInputLayoutReceivingBank.visibility = View.VISIBLE
            binding.viewReceivingBankForm.constraintLayoutBeneficiary.visibility = View.GONE
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
                binding.viewProposedTransactionDate.textViewEndDateTitle.visibility = View.VISIBLE
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
            binding.viewProposedTransactionDate.imageViewProposedTransactionDate.visibility = View.VISIBLE
            binding.viewProposedTransactionDate.constraintLayoutProposedTransactionDate.visibility = View.GONE
        }
    }

    private fun setPermission(permissionCollection: PermissionCollection) {
        if (permissionCollection.hasAllowToCreateTransactionAdhoc) {
            binding.viewTransferToForm.textInputLayoutTransferTo.setBoxBackgroundColorResource(R.color.colorTransparent)
            binding.textInputLayoutAccountNumber.setBoxBackgroundColorResource(R.color.colorTransparent)
            binding.viewReceivingBankForm.textInputLayoutReceivingBank.setBoxBackgroundColorResource(R.color.colorTransparent)
            binding.viewTransferToForm.textInputEditTextTransferTo.isEnabled = true
            binding.textInputEditTextAccountNumber.isEnabled = true
            binding.viewReceivingBankForm.textInputEditTextReceivingBank.isEnabled = true
            binding.viewTransferToForm.viewImageViewBackground.background = null
        } else {
            binding.viewTransferToForm.textInputLayoutTransferTo.setBoxBackgroundColorResource(R.color.colorDisableTextInputEditText)
            binding.textInputLayoutAccountNumber.setBoxBackgroundColorResource(R.color.colorDisableTextInputEditText)
            binding.viewReceivingBankForm.textInputLayoutReceivingBank.setBoxBackgroundColorResource(R.color.colorDisableTextInputEditText)
            binding.viewTransferToForm.textInputEditTextTransferTo.isEnabled = false
            binding.textInputEditTextAccountNumber.isEnabled = false
            binding.viewReceivingBankForm.textInputEditTextReceivingBank.isEnabled = false
            binding.viewTransferToForm.viewImageViewBackground.background =
                ContextCompat.getDrawable(this, R.drawable.bg_edittext_right_form)
        }

        if (permissionCollection.hasAllowToCreateTransactionBeneficiaryMaster) {
            binding.viewTransferToForm.imageViewTransferTo.setOnClickListener(this)
        } else {
            binding.viewTransferToForm.imageViewTransferTo.setOnClickListener(null)
        }

        if (selectedAccount != null) {
            binding.viewProposedTransactionDate.textInputEditTextProposedTransactionDate.setOnClickListener(this)
            binding.viewProposedTransactionDate.imageViewProposedTransactionDate.setOnClickListener(this)
            binding.viewProposedTransactionDate.textInputLayoutProposedTransactionDate.setBoxBackgroundColorResource(R.color.colorTransparent)
            binding.viewPurpose.textInputLayoutPurpose.setBoxBackgroundColorResource(R.color.colorTransparent)
            binding.textInputLayoutRemarks.setBoxBackgroundColorResource(R.color.colorTransparent)
            binding.etAmount.setEnableAmount(true)
            binding.viewProposedTransactionDate.textInputEditTextProposedTransactionDate.isEnabled = true
            binding.viewPurpose.textInputEditTextPurpose.isEnabled = true
            binding.textInputEditTextRemarks.isEnabled = true
        }
    }

    private fun navigateToBeneficiaryScreen() {
        val bundle = Bundle()
        bundle.putString(
            BeneficiaryActivity.EXTRA_ID,
            channel.id.toString()
        )
        bundle.putString(
            BeneficiaryActivity.EXTRA_SOURCE_ACCOUNT_ID,
            selectedAccount?.id.toString()
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

    private fun navigateAccountSelection() {
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
            false,
            true,
            Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
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

    private fun navigateBanks() {
        val bundle = Bundle()
        bundle.putString(
            BankActivity.EXTRA_CHANNEL_ID,
            channel.id.toString()
        )
        navigator.navigate(
            this,
            BankActivity::class.java,
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
        val fundTransferPesoNetForm =
            FundTransferPesoNetForm()
        fundTransferPesoNetForm.channelId = channel.id.toString()
        fundTransferPesoNetForm.immediate = proposedTransferDate?.immediately
        fundTransferPesoNetForm.referenceNumber = viewModel.uuid.value
        fundTransferPesoNetForm.purpose = purpose?.code
        fundTransferPesoNetForm.purposeDesc =
            if (purpose?.description.equals("Others", true))
                binding.viewPurpose.textInputEditTextLeavePurpose.text.toString()
            else purpose?.description
        fundTransferPesoNetForm.receivingBankName = binding.viewReceivingBankForm.textInputEditTextReceivingBank.text.toString()
        fundTransferPesoNetForm.receivingBank = selectedBank?.pddtsFinacleCode
        fundTransferPesoNetForm.instructions =
            if (binding.viewPurpose.textInputEditTextLeavePurpose.text.toString() != "-")
                binding.viewPurpose.textInputEditTextLeavePurpose.text.toString()
            else null
        fundTransferPesoNetForm.amount = binding.etAmount.getNumericValue()
        fundTransferPesoNetForm.beneficiaryMasterForm =
            if (beneficiaryMaster != null)
                ConstantHelper.Object.getBeneficiaryForm(this, beneficiaryMaster!!) else null
        fundTransferPesoNetForm.beneficiaryMasterId = beneficiaryMaster?.id
        fundTransferPesoNetForm.beneficiaryName = binding.viewTransferToForm.textInputEditTextTransferTo.text.toString()
        fundTransferPesoNetForm.senderAccountNumber = selectedAccount?.accountNumber
        fundTransferPesoNetForm.receiverAccountNumber =
            binding.textInputEditTextAccountNumber.text.toString().replace(" ", "")
        fundTransferPesoNetForm.remarks = binding.textInputEditTextRemarks.text.toString()
        fundTransferPesoNetForm.transferDate = proposedTransferDate?.startDate
        fundTransferPesoNetForm.frequency = proposedTransferDate?.frequency
        fundTransferPesoNetForm.occurrencesText = proposedTransferDate?.occurrencesText
        fundTransferPesoNetForm.occurrences =
            if (proposedTransferDate?.occurrences == 0) null
            else proposedTransferDate?.occurrences
        fundTransferPesoNetForm.recurrenceTypeId = proposedTransferDate?.recurrenceTypeId ?: "1"
        if (proposedTransferDate?.immediately == false) {
            fundTransferPesoNetForm.recurrenceEndDate = proposedTransferDate?.endDate
        }
        val bundle = Bundle()
        bundle.putString(
            PDDTSConfirmationActivity.EXTRA_ACCOUNT,
            JsonHelper.toJson(selectedAccount)
        )
        bundle.putString(
            PDDTSConfirmationActivity.EXTRA_ACCOUNT_TYPE,
            getString(R.string.title_pddts)
        )
        bundle.putString(
            PDDTSConfirmationActivity.EXTRA_FUND_TRANSFER,
            JsonHelper.toJson(fundTransferPesoNetForm)
        )
        bundle.putString(
            PDDTSConfirmationActivity.EXTRA_SERVICE_FEE,
            intent.getStringExtra(EXTRA_SERVICE_FEE)
        )
        bundle.putString(
            PDDTSConfirmationActivity.EXTRA_CUSTOM_SERVICE_FEE,
            intent.getStringExtra(EXTRA_CUSTOM_SERVICE_FEE)
        )
        bundle.putString(
            PDDTSConfirmationActivity.EXTRA_REMINDERS,
            JsonHelper.toJson(channel.formattedReminders)
        )
        navigator.navigate(
            this,
            PDDTSConfirmationActivity::class.java,
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
        if (intent.getStringExtra(EXTRA_CUSTOM_SERVICE_FEE) != null &&
            intent.getStringExtra(EXTRA_SERVICE_FEE) != null
        ) {
            val customServiceFee = JsonHelper.fromJson<ServiceFee>(
                intent.getStringExtra(EXTRA_CUSTOM_SERVICE_FEE)
            )
            if (0.00 >= customServiceFee.value?.toDouble() ?: 0.00) {
                binding.viewChannelHeader.textViewServiceFee.text = getString(R.string.value_service_fee_free)
            } else {
                binding.viewChannelHeader.textViewServiceFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        customServiceFee.value,
                        customServiceFee.currency
                    )
                )
            }
            binding.viewChannelHeader.textViewServiceDiscountFee.visibility(true)
            binding.viewChannelHeader.viewBorderServiceDiscountFee.visibility(true)
        } else {
            binding.viewChannelHeader.textViewServiceDiscountFee.visibility(false)
            binding.viewChannelHeader.viewBorderServiceDiscountFee.visibility(false)
        }
        if (intent.getStringExtra(EXTRA_SERVICE_FEE) != null) {
            val serviceFee =
                JsonHelper.fromJson<ServiceFee>(intent.getStringExtra(EXTRA_SERVICE_FEE))
            if (intent.getStringExtra(EXTRA_CUSTOM_SERVICE_FEE) != null) {
                binding.viewChannelHeader.textViewServiceDiscountFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        serviceFee.value,
                        serviceFee.currency
                    )
                )
            } else {
                binding.viewChannelHeader.textViewServiceFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        serviceFee.value,
                        serviceFee.currency
                    )
                )
            }
        } else {
            binding.viewChannelHeader.textViewServiceFee.text = getString(R.string.value_service_fee_free)
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
        const val EXTRA_CUSTOM_SERVICE_FEE = "custom_service_fee"

        const val TAG_VALIDATE_TIME_ZONE_DIALOG = "validate_time_zone_dialog"
    }

    override val layoutId: Int
        get() = R.layout.activity_fund_transfer_form_pesonet

    override val viewModelClassType: Class<PDDTSViewModel>
        get() = PDDTSViewModel::class.java
}
