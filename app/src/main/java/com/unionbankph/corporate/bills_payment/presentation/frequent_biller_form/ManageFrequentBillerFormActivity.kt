package com.unionbankph.corporate.bills_payment.presentation.frequent_biller_form

import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.bills_payment.data.form.FrequentBillerForm
import com.unionbankph.corporate.bills_payment.data.model.*
import com.unionbankph.corporate.bills_payment.presentation.biller.BillerMainActivity
import com.unionbankph.corporate.bills_payment.presentation.biller.biller_all.AllBillerFragment
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.general.presentation.result.ResultLandingPageActivity
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_manage_frequent_biller_form.*
import kotlinx.android.synthetic.main.widget_channel_header.*
import kotlinx.android.synthetic.main.widget_edit_text_allowed_source_account.*
import kotlinx.android.synthetic.main.widget_edit_text_biller.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*
import java.util.*

class ManageFrequentBillerFormActivity :
    BaseActivity<ManageFrequentBillerFormViewModel>(R.layout.activity_manage_frequent_biller_form),
    View.OnClickListener,
    OnTutorialListener,
    OnConfirmationPageCallBack {

    private lateinit var buttonAction: Button

    private lateinit var imeOptionEditText: ImeOptionEditText

    private var saveChangesConfirmationBottomSheet: ConfirmationBottomSheet? = null

    private var disposable: Disposable? = null

    private var biller: Biller? = null

    private var billerFields: MutableList<BillerField>? = null

    private var rxValidationResultList = mutableListOf<ReferenceTextInputEditText>()

    private var isActivatedValidationForm: Boolean = false

    private var isLoadInitial: Boolean = true

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initEditFrequentBiller()
        setupBindings()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initTutorialViewModel()
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

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[ManageFrequentBillerFormViewModel::class.java]
        viewModel.state.observe(this, Observer {

            when (it) {
                is ShowManageFrequentBillerFormLoading -> {
                    showProgressAlertDialog(ManageFrequentBillerFormActivity::class.java.simpleName)
                }
                is ShowManageFrequentBillerFormDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowManageFrequentBillerFormSubmit -> {
                    frequentBillerSubmissionSuccess(it.creationFrequentBillerDto)
                }
                is ShowManageFrequentBillerGetBillerFields -> {
                    initBillerFields(it.billerFields)
                }
                is ShowManageFrequentBillerFormGetOrganizationName -> {
                    setToolbarTitle(
                        textViewTitle,
                        textViewCorporationName,
                        String.format(
                            if (isCreationForm())
                                getString(R.string.title_create_frequent_biller)
                            else
                                getString(R.string.title_edit_frequent_biller)
                        ),
                        it.orgName
                    )
                }
                is ShowManageFrequentBillerFormError -> {
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
                onBackPressed()
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
                navigateSourceAccountScreen()
            }
            R.id.textInputEditTextSelectBiller -> {
                navigateBillersScreen()
            }
        }
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        tutorialEngineUtil.setOnTutorialListener(this)
        textInputEditTextAllowedSourceAccounts.setOnClickListener(this)
        textInputEditTextSelectBiller.setOnClickListener(this)
        linearLayoutAllowedSourceAccounts.setOnClickListener(this)
        imeOptionEditText =
            ImeOptionEditText()
        imeOptionEditText.addEditText(textInputEditTextBillerAlias)
        imeOptionEditText.startListener()
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
                    viewUtil.setFocusOnView(scrollView, viewTutorialBillerAlias)
                    tutorialEngineUtil.startTutorial(
                        this,
                        viewTutorialBillerAlias,
                        R.layout.frame_tutorial_upper_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_frequent_biller_alias),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                viewTutorialBillerAlias -> {
                    viewUtil.setFocusOnView(scrollView, viewTutorialSelectBiller)
                    tutorialEngineUtil.startTutorial(
                        this,
                        viewTutorialSelectBiller,
                        R.layout.frame_tutorial_upper_left,
                        radius,
                        false,
                        getString(R.string.msg_tutorial_frequent_biller_select_biller),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
                viewTutorialSelectBiller -> {
                    if (billerFields != null) {
                        viewUtil.setFocusOnView(scrollView, viewTutorialBillerFields)
                        tutorialEngineUtil.startTutorial(
                            this,
                            viewTutorialBillerFields,
                            R.layout.frame_tutorial_lower_left,
                            radius,
                            false,
                            getString(R.string.msg_tutorial_frequent_biller_fields),
                            GravityEnum.TOP,
                            OverlayAnimationEnum.ANIM_EXPLODE
                        )
                    } else {
                        startSaveTutorial()
                    }
                }
                viewTutorialBillerFields -> {
                    startSaveTutorial()
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

    private fun startSaveTutorial() {
        val buttonRadius = resources.getDimension(R.dimen.button_radius)
        tutorialEngineUtil.startTutorial(
            this,
            buttonAction,
            R.layout.frame_tutorial_upper_right,
            buttonRadius,
            false,
            getString(R.string.msg_tutorial_frequent_biller_button),
            GravityEnum.BOTTOM,
            OverlayAnimationEnum.ANIM_EXPLODE
        )
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            if (it.eventType == InputSyncEvent.ACTION_INPUT_BILLER) {
                showBillerFields(it)
            } else if (it.eventType == InputSyncEvent.ACTION_INPUT_SOURCE_ACCOUNTS) {
                viewModel.onSetSourceAccounts(it.payload)
            }
        }.addTo(disposables)
    }

    private fun showBillerFields(it: BaseEvent<String>) {
        this.biller = JsonHelper.fromJson(it.payload)
        rxValidationResultList.clear()
        linearLayoutSelectBillerDetails.removeAllViews()
        textInputEditTextSelectBiller.setText(biller?.name)
        viewModel.getBillerFields(biller?.serviceId!!)
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
                formatString(R.string.title_frequent_biller)
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

    private fun navigateSourceAccountScreen() {
        val bundle = Bundle()
        bundle.putParcelable(
            SourceAccountActivity.EXTRA_SOURCE_ACCOUNT_FORM,
            viewModel.sourceAccountForm.value
        )
        bundle.putString(
            SourceAccountActivity.EXTRA_PAGE,
            SourceAccountActivity.PAGE_FREQUENT_BILLER_FORM
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

    private fun clickSave() {
        viewUtil.dismissKeyboard(this)
        if (isValidForm) {
            if (isCreationForm()) {
                submitForm()
            } else {
                showSaveChangesBottomSheet()
            }
        } else {
            if (!isActivatedValidationForm) {
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

    private fun initBillerFields(billerFields: MutableList<BillerField>) {
        this.billerFields = billerFields
        isValidForm = false
        linearLayoutSelectBillerDetails.removeAllViews()
        textInputEditTextBillerAlias.imeOptions = EditorInfo.IME_ACTION_NEXT
        imeOptionEditText.removeListener()
        imeOptionEditText.addEditText(textInputEditTextBillerAlias)
        billerFields
            .sortedWith(compareBy { it.index })
            .forEach {
                initEditText(it)
            }
        if (isActivatedValidationForm || !isCreationForm()) {
            validateForm(true)
        }
        imeOptionEditText.startListener()
    }

    private fun initEditText(
        billerField: BillerField
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

        linearLayoutSelectBillerDetails?.addView(view)
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
        textInputEditTextBillerField.tag = billerField.name
        textInputLayoutBillerField.setContextCompatBackground(
            if (billerField.parentFieldIndex != null)
                if (isCreationForm()) {
                    R.color.colorDisableTextInputEditText
                } else {
                    R.color.colorTransparent
                }
            else R.color.colorTransparent
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
                    textInputEditTextBillerField.setOnClickListener { onClick ->
                        MaterialDialog(this@ManageFrequentBillerFormActivity).show {
                            lifecycleOwner(this@ManageFrequentBillerFormActivity)
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
                        DateFormatEnum.DATE_FORMAT_DATE_SLASH.value
                    )
                )
            }
        )
    }

    private fun setupFormType(billerField: BillerField, isEnabled: Boolean, option: Option) {
        rxValidationResultList
            .filter { it.billerField?.parentFieldIndex.toString() == billerField.index }
            .forEach { rxValidation ->
                rxValidation.textInputEditText?.text?.clear()
                viewUtil.getTextInputLayout(rxValidation.textInputEditText)
                    ?.setContextCompatBackground(
                        when {
                            isEnabled -> R.color.colorTransparent
                            isCreationForm() -> R.color.colorDisableTextInputEditText
                            else -> R.color.colorTransparent
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
                            lifecycleOwner(this@ManageFrequentBillerFormActivity)
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

    private fun validateForm(isValueChanged: Boolean) {
        if (disposable != null) disposables.remove(disposable!!)
        val rxValidationResults = mutableListOf<ReferenceTextInputEditText>()
        val allowedSourceAccountsObservable = viewUtil.rxTextChanges(
            true,
            isValueChanged,
            resources.getInteger(R.integer.min_length_field),
            resources.getInteger(R.integer.max_length_field),
            textInputEditTextAllowedSourceAccounts
        )
        val billerAliasObservable = viewUtil.rxTextChanges(
            true,
            isValueChanged,
            resources.getInteger(R.integer.min_length_field),
            resources.getInteger(R.integer.max_length_field),
            textInputEditTextBillerAlias
        )
        val selectBillerObservable = viewUtil.rxTextChanges(
            true,
            isValueChanged,
            resources.getInteger(R.integer.min_length_field),
            resources.getInteger(R.integer.max_length_field),
            textInputEditTextSelectBiller
        )
        initSetError(allowedSourceAccountsObservable)
        initSetError(billerAliasObservable)
        initSetError(selectBillerObservable)
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
                        ReferenceTextInputEditText(
                            observableTextInputEditText,
                            it.textInputEditText,
                            it.billerField
                        )
                    )
                }
            }

        rxValidationResults.add(
            ReferenceTextInputEditText(
                allowedSourceAccountsObservable,
                textInputEditTextAllowedSourceAccounts,
                null
            )
        )
        rxValidationResults.add(
            ReferenceTextInputEditText(
                billerAliasObservable,
                textInputEditTextBillerAlias,
                null
            )
        )
        rxValidationResults.add(
            ReferenceTextInputEditText(
                selectBillerObservable,
                textInputEditTextSelectBiller,
                null
            )
        )
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
                    clickSave()
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
        disposables.add(disposable!!)
        initEditTextDefaultValue(rxValidationResults)
    }

    private fun initEditTextDefaultValue(
        rxValidationResults: MutableList<ReferenceTextInputEditText>
    ) {
        if (isCreationForm()) {
            resetDefaultValues(rxValidationResults)
        } else {
            if (isLoadInitial) {
                isLoadInitial = false
                resetDefaultValues(rxValidationResults)
                viewModel.frequentBiller.value?.fields?.forEach { frequentBiller ->
                    val isFoundSameEditText =
                        rxValidationResultList.find { it.billerField?.name == frequentBiller.name }
                    isFoundSameEditText?.textInputEditText?.setText(frequentBiller.value)
                    if (isFoundSameEditText != null) {
                        val currentOption =
                            isFoundSameEditText.billerField!!.options
                                ?.find { it.value == frequentBiller.value }
                        if (currentOption != null &&
                            isFoundSameEditText.billerField?.type == Constant.FormType.FORM_TYPE_SELECT
                        ) {
                            setupFormType(
                                isFoundSameEditText.billerField!!,
                                true,
                                currentOption
                            )
                        }
                    }
                }
            } else {
                resetDefaultValues(rxValidationResults)
            }
        }
    }

    private fun resetDefaultValues(rxValidationResults: MutableList<ReferenceTextInputEditText>) {
        if (isActivatedValidationForm) {
            rxValidationResults.forEach {
                if (it.textInputEditText != null) {
                    it.textInputEditText?.setText(it.textInputEditText?.text.toString())
                }
            }
        }
    }

    private fun submitForm() {
        clearFormFocus()
        val frequentBillerForm =
            FrequentBillerForm()
        frequentBillerForm.accountNumber = biller?.accountNumber
        frequentBillerForm.billerName = biller?.name
        frequentBillerForm.code = biller?.code
        frequentBillerForm.serviceId = biller?.serviceId
        frequentBillerForm.shortName = biller?.shortName
        frequentBillerForm.name = textInputEditTextBillerAlias.text.toString().trim()
        frequentBillerForm.fields = getBillerFields()
        frequentBillerForm.allAccountsSelected =
            viewModel.sourceAccountForm.value?.allAccountsSelected.notNullable()
        if (viewModel.sourceAccountForm.value?.allAccountsSelected.notNullable()) {
            frequentBillerForm.excludedAccountIds =
                viewModel.sourceAccountForm.value?.excludedAccountIds.notNullable()
        } else {
            frequentBillerForm.sourceAccountIds =
                viewModel.sourceAccountForm.value?.selectedAccounts
                    ?.map { it.id.notNullable() }
                    ?.toMutableList()
        }
        if (isCreationForm()) {
            viewModel.createFrequentBiller(frequentBillerForm)
        } else {
            viewModel.updateFrequentBiller(frequentBillerForm)
        }
    }

    private fun frequentBillerSubmissionSuccess(
        creationFrequentBillerDto: CreationFrequentBillerDto
    ) {
        eventBus.actionSyncEvent.emmit(BaseEvent(ActionSyncEvent.ACTION_UPDATE_FREQUENT_BILLER_LIST))
        if (!isCreationForm()) {
            eventBus.actionSyncEvent.emmit(
                BaseEvent(
                    ActionSyncEvent.ACTION_UPDATE_FREQUENT_BILLER_DETAIL,
                    JsonHelper.toJson(creationFrequentBillerDto)
                )
            )
        }
        val bundle = Bundle()
        bundle.putString(
            ResultLandingPageActivity.EXTRA_PAGE,
            if (isCreationForm()) {
                ResultLandingPageActivity.PAGE_FREQUENT_BILLER_CREATE
            } else {
                ResultLandingPageActivity.PAGE_FREQUENT_BILLER_UPDATE
            }
        )
        bundle.putString(
            ResultLandingPageActivity.EXTRA_TITLE,
            if (isCreationForm()) {
                getString(R.string.title_frequent_biller_added)
            } else {
                getString(R.string.title_frequent_biller_edited)
            }

        )
        bundle.putString(
            ResultLandingPageActivity.EXTRA_DESC,
            if (isCreationForm()) {
                getString(R.string.desc_frequent_biller_added)
            } else {
                String.format(
                    getString(R.string.params_desc_frequent_biller_edited),
                    creationFrequentBillerDto.name
                )
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
            true,
            true,
            Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun initEditFrequentBiller() {
        textViewServiceFeeTitle.visibility(false)
        textViewServiceFee.visibility(false)
        viewModel.getOrgName()
        val frequentBiller = intent.getParcelableExtra<FrequentBiller>(EXTRA_FREQUENT_BILLER)
        if (frequentBiller != null) {
            viewModel.frequentBiller.onNext(frequentBiller)
        } else {
            viewModel.getSourceAccounts(
                ChannelBankEnum.BILLS_PAYMENT.getChannelId().toString(),
                "62"
            )
        }
    }

    private fun setupBindings() {
        viewModel.frequentBiller.subscribe { frequentBiller ->
            this.biller = Biller(
                frequentBiller.accountNumber,
                frequentBiller.code,
                frequentBiller.billerName,
                frequentBiller.serviceId,
                frequentBiller.shortName
            )
            if (frequentBiller.accounts != null) {
                viewModel.onSetSourceAccountsFromEdit(frequentBiller.accounts.notNullable())
            }
            textInputEditTextBillerAlias.setText(frequentBiller.name)
            textInputEditTextSelectBiller.setText(frequentBiller.billerName)
            viewModel.getBillerFields(frequentBiller.serviceId.notNullable())
        }.addTo(disposables)
    }

    private fun navigateBillersScreen() {
        val bundle = Bundle()
        bundle.putString(
            BillerMainActivity.EXTRA_PAGE,
            BillerMainActivity.PAGE_FREQUENT_BILLER_FORM
        )
        bundle.putString(
            BillerMainActivity.EXTRA_TYPE,
            AllBillerFragment.TYPE_BILLER
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

    private fun getBillerFields(): MutableList<Fields> {
        val fields = mutableListOf<Fields>()
        rxValidationResultList
            .sortedWith(compareBy { it.billerField?.index })
            .forEach { rxValidation ->
                if (rxValidation.billerField != null) {
                    fields.add(
                        Fields(
                            index = rxValidation.billerField?.index?.toInt(),
                            name = rxValidation.textInputEditText?.tag.toString(),
                            value = rxValidation.textInputEditText?.text.toString()
                        )
                    )
                }
            }
        return fields
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
                this@ManageFrequentBillerFormActivity.onBackPressed(true)
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

    private fun showSaveChangesBottomSheet() {
        saveChangesConfirmationBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_save_changes),
            formatString(
                R.string.params_desc_save_changes,
                viewModel.frequentBiller.value?.name
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

    private fun startViewTutorial() {
        val radius = resources.getDimension(R.dimen.field_radius)
        tutorialEngineUtil.startTutorial(
            this,
            viewTutorialAllowedSourceAccounts,
            R.layout.frame_tutorial_upper_left,
            radius,
            false,
            getString(R.string.msg_tutorial_frequent_biller_allowed_source_accounts),
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
        const val EXTRA_FREQUENT_BILLER = "frequent_biller"
        const val EXTRA_TYPE = "type"
        const val TYPE_CREATE = "create"
        const val TYPE_UPDATE = "update"

        const val TAG_SAVE_CHANGES_DIALOG = "save_changes_dialog"
    }
}
