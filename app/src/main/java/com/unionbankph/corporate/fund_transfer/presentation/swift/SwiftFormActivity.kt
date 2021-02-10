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
import com.unionbankph.corporate.fund_transfer.data.form.FundTransferSwiftForm
import com.unionbankph.corporate.fund_transfer.data.model.Beneficiary
import com.unionbankph.corporate.fund_transfer.data.model.Purpose
import com.unionbankph.corporate.fund_transfer.data.model.SwiftBank
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_selection.BeneficiaryActivity
import com.unionbankph.corporate.fund_transfer.presentation.proposed_transfer.ProposedTransferDateActivity
import com.unionbankph.corporate.fund_transfer.presentation.swift_bank.SwiftBankActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_fund_transfer_form_swift.*
import kotlinx.android.synthetic.main.widget_channel_header.*
import kotlinx.android.synthetic.main.widget_edit_text_proposed_date.*
import kotlinx.android.synthetic.main.widget_edit_text_purpose.*
import kotlinx.android.synthetic.main.widget_edit_text_swift_receiving_bank.*
import kotlinx.android.synthetic.main.widget_edit_text_transfer_to.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*
import java.util.regex.Pattern
import javax.annotation.concurrent.ThreadSafe

class SwiftFormActivity :
    BaseActivity<SwiftViewModel>(R.layout.activity_fund_transfer_form_swift),
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
        initToolbar(toolbar, viewToolbar)
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
                        textViewTitle,
                        textViewCorporationName,
                        formatString(R.string.title_swift),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[SwiftViewModel::class.java]
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
        et_amount.setEnableAmount(false)
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
                scrollView.post { scrollView.smoothScrollTo(0, 0) }
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
        textInputEditTextTransferFrom.setOnClickListener(this)
        textInputEditTextReceivingBank.setOnClickListener(this)
        textInputEditTextPurpose.setOnClickListener(this)
        imageViewBeneficiaryClose.setOnClickListener(this)
        constraintLayoutProposedTransactionDate.setOnClickListener(this)
        imageViewSelectedProposedTransactionDate.setOnClickListener(this)

        imeOptionEditText =
            ImeOptionEditText()
        imeOptionEditText.addEditText(
            textInputEditTextTransferTo,
            textInputEditTextAccountNumber,
            textInputEditTextBeneficiaryAddress,
            et_amount,
            textInputEditTextRemarks
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
            et_amount.setCurrencySymbol(it.currency.notNullable(), true)
            textInputEditTextTransferTo.text?.clear()
            textInputEditTextAccountNumber.text?.clear()
            textInputEditTextBeneficiaryAddress.text?.clear()
            textInputEditTextTransferFrom.setText(
                (it.name + "\n" + it.accountNumber.formatAccountNumber())
            )
            invalidateOptionsMenu()
            setPermission(permissionCollection!!)
        }
    }

    private fun showReceivingBank(it: BaseEvent<String>) {
        swiftBank = JsonHelper.fromJson(it.payload)
        textInputEditTextReceivingBank.setText(swiftBank?.bankName)
        textViewSwiftCode.text = viewUtil.getStringOrEmpty(swiftBank?.swiftBicCode)
        textViewReceivingBank.text = viewUtil.getStringOrEmpty(swiftBank?.bankName)
        textViewBankAddress.text = swiftBank?.let {
            if (it.address1 == null && it.address2 == null) {
                Constant.EMPTY
            } else {
                it.address1.notNullable() + it.address2.notNullable()
            }
        }
        textViewCountry.text = swiftBank?.country
        showReceivingBank(true)
    }

    private fun showBeneficiaryDetails(it: BaseEvent<String>) {
        beneficiaryMaster = JsonHelper.fromJson(it.payload)
        textInputEditTextTransferTo.setText(beneficiaryMaster?.name)
        textInputEditTextAccountNumber.setText(beneficiaryMaster?.accountNumber)
        textInputEditTextReceivingBank.setText(beneficiaryMaster?.swiftBankDetails?.bankName)
        textInputEditTextBeneficiaryAddress.setText(beneficiaryMaster?.address)
        textViewBeneficiaryCode.text = viewUtil.getStringOrEmpty(beneficiaryMaster?.code)
        textViewBeneficiaryName.text = viewUtil.getStringOrEmpty(beneficiaryMaster?.name)
        textViewAccountNumber.text = beneficiaryMaster?.accountNumber.formatAccountNumber()
        textViewBeneficiaryAddress.text =
            viewUtil.getStringOrEmpty(beneficiaryMaster?.address)
        textViewSwiftCode.text =
            viewUtil.getStringOrEmpty(beneficiaryMaster?.swiftBankDetails?.swiftBicCode)
        textViewReceivingBank.text =
            viewUtil.getStringOrEmpty(beneficiaryMaster?.swiftBankDetails?.bankName)
        textViewBankAddress.text = beneficiaryMaster?.swiftBankDetails?.let {
            if (it.address1 == null && it.address2 == null) {
                Constant.EMPTY
            } else {
                it.address1.notNullable() + it.address2.notNullable()
            }
        }
        textViewCountry.text =
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
            scrollView.post { scrollView.smoothScrollTo(0, 0) }
        } else {
            val radius = resources.getDimension(R.dimen.field_radius)
            when (view) {
                viewTutorialTransferFrom -> {
                    tutorialEngineUtil.startTutorial(
                        this,
                        viewTutorialTransferTo,
                        R.layout.frame_tutorial_upper_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_pesonet_form_transfer_to),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                viewTutorialTransferTo -> {
                    tutorialEngineUtil.startTutorial(
                        this,
                        textInputLayoutAccountNumber,
                        R.layout.frame_tutorial_upper_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_pesonet_form_account_number),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                textInputLayoutAccountNumber -> {
                    viewUtil.setFocusOnView(scrollView, textInputLayoutBeneficiaryAddress)
                    tutorialEngineUtil.startTutorial(
                        this,
                        textInputLayoutBeneficiaryAddress,
                        R.layout.frame_tutorial_upper_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_pesonet_form_beneficiary_address),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                textInputLayoutBeneficiaryAddress -> {
                    viewUtil.setFocusOnView(scrollView, viewTutorialReceiving)
                    tutorialEngineUtil.startTutorial(
                        this,
                        viewTutorialReceiving,
                        R.layout.frame_tutorial_upper_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_pesonet_form_receiving_bank),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                viewTutorialReceiving -> {
                    viewUtil.setFocusOnView(scrollView, viewTutorialAmount)
                    tutorialEngineUtil.startTutorial(
                        this,
                        viewTutorialAmount,
                        R.layout.frame_tutorial_upper_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_ubp_form_amount),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                viewTutorialAmount -> {
                    viewUtil.setFocusOnView(scrollView, viewTutorialProposedTransactionDate)
                    tutorialEngineUtil.startTutorial(
                        this,
                        viewTutorialProposedTransactionDate,
                        R.layout.frame_tutorial_lower_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_ft_form_transaction_date),
                        GravityEnum.TOP,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                viewTutorialProposedTransactionDate -> {
                    viewUtil.setFocusOnView(scrollView, viewTutorialPurpose)
                    tutorialEngineUtil.startTutorial(
                        this,
                        viewTutorialPurpose,
                        R.layout.frame_tutorial_lower_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_pesonet_form_purpose),
                        GravityEnum.TOP,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                viewTutorialPurpose -> {
                    viewUtil.setFocusOnView(scrollView, viewTutorialRemarks)
                    tutorialEngineUtil.startTutorial(
                        this,
                        viewTutorialRemarks,
                        R.layout.frame_tutorial_lower_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_ft_form_remarks),
                        GravityEnum.TOP,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                viewTutorialRemarks -> {
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
                    scrollView.post { scrollView.smoothScrollTo(0, 0) }
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
            textInputLayoutProposedTransactionDate.visibility = View.GONE
            viewBorderProposedTransactionDate.visibility = View.GONE
            imageViewProposedTransactionDate.visibility = View.GONE
            constraintLayoutProposedTransactionDate.visibility = View.VISIBLE
            if (proposedTransferDate?.frequency == getString(R.string.title_one_time)) {
                textViewEndDateTitle.visibility = View.GONE
                textViewEndDate.visibility = View.GONE
            } else {
                textViewEndDateTitle.visibility = View.VISIBLE
                textViewEndDate.visibility = View.VISIBLE
            }
        } else {
            textInputEditTextProposedTransactionDate.setText(R.string.title_immediately)
            textInputLayoutProposedTransactionDate.visibility = View.VISIBLE
            viewBorderProposedTransactionDate.visibility = View.VISIBLE
            imageViewProposedTransactionDate.visibility = View.VISIBLE
            constraintLayoutProposedTransactionDate.visibility = View.GONE
        }
    }

    private fun setDefaultViewTutorial(isShown: Boolean) {
        if (!isShown && beneficiaryMaster != null) {
            constraintLayoutBeneficiary.visibility = View.VISIBLE
            textInputLayoutTransferTo.visibility = View.GONE
            viewBorderTransferTo.visibility = View.GONE
            imageViewTransferTo.visibility = View.GONE
            textInputLayoutAccountNumber.visibility = View.GONE
            textInputLayoutBeneficiaryAddress.visibility = View.GONE
            textInputLayoutReceivingBank.visibility = View.GONE
            textViewBeneficiaryCodeTitle.visibility = View.VISIBLE
            textViewBeneficiaryCode.visibility = View.VISIBLE
            textViewBeneficiaryNameTitle.visibility = View.VISIBLE
            textViewBeneficiaryName.visibility = View.VISIBLE
            textViewAccountNumberTitle.visibility = View.VISIBLE
            textViewAccountNumber.visibility = View.VISIBLE
            textViewBeneficiaryAddressTitle.visibility = View.VISIBLE
            textViewBeneficiaryAddress.visibility = View.VISIBLE
            textViewCountryTitle.visibility = View.VISIBLE
            textViewCountry.visibility = View.VISIBLE
        } else {
            if (swiftBank != null) {
                textInputLayoutReceivingBank.visibility = View.GONE
                constraintLayoutBeneficiary.visibility = View.VISIBLE
            } else {
                textInputLayoutReceivingBank.visibility = View.VISIBLE
                constraintLayoutBeneficiary.visibility = View.GONE
            }
            textInputLayoutTransferTo.visibility = View.VISIBLE
            viewBorderTransferTo.visibility = View.VISIBLE
            imageViewTransferTo.visibility = View.VISIBLE
            textInputLayoutAccountNumber.visibility = View.VISIBLE
            textInputLayoutBeneficiaryAddress.visibility = View.VISIBLE
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
                        this@SwiftFormActivity.textInputLayoutLeavePurpose.visibility = View.VISIBLE
                        this@SwiftFormActivity.textInputEditTextLeavePurpose.text?.clear()
                    } else {
                        this@SwiftFormActivity.textInputLayoutLeavePurpose.visibility = View.GONE
                        this@SwiftFormActivity.textInputEditTextLeavePurpose.setText("-")
                    }
                    this@SwiftFormActivity.textInputEditTextPurpose.setText(text.toString())
                })
        }
    }

    private fun validateForm(isValueChanged: Boolean) {
        val transferFromObservable = viewUtil.rxTextChanges(
            true,
            isValueChanged,
            resources.getInteger(R.integer.min_length_field),
            resources.getInteger(R.integer.max_length_field_100),
            textInputEditTextTransferFrom
        )
        val transferToObservable = viewUtil.rxTextChanges(
            true,
            isValueChanged,
            resources.getInteger(R.integer.min_length_field),
            resources.getInteger(R.integer.max_length_field_100),
            textInputEditTextTransferTo
        )
        val accountNumberObservable =
            viewUtil.rxTextChanges(
                true,
                isValueChanged,
                resources.getInteger(R.integer.min_length_account_number),
                resources.getInteger(R.integer.max_length_field_100),
                textInputEditTextAccountNumber,
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
                textInputEditTextBeneficiaryAddress
            )
        val receivingBankObservable =
            viewUtil.rxTextChanges(
                true,
                isValueChanged,
                resources.getInteger(R.integer.min_length_field),
                resources.getInteger(R.integer.max_length_field_100),
                textInputEditTextReceivingBank
            )
        val amountObservable = viewUtil.rxTextChangesAmount(
            true,
            isValueChanged,
            et_amount,
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
                textInputEditTextPurpose
            )

        val leavePurposeOservable =
            viewUtil.rxTextChanges(
                true,
                isValueChanged,
                resources.getInteger(R.integer.min_length_field),
                resources.getInteger(R.integer.max_length_field_100),
                textInputEditTextLeavePurpose
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
        if (textInputEditTextTransferFrom.length() != 0) {
            textInputEditTextTransferFrom.setText(textInputEditTextTransferFrom.text.toString())
        }
        if (textInputEditTextTransferTo.length() != 0) {
            textInputEditTextTransferTo.setText(textInputEditTextTransferTo.text.toString())
        }
        if (textInputEditTextAccountNumber.length() != 0) {
            textInputEditTextAccountNumber.setText(textInputEditTextAccountNumber.text.toString())
        }
        if (textInputEditTextBeneficiaryAddress.length() != 0) {
            textInputEditTextBeneficiaryAddress.setText(textInputEditTextBeneficiaryAddress.text.toString())
        }
        if (textInputEditTextReceivingBank.length() != 0) {
            textInputEditTextReceivingBank.setText(textInputEditTextReceivingBank.text.toString())
        }
        if (et_amount.length() != 0) {
            et_amount.setText(et_amount.text.toString())
        }
        if (textInputEditTextPurpose.length() != 0) {
            textInputEditTextPurpose.setText(textInputEditTextPurpose.text.toString())
        }
        if (textInputEditTextLeavePurpose.length() != 0) {
            textInputEditTextLeavePurpose.setText(textInputEditTextLeavePurpose.text.toString())
        }
    }

    private fun showBeneficiaryMasterField(isShown: Boolean) {
        if (isShown) {
            constraintLayoutBeneficiary.visibility = View.VISIBLE
            textInputLayoutTransferTo.visibility = View.GONE
            viewBorderTransferTo.visibility = View.GONE
            imageViewTransferTo.visibility = View.GONE
            textInputLayoutAccountNumber.visibility = View.GONE
            textInputLayoutBeneficiaryAddress.visibility = View.GONE
            textInputLayoutReceivingBank.visibility = View.GONE
            textViewBeneficiaryCodeTitle.visibility = View.VISIBLE
            textViewBeneficiaryCode.visibility = View.VISIBLE
            textViewBeneficiaryNameTitle.visibility = View.VISIBLE
            textViewBeneficiaryName.visibility = View.VISIBLE
            textViewAccountNumberTitle.visibility = View.VISIBLE
            textViewAccountNumber.visibility = View.VISIBLE
            textViewBeneficiaryAddressTitle.visibility = View.VISIBLE
            textViewBeneficiaryAddress.visibility = View.VISIBLE
            textViewCountryTitle.visibility = View.VISIBLE
            textViewCountry.visibility = View.VISIBLE
            constraintLayoutBeneficiary.setOnClickListener {
                navigateToBeneficiaryScreen()
            }
        } else {
            this.beneficiaryMaster = null
            this.swiftBank = null
            textInputEditTextTransferTo.text?.clear()
            textInputEditTextAccountNumber.text?.clear()
            textInputEditTextBeneficiaryAddress.text?.clear()
            textInputEditTextReceivingBank.text?.clear()
            textInputLayoutTransferTo.visibility = View.VISIBLE
            viewBorderTransferTo.visibility = View.VISIBLE
            imageViewTransferTo.visibility = View.VISIBLE
            textInputLayoutAccountNumber.visibility = View.VISIBLE
            textInputLayoutBeneficiaryAddress.visibility = View.VISIBLE
            textInputLayoutReceivingBank.visibility = View.VISIBLE
            constraintLayoutBeneficiary.visibility = View.GONE
        }
    }

    private fun showProposedTransferDate(isShown: Boolean) {
        if (isShown) {
            textInputLayoutProposedTransactionDate.visibility = View.GONE
            viewBorderProposedTransactionDate.visibility = View.GONE
            imageViewProposedTransactionDate.visibility = View.GONE
            constraintLayoutProposedTransactionDate.visibility = View.VISIBLE
            if (proposedTransferDate?.frequency == getString(R.string.title_one_time)) {
                textViewEndDateTitle.visibility = View.GONE
                textViewEndDate.visibility = View.GONE
            } else {
                textViewEndDateTitle.visibility = View.VISIBLE
                textViewEndDate.visibility = View.VISIBLE
            }
            textViewStartDate.text =
                viewUtil.getDateFormatByDateString(
                    proposedTransferDate?.startDate,
                    ViewUtil.DATE_FORMAT_ISO,
                    ViewUtil.DATE_FORMAT_DEFAULT
                )
            textViewFrequency.text = proposedTransferDate?.frequency
            if (proposedTransferDate?.endDate != null) {
                val endDateDesc = viewUtil.getDateFormatByDateString(
                    proposedTransferDate?.endDate,
                    ViewUtil.DATE_FORMAT_ISO,
                    ViewUtil.DATE_FORMAT_DATE
                )
                textViewEndDate.text =
                    ("${proposedTransferDate?.occurrencesText}\n(Until $endDateDesc)")
            } else {
                textViewEndDate.text = proposedTransferDate?.occurrencesText
            }
        } else {
            textInputEditTextProposedTransactionDate.setText(R.string.title_immediately)
            textInputLayoutProposedTransactionDate.visibility = View.VISIBLE
            viewBorderProposedTransactionDate.visibility = View.VISIBLE
            imageViewProposedTransactionDate.visibility = View.VISIBLE
            constraintLayoutProposedTransactionDate.visibility = View.GONE
        }
    }

    private fun showReceivingBank(isShown: Boolean) {
        val constraintLayoutBeneficiaryParams =
            constraintLayoutBeneficiary.layoutParams as ViewGroup.MarginLayoutParams
        val textViewSwiftCodeTitleParams =
            textViewSwiftCodeTitle.layoutParams as ViewGroup.MarginLayoutParams
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
            constraintLayoutBeneficiary.visibility = View.VISIBLE
            textInputLayoutReceivingBank.visibility = View.GONE
            textViewBeneficiaryCodeTitle.visibility = View.GONE
            textViewBeneficiaryCode.visibility = View.GONE
            textViewBeneficiaryNameTitle.visibility = View.GONE
            textViewBeneficiaryName.visibility = View.GONE
            textViewAccountNumberTitle.visibility = View.GONE
            textViewAccountNumber.visibility = View.GONE
            textViewBeneficiaryAddressTitle.visibility = View.GONE
            textViewBeneficiaryAddress.visibility = View.GONE
            constraintLayoutBeneficiary.setOnClickListener {
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
            textInputEditTextReceivingBank.text?.clear()
            constraintLayoutBeneficiary.visibility = View.GONE
            textInputLayoutReceivingBank.visibility = View.VISIBLE
            textViewBeneficiaryCodeTitle.visibility = View.VISIBLE
            textViewBeneficiaryCode.visibility = View.VISIBLE
            textViewBeneficiaryNameTitle.visibility = View.VISIBLE
            textViewBeneficiaryName.visibility = View.VISIBLE
            textViewAccountNumberTitle.visibility = View.VISIBLE
            textViewAccountNumber.visibility = View.VISIBLE
            textViewBeneficiaryAddressTitle.visibility = View.VISIBLE
            textViewBeneficiaryAddress.visibility = View.VISIBLE
        }
    }

    private fun setPermission(permissionCollection: PermissionCollection) {
        if (permissionCollection.hasAllowToCreateTransactionAdhoc) {
            textInputLayoutTransferTo.setBoxBackgroundColorResource(R.color.colorTransparent)
            textInputLayoutAccountNumber.setBoxBackgroundColorResource(R.color.colorTransparent)
            textInputLayoutReceivingBank.setBoxBackgroundColorResource(R.color.colorTransparent)
            textInputLayoutBeneficiaryAddress.setBoxBackgroundColorResource(R.color.colorTransparent)
            textInputEditTextTransferTo.isEnabled = true
            textInputEditTextAccountNumber.isEnabled = true
            textInputEditTextBeneficiaryAddress.isEnabled = true
            textInputEditTextReceivingBank.isEnabled = true
            viewImageViewBackground.background = null
        } else {
            textInputLayoutTransferTo.setBoxBackgroundColorResource(R.color.colorDisableTextInputEditText)
            textInputLayoutAccountNumber.setBoxBackgroundColorResource(R.color.colorDisableTextInputEditText)
            textInputLayoutReceivingBank.setBoxBackgroundColorResource(R.color.colorDisableTextInputEditText)
            textInputLayoutBeneficiaryAddress.setBoxBackgroundColorResource(R.color.colorDisableTextInputEditText)
            textInputEditTextTransferTo.isEnabled = false
            textInputEditTextAccountNumber.isEnabled = false
            textInputEditTextBeneficiaryAddress.isEnabled = false
            textInputEditTextReceivingBank.isEnabled = false
            viewImageViewBackground.background =
                ContextCompat.getDrawable(this, R.drawable.bg_edittext_right_form)
        }

        if (permissionCollection.hasAllowToCreateTransactionBeneficiaryMaster) {
            imageViewTransferTo.setOnClickListener(this)
        } else {
            imageViewTransferTo.setOnClickListener(null)
        }

        if (selectedAccount != null) {
            textInputEditTextProposedTransactionDate.setOnClickListener(this)
            imageViewProposedTransactionDate.setOnClickListener(this)
            textInputLayoutProposedTransactionDate.setBoxBackgroundColorResource(R.color.colorTransparent)
            textInputLayoutPurpose.setBoxBackgroundColorResource(R.color.colorTransparent)
            textInputLayoutLeavePurpose.setBoxBackgroundColorResource(R.color.colorTransparent)
            textInputLayoutRemarks.setBoxBackgroundColorResource(R.color.colorTransparent)
            et_amount.setEnableAmount(true)
            textInputEditTextProposedTransactionDate.isEnabled = true
            textInputEditTextPurpose.isEnabled = true
            textInputEditTextLeavePurpose.isEnabled = true
            textInputEditTextRemarks.isEnabled = true
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
                textInputEditTextLeavePurpose.text.toString()
            else purpose?.description
        fundTransferSwiftForm.swiftCode =
            if (swiftBank != null) swiftBank?.swiftBicCode
            else beneficiaryMaster?.swiftBankDetails?.swiftBicCode
        fundTransferSwiftForm.receivingBank = textInputEditTextReceivingBank.text.toString()
        fundTransferSwiftForm.receivingBankAddress = swiftBank?.let {
            if (it.address1 == null && it.address2 == null) {
                Constant.EMPTY
            } else {
                it.address1.notNullable() + it.address2.notNullable()
            }
        }
        fundTransferSwiftForm.instructions =
            if (textInputEditTextLeavePurpose.text.toString() != "-")
                textInputEditTextLeavePurpose.text.toString()
            else null
        fundTransferSwiftForm.amount = et_amount.getNumericValue()
        fundTransferSwiftForm.beneficiaryMasterForm =
            if (beneficiaryMaster != null)
                ConstantHelper.Object.getSwiftBeneficiaryForm(beneficiaryMaster!!)
            else null
        fundTransferSwiftForm.beneficiaryMasterId = beneficiaryMaster?.id
        fundTransferSwiftForm.beneficiaryName = textInputEditTextTransferTo.text.toString()
        fundTransferSwiftForm.beneficiaryAddress =
            textInputEditTextBeneficiaryAddress.text.toString()
        fundTransferSwiftForm.senderAccountNumber = selectedAccount?.accountNumber
        fundTransferSwiftForm.receiverAccountNumber =
            textInputEditTextAccountNumber.text.toString().replace(" ", "")
        fundTransferSwiftForm.remarks = textInputEditTextRemarks.text.toString()
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
        textInputEditTextAccountNumber.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.ic_warning_circle_orange,
            0
        )
        textInputEditTextAccountNumber.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= textInputEditTextAccountNumber.right -
                    textInputEditTextAccountNumber.compoundDrawables[DRAWABLE_RIGHT].bounds.width()
                ) {
                    showSwiftAccountNumberToolTip()
                    return@OnTouchListener true
                }
            }
            false
        })
        textViewChannel.visibility = View.GONE
        imageViewChannel.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        imageViewChannel.setImageResource(
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
        textViewChannel.text = channel.contextChannel?.displayName
        if (intent.getStringExtra(EXTRA_CUSTOM_SERVICE_FEE) != null &&
            intent.getStringExtra(EXTRA_SERVICE_FEE) != null
        ) {
            val customServiceFee = JsonHelper.fromJson<ServiceFee>(
                intent.getStringExtra(EXTRA_CUSTOM_SERVICE_FEE)
            )
            if (0.00 >= customServiceFee.value?.toDouble() ?: 0.00) {
                textViewServiceFee.text = getString(R.string.value_service_fee_free)
            } else {
                textViewServiceFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        customServiceFee.value,
                        customServiceFee.currency
                    )
                )
            }
            textViewServiceDiscountFee.visibility(true)
            viewBorderServiceDiscountFee.visibility(true)
        } else {
            textViewServiceDiscountFee.visibility(false)
            viewBorderServiceDiscountFee.visibility(false)
        }
        if (intent.getStringExtra(EXTRA_SERVICE_FEE) != null) {
            val serviceFee = JsonHelper.fromJson<ServiceFee>(
                intent.getStringExtra(EXTRA_SERVICE_FEE)
            )
            if (intent.getStringExtra(EXTRA_CUSTOM_SERVICE_FEE) != null) {
                textViewServiceDiscountFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        serviceFee.value,
                        serviceFee.currency
                    )
                )
            } else {
                textViewServiceFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        serviceFee.value,
                        serviceFee.currency
                    )
                )
            }
        } else {
            textViewServiceFee.text = getString(R.string.value_service_fee_free)
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
            viewTutorialTransferFrom,
            R.layout.frame_tutorial_upper_left,
            radius,
            false,
            getString(R.string.msg_tutorial_ubp_form_transfer_from),
            GravityEnum.BOTTOM,
            OverlayAnimationEnum.ANIM_EXPLODE
        )
    }

    private fun clearFormFocus() {
        constraintLayoutParent.requestFocus()
        constraintLayoutParent.isFocusableInTouchMode = true
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
}
