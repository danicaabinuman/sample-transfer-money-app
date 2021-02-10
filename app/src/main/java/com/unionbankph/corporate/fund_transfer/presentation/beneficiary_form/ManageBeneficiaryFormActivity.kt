package com.unionbankph.corporate.fund_transfer.presentation.beneficiary_form

import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.presentation.source_account.SourceAccountActivity
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.InputSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.edittext.ImeOptionEditText
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.corporate.data.model.Channel
import com.unionbankph.corporate.fund_transfer.data.form.BeneficiaryForm
import com.unionbankph.corporate.fund_transfer.data.model.Bank
import com.unionbankph.corporate.fund_transfer.data.model.BeneficiaryDetailDto
import com.unionbankph.corporate.fund_transfer.data.model.CreationBeneficiaryDto
import com.unionbankph.corporate.fund_transfer.data.model.SwiftBank
import com.unionbankph.corporate.fund_transfer.presentation.bank.BankActivity
import com.unionbankph.corporate.fund_transfer.presentation.swift_bank.SwiftBankActivity
import com.unionbankph.corporate.general.presentation.result.ResultLandingPageActivity
import com.unionbankph.corporate.settings.presentation.country.CountryActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_manage_beneficiary_form.*
import kotlinx.android.synthetic.main.item_edittext_bank_details.*
import kotlinx.android.synthetic.main.widget_channel_header.*
import kotlinx.android.synthetic.main.widget_edit_text_allowed_source_account.*
import kotlinx.android.synthetic.main.widget_edit_text_mobile_number.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*
import timber.log.Timber

