package com.unionbankph.corporate.bills_payment.presentation.bills_payment_form

import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItems
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidator
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.bills_payment.data.form.BillsPaymentForm
import com.unionbankph.corporate.bills_payment.data.model.*
import com.unionbankph.corporate.bills_payment.presentation.biller.BillerMainActivity
import com.unionbankph.corporate.bills_payment.presentation.biller.biller_all.AllBillerFragment
import com.unionbankph.corporate.bills_payment.presentation.bills_payment_confirmation.BillsPaymentConfirmationActivity
import com.unionbankph.corporate.common.data.model.PermissionCollection
import com.unionbankph.corporate.common.data.model.ProposedTransferDate
import com.unionbankph.corporate.common.data.model.ServiceFee
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialHasTutorial
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.corporate.data.model.Channel
import com.unionbankph.corporate.databinding.ActivityBillsPaymentFormBinding
import com.unionbankph.corporate.fund_transfer.presentation.proposed_transfer.ProposedTransferDateActivity
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import java.util.*
import java.util.regex.Pattern

class BillsPaymentFormActivity :
    BaseActivity<ActivityBillsPaymentFormBinding, BillsPaymentFormViewModel>(),
    View.OnClickListener,
    OnTutorialListener,
    OnConfirmationPageCallBack, ImeOptionEditText.OnImeOptionListener {

    private val channel by lazyFast {
        JsonHelper.fromJson<Channel>(intent.getStringExtra(EXTRA_CHANNEL).notNullable())
    }

    private lateinit var buttonAction: Button

    private lateinit var imeOptionEditText: ImeOptionEditText

    private var validateTransactionBottomSheet: ConfirmationBottomSheet? = null

    private var validateTimeZoneBottomSheet: ConfirmationBottomSheet? = null

    private var billsPaymentForm: BillsPaymentForm? = null

    private var permissionCollection: PermissionCollection? = null

    private var disposable: Disposable? = null

    private var frequentBiller: FrequentBiller? = null

    private var biller: Biller? = null

    private var proposedTransferDate: ProposedTransferDate? = null

    private var billerFields: MutableList<BillerField>? = null

    private var selectedAccount: Account? = null

    private var destinationAccountNumber: String? = null

    private var serviceId: String? = null

    private var billerId: String? = null

    private var rxValidationResultList = ArrayList<ReferenceTextInputEditText>()

    private var fields = mutableListOf<Field>()

    private var isEnableButton: Boolean = false

    private var isActivatedValidationForm: Boolean = false

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

    override fun onViewsBound() {
        super.onViewsBound()
        binding.etAmount.setEnableAmount(false)
        initChannelView(channel)
        initBiller()
        binding.viewProposedTransactionDate.textInputEditTextProposedTransactionDate
            .setText(getString(R.string.title_immediately))
        tutorialEngineUtil.setOnTutorialListener(this)
    }

    private fun initViewModel() {
        viewModel.formState.observe(this, Observer {
            when (it) {
                is ShowBillsPaymentFormLoading -> {
                    showProgressAlertDialog(this::class.java.simpleName)
                }
                is ShowBillsPaymentFormDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowBillsPaymentFormGetBillerFields -> {
                    billerFields = it.billerFields
                    initBillerFields(billerFields)
                }
                is ShowBillsPaymentFormValidate -> {
                    if (it.billsPaymentValidate.hasSimilarPayment == true) {
                        billsPaymentForm = it.billsPaymentForm
                        showValidateTransactionBottomSheet(it.billsPaymentValidate)
                    } else {
                        navigateBillsPaymentConfirmationScreen(it.billsPaymentForm)
                    }
                }
                is ShowBillsPaymentFormError -> {
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
            showPaymentFrom(it)
        })
        viewModel.getAccountsPermission(
            channelId = channel.id.toString(),
            permissionId = channel.permission?.id?.toString(),
            destinationId = selectedAccount?.id?.toString(),
            exceptCurrency = CURRENCY_USD
        )
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        binding.viewToolbar.textViewTitle,
                        binding.viewToolbar.textViewCorporationName,
                        formatString(R.string.title_pay_bills),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
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
        binding.textInputEditTextTransferFrom.setOnClickListener(this)
        binding.viewPaymentTo.textInputEditTextPaymentTo.setOnClickListener(this)
        binding.viewPaymentTo.imageViewFrequentBillerClose.setOnClickListener(this)
        binding.viewProposedTransactionDate.textInputEditTextProposedTransactionDate.setOnClickListener(null)
        imeOptionEditText =
            ImeOptionEditText()
        if (isSSSChannel()) {
            imeOptionEditText.addEditText(binding.textInputEditTextRemarks)
        } else {
            imeOptionEditText.addEditText(binding.etAmount, binding.textInputEditTextRemarks)
        }
        imeOptionEditText.setOnImeOptionListener(this)
        imeOptionEditText.startListener()
    }

    override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
        if (tag == TAG_VALIDATE_TRANSACTION_DIALOG) {
            validateTransactionBottomSheet?.dismiss()
        }
    }

    override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
        if (tag == TAG_VALIDATE_TRANSACTION_DIALOG) {
            validateTransactionBottomSheet?.dismiss()
            navigateBillsPaymentConfirmationScreen(billsPaymentForm!!)
        }
    }

    override fun onDataSetDialog(
        tag: String?,
        linearLayout: LinearLayout,
        buttonPositive: Button,
        buttonNegative: Button,
        data: String?,
        dynamicData: String?
    ) {
        if (tag == TAG_VALIDATE_TRANSACTION_DIALOG) {
            linearLayout.setPadding(
                resources.getDimensionPixelOffset(R.dimen.content_spacing_large),
                resources.getDimensionPixelOffset(R.dimen.content_spacing_half),
                resources.getDimensionPixelOffset(R.dimen.content_spacing_large),
                resources.getDimensionPixelOffset(R.dimen.content_spacing_half)
            )
            val billsPaymentValidate =
                JsonHelper.fromJson<BillsPaymentValidate>(dynamicData.notNullable())
            billsPaymentValidate.details?.references?.forEach {
                val viewBillerFields =
                    layoutInflater.inflate(R.layout.item_confirmation_billers_field, null)
                val textViewBillerFieldTitle =
                    viewBillerFields.findViewById<TextView>(R.id.textViewBillerFieldTitle)
                val textViewBillerFieldValue =
                    viewBillerFields.findViewById<TextView>(R.id.textViewBillerFieldValue)
                textViewBillerFieldTitle.text = ("${it.name}: ")
                textViewBillerFieldValue.text = it.value
                linearLayout.addView(viewBillerFields)
            }
        }
    }

    private fun initEventBus() {
        eventBus.accountSyncEvent.flowable.subscribe {
            if (it.eventType == AccountSyncEvent.ACTION_UPDATE_SELECTED_ACCOUNT) {
                showPaymentFrom(it.payload)
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
        eventBus.inputSyncEvent.flowable.subscribe {
            rxValidationResultList.clear()
            if (it.eventType == InputSyncEvent.ACTION_INPUT_BILLER) {
                showBillerFields(it)
            } else if (it.eventType == InputSyncEvent.ACTION_INPUT_FREQUENT_BILLER) {
                showFrequentBiller(it)
            }
        }.addTo(disposables)
    }

    private fun showPaymentFrom(account: Account?) {
        account?.let {
            selectedAccount = it
            permissionCollection = selectedAccount?.permissionCollection
            isEnableButton = true
            binding.etAmount.setCurrencySymbol(selectedAccount?.currency.notNullable(), true)
            binding.textInputEditTextTransferFrom.setText(
                (selectedAccount?.name + "\n" + selectedAccount?.accountNumber.formatAccountNumber())
            )
            if (frequentBiller != null) clearFrequentBiller()
            invalidateOptionsMenu()
            setupViews()
        }
    }

    private fun showFrequentBiller(it: BaseEvent<String>) {
        frequentBiller = JsonHelper.fromJson(it.payload)
        fields = frequentBiller?.fields ?: mutableListOf()
        destinationAccountNumber = frequentBiller?.accountNumber
        serviceId = frequentBiller?.serviceId
        billerId = frequentBiller?.code
        binding.viewPaymentTo.linearLayoutBillerFields.removeAllViews()
        binding.viewPaymentTo.constraintLayoutFrequentBiller.visibility = View.VISIBLE
        binding.viewPaymentTo.textInputLayoutPaymentTo.visibility = View.GONE
        binding.viewPaymentTo.textInputEditTextPaymentTo.setText(frequentBiller?.billerName)
        binding.viewPaymentTo.textViewFrequentBiller.text = frequentBiller?.billerName
        initFrequentBiller(frequentBiller?.fields)
    }

    private fun showBillerFields(it: BaseEvent<String>) {
        binding.viewPaymentTo.linearLayoutBillerFields?.removeAllViews()
        biller = JsonHelper.fromJson(it.payload)
        destinationAccountNumber = biller?.accountNumber
        serviceId = biller?.serviceId
        billerId = biller?.code
        viewModel.getBillerFields(biller?.serviceId!!)
    }

    override fun onClick(view: View?) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return
        mLastClickTime = SystemClock.elapsedRealtime()
        when (view?.id) {
            R.id.textInputEditTextTransferFrom -> {
                clickTransferFrom()
            }
            R.id.textInputEditTextPaymentTo -> {
                navigateBillersScreen()
            }
            R.id.imageViewFrequentBillerClose -> {
                clickCloseFrequentBiller()
            }
            R.id.textInputEditTextProposedTransactionDate,
            R.id.constraintLayoutProposedTransactionDate -> {
                if (!viewUtil.isGMTPlus8()) {
                    showValidateTimeZoneBottomSheet()
                } else {
                    navigateProposedTransactionDate()
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
                    if (isBIRChannel() || isSSSChannel()) {
                        startTutorialPaymentTo()
                    } else {
                        viewUtil.setFocusOnView(binding.scrollView, binding.viewPaymentTo.viewTutorialPaymentTo)
                        tutorialEngineUtil.startTutorial(
                            this,
                            binding.viewPaymentTo.viewTutorialPaymentTo,
                            R.layout.frame_tutorial_upper_left,
                            radius,
                            false,
                            getString(R.string.msg_tutorial_bills_payment_form_payment_to),
                            GravityEnum.BOTTOM,
                            OverlayAnimationEnum.ANIM_EXPLODE
                        )
                    }
                }
                binding.viewPaymentTo.viewTutorialPaymentTo -> {
                    startTutorialPaymentTo()
                }
                binding.viewPaymentTo.viewTutorialBillerFields -> {
                    if (isSSSChannel()) {
                        startTutorialProposedPaymentDate()
                    } else {
                        startTutorialAmount()
                    }
                }
                binding.viewTutorialAmount -> {
                    startTutorialProposedPaymentDate()
                }
                binding.viewTutorialProposedTransactionDate -> {
                    viewUtil.setFocusOnView(binding.scrollView, binding.viewTutorialRemarks)
                    tutorialEngineUtil.startTutorial(
                        this,
                        binding.viewTutorialRemarks,
                        R.layout.frame_tutorial_lower_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_bp_form_remarks),
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
                    // tutorialViewModel.setTutorial(TutorialScreenEnum.BILLS_PAYMENT_FORM, false)
                }
            }
        }
    }

    private fun startTutorialProposedPaymentDate() {
        val radius = resources.getDimension(R.dimen.field_radius)
        viewUtil.setFocusOnView(binding.scrollView, binding.viewTutorialProposedTransactionDate)
        tutorialEngineUtil.startTutorial(
            this,
            binding.viewTutorialProposedTransactionDate,
            R.layout.frame_tutorial_lower_left,
            radius,
            false,
            getString(R.string.msg_tutorial_bp_form_transaction_date),
            GravityEnum.TOP,
            OverlayAnimationEnum.ANIM_EXPLODE
        )
    }

    private fun startTutorialPaymentTo() {
        val radius = resources.getDimension(R.dimen.field_radius)
        if (billerFields != null && frequentBiller == null) {
            viewUtil.setFocusOnView(binding.scrollView, binding.viewPaymentTo.viewTutorialBillerFields)
            tutorialEngineUtil.startTutorial(
                this,
                binding.viewPaymentTo.viewTutorialBillerFields,
                R.layout.frame_tutorial_lower_left,
                radius,
                false,
                getString(R.string.msg_tutorial_bills_payment_form_biller_fields),
                GravityEnum.TOP,
                OverlayAnimationEnum.ANIM_EXPLODE
            )
        } else {
            if (isSSSChannel()) {
                startTutorialProposedPaymentDate()
            } else {
                startTutorialAmount()
            }
        }
    }

    private fun startTutorialAmount() {
        val radius = resources.getDimension(R.dimen.field_radius)
        viewUtil.setFocusOnView(binding.scrollView, binding.viewTutorialAmount)
        tutorialEngineUtil.startTutorial(
            this,
            binding.viewTutorialAmount,
            R.layout.frame_tutorial_lower_left,
            radius,
            false,
            getString(R.string.msg_tutorial_ubp_form_amount),
            GravityEnum.TOP,
            OverlayAnimationEnum.ANIM_EXPLODE
        )
    }

    private fun setDefaultViewTutorial(isShown: Boolean) {
        if (!isShown && proposedTransferDate != null && proposedTransferDate?.immediately == false) {
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
            binding.viewProposedTransactionDate.imageViewProposedTransactionDate.visibility = View.VISIBLE
            binding.viewProposedTransactionDate.viewBorderProposedTransactionDate.visibility = View.VISIBLE
            binding.viewProposedTransactionDate.constraintLayoutProposedTransactionDate.visibility = View.GONE
        }
    }

    private fun validateForm(isValueChanged: Boolean) {
        if (disposable != null) disposables.remove(disposable!!)
        val rxValidationResults = mutableListOf<ReferenceEditText>()
        val transferFromObservable = viewUtil.rxTextChanges(
            true,
            isValueChanged,
            resources.getInteger(R.integer.min_length_field),
            resources.getInteger(R.integer.max_length_field),
            binding.textInputEditTextTransferFrom
        )
        val paymentToObservable = viewUtil.rxTextChanges(
            true,
            isValueChanged,
            resources.getInteger(R.integer.min_length_field),
            resources.getInteger(R.integer.max_length_field),
            binding.viewPaymentTo.textInputEditTextPaymentTo
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
        initSetError(paymentToObservable)
        if (channel.id != ChannelBankEnum.SSS.getChannelId()) {
            initSetError(amountObservable)
        }
        rxValidationResultList
            .filter { it.rxValidationResult == null }
            .forEach {
                if (it.billerField?.required!!) {
                    val observableTextInputEditText = viewUtil.rxTextChanges(
                        true,
                        isValueChanged,
                        if (it.billerField?.min != 0 && it.billerField?.min != null) {
                            it.billerField?.min!!
                        } else {
                            resources.getInteger(R.integer.min_length_field)
                        },
                        if (it.billerField?.max != null)
                            it.billerField?.max!!
                        else
                            resources.getInteger(R.integer.max_length_field_100),
                        it.textInputEditText!!
                    )
                    initSetError(observableTextInputEditText)
                    rxValidationResults.add(
                        ReferenceEditText(
                            observableTextInputEditText,
                            it.billerField,
                            it.textInputEditText,
                            null
                        )
                    )
                }
            }
        rxValidationResults.add(
            ReferenceEditText(
                transferFromObservable,
                null,
                binding.textInputEditTextTransferFrom
            )
        )
        if (channel.id != ChannelBankEnum.BIR.getChannelId() &&
            channel.id != ChannelBankEnum.SSS.getChannelId()
        ) {
            rxValidationResults.add(
                ReferenceEditText(
                    paymentToObservable,
                    null,
                    binding.viewPaymentTo.textInputEditTextPaymentTo
                )
            )
        }
        if (channel.id != ChannelBankEnum.SSS.getChannelId()) {
            rxValidationResults.add(
                ReferenceEditText(
                    amountObservable,
                    null,
                    null,
                    binding.etAmount
                )
            )
        }
        disposable = Observable.combineLatest(
            rxValidationResults.map { it.rxValidationResult }
        ) { arrays ->
            arrays.forEach {
                if (!(it as RxValidationResult<*>).isProper) {
                    return@combineLatest false
                }
            }
            return@combineLatest true
        }
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
                    isActivatedValidationForm = true
                    validateForm(true)
                }
            }.subscribe()
        disposable?.addTo(disposables)
        initEditTextDefaultValue(rxValidationResults)
    }

    private fun initEditTextDefaultValue(rxValidationResults: MutableList<ReferenceEditText>) {
        if (isActivatedValidationForm) {
            rxValidationResults.forEach {
                if (it.textInputEditText != null) {
                    it.textInputEditText?.setText(it.textInputEditText?.text.toString())
                }
                if (it.editText != null) {
                    it.editText?.setText(it.editText?.text.toString())
                }
            }
        }
    }

    private fun showProposedTransferDate(isShown: Boolean) {
        if (isShown) {
            binding.viewProposedTransactionDate.textInputLayoutProposedTransactionDate.visibility = View.GONE
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
            binding.viewProposedTransactionDate.textViewEndDate.text =
                viewUtil.getDateFormatByDateString(
                    proposedTransferDate?.endDate,
                    ViewUtil.DATE_FORMAT_ISO,
                    ViewUtil.DATE_FORMAT_DATE
                )
        } else {
            binding.viewProposedTransactionDate.textInputEditTextProposedTransactionDate.setText(R.string.title_immediately)
            binding.viewProposedTransactionDate.textInputLayoutProposedTransactionDate.visibility = View.VISIBLE
            binding.viewProposedTransactionDate.imageViewProposedTransactionDate.visibility = View.VISIBLE
            binding.viewProposedTransactionDate.constraintLayoutProposedTransactionDate.visibility = View.GONE
        }
    }

    private fun getReferences(isFrequentBiller: Boolean): MutableList<Reference> {
        val references = mutableListOf<Reference>()
        if (isFrequentBiller) {
            fields
                .sortedWith(compareBy { it.index })
                .forEach { field ->
                    references.add(
                        Reference(
                            index = field.index.toString(),
                            name = field.name,
                            value = field.value
                        )
                    )
                }
        } else {
            rxValidationResultList
                .sortedWith(compareBy { it.billerField?.index })
                .forEach { rxValidation ->
                    if (rxValidation.billerField != null) {
                        references.add(
                            Reference(
                                index = rxValidation.billerField?.index,
                                type = rxValidation.textInputEditText?.tag.toString(),
                                name = rxValidation.textInputEditText?.hint.toString(),
                                value = rxValidation.textInputEditText?.text.toString()
                            )
                        )
                    }
                }
        }
        return references
    }

    private fun setupFormType(billerField: BillerField, isEnabled: Boolean, option: Option) {
        rxValidationResultList
            .filter { it.billerField?.parentFieldIndex.toString() == billerField.index }
            .forEach { rxValidation ->
                rxValidation.textInputEditText?.text?.clear()
                viewUtil.getTextInputLayout(rxValidation.textInputEditText)
                    ?.setBoxBackgroundColorResource(
                        when {
                            isEnabled -> R.color.colorTransparent
                            else -> R.color.colorDisableTextInputEditText
                        }
                    )
                rxValidation.textInputEditText?.isEnabled = isEnabled
                rxValidation.billerField?.options?.let {
                    val options = it
                        .asSequence()
                        .filter { it.parentId == option.id }
                        .map { it.name.notNullable() }
                        .toMutableList()

                    rxValidation.textInputEditText?.setOnClickListener {
                        MaterialDialog(this).show {
                            lifecycleOwner(this@BillsPaymentFormActivity)
                            title(text = rxValidation.billerField?.name.notNullable() + ": " + option.name)
                            listItems(
                                items = options,
                                selection = { _, index, _ ->
                                    rxValidation.textInputEditText?.setText(options[index])
                                    setupFormType(
                                        rxValidation.billerField!!,
                                        true,
                                        rxValidation.billerField!!.options!![index]
                                    )
                                }
                            )
                        }
                    }
                }
            }
    }

    private fun setupViews() {
        if (selectedAccount != null) {
            binding.viewPaymentTo.textInputLayoutPaymentTo.setBoxBackgroundColorResource(R.color.colorTransparent)
            binding.viewProposedTransactionDate.textInputLayoutProposedTransactionDate.setBoxBackgroundColorResource(R.color.colorTransparent)
            binding.textInputLayoutRemarks.setBoxBackgroundColorResource(R.color.colorTransparent)
            binding.viewProposedTransactionDate.textInputEditTextProposedTransactionDate.setOnClickListener(this)
            binding.viewProposedTransactionDate.constraintLayoutProposedTransactionDate.setOnClickListener(this)
            binding.viewPaymentTo.textInputEditTextPaymentTo.text?.clear()
            binding.viewPaymentTo.textInputEditTextPaymentTo.isEnabled = true
            binding.etAmount.setEnableAmount(true)
            binding.viewProposedTransactionDate.textInputEditTextProposedTransactionDate.isEnabled = true
            binding.textInputEditTextRemarks.isEnabled = true
        } else {
            binding.viewPaymentTo.constraintLayoutFrequentBiller.visibility = View.GONE
            binding.viewProposedTransactionDate.constraintLayoutProposedTransactionDate.visibility = View.GONE
            binding.viewPaymentTo.linearLayoutBillerFields.removeAllViews()
            binding.viewPaymentTo.textInputLayoutPaymentTo.setBoxBackgroundColorResource(R.color.colorDisableTextInputEditText)
            binding.viewProposedTransactionDate.textInputLayoutProposedTransactionDate.setBoxBackgroundColorResource(R.color.colorDisableTextInputEditText)
            binding.textInputLayoutRemarks.setBoxBackgroundColorResource(R.color.colorDisableTextInputEditText)
            binding.viewProposedTransactionDate.textInputEditTextProposedTransactionDate.setOnClickListener(null)
            binding.viewProposedTransactionDate.constraintLayoutProposedTransactionDate.setOnClickListener(null)
            binding.viewPaymentTo.textInputEditTextPaymentTo.isEnabled = false
            binding.etAmount.setEnableAmount(false)
            binding.viewProposedTransactionDate.textInputEditTextProposedTransactionDate.isEnabled = false
            binding.textInputEditTextRemarks.isEnabled = false
            binding.textInputEditTextTransferFrom.text?.clear()
            binding.viewPaymentTo.textInputEditTextPaymentTo.text?.clear()
            binding.etAmount.text?.clear()
            binding.textInputEditTextRemarks.text?.clear()
            binding.etAmount.clearFocus()
            showProposedTransferDate(false)
        }

        if (permissionCollection?.hasAllowToCreateBillsPaymentAdhoc == true ||
            permissionCollection?.hasAllowToCreateBillsPaymentFrequent == true
        ) {
            binding.viewPaymentTo.textInputLayoutPaymentTo.setBoxBackgroundColorResource(R.color.colorTransparent)
            binding.viewPaymentTo.textInputEditTextPaymentTo.isEnabled = true
        } else {
            binding.viewPaymentTo.textInputLayoutPaymentTo.setBoxBackgroundColorResource(R.color.colorDisableTextInputEditText)
            binding.viewPaymentTo.textInputEditTextPaymentTo.isEnabled = false
        }
    }

    private fun clearFrequentBiller() {
        frequentBiller = null
        fields = mutableListOf()
        destinationAccountNumber = null
        serviceId = null
        billerId = null
        binding.viewPaymentTo.linearLayoutBillerFields.removeAllViews()
        binding.viewPaymentTo.constraintLayoutFrequentBiller.visibility = View.GONE
        binding.viewPaymentTo.textInputLayoutPaymentTo.visibility = View.VISIBLE
        binding.viewPaymentTo.textInputEditTextPaymentTo.text?.clear()
    }

    private fun initBillerFields(billerFields: MutableList<BillerField>?) {
        isValidForm = false
        invalidateOptionsMenu()
        binding.viewPaymentTo.textInputEditTextPaymentTo.setText(biller?.name)
        binding.viewPaymentTo.constraintLayoutFrequentBiller?.visibility = View.GONE
        binding.viewPaymentTo.linearLayoutBillerFields.removeAllViews()
        imeOptionEditText.removeListener()
        billerFields
            ?.sortedWith(compareBy { it.index })
            ?.forEach {
                initEditText(it, binding.viewPaymentTo.linearLayoutBillerFields)
            }
        if (isActivatedValidationForm) {
            validateForm(true)
        }
        if (isSSSChannel()) {
            imeOptionEditText.addEditText(binding.textInputEditTextRemarks)
        } else {
            imeOptionEditText.addEditText(binding.etAmount, binding.textInputEditTextRemarks)
        }
        imeOptionEditText.startListener()
    }

    private fun initEditText(
        billerField: BillerField,
        linearLayout: LinearLayout?
    ) {
        val view: View?
        when (billerField.type) {
            Constant.FormType.FORM_TYPE_DATE -> {
                view = layoutInflater.inflate(R.layout.item_edittext_biller_date, null)
                setupField(view, billerField, Constant.FormType.FORM_TYPE_DATE)
            }
            Constant.FormType.FORM_TYPE_SELECT -> {
                view = layoutInflater.inflate(R.layout.item_edittext_biller_select, null)
                setupField(view, billerField, Constant.FormType.FORM_TYPE_SELECT)
            }
            else -> {
                view = layoutInflater.inflate(R.layout.item_edittext_biller, null)
                setupField(view, billerField, Constant.FormType.FORM_TYPE_STRING)
            }
        }

        linearLayout?.addView(view)
    }

    private fun setupField(
        view: View,
        billerField: BillerField,
        type: String
    ) {
        val textInputLayoutBillerField =
            view.findViewById<TextInputLayout>(R.id.textInputLayoutBillerField)
        val textInputEditTextBillerField =
            view.findViewById<TextInputEditText>(R.id.textInputEditTextBillerField)
        val imageViewBillerField =
            view.findViewById<ImageView>(R.id.imageViewBillerField)
        viewUtil.setInputType(textInputEditTextBillerField, billerField.type)
        textInputEditTextBillerField.id = billerField.index?.toInt()!!
        textInputLayoutBillerField.hint = billerField.name
        textInputEditTextBillerField.tag = billerField.type
        textInputLayoutBillerField.setBoxBackgroundColorResource(
            if (billerField.parentFieldIndex != null)
                R.color.colorDisableTextInputEditText
            else
                R.color.colorTransparent
        )

        when (type) {
            Constant.FormType.FORM_TYPE_DATE -> {
                textInputEditTextBillerField.isEnabled = billerField.parentFieldIndex == null
                imageViewBillerField.isEnabled = billerField.parentFieldIndex == null
                imageViewBillerField.setOnClickListener {
                    val calendarDate = Calendar.getInstance()
                    showDatePickerDynamicField(calendarDate, textInputEditTextBillerField)
                }
                textInputEditTextBillerField.setOnClickListener {
                    val calendarDate = Calendar.getInstance()
                    showDatePickerDynamicField(calendarDate, textInputEditTextBillerField)
                }
            }
            Constant.FormType.FORM_TYPE_SELECT -> {
                val options = billerField.options!!
                    .asSequence()
                    .map { option -> option.name!! }
                    .toMutableList()

                if (billerField.parentFieldIndex == null) {
                    textInputEditTextBillerField.setOnClickListener {
                        MaterialDialog(this@BillsPaymentFormActivity).show {
                            lifecycleOwner(this@BillsPaymentFormActivity)
                            title(text = billerField.name.notNullable())
                            listItems(
                                items = options,
                                selection = { _, index, _ ->
                                    textInputEditTextBillerField.setText(options[index])
                                    setupFormType(billerField, true, billerField.options!![index])
                                }
                            )
                        }
                    }
                }
            }
            else -> {
                viewUtil.setEditTextMaxLength(textInputEditTextBillerField, billerField.max!!)
                imeOptionEditText.addEditText(textInputEditTextBillerField)
            }
        }
        rxValidationResultList.add(
            ReferenceTextInputEditText(
                null,
                textInputEditTextBillerField,
                billerField
            )
        )
    }

    private fun showDatePickerDynamicField(calendar: Calendar, editText: EditText) {
        showDatePicker(
            calendar = calendar,
            callback = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)
                editText.setText(
                    viewUtil.getDateFormatByTimeMilliSeconds(
                        calendar.timeInMillis,
                        DateFormatEnum.DATE_FORMAT_DATE.value
                    )
                )
            }
        )
    }

    private fun initFrequentBiller(frequentFields: MutableList<Field>?) {
        val inflater = layoutInflater
        binding.viewPaymentTo.linearLayoutFrequentFields.removeAllViews()
        frequentFields?.forEach {
            val view = inflater.inflate(R.layout.item_textview_frequent_biller, null)
            val textViewFrequencyTitle =
                view.findViewById<TextView>(R.id.textViewFrequentBillerTitle)
            val textViewFrequentBiller = view.findViewById<TextView>(R.id.textViewFrequentBiller)
            textViewFrequencyTitle.text = it.name
            textViewFrequentBiller.text = it.value
            binding.viewPaymentTo.linearLayoutFrequentFields.addView(view)
        }
    }

    private fun navigateBillersScreen() {
        val bundle = Bundle()
        bundle.putString(
            BillerMainActivity.EXTRA_PAGE,
            BillerMainActivity.PAGE_BILLS_PAYMENT_FORM
        )
        bundle.putString(
            BillerMainActivity.EXTRA_TYPE,
            AllBillerFragment.TYPE_BILLER
        )
        bundle.putString(
            BillerMainActivity.EXTRA_ACCOUNT_ID,
            selectedAccount?.id.toString()
        )
        bundle.putString(
            BillerMainActivity.EXTRA_PERMISSION,
            JsonHelper.toJson(permissionCollection)
        )
        navigator.navigate(
            this,
            BillerMainActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun clickTransferFrom() {
        val bundle = Bundle()
        bundle.putString(
            AccountSelectionActivity.EXTRA_PAGE,
            AccountSelectionActivity.PAGE_BILLS_PAYMENT
        )
        if (selectedAccount != null)
            bundle.putString(
                AccountSelectionActivity.EXTRA_ID,
                selectedAccount?.id?.toString()
            )
        bundle.putString(
            AccountSelectionActivity.EXTRA_CHANNEL_ID,
            channel.id?.toString()
        )
        bundle.putString(
            AccountSelectionActivity.EXTRA_EXCEPT_CURRENCY,
            CURRENCY_USD
        )
        bundle.putString(
            AccountSelectionActivity.EXTRA_PERMISSION_ID,
            channel.permission?.id?.toString()
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

    private fun clickCloseFrequentBiller() {
        fields.clear()
        serviceId = null
        billerId = null
        frequentBiller = null
        rxValidationResultList.clear()
        billerFields?.clear()
        binding.viewPaymentTo.constraintLayoutFrequentBiller.visibility = View.GONE
        binding.viewPaymentTo.linearLayoutBillerFields.visibility = View.VISIBLE
        binding.viewPaymentTo.textInputLayoutPaymentTo.visibility = View.VISIBLE
        binding.viewPaymentTo.textInputEditTextPaymentTo.text?.clear()
        binding.viewPaymentTo.linearLayoutBillerFields.removeAllViews()
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
            ProposedTransferDateActivity.PAGE_BILLS_PAYMENT
        )
        bundle.putBoolean(
            ProposedTransferDateActivity.EXTRA_ENABLED_SCHEDULED,
            false
        )
        bundle.putString(
            ProposedTransferDateActivity.EXTRA_BILLER_ID,
            billerId
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
            if (isInitialSubmitForm && !isActivatedValidationForm) {
                isInitialSubmitForm = false
                validateForm(false)
            } else {
                showMissingFieldDialog()
            }
        }
    }

    private fun showMissingFieldDialog() {
        showMaterialDialogError(message = formatString(R.string.msg_error_missing_fields))
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
        val billsPaymentForm =
            BillsPaymentForm()
        billsPaymentForm.immediate = proposedTransferDate?.immediately
        billsPaymentForm.amount = if (isSSSChannel()) {
            null
        } else {
            Amount(selectedAccount?.currency, binding.etAmount.getNumericValue())
        }
        billsPaymentForm.source = selectedAccount?.accountNumber
        billsPaymentForm.destination = destinationAccountNumber
        billsPaymentForm.remarks = binding.textInputEditTextRemarks.text.toString()
        billsPaymentForm.billerServiceId = serviceId
        billsPaymentForm.billerId = billerId
        billsPaymentForm.referenceNumber = viewModel.uuid.value
        billsPaymentForm.frequentBillerVersionId = frequentBiller?.versionId
        billsPaymentForm.billerName =
            if (isBIRChannel() || isSSSChannel())
                biller?.name
            else
                binding.viewPaymentTo.textInputEditTextPaymentTo.text.toString()
        billsPaymentForm.frequency = proposedTransferDate?.frequency
        billsPaymentForm.references =
            getReferences(binding.viewPaymentTo.constraintLayoutFrequentBiller.visibility == View.VISIBLE)
        billsPaymentForm.paymentDate = proposedTransferDate?.startDate
        billsPaymentForm.occurrences =
            if (proposedTransferDate?.occurrences == 0) null
            else proposedTransferDate?.occurrences
        billsPaymentForm.recurrenceTypeId = proposedTransferDate?.recurrenceTypeId ?: "1"
        if (proposedTransferDate?.immediately == false) {
            billsPaymentForm.recurrenceEndDate = proposedTransferDate?.endDate
        }
        billsPaymentForm.references =
            getValidReferences(billsPaymentForm.references)
        viewModel.validateBillsPayment(billsPaymentForm, selectedAccount?.currency ?: "PHP")
    }

    private fun navigateBillsPaymentConfirmationScreen(billsPaymentForm: BillsPaymentForm) {
        val bundle = Bundle()
        bundle.putString(
            BillsPaymentConfirmationActivity.EXTRA_ACCOUNT,
            JsonHelper.toJson(selectedAccount)
        )
        bundle.putString(
            BillsPaymentConfirmationActivity.EXTRA_ACCOUNT_TYPE,
            getString(R.string.title_bills_payment)
        )
        bundle.putInt(
            BillsPaymentConfirmationActivity.EXTRA_CHANNEL_ID,
            channel.id!!
        )
        bundle.putString(
            BillsPaymentConfirmationActivity.EXTRA_BILLS_PAYMENT,
            JsonHelper.toJson(billsPaymentForm)
        )
        bundle.putString(
            BillsPaymentConfirmationActivity.EXTRA_REMINDERS,
            JsonHelper.toJson(channel.formattedReminders)
        )
        navigator.navigate(
            this,
            BillsPaymentConfirmationActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
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

    private fun initChannelView(channel: Channel) {
        binding.viewChannelHeader.imageViewChannel.setImageResource(
            when {
                channel.id == ChannelBankEnum.BILLS_PAYMENT.getChannelId() ->
                    R.drawable.ic_channel_bills_payment
                isBIRChannel() ->
                    R.drawable.ic_channel_bir
                isSSSChannel() ->
                    R.drawable.ic_channel_sss
                channel.id == ChannelBankEnum.UBP_TO_UBP.getChannelId() ->
                    R.drawable.ic_ub_orange
                channel.id == ChannelBankEnum.PESONET.getChannelId() ->
                    R.drawable.ic_fund_transfer_pesonet
                channel.id == ChannelBankEnum.SWIFT.getChannelId() ->
                    R.drawable.ic_fund_transfer_swift
                channel.id == ChannelBankEnum.INSTAPAY.getChannelId() ->
                    R.drawable.ic_fund_transfer_instapay
                channel.id == ChannelBankEnum.PDDTS.getChannelId() ->
                    R.drawable.ic_fund_transfer_pddts
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

    private fun initBiller() {
        if (isBIRChannel() || isSSSChannel()) {
            biller = JsonHelper.fromJson(intent.getStringExtra(EXTRA_BILLER))
            destinationAccountNumber = biller?.accountNumber
            serviceId = biller?.serviceId
            billerId = biller?.code
            binding.viewPaymentTo.textInputLayoutPaymentTo.visibility = View.GONE
            binding.viewPaymentTo.textViewPaymentToBiller.visibility = View.VISIBLE
            binding.viewPaymentTo.textViewPaymentToBiller.text = biller?.name
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.viewPaymentTo.root as ConstraintLayout)
            constraintSet.connect(
                binding.viewPaymentTo.linearLayoutBillerFields.id,
                ConstraintSet.TOP,
                binding.viewPaymentTo.textViewPaymentToBiller.id,
                ConstraintSet.BOTTOM
            )
            constraintSet.applyTo(binding.viewPaymentTo.root as ConstraintLayout)
            if (isSSSChannel()) {
                binding.textViewAmount.visibility = View.GONE
                binding.tilAmount.visibility = View.GONE
            }
        }
    }

    private fun getValidReferences(references: MutableList<Reference>): MutableList<Reference> {
        references.forEachIndexed { index, reference ->
            if (viewUtil.isValidDateFormat(ViewUtil.DATE_FORMAT_DATE, reference.value) &&
                reference.name.equals("Return Period", true)
            ) {
                references[index].value = viewUtil.getDateFormatByDateString(
                    reference.value,
                    ViewUtil.DATE_FORMAT_DATE,
                    ViewUtil.DATE_FORMAT_DATE_SLASH
                )
            } else if (reference.type != null &&
                reference.type.equals(Constant.FormType.FORM_TYPE_NUMERIC_PADDING)
            ) {
                val billerField = billerFields
                    ?.find {
                        it.name == reference.name &&
                                it.type == Constant.FormType.FORM_TYPE_NUMERIC_PADDING
                    }
                references[index].value = viewUtil.padLeftZeros(
                    reference.value.toString(),
                    billerField?.max ?: 0
                )
            }
        }
        return references
    }

    private fun showValidateTransactionBottomSheet(billsPaymentValidate: BillsPaymentValidate) {
        validateTransactionBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            getString(R.string.title_bills_payment_validation),
            null,
            getString(R.string.action_continue),
            getString(R.string.action_back),
            null,
            JsonHelper.toJson(billsPaymentValidate)
        )
        validateTransactionBottomSheet?.setOnConfirmationPageCallBack(this)
        validateTransactionBottomSheet?.show(
            supportFragmentManager,
            TAG_VALIDATE_TRANSACTION_DIALOG
        )
    }

    private fun showCancelTransactionBottomSheet() {
        val leavePageBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_leave_page),
            formatString(R.string.msg_leave_page),
            formatString(R.string.action_confirm),
            formatString(R.string.action_no)
        )
        leavePageBottomSheet.setOnConfirmationPageCallBack(object: OnConfirmationPageCallBack {
            override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
                super.onClickPositiveButtonDialog(data, tag)
                leavePageBottomSheet.dismiss()
                this@BillsPaymentFormActivity.onBackPressed(true)
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

    private fun clearFormFocus() {
        binding.constraintLayoutParent.requestFocus()
        binding.constraintLayoutParent.isFocusableInTouchMode = true
    }

    private fun isBIRChannel(): Boolean {
        return channel.id == ChannelBankEnum.BIR.getChannelId()
    }

    private fun isSSSChannel(): Boolean {
        return channel.id == ChannelBankEnum.SSS.getChannelId()
    }

    companion object {
        const val EXTRA_CHANNEL = "channel"
        const val EXTRA_SERVICE_FEE = "service_fee"
        const val EXTRA_BILLER = "biller"

        const val TAG_VALIDATE_TRANSACTION_DIALOG = "validate_transaction_dialog"
        const val TAG_VALIDATE_TIME_ZONE_DIALOG = "validate_time_zone_dialog"

        const val CURRENCY_USD = "USD"
    }

    override val viewModelClassType: Class<BillsPaymentFormViewModel>
        get() = BillsPaymentFormViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityBillsPaymentFormBinding
        get() = ActivityBillsPaymentFormBinding::inflate
}
