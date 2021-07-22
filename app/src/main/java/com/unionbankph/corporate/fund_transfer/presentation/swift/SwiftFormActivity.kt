package com.unionbankph.corporate.fund_transfer.presentation.swift

import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItems
import com.google.android.material.button.MaterialButton
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
import com.unionbankph.corporate.common.presentation.constant.Constant
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
import com.unionbankph.corporate.databinding.ActivityFundTransferFormSwiftBinding
import com.unionbankph.corporate.fund_transfer.data.form.FundTransferSwiftForm
import com.unionbankph.corporate.fund_transfer.data.model.Beneficiary
import com.unionbankph.corporate.fund_transfer.data.model.Purpose
import com.unionbankph.corporate.fund_transfer.data.model.SwiftBank
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_selection.BeneficiaryActivity
import com.unionbankph.corporate.fund_transfer.presentation.proposed_transfer.ProposedTransferDateActivity
import com.unionbankph.corporate.fund_transfer.presentation.swift_bank.SwiftBankActivity
import io.reactivex.rxkotlin.addTo
import java.util.regex.Pattern
import javax.annotation.concurrent.ThreadSafe

class SwiftFormActivity :
    BaseActivity<ActivityFundTransferFormSwiftBinding, SwiftViewModel>(),
    View.OnClickListener,
    OnTutorialListener,
    OnConfirmationPageCallBack,
    ImeOptionEditText.OnImeOptionListener {

    private lateinit var buttonAction: Button

    private val channel by lazyFast {
        JsonHelper.fromJson<Channel>(intent.getStringExtra(EXTRA_CHANNEL).notNullable())
    }

    private lateinit var imeOptionEditText: ImeOptionEditText

    private var validateTimeZoneBottomSheet: ConfirmationBottomSheet? = null

    private var permissionCollection: PermissionCollection? = null

    private var proposedTransferDate: ProposedTransferDate? = null

    private var purposes: MutableList<Purpose> = mutableListOf()

    private var selectedAccount: Account? = null

    private var beneficiaryMaster: Beneficiary? = null

    private var swiftBank: SwiftBank? = null

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
        // tutorialViewModel.hasTutorial(TutorialScreenEnum.SWIFT_FORM)
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        binding.viewToolbar.textViewTitle,
                        binding.viewToolbar.textViewCorporationName,
                        formatString(R.string.title_swift),
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
                is ShowSwiftLoading -> {
                    showProgressAlertDialog(SwiftFormActivity::class.java.simpleName)
                }
                is ShowSwiftDismissLoading -> dismissProgressAlertDialog()

                is ShowSwiftGetPurposes -> {
                    purposes = it.list
                    showPurposesList()
                }
                is ShowSwiftError -> {
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
        binding.textInputEditTextTransferFrom.setOnClickListener(this)
        binding.viewReceivingBankForm.textInputEditTextReceivingBank.setOnClickListener(this)
        binding.viewPurpose.textInputEditTextPurpose.setOnClickListener(this)
        binding.viewReceivingBankForm.imageViewBeneficiaryClose.setOnClickListener(this)
        binding.viewProposedTransactionDate.constraintLayoutProposedTransactionDate.setOnClickListener(this)
        binding.viewProposedTransactionDate.imageViewSelectedProposedTransactionDate.setOnClickListener(this)

        imeOptionEditText =
            ImeOptionEditText()
        imeOptionEditText.addEditText(
            binding.viewTransferToForm.textInputEditTextTransferTo,
            binding.textInputEditTextAccountNumber,
            binding.textInputEditTextBeneficiaryAddress,
            binding.etAmount,
            binding.textInputEditTextRemarks
        )
        imeOptionEditText.setOnImeOptionListener(this)
        imeOptionEditText.startListener()
    }

    private fun initEventBus() {
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
                this@SwiftFormActivity.onBackPressed(true)
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

    private fun showTransferFrom(account: Account?) {
        account?.let {
            if (beneficiaryMaster != null) showBeneficiaryMasterField(false)
            selectedAccount = it
            permissionCollection = it.permissionCollection
            isEnableButton = true
            binding.etAmount.setCurrencySymbol(it.currency.notNullable(), true)
            binding.viewTransferToForm.textInputEditTextTransferTo.text?.clear()
            binding.textInputEditTextAccountNumber.text?.clear()
            binding.textInputEditTextBeneficiaryAddress.text?.clear()
            binding.textInputEditTextTransferFrom.setText(
                (it.name + "\n" + it.accountNumber.formatAccountNumber())
            )
            invalidateOptionsMenu()
            setPermission(permissionCollection!!)
        }
    }

    private fun showReceivingBank(it: BaseEvent<String>) {
        swiftBank = JsonHelper.fromJson(it.payload)
        binding.viewReceivingBankForm.textInputEditTextReceivingBank.setText(swiftBank?.bankName)
        binding.viewReceivingBankForm.textViewSwiftCode.text = viewUtil.getStringOrEmpty(swiftBank?.swiftBicCode)
        binding.viewReceivingBankForm.textViewReceivingBank.text = viewUtil.getStringOrEmpty(swiftBank?.bankName)
        binding.viewReceivingBankForm.textViewBankAddress.text = swiftBank?.let {
            if (it.address1 == null && it.address2 == null) {
                Constant.EMPTY
            } else {
                it.address1.notNullable() + it.address2.notNullable()
            }
        }
        binding.viewReceivingBankForm.textViewCountry.text = swiftBank?.country
        showReceivingBank(true)
    }

    private fun showBeneficiaryDetails(it: BaseEvent<String>) {
        beneficiaryMaster = JsonHelper.fromJson(it.payload)
        binding.viewTransferToForm.textInputEditTextTransferTo.setText(beneficiaryMaster?.name)
        binding.textInputEditTextAccountNumber.setText(beneficiaryMaster?.accountNumber)
        binding.viewReceivingBankForm.textInputEditTextReceivingBank.setText(beneficiaryMaster?.swiftBankDetails?.bankName)
        binding.textInputEditTextBeneficiaryAddress.setText(beneficiaryMaster?.address)
        binding.viewReceivingBankForm.textViewBeneficiaryCode.text = viewUtil.getStringOrEmpty(beneficiaryMaster?.code)
        binding.viewReceivingBankForm.textViewBeneficiaryName.text = viewUtil.getStringOrEmpty(beneficiaryMaster?.name)
        binding.viewReceivingBankForm.textViewAccountNumber.text = beneficiaryMaster?.accountNumber.formatAccountNumber()
        binding.viewReceivingBankForm.textViewBeneficiaryAddress.text =
            viewUtil.getStringOrEmpty(beneficiaryMaster?.address)
        binding.viewReceivingBankForm.textViewSwiftCode.text =
            viewUtil.getStringOrEmpty(beneficiaryMaster?.swiftBankDetails?.swiftBicCode)
        binding.viewReceivingBankForm.textViewReceivingBank.text =
            viewUtil.getStringOrEmpty(beneficiaryMaster?.swiftBankDetails?.bankName)
        binding.viewReceivingBankForm.textViewBankAddress.text = beneficiaryMaster?.swiftBankDetails?.let {
            if (it.address1 == null && it.address2 == null) {
                Constant.EMPTY
            } else {
                it.address1.notNullable() + it.address2.notNullable()
            }
        }
        binding.viewReceivingBankForm.textViewCountry.text =
            viewUtil.getStringOrEmpty(beneficiaryMaster?.swiftBankDetails?.country)
        swiftBank = null
        showBeneficiaryMasterField(true)
    }

    override fun onClick(view: View?) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return
        mLastClickTime = SystemClock.elapsedRealtime()
        when (view?.id) {
            R.id.textInputEditTextTransferFrom -> {
                navigateAccountSelection()
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
            R.id.textInputEditTextReceivingBank -> {
                navigateToSwiftBanks()
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
                if (swiftBank != null) {
                    showReceivingBank(false)
                } else {
                    showBeneficiaryMasterField(false)
                }
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
                    viewUtil.setFocusOnView(binding.scrollView, binding.textInputLayoutBeneficiaryAddress)
                    tutorialEngineUtil.startTutorial(
                        this,
                        binding.textInputLayoutBeneficiaryAddress,
                        R.layout.frame_tutorial_upper_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_pesonet_form_beneficiary_address),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                binding.textInputLayoutBeneficiaryAddress -> {
                    viewUtil.setFocusOnView(binding.scrollView, binding.viewTutorialReceiving)
                    tutorialEngineUtil.startTutorial(
                        this,
                        binding.viewTutorialReceiving,
                        R.layout.frame_tutorial_upper_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_pesonet_form_receiving_bank),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                binding.viewTutorialReceiving -> {
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
                    // tutorialViewModel.setTutorial(TutorialScreenEnum.SWIFT_FORM, false)
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
            binding.viewReceivingBankForm.constraintLayoutBeneficiary.visibility = View.VISIBLE
            binding.viewTransferToForm.textInputLayoutTransferTo.visibility = View.GONE
            binding.viewTransferToForm.viewBorderTransferTo.visibility = View.GONE
            binding.viewTransferToForm.imageViewTransferTo.visibility = View.GONE
            binding.textInputLayoutAccountNumber.visibility = View.GONE
            binding.textInputLayoutBeneficiaryAddress.visibility = View.GONE
            binding.viewReceivingBankForm.textInputLayoutReceivingBank.visibility = View.GONE
            binding.viewReceivingBankForm.textViewBeneficiaryCodeTitle.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewBeneficiaryCode.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewBeneficiaryNameTitle.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewBeneficiaryName.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewAccountNumberTitle.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewAccountNumber.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewBeneficiaryAddressTitle.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewBeneficiaryAddress.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewCountryTitle.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewCountry.visibility = View.VISIBLE
        } else {
            if (swiftBank != null) {
                binding.viewReceivingBankForm.textInputLayoutReceivingBank.visibility = View.GONE
                binding.viewReceivingBankForm.constraintLayoutBeneficiary.visibility = View.VISIBLE
            } else {
                binding.viewReceivingBankForm.textInputLayoutReceivingBank.visibility = View.VISIBLE
                binding.viewReceivingBankForm.constraintLayoutBeneficiary.visibility = View.GONE
            }
            binding.viewTransferToForm.textInputLayoutTransferTo.visibility = View.VISIBLE
            binding.viewTransferToForm.viewBorderTransferTo.visibility = View.VISIBLE
            binding.viewTransferToForm.imageViewTransferTo.visibility = View.VISIBLE
            binding.textInputLayoutAccountNumber.visibility = View.VISIBLE
            binding.textInputLayoutBeneficiaryAddress.visibility = View.VISIBLE
        }
        setDefaultProposedDate(!isShown)
    }

    private fun showPurposesList() {
        val purposesDesc = purposes
            .asSequence()
            .map { purposeDesc -> purposeDesc.description.notNullable() }
            .toMutableList()
        MaterialDialog(this).show {
            lifecycleOwner(this@SwiftFormActivity)
            title(R.string.hint_select_purpose)
            listItems(
                items = purposesDesc,
                selection = { _, index, text ->
                    purpose = purposes[index]
                    if (purpose?.description.equals("Others", true)) {
                        this@SwiftFormActivity.binding.viewPurpose.textInputLayoutLeavePurpose.visibility = View.VISIBLE
                        this@SwiftFormActivity.binding.viewPurpose.textInputEditTextLeavePurpose.text?.clear()
                    } else {
                        this@SwiftFormActivity.binding.viewPurpose.textInputLayoutLeavePurpose.visibility = View.GONE
                        this@SwiftFormActivity.binding.viewPurpose.textInputEditTextLeavePurpose.setText("-")
                    }
                    this@SwiftFormActivity.binding.viewPurpose.textInputEditTextPurpose.setText(text.toString())
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
        val beneficiaryAddressObservable =
            viewUtil.rxTextChanges(
                true,
                isValueChanged,
                resources.getInteger(R.integer.min_length_field),
                resources.getInteger(R.integer.max_length_field_100),
                binding.textInputEditTextBeneficiaryAddress
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
        initSetError(beneficiaryAddressObservable)
        initSetError(receivingBankObservable)
        initSetError(amountObservable)
        initSetError(purposeOservable)
        initSetError(leavePurposeOservable)

        RxCombineValidator(
            transferFromObservable,
            transferToObservable,
            accountNumberObservable,
            beneficiaryAddressObservable,
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
        if (binding.textInputEditTextBeneficiaryAddress.length() != 0) {
            binding.textInputEditTextBeneficiaryAddress.setText(
                binding.textInputEditTextBeneficiaryAddress.text.toString()
            )
        }
        if (binding.viewReceivingBankForm.textInputEditTextReceivingBank.length() != 0) {
            binding.viewReceivingBankForm. textInputEditTextReceivingBank.setText(
                binding.viewReceivingBankForm.textInputEditTextReceivingBank.text.toString()
            )
        }
        if (binding.etAmount.length() != 0) {
            binding.etAmount.setText(
                binding.etAmount.text.toString()
            )
        }
        if (binding.viewPurpose.textInputEditTextPurpose.length() != 0) {
            binding.viewPurpose.textInputEditTextPurpose.setText(
                binding.viewPurpose.textInputEditTextPurpose.text.toString()
            )
        }
        if (binding.viewPurpose.textInputEditTextLeavePurpose.length() != 0) {
            binding.viewPurpose.textInputEditTextLeavePurpose.setText(
                binding.viewPurpose.textInputEditTextLeavePurpose.text.toString()
            )
        }
    }

    private fun showBeneficiaryMasterField(isShown: Boolean) {
        if (isShown) {
            binding.viewReceivingBankForm.constraintLayoutBeneficiary.visibility = View.VISIBLE
            binding.viewTransferToForm.textInputLayoutTransferTo.visibility = View.GONE
            binding.viewTransferToForm.viewBorderTransferTo.visibility = View.GONE
            binding.viewTransferToForm.imageViewTransferTo.visibility = View.GONE
            binding.textInputLayoutAccountNumber.visibility = View.GONE
            binding.textInputLayoutBeneficiaryAddress.visibility = View.GONE
            binding.viewReceivingBankForm.textInputLayoutReceivingBank.visibility = View.GONE
            binding.viewReceivingBankForm.textViewBeneficiaryCodeTitle.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewBeneficiaryCode.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewBeneficiaryNameTitle.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewBeneficiaryName.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewAccountNumberTitle.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewAccountNumber.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewBeneficiaryAddressTitle.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewBeneficiaryAddress.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewCountryTitle.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewCountry.visibility = View.VISIBLE
            binding.viewReceivingBankForm.constraintLayoutBeneficiary.setOnClickListener {
                navigateToBeneficiaryScreen()
            }
        } else {
            this.beneficiaryMaster = null
            this.swiftBank = null
            binding.viewTransferToForm.textInputEditTextTransferTo.text?.clear()
            binding.textInputEditTextAccountNumber.text?.clear()
            binding.textInputEditTextBeneficiaryAddress.text?.clear()
            binding.viewReceivingBankForm.textInputEditTextReceivingBank.text?.clear()
            binding.viewTransferToForm.textInputLayoutTransferTo.visibility = View.VISIBLE
            binding.viewTransferToForm.viewBorderTransferTo.visibility = View.VISIBLE
            binding.viewTransferToForm.imageViewTransferTo.visibility = View.VISIBLE
            binding.textInputLayoutAccountNumber.visibility = View.VISIBLE
            binding.textInputLayoutBeneficiaryAddress.visibility = View.VISIBLE
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
            binding.viewProposedTransactionDate.viewBorderProposedTransactionDate.visibility = View.VISIBLE
            binding.viewProposedTransactionDate.imageViewProposedTransactionDate.visibility = View.VISIBLE
            binding.viewProposedTransactionDate.constraintLayoutProposedTransactionDate.visibility = View.GONE
        }
    }

    private fun showReceivingBank(isShown: Boolean) {
        val constraintLayoutBeneficiaryParams =
            binding.viewReceivingBankForm.constraintLayoutBeneficiary.layoutParams as ViewGroup.MarginLayoutParams
        val textViewSwiftCodeTitleParams =
            binding.viewReceivingBankForm.textViewSwiftCodeTitle.layoutParams as ViewGroup.MarginLayoutParams
        if (isShown) {
            constraintLayoutBeneficiaryParams.setMargins(
                constraintLayoutBeneficiaryParams.leftMargin,
                resources.getDimensionPixelSize(R.dimen.content_spacing_form_header),
                constraintLayoutBeneficiaryParams.rightMargin,
                constraintLayoutBeneficiaryParams.bottomMargin
            )
            textViewSwiftCodeTitleParams.setMargins(
                textViewSwiftCodeTitleParams.leftMargin,
                resources.getDimensionPixelSize(R.dimen.grid_0),
                textViewSwiftCodeTitleParams.rightMargin,
                textViewSwiftCodeTitleParams.bottomMargin
            )
            binding.viewReceivingBankForm.constraintLayoutBeneficiary.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textInputLayoutReceivingBank.visibility = View.GONE
            binding.viewReceivingBankForm.textViewBeneficiaryCodeTitle.visibility = View.GONE
            binding.viewReceivingBankForm.textViewBeneficiaryCode.visibility = View.GONE
            binding.viewReceivingBankForm.textViewBeneficiaryNameTitle.visibility = View.GONE
            binding.viewReceivingBankForm.textViewBeneficiaryName.visibility = View.GONE
            binding.viewReceivingBankForm.textViewAccountNumberTitle.visibility = View.GONE
            binding.viewReceivingBankForm.textViewAccountNumber.visibility = View.GONE
            binding.viewReceivingBankForm.textViewBeneficiaryAddressTitle.visibility = View.GONE
            binding.viewReceivingBankForm.textViewBeneficiaryAddress.visibility = View.GONE
            binding.viewReceivingBankForm.constraintLayoutBeneficiary.setOnClickListener {
                navigateToSwiftBanks()
            }
        } else {
            constraintLayoutBeneficiaryParams.setMargins(
                constraintLayoutBeneficiaryParams.leftMargin,
                resources.getDimensionPixelSize(R.dimen.content_spacing_half),
                constraintLayoutBeneficiaryParams.rightMargin,
                constraintLayoutBeneficiaryParams.bottomMargin
            )
            textViewSwiftCodeTitleParams.setMargins(
                textViewSwiftCodeTitleParams.leftMargin,
                resources.getDimensionPixelSize(R.dimen.content_group_spacing),
                textViewSwiftCodeTitleParams.rightMargin,
                textViewSwiftCodeTitleParams.bottomMargin
            )
            binding.viewReceivingBankForm.textInputEditTextReceivingBank.text?.clear()
            binding.viewReceivingBankForm.constraintLayoutBeneficiary.visibility = View.GONE
            binding.viewReceivingBankForm.textInputLayoutReceivingBank.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewBeneficiaryCodeTitle.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewBeneficiaryCode.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewBeneficiaryNameTitle.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewBeneficiaryName.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewAccountNumberTitle.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewAccountNumber.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewBeneficiaryAddressTitle.visibility = View.VISIBLE
            binding.viewReceivingBankForm.textViewBeneficiaryAddress.visibility = View.VISIBLE
        }
    }

    private fun setPermission(permissionCollection: PermissionCollection) {
        if (permissionCollection.hasAllowToCreateTransactionAdhoc) {
            binding.viewTransferToForm.textInputLayoutTransferTo.setBoxBackgroundColorResource(R.color.colorTransparent)
            binding.textInputLayoutAccountNumber.setBoxBackgroundColorResource(R.color.colorTransparent)
            binding.viewReceivingBankForm.textInputLayoutReceivingBank.setBoxBackgroundColorResource(R.color.colorTransparent)
            binding.textInputLayoutBeneficiaryAddress.setBoxBackgroundColorResource(R.color.colorTransparent)
            binding.viewTransferToForm.textInputEditTextTransferTo.isEnabled = true
            binding.textInputEditTextAccountNumber.isEnabled = true
            binding.textInputEditTextBeneficiaryAddress.isEnabled = true
            binding.viewReceivingBankForm.textInputEditTextReceivingBank.isEnabled = true
            binding.viewTransferToForm.viewImageViewBackground.background = null
        } else {
            binding.viewTransferToForm.textInputLayoutTransferTo.setBoxBackgroundColorResource(R.color.colorDisableTextInputEditText)
            binding.textInputLayoutAccountNumber.setBoxBackgroundColorResource(R.color.colorDisableTextInputEditText)
            binding.viewReceivingBankForm.textInputLayoutReceivingBank.setBoxBackgroundColorResource(R.color.colorDisableTextInputEditText)
            binding.textInputLayoutBeneficiaryAddress.setBoxBackgroundColorResource(R.color.colorDisableTextInputEditText)
            binding.viewTransferToForm.textInputEditTextTransferTo.isEnabled = false
            binding.textInputEditTextAccountNumber.isEnabled = false
            binding.textInputEditTextBeneficiaryAddress.isEnabled = false
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
            binding.viewPurpose.textInputLayoutLeavePurpose.setBoxBackgroundColorResource(R.color.colorTransparent)
            binding.textInputLayoutRemarks.setBoxBackgroundColorResource(R.color.colorTransparent)
            binding.etAmount.setEnableAmount(true)
            binding.viewProposedTransactionDate.textInputEditTextProposedTransactionDate.isEnabled = true
            binding.viewPurpose.textInputEditTextPurpose.isEnabled = true
            binding.viewPurpose.textInputEditTextLeavePurpose.isEnabled = true
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
            false,
            true, Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
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
        val fundTransferSwiftForm =
            FundTransferSwiftForm()
        fundTransferSwiftForm.channelId = channel.id.toString()
        fundTransferSwiftForm.immediate = proposedTransferDate?.immediately
        fundTransferSwiftForm.referenceNumber = viewModel.uuid.value
        fundTransferSwiftForm.purpose = purpose?.code
        fundTransferSwiftForm.purposeDesc =
            if (purpose?.description.equals("Others", true))
                binding.viewPurpose.textInputEditTextLeavePurpose.text.toString()
            else purpose?.description
        fundTransferSwiftForm.swiftCode =
            if (swiftBank != null) swiftBank?.swiftBicCode
            else beneficiaryMaster?.swiftBankDetails?.swiftBicCode
        fundTransferSwiftForm.receivingBank = binding.viewReceivingBankForm.textInputEditTextReceivingBank.text.toString()
        fundTransferSwiftForm.receivingBankAddress = swiftBank?.let {
            if (it.address1 == null && it.address2 == null) {
                Constant.EMPTY
            } else {
                it.address1.notNullable() + it.address2.notNullable()
            }
        }
        fundTransferSwiftForm.instructions =
            if (binding.viewPurpose.textInputEditTextLeavePurpose.text.toString() != "-")
                binding.viewPurpose.textInputEditTextLeavePurpose.text.toString()
            else null
        fundTransferSwiftForm.amount = binding.etAmount.getNumericValue()
        fundTransferSwiftForm.beneficiaryMasterForm =
            if (beneficiaryMaster != null)
                ConstantHelper.Object.getSwiftBeneficiaryForm(beneficiaryMaster!!)
            else null
        fundTransferSwiftForm.beneficiaryMasterId = beneficiaryMaster?.id
        fundTransferSwiftForm.beneficiaryName = binding.viewTransferToForm.textInputEditTextTransferTo.text.toString()
        fundTransferSwiftForm.beneficiaryAddress =
            binding.textInputEditTextBeneficiaryAddress.text.toString()
        fundTransferSwiftForm.senderAccountNumber = selectedAccount?.accountNumber
        fundTransferSwiftForm.receiverAccountNumber =
            binding.textInputEditTextAccountNumber.text.toString().replace(" ", "")
        fundTransferSwiftForm.remarks = binding.textInputEditTextRemarks.text.toString()
        fundTransferSwiftForm.transferDate = proposedTransferDate?.startDate
        fundTransferSwiftForm.frequency = proposedTransferDate?.frequency
        fundTransferSwiftForm.occurrencesText = proposedTransferDate?.occurrencesText
        fundTransferSwiftForm.occurrences =
            if (proposedTransferDate?.occurrences == 0) null
            else proposedTransferDate?.occurrences
        fundTransferSwiftForm.recurrenceTypeId = proposedTransferDate?.recurrenceTypeId ?: "1"
        if (proposedTransferDate?.immediately == false) {
            fundTransferSwiftForm.recurrenceEndDate = proposedTransferDate?.endDate
        }
        val bundle = Bundle()
        bundle.putString(
            SwiftConfirmationActivity.EXTRA_ACCOUNT,
            JsonHelper.toJson(selectedAccount)
        )
        bundle.putString(
            SwiftConfirmationActivity.EXTRA_ACCOUNT_TYPE,
            getString(R.string.title_swift)
        )
        bundle.putString(
            SwiftConfirmationActivity.EXTRA_FUND_TRANSFER,
            JsonHelper.toJson(fundTransferSwiftForm)
        )
        bundle.putString(
            SwiftConfirmationActivity.EXTRA_SERVICE_FEE,
            intent.getStringExtra(EXTRA_SERVICE_FEE)
        )
        bundle.putString(
            SwiftConfirmationActivity.EXTRA_CUSTOM_SERVICE_FEE,
            intent.getStringExtra(EXTRA_CUSTOM_SERVICE_FEE)
        )
        bundle.putString(
            SwiftConfirmationActivity.EXTRA_REMINDERS,
            JsonHelper.toJson(channel.formattedReminders)
        )
        navigator.navigate(
            this,
            SwiftConfirmationActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateToSwiftBanks() {
        navigator.navigate(
            this,
            SwiftBankActivity::class.java,
            null,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun initChannelView(channel: Channel) {
        binding.textInputEditTextAccountNumber.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.ic_warning_circle_orange,
            0
        )
        binding.textInputEditTextAccountNumber.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= binding.textInputEditTextAccountNumber.right -
                    binding.textInputEditTextAccountNumber.compoundDrawables[DRAWABLE_RIGHT].bounds.width()
                ) {
                    showSwiftAccountNumberToolTip()
                    return@OnTouchListener true
                }
            }
            false
        })
        binding.viewChannelHeader.textViewChannel.visibility = View.GONE
        binding.viewChannelHeader.imageViewChannel.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.viewChannelHeader.imageViewChannel.setImageResource(
            when (channel.id) {
                ChannelBankEnum.BILLS_PAYMENT.getChannelId() ->
                    R.drawable.ic_channel_bills_payment
                ChannelBankEnum.UBP_TO_UBP.getChannelId() ->
                    R.drawable.ic_channel_ubp
                ChannelBankEnum.PESONET.getChannelId() ->
                    R.drawable.ic_channel_pesonet
                ChannelBankEnum.SWIFT.getChannelId() ->
                    R.drawable.ic_channel_swift
                ChannelBankEnum.INSTAPAY.getChannelId() ->
                    R.drawable.ic_channel_instapay
                ChannelBankEnum.PDDTS.getChannelId() ->
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
            val serviceFee = JsonHelper.fromJson<ServiceFee>(
                intent.getStringExtra(EXTRA_SERVICE_FEE)
            )
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

    private fun showSwiftAccountNumberToolTip() {
        val swiftAccountToolTip = MaterialDialog(this).apply {
            lifecycleOwner(this@SwiftFormActivity)
            customView(R.layout.dialog_tool_tip)
        }
        val buttonClose =
            swiftAccountToolTip.view.findViewById<MaterialButton>(R.id.buttonClose)
        val textViewTitle =
            swiftAccountToolTip.view.findViewById<TextView>(R.id.textViewTitle)
        val textViewContent =
            swiftAccountToolTip.view.findViewById<TextView>(R.id.textViewContent)
        textViewTitle.text = formatString(R.string.title_swift_account_number)
        textViewContent.text = formatString(R.string.msg_tooltip_swift_account_number)
        buttonClose.setOnClickListener { swiftAccountToolTip.dismiss() }
        swiftAccountToolTip.window?.attributes?.windowAnimations =
            R.style.SlideUpAnimation
        swiftAccountToolTip.window?.setGravity(Gravity.CENTER)
        swiftAccountToolTip.show()
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

    @ThreadSafe
    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_CHANNEL = "channel"
        const val EXTRA_SERVICE_FEE = "service_fee"
        const val EXTRA_CUSTOM_SERVICE_FEE = "custom_service_fee"

        const val TAG_VALIDATE_TIME_ZONE_DIALOG = "validate_time_zone_dialog"

        const val DRAWABLE_RIGHT = 2
    }

    override val viewModelClassType: Class<SwiftViewModel>
        get() = SwiftViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityFundTransferFormSwiftBinding
        get() = ActivityFundTransferFormSwiftBinding::inflate
}