class ManageBeneficiaryFormActivity :
    BaseActivity<ManageBeneficiaryFormViewModel>(R.layout.activity_manage_beneficiary_form),
    View.OnClickListener,
    TextView.OnEditorActionListener,
    OnTutorialListener,
    OnConfirmationPageCallBack, ImeOptionEditText.OnImeOptionListener {

    private lateinit var imeOptionEditText: ImeOptionEditText

    private lateinit var channel: Channel

    private lateinit var buttonAction: Button

    private var saveChangesConfirmationBottomSheet: ConfirmationBottomSheet? = null

    private var bank: Bank? = null

    private var swiftBank: SwiftBank? = null

    private var isActivatedValidationForm: Boolean = false

    private var isFocusedEditTextMobileNumber: Boolean = false

    private var countryCode = Constant.getDefaultCountryCode()

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        tutorialEngineUtil.setOnTutorialListener(this)
        initEditBeneficiaryDetail()
        setupBindings()
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
                        String.format(
                            if (isCreationForm())
                                getString(R.string.title_create_beneficiary)
                            else
                                getString(R.string.title_edit_beneficiary),
                            if (channel.id == ChannelBankEnum.UBP_TO_UBP.getChannelId()) {
                                getString(R.string.title_ubp_channel)
                            } else {
                                ConstantHelper.Text.getChannelByChannelId(channel.id)
                            }
                        ),
                        it.orgName
                    )
                }
            }
        })
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[ManageBeneficiaryFormViewModel::class.java]
        viewModel.stateManageBeneficiaryForm.observe(this, Observer {
            when (it) {
                is ShowManageBeneficiaryFormLoading -> {
                    showProgressAlertDialog(ManageBeneficiaryFormActivity::class.java.simpleName)
                }
                is ShowManageBeneficiaryFormDismissLoading -> {
                    dismissProgressAlertDialog()
                }

                is ShowManageBeneficiaryFormSubmit -> {
                    beneficiarySubmission(it.creationBeneficiaryDto)
                }
                is ShowManageBeneficiaryFormError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.accounts.observe(this, Observer {
            showAllowedSourceAccounts(it.first, it.second)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        val menuActionButton = menu.findItem(R.id.menu_action_button)
        val menuView = menuActionButton.actionView
        val helpMenu = menu.findItem(R.id.menu_help)
        buttonAction = menuView.findViewById(R.id.buttonAction)
        buttonAction.text = getString(R.string.action_save)
        buttonAction.enableButton(true)
        helpMenu.isVisible = true
        menuActionButton.isVisible = true
        buttonAction.setOnClickListener { onOptionsItemSelected(menuActionButton) }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                showLeavePageBottomSheet()
                true
            }
            R.id.menu_help -> {
                isClickedHelpTutorial = true
                viewUtil.dismissKeyboard(this)
                scrollView.post { scrollView.smoothScrollTo(0, 0) }
                Handler().postDelayed(
                    {
                        startViewTutorial()
                    }, resources.getInteger(R.integer.time_enter_tutorial_immediate).toLong()
                )
                true
            }
            R.id.menu_action_button -> {
                clickSave()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        showLeavePageBottomSheet()
    }

    override fun onClick(view: View?) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return
        mLastClickTime = SystemClock.elapsedRealtime()
        when (view?.id) {
            R.id.linearLayoutAllowedSourceAccounts,
            R.id.textInputEditTextAllowedSourceAccounts -> {
                clickAllowedSourceAccounts()
            }
            R.id.textInputEditTextReceivingBank -> {
                clickReceivingBank()
            }
            R.id.constraintLayoutCountryCode -> {
                navigateCountryScreen()
            }
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            when (v?.id) {
                R.id.textInputEditTextAccountNumber,
                R.id.textInputEditTextBeneficiaryAddress -> {
                    clickReceivingBank()
                }
            }
            return true
        }
        return false
    }

    override fun onFocusEditText(v: View, hasFocus: Boolean) {
        if (v.id == R.id.editTextMobileNumber) {
            isFocusedEditTextMobileNumber = hasFocus
            setBackgroundEditTextMobileNumber()
        }
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        initClickEvent()
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
            scrollView.post { scrollView.smoothScrollTo(0, 0) }
        } else {
            val radius = resources.getDimension(R.dimen.field_radius)
            when (view) {
                viewTutorialAllowedSourceAccounts -> {
                    viewUtil.setFocusOnView(scrollView, textInputLayoutBeneficiaryCode)
                    tutorialEngineUtil.startTutorial(
                        this,
                        textInputLayoutBeneficiaryCode,
                        R.layout.frame_tutorial_upper_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_beneficiary_form_beneficiary_code),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                textInputLayoutBeneficiaryCode -> {
                    viewUtil.setFocusOnView(scrollView, textInputLayoutBeneficiaryName)
                    tutorialEngineUtil.startTutorial(
                        this,
                        textInputLayoutBeneficiaryName,
                        R.layout.frame_tutorial_upper_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_beneficiary_form_beneficiary_name),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                textInputLayoutBeneficiaryName -> {
                    viewUtil.setFocusOnView(scrollView, textInputLayoutAccountNumber)
                    tutorialEngineUtil.startTutorial(
                        this,
                        textInputLayoutAccountNumber,
                        R.layout.frame_tutorial_upper_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_beneficiary_form_beneficiary_account_number),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                textInputLayoutAccountNumber -> {
                    if (channel.id == ChannelBankEnum.UBP_TO_UBP.getChannelId()) {
                        startButtonSaveTutorial()
                    } else {
                        if (channel.id == ChannelBankEnum.SWIFT.getChannelId()) {
                            viewUtil.setFocusOnView(scrollView, textInputLayoutBeneficiaryAddress)
                            tutorialEngineUtil.startTutorial(
                                this,
                                textInputLayoutBeneficiaryAddress,
                                R.layout.frame_tutorial_lower_left,
                                radius,
                                false,
                                getString(R.string.msg_tutorial_beneficiary_form_beneficiary_address),
                                GravityEnum.TOP,
                                OverlayAnimationEnum.ANIM_EXPLODE
                            )
                        } else {
                            startBankDetailsTutorial()
                        }
                    }
                }
                textInputLayoutBeneficiaryAddress -> {
                    startBankDetailsTutorial()
                }
                viewTutorialBankDetails -> {
                    startButtonSaveTutorial()
                }
                buttonAction -> {
                    scrollView.post { scrollView.smoothScrollTo(0, 0) }
                }
            }
        }
    }

    override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
        saveChangesConfirmationBottomSheet?.dismiss()
        submitForm()
    }

    override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
        saveChangesConfirmationBottomSheet?.dismiss()
    }

    private fun startButtonSaveTutorial() {
        val buttonRadius = resources.getDimension(R.dimen.button_radius)
        tutorialEngineUtil.startTutorial(
            this,
            buttonAction,
            R.layout.frame_tutorial_upper_right,
            buttonRadius,
            false,
            getString(R.string.msg_tutorial_beneficiary_form_beneficiary_button),
            GravityEnum.BOTTOM,
            OverlayAnimationEnum.ANIM_EXPLODE
        )
    }

    private fun startBankDetailsTutorial() {
        val radius = resources.getDimension(R.dimen.field_radius)
        viewUtil.setFocusOnView(scrollView, viewTutorialBankDetails)
        tutorialEngineUtil.startTutorial(
            this,
            viewTutorialBankDetails,
            R.layout.frame_tutorial_lower_left,
            radius,
            false,
            getString(R.string.msg_tutorial_beneficiary_form_beneficiary_bank_details),
            GravityEnum.TOP,
            OverlayAnimationEnum.ANIM_EXPLODE
        )
    }

    private fun initKeyboardActionEvent() {
        if (channel.id == ChannelBankEnum.SWIFT.getChannelId()) {
            textInputEditTextBeneficiaryAddress.setOnEditorActionListener(this)
        } else if (channel.id != ChannelBankEnum.UBP_TO_UBP.getChannelId()) {
            textInputEditTextAccountNumber.setOnEditorActionListener(this)
        }
    }

    private fun initClickEvent() {
        textInputEditTextAllowedSourceAccounts.setOnClickListener(this)
        linearLayoutAllowedSourceAccounts.setOnClickListener(this)
        textInputEditTextReceivingBank.setOnClickListener(this)
        constraintLayoutCountryCode.setOnClickListener(this)
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            if (it.eventType == InputSyncEvent.ACTION_INPUT_BANK_RECEIVER) {
                if (channel.id == ChannelBankEnum.SWIFT.getChannelId()) {
                    showSwiftBankDetails(
                        JsonHelper.fromJson(it.payload)
                    )
                } else {
                    showBankDetails(JsonHelper.fromJson(it.payload))
                }
            } else if (it.eventType == InputSyncEvent.ACTION_INPUT_SOURCE_ACCOUNTS) {
                viewModel.onSetSourceAccounts(it.payload)
            } else if (it.eventType == InputSyncEvent.ACTION_INPUT_COUNTRY) {
                this.countryCode = JsonHelper.fromJson(it.payload)
                showCountryDetails(countryCode)
            }
        }.addTo(disposables)
    }

    private fun showCountryDetails(countryCode: CountryCode?) {
        textViewCallingCode.text = countryCode?.callingCode
        imageViewFlag.setImageResource(
            viewUtil.getDrawableById("ic_flag_${countryCode?.code?.toLowerCase()}")
        )
    }

    private fun showAllowedSourceAccounts(
        sourceAccounts: MutableList<Account>,
        totalElements: Int
    ) {
        linearLayoutAllowedSourceAccounts.removeAllViews()
        textInputEditTextAllowedSourceAccounts.setText("-")
        textInputLayoutAllowedSourceAccounts.visibility = View.GONE
        linearLayoutAllowedSourceAccounts.visibility = View.VISIBLE
        imageViewClose.visibility = View.VISIBLE
        imageViewClose.setOnClickListener {
            clearAllowedSourceAccounts()
        }

        if (sourceAccounts.size > 3 || totalElements > 3) {
            val viewAllowedSourceAccount =
                layoutInflater.inflate(R.layout.item_textview_allowed_source_account, null)
            val textViewAccountName =
                viewAllowedSourceAccount.findViewById<TextView>(R.id.textViewAccountName)
            val textViewAccountNumber =
                viewAllowedSourceAccount.findViewById<TextView>(R.id.textViewAccountNumber)
            textViewAccountNumber.visibility = View.GONE
            textViewAccountName.text = formatString(
                R.string.params_select_allowed_source_accounts,
                formatString(
                    R.string.param_color,
                    convertColorResourceToHex(getAccentColor()),
                    totalElements
                ),
                formatString(R.string.title_beneficiary)
            ).toHtmlSpan()
            linearLayoutAllowedSourceAccounts.addView(viewAllowedSourceAccount)
        } else {
            sourceAccounts.forEach {
                val viewAllowedSourceAccount =
                    layoutInflater.inflate(R.layout.item_textview_allowed_source_account, null)
                val textViewAccountName =
                    viewAllowedSourceAccount.findViewById<TextView>(R.id.textViewAccountName)
                val textViewAccountNumber =
                    viewAllowedSourceAccount.findViewById<TextView>(R.id.textViewAccountNumber)
                textViewAccountName.text = it.name
                textViewAccountNumber.text = viewUtil.getAccountNumberFormat(it.accountNumber)
                linearLayoutAllowedSourceAccounts.addView(viewAllowedSourceAccount)
            }
        }
    }

    private fun clickAllowedSourceAccounts() {
        val bundle = Bundle()
        bundle.putParcelable(
            SourceAccountActivity.EXTRA_SOURCE_ACCOUNT_FORM,
            viewModel.sourceAccountForm.value
        )
        bundle.putString(
            SourceAccountActivity.EXTRA_PAGE,
            SourceAccountActivity.PAGE_BENEFICIARY_FORM
        )
        bundle.putString(
            SourceAccountActivity.EXTRA_CHANNEL_ID,
            channel.id.toString()
        )
        navigator.navigate(
            this,
            SourceAccountActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun clickReceivingBank() {
        if (channel.id == ChannelBankEnum.SWIFT.getChannelId()) {
            navigator.navigate(
                this,
                SwiftBankActivity::class.java,
                null,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
            )
        } else {
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
    }

    private fun clickSave() {
        viewUtil.dismissKeyboard(this)
        if (isValidForm) {
            if (isCreationForm()) {
                submitForm()
            } else {
                showSaveChangesBottomSheet()
            }
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

    private fun clearAllowedSourceAccounts() {
        viewModel.clearSourceAccounts()
        linearLayoutAllowedSourceAccounts.visibility = View.GONE
        imageViewClose.visibility = View.GONE
        textInputLayoutAllowedSourceAccounts.visibility = View.VISIBLE
        textInputEditTextAllowedSourceAccounts.text?.clear()
    }

    private fun initFields(channel: Channel) {
        initKeyboardActionEvent()
        viewUtil.setEditTextMaxLength(
            textInputEditTextAccountNumber,
            when (channel.id) {
                ChannelBankEnum.UBP_TO_UBP.getChannelId() -> {
                    resources.getInteger(R.integer.max_length_account_number_ubp)
                }
                ChannelBankEnum.SWIFT.getChannelId() -> {
                    resources.getInteger(R.integer.max_length_account_number_swift_banks)
                }
                else -> {
                    resources.getInteger(R.integer.max_length_account_number_other_banks)
                }
            }
        )
        if (channel.id != ChannelBankEnum.SWIFT.getChannelId()) {
            viewUtil.setEditTextMaskListener(
                textInputEditTextAccountNumber,
                if (channel.id == ChannelBankEnum.UBP_TO_UBP.getChannelId()) {
                    getString(R.string.hint_account_number_format)
                } else {
                    getString(R.string.hint_other_bank_account_number_format)
                }
            )
        }
        textInputLayoutReceivingBank.hint =
            if (channel.id == ChannelBankEnum.SWIFT.getChannelId()) {
                getString(R.string.hint_swift_receiving_bank)
            } else {
                getString(R.string.hint_pesonet_receiving_bank)
            }
        imeOptionEditText =
            ImeOptionEditText()
        imeOptionEditText.addEditText(
            textInputEditTextBeneficiaryCode,
            textInputEditTextBeneficiaryName,
            textInputEditTextAccountNumber,
            textInputEditTextBeneficiaryAddress,
            textInputEditTextBeneficiaryEmailAddress,
            editTextMobileNumber
        )
        imeOptionEditText.setOnImeOptionListener(this)
        setRightIconEditText(textInputEditTextBeneficiaryCode)
        textInputEditTextBeneficiaryCode.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= textInputEditTextBeneficiaryCode.right -
                    textInputEditTextBeneficiaryCode.compoundDrawables[DRAWABLE_RIGHT].bounds.width()
                ) {
                    showToolTip(
                        formatString(R.string.title_beneficiary_code),
                        formatString(R.string.msg_tooltip_beneficiary_code)
                    )
                    return@OnTouchListener true
                }
            }
            false
        })
        if (channel.id == ChannelBankEnum.SWIFT.getChannelId()) {
            viewUtil.setInputType(textInputEditTextAccountNumber, "string")
            viewUtil.setEditTextFilter(
                textInputEditTextAccountNumber,
                ViewUtil.REGEX_FORMAT_ALPHA_NUMERIC
            )
            viewUtil.setEditTextMaxLength(
                textInputEditTextAccountNumber,
                resources.getInteger(R.integer.max_length_account_number_swift_banks)
            )
            textInputLayoutBeneficiaryAddress.visibility = View.VISIBLE
            imeOptionEditText.addEditText(textInputEditTextBeneficiaryAddress)
            setRightIconEditText(textInputEditTextAccountNumber)
            textInputEditTextAccountNumber.setOnTouchListener(View.OnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= textInputEditTextAccountNumber.right -
                        textInputEditTextAccountNumber.compoundDrawables[DRAWABLE_RIGHT].bounds.width()
                    ) {
                        showToolTip(
                            formatString(R.string.title_swift_account_number),
                            formatString(R.string.msg_tooltip_swift_account_number)
                        )
                        return@OnTouchListener true
                    }
                }
                false
            })
        }
        if (channel.id == ChannelBankEnum.UBP_TO_UBP.getChannelId()) {
            textViewBankDetails.visibility = View.GONE
            viewBankDetails.visibility = View.GONE
        }
        imeOptionEditText.startListener()
    }

    private fun setRightIconEditText(editText: EditText) {
        editText.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.ic_warning_circle_orange,
            0
        )
    }

    private fun validateForm(isValueChanged: Boolean) {
        val textInputEditTextAllowedSourceAccountsObservable = viewUtil.rxTextChanges(
            true,
            isValueChanged,
            resources.getInteger(R.integer.min_length_field),
            resources.getInteger(R.integer.max_length_field_100),
            textInputEditTextAllowedSourceAccounts
        )
        val textInputEditTextBeneficiaryNameObservable = viewUtil.rxTextChanges(
            true,
            isValueChanged,
            resources.getInteger(R.integer.min_length_field),
            resources.getInteger(R.integer.max_length_field_100),
            textInputEditTextBeneficiaryName
        )
        val textInputEditTextAccountNumberObservable = viewUtil.rxTextChanges(
            true,
            isValueChanged,
            resources.getInteger(R.integer.min_length_field),
            resources.getInteger(R.integer.max_length_field_100),
            textInputEditTextAccountNumber
        )
        val textInputEditTextBeneficiaryAddressObservable = viewUtil.rxTextChanges(
            true,
            isValueChanged,
            resources.getInteger(R.integer.min_length_field),
            resources.getInteger(R.integer.max_length_field_100),
            textInputEditTextBeneficiaryAddress
        )
        val textInputEditTextReceivingBankObservable = viewUtil.rxTextChanges(
            true,
            isValueChanged,
            resources.getInteger(R.integer.min_length_field),
            resources.getInteger(R.integer.max_length_field_100),
            textInputEditTextReceivingBank
        )
//        val textInputEditTextBeneficiaryMobileNumberObservable = viewUtil.rxTextChanges(
//            true,
//            isValueChanged,
//            resources.getInteger(R.integer.min_length_field),
//            resources.getInteger(R.integer.max_length_field_100),
//            textInputEditTextBeneficiaryMobileNumber
//        )

        initSetError(textInputEditTextAllowedSourceAccountsObservable)
        initSetError(textInputEditTextBeneficiaryNameObservable)
        initSetError(textInputEditTextAccountNumberObservable)
        if (channel.id == ChannelBankEnum.SWIFT.getChannelId()) {
            initSetError(textInputEditTextBeneficiaryAddressObservable)
        }
        initSetError(textInputEditTextReceivingBankObservable)
        // initSetError(textInputEditTextBeneficiaryMobileNumberObservable)
        val rxCombineValidator = when {
            channel.id == ChannelBankEnum.SWIFT.getChannelId() ->
                RxCombineValidator(
                    textInputEditTextAllowedSourceAccountsObservable,
                    textInputEditTextBeneficiaryNameObservable,
                    textInputEditTextAccountNumberObservable,
                    textInputEditTextBeneficiaryAddressObservable,
                    textInputEditTextReceivingBankObservable
                )
            channel.id == ChannelBankEnum.UBP_TO_UBP.getChannelId() ->
                RxCombineValidator(
                    textInputEditTextAllowedSourceAccountsObservable,
                    textInputEditTextBeneficiaryNameObservable,
                    textInputEditTextAccountNumberObservable
                )
            else -> RxCombineValidator(
                textInputEditTextAllowedSourceAccountsObservable,
                textInputEditTextBeneficiaryNameObservable,
                textInputEditTextAccountNumberObservable,
                textInputEditTextReceivingBankObservable
            )
        }
        rxCombineValidator
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnNext {
                isValidForm = it
                if (!isValueChanged && isValidForm) {
                    clickSave()
                }
                if (!isValueChanged && !isValidForm) {
                    showMissingFieldDialog()
                }
            }
            .doOnComplete {
                Timber.d("doOnComplete")
                if (!isValueChanged) {
                    isActivatedValidationForm = true
                    validateForm(true)
                }
                initEditTextDefaultValue()
            }
            .subscribe()
            .addTo(disposables)
    }

    private fun initEditTextDefaultValue() {
        if (isActivatedValidationForm) {
            if (textInputEditTextAllowedSourceAccounts.length() != 0) {
                textInputEditTextAllowedSourceAccounts.setText(
                    textInputEditTextAllowedSourceAccounts.text.toString()
                )
            }
            if (textInputEditTextBeneficiaryName.length() != 0) {
                textInputEditTextBeneficiaryName.setText(textInputEditTextBeneficiaryName.text.toString())
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
        }
    }

    private fun setBackgroundEditTextMobileNumber() {
        if (isFocusedEditTextMobileNumber) {
            constraintLayoutMobileNumber.background =
                ContextCompat.getDrawable(
                    this,
                    R.drawable.bg_edittext_form_focused
                )
        } else {
            constraintLayoutMobileNumber.background =
                ContextCompat.getDrawable(
                    this,
                    R.drawable.bg_edittext_form
                )
        }
    }

    private fun showSwiftBankDetails(swiftBank: SwiftBank) {
        this.swiftBank = swiftBank
        val constraintLayoutBankDetails =
            viewBankDetails.findViewById<ConstraintLayout>(R.id.constraintLayoutBankDetails)
        val textViewSwiftCode = viewBankDetails.findViewById<TextView>(R.id.textViewSwiftCode)
        val textViewReceivingBank =
            viewBankDetails.findViewById<TextView>(R.id.textViewReceivingBank)
        val textViewBankAddress = viewBankDetails.findViewById<TextView>(R.id.textViewBankAddress)
        val textViewCountry = viewBankDetails.findViewById<TextView>(R.id.textViewCountry)
        val imageViewClose = viewBankDetails.findViewById<ImageView>(R.id.imageViewBeneficiaryClose)
        textInputLayoutReceivingBank.visibility = View.GONE
        textInputEditTextReceivingBank.setText(swiftBank.bankName)
        textViewSwiftCode.text = viewUtil.getStringOrEmpty(swiftBank.swiftBicCode)
        textViewReceivingBank.text = viewUtil.getStringOrEmpty(swiftBank.bankName)
        textViewBankAddress.text = swiftBank.let {
            if (it.address1 == null && it.address2 == null) {
                Constant.EMPTY
            } else {
                it.address1.notNullable() + it.address2.notNullable()
            }
        }
        textViewCountry.text = swiftBank.country
        constraintLayoutBankDetails.setOnClickListener {
            clickReceivingBank()
        }
        imageViewClose.setOnClickListener {
            constraintLayoutBankDetails.visibility = View.GONE
            textInputEditTextReceivingBank.text?.clear()
            textInputLayoutReceivingBank.visibility = View.VISIBLE
        }
        constraintLayoutBankDetails.visibility = View.VISIBLE
    }

    private fun initEditBeneficiaryDetail() {
        val beneficiaryDetailDto =
            intent.getParcelableExtra<BeneficiaryDetailDto>(EXTRA_BENEFICIARY)
        if (beneficiaryDetailDto != null) {
            viewModel.beneficiaryDetailDto.onNext(beneficiaryDetailDto)
        } else {
            val channel = JsonHelper.fromJson<Channel>(intent.getStringExtra(EXTRA_CHANNEL))
            initBeneficiaryFormDetail(channel)
            viewModel.getSourceAccounts(channel.id.toString(), "25")
        }
    }

    private fun setupBindings() {
        viewModel.beneficiaryDetailDto.subscribe { beneficiaryDetailDto ->
            this.countryCode =
                beneficiaryDetailDto.countryCode ?: Constant.getDefaultCountryCode()
            beneficiaryDetailDto?.let { beneficiary ->
                val channelDesc = ConstantHelper.Text.getChannelByChannelId(beneficiary.channelId)
                val channel = Channel(
                    beneficiary.channelId,
                    channelDesc
                )
                initBeneficiaryFormDetail(
                    Channel(
                        beneficiary.channelId,
                        channelDesc
                    )
                )
                validateForm(true)
                Handler().post {
                    if (channel.id == ChannelBankEnum.SWIFT.getChannelId()) {
                        showSwiftBankDetails(
                            SwiftBank(
                                beneficiary.swiftBankDetails?.address1,
                                beneficiary.swiftBankDetails?.address2,
                                beneficiary.swiftBankDetails?.bankName,
                                beneficiary.swiftBankDetails?.cityName,
                                beneficiary.swiftBankDetails?.country,
                                beneficiary.swiftBankDetails?.location,
                                beneficiary.swiftBankDetails?.swiftBicCode
                            )
                        )
                    } else if (channel.id != ChannelBankEnum.SWIFT.getChannelId() &&
                        channel.id != ChannelBankEnum.UBP_TO_UBP.getChannelId()
                    ) {
                        showBankDetails(
                            Bank(
                                bank = beneficiary.bankDetails?.name,
                                instapayCode = beneficiary.instapayCode,
                                pesonetBrstn = beneficiary.brstnCode,
                                pddtsFinacleCode = beneficiary.firmCode
                            )
                        )
                    }
                    if (beneficiary.accounts != null) {
                        viewModel.onSetSourceAccountsFromEdit(beneficiary.accounts.notNullable())
                    }
                    textInputEditTextBeneficiaryCode.setText(beneficiary.code)
                    textInputEditTextBeneficiaryName.setText(beneficiary.name)
                    textInputEditTextAccountNumber.setText(beneficiary.accountNumber)
                    textInputEditTextBeneficiaryEmailAddress.setText(beneficiary.emailAddress)
                    editTextMobileNumber.setText(beneficiary.mobileNumber)
                    showCountryDetails(countryCode)
                    if (channel.id == ChannelBankEnum.SWIFT.getChannelId()) {
                        textInputEditTextBeneficiaryAddress.setText(beneficiary.address)
                    }
                }
            }
        }.addTo(disposables)
    }

    private fun showBankDetails(bank: Bank) {
        this.bank = bank
        textInputEditTextReceivingBank.setText(bank.bank)
    }

    private fun submitForm() {
        clearFormFocus()
        val beneficiaryForm = BeneficiaryForm()
        beneficiaryForm.code = textInputEditTextBeneficiaryCode.text.toString()
        beneficiaryForm.name = textInputEditTextBeneficiaryName.text.toString()
        beneficiaryForm.emailAddress =
            textInputEditTextBeneficiaryEmailAddress.getEditTextNullable()
        beneficiaryForm.accountNumber =
            textInputEditTextAccountNumber.text.toString().replace(" ", "")
        beneficiaryForm.swiftCode = if (swiftBank != null) swiftBank?.swiftBicCode else null
        when (channel.id) {
            ChannelBankEnum.PESONET.getChannelId() -> {
                beneficiaryForm.brstnCode = bank?.pesonetBrstn
            }
            ChannelBankEnum.PDDTS.getChannelId() -> {
                beneficiaryForm.firmCode = bank?.pddtsFinacleCode
            }
            ChannelBankEnum.INSTAPAY.getChannelId() -> {
                beneficiaryForm.instapayCode = bank?.instapayCode
            }
        }
        beneficiaryForm.mobileNumber = editTextMobileNumber.getEditTextNullable()
        beneficiaryForm.countryCodeId = countryCode.id
        beneficiaryForm.address =
            if (textInputEditTextBeneficiaryAddress != null)
                textInputEditTextBeneficiaryAddress?.text.toString()
            else null
        beneficiaryForm.channelId = channel.id
        beneficiaryForm.bankName =
            if (channel.id != ChannelBankEnum.UBP_TO_UBP.getChannelId())
                if (swiftBank != null)
                    swiftBank?.bankName
                else
                    bank?.bank
            else
                getString(R.string.value_receiving_bank_ubp)
        beneficiaryForm.allAccountsSelected =
            viewModel.sourceAccountForm.value?.allAccountsSelected.notNullable()
        if (viewModel.sourceAccountForm.value?.allAccountsSelected.notNullable()) {
            beneficiaryForm.excludedAccountIds =
                viewModel.sourceAccountForm.value?.excludedAccountIds.notNullable()
        } else {
            beneficiaryForm.accountIds =
                viewModel.sourceAccountForm.value?.selectedAccounts
                    ?.map { it.id.toString() }
                    ?.toMutableList()
        }
        if (isCreationForm()) {
            viewModel.createBeneficiary(beneficiaryForm)
        } else {
            viewModel.updateBeneficiary(beneficiaryForm)
        }
    }

    private fun beneficiarySubmission(creationBeneficiaryDto: CreationBeneficiaryDto) {
        if (!isCreationForm()) {
            eventBus.actionSyncEvent.emmit(
                BaseEvent(
                    ActionSyncEvent.ACTION_UPDATE_BENEFICIARY_DETAIL,
                    JsonHelper.toJson(creationBeneficiaryDto)
                )
            )
        }
        val bundle = Bundle()
        bundle.putString(
            ResultLandingPageActivity.EXTRA_PAGE,
            if (isCreationForm()) {
                ResultLandingPageActivity.PAGE_BENEFICIARY_CREATE
            } else {
                ResultLandingPageActivity.PAGE_BENEFICIARY_UPDATE
            }
        )
        bundle.putString(
            ResultLandingPageActivity.EXTRA_TITLE,
            if (isCreationForm()) {
                getString(R.string.title_beneficiary_created)
            } else {
                getString(R.string.title_beneficiary_updated)
            }
        )
        bundle.putString(
            ResultLandingPageActivity.EXTRA_DESC,
            if (isCreationForm()) {
                getString(R.string.desc_beneficiary_created)
            } else {
                formatString(R.string.params_desc_beneficiary_edited, creationBeneficiaryDto.name)
            }
        )
        bundle.putString(
            ResultLandingPageActivity.EXTRA_BUTTON,
            getString(R.string.action_close)
        )
        navigator.navigate(
            this,
            ResultLandingPageActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateCountryScreen() {
        val bundle = Bundle()
        bundle.putSerializable(
            CountryActivity.EXTRA_PAGE,
            CountryActivity.CountryScreen.BENEFICIARY_SCREEN
        )
        navigator.navigate(
            this,
            CountryActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun initChannelView(channel: Channel) {
        textViewChannel.visibility = View.GONE
        imageViewChannel.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        imageViewChannel.setImageResource(
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
        textViewServiceFeeTitle.visibility(false)
        textViewServiceFee.visibility(false)
    }

    private fun showSaveChangesBottomSheet() {
        saveChangesConfirmationBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_save_changes),
            formatString(
                R.string.params_desc_save_changes,
                viewModel.beneficiaryDetailDto.value?.name
            ),
            formatString(R.string.action_confirm),
            formatString(R.string.action_no)
        )
        saveChangesConfirmationBottomSheet?.setOnConfirmationPageCallBack(this)
        saveChangesConfirmationBottomSheet?.show(
            supportFragmentManager,
            TAG_SAVE_CHANGES_DIALOG
        )
    }

    private fun showLeavePageBottomSheet() {
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
                this@ManageBeneficiaryFormActivity.onBackPressed(true)
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

    private fun initBeneficiaryFormDetail(channel: Channel) {
        this.channel = channel
        generalViewModel.getOrgName()
        initChannelView(channel)
        initFields(channel)
    }

    private fun startViewTutorial() {
        val radius = resources.getDimension(R.dimen.field_radius)
        tutorialEngineUtil.startTutorial(
            this,
            viewTutorialAllowedSourceAccounts,
            R.layout.frame_tutorial_upper_left,
            radius,
            false,
            getString(R.string.msg_tutorial_beneficiary_form_allowed_source_accounts),
            GravityEnum.BOTTOM,
            OverlayAnimationEnum.ANIM_EXPLODE
        )
    }

    private fun clearFormFocus() {
        constraintLayoutParent.requestFocus()
        constraintLayoutParent.isFocusableInTouchMode = true
    }

    private fun isCreationForm() = intent.getStringExtra(EXTRA_TYPE) == TYPE_CREATE

    companion object {
        const val EXTRA_BENEFICIARY = "beneficiary"
        const val EXTRA_CHANNEL = "channel"
        const val EXTRA_TYPE = "type"
        const val TYPE_CREATE = "create"
        const val TYPE_UPDATE = "update"
        const val TAG_SAVE_CHANGES_DIALOG = "save_changes_dialog"

        const val DRAWABLE_RIGHT = 2
    }
}
