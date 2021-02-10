package com.unionbankph.corporate.fund_transfer.presentation.beneficiary_detail

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.presentation.source_account.SourceAccountActivity
import com.unionbankph.corporate.account.presentation.source_account.SourceAccountForm
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.approval.presentation.approval_activity_log.ActivityLogActivity
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.fund_transfer.data.model.BeneficiaryDetailDto
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_form.ManageBeneficiaryFormActivity
import com.unionbankph.corporate.general.presentation.result.ResultLandingPageActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_beneficiary_detail.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*

class ManageBeneficiaryDetailActivity :
    BaseActivity<ManageBeneficiaryDetailViewModel>(R.layout.activity_beneficiary_detail),
    OnConfirmationPageCallBack, View.OnClickListener {

    private var deleteConfirmationBottomSheet: ConfirmationBottomSheet? = null

    private lateinit var beneficiaryDetailDto: BeneficiaryDetailDto

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setToolbarTitle(tvToolbar, getString(R.string.title_beneficiary_details))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        buttonViewEditLog.setOnClickListener(this)
        buttonEditDetails.setOnClickListener(this)
        buttonDelete.setOnClickListener(this)
        eventBus.actionSyncEvent.flowable.subscribe {
            if (it.eventType == ActionSyncEvent.ACTION_UPDATE_BENEFICIARY_DETAIL) {
                scrollViewBeneficiaryDetail?.fullScroll(ScrollView.FOCUS_UP)
                val beneficiaryDetailDto =
                    JsonHelper.fromJson<BeneficiaryDetailDto>(it.payload)
                this.beneficiaryDetailDto = beneficiaryDetailDto
                initView()
            }
        }.addTo(disposables)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[ManageBeneficiaryDetailViewModel::class.java]
        viewModel.beneficiaryDetailState.observe(this, Observer {
            when (it) {
                is ShowBeneficiaryDetailLoading -> {
                    constraintLayout.visibility(false)
                    viewLoadingState.visibility(true)
                }
                is ShowBeneficiaryDetailDismissLoading -> {
                    viewLoadingState.visibility(false)
                }
                is ShowBeneficiaryDetailProgressBarLoading -> {
                    showProgressAlertDialog(ManageBeneficiaryDetailActivity::class.java.simpleName)
                }
                is ShowBeneficiaryDetailDismissProgressBarLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowBeneficiaryDetailGetBeneficiaryDetail -> {
                    constraintLayout.visibility(true)
                    beneficiaryDetailDto = it.data
                    initView()
                }
                is ShowBeneficiaryDetailDeleteBeneficiary -> {
                    deleteBeneficiary()
                }
                is ShowBeneficiaryDetailError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.getBeneficiaryDetail(intent.getStringExtra(EXTRA_ID).notNullable())
    }

    private fun initView() {
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        if (beneficiaryDetailDto.swiftBankDetails != null) {
            constraintSet.connect(
                textViewHeaderContactDetails.id,
                ConstraintSet.TOP,
                cardViewSwiftBankDetails.id,
                ConstraintSet.BOTTOM,
                resources.getDimension(R.dimen.content_spacing).toInt()
            )
        } else {
            constraintSet.connect(
                textViewHeaderContactDetails.id,
                ConstraintSet.TOP,
                cardViewReceivingBankDetails.id,
                ConstraintSet.BOTTOM,
                resources.getDimension(R.dimen.content_spacing).toInt()
            )
        }
        constraintSet.applyTo(constraintLayout)

        cardViewReceivingBankDetails.visibility(beneficiaryDetailDto.swiftBankDetails == null)
        cardViewSwiftBankDetails.visibility(beneficiaryDetailDto.swiftBankDetails != null)

        val isVisibleContactDetails =
            (beneficiaryDetailDto.emailAddress != null
                    && beneficiaryDetailDto.emailAddress != "")
                    || (beneficiaryDetailDto.mobileNumber != null
                    && beneficiaryDetailDto.mobileNumber != "")

        textViewHeaderContactDetails.visibility(isVisibleContactDetails)
        cardViewContactDetails.visibility(isVisibleContactDetails)

        textViewBeneficiaryAddressTitle.visibility(
            beneficiaryDetailDto.channelId == ChannelBankEnum.SWIFT.getChannelId()
        )
        textViewBeneficiaryAddress.visibility(
            beneficiaryDetailDto.channelId == ChannelBankEnum.SWIFT.getChannelId()
        )
        viewBorderBeneficiaryAddress.visibility(
            beneficiaryDetailDto.channelId == ChannelBankEnum.SWIFT.getChannelId()
        )
        textViewBeneficiaryAddress.text = beneficiaryDetailDto.address.notEmpty()

        textViewBeneficiaryName.text = beneficiaryDetailDto.name
        textViewBeneficiaryCode.text = beneficiaryDetailDto.code
        textViewBeneficiaryAccountNumber.text =
            viewUtil.getAccountNumberFormat(beneficiaryDetailDto.accountNumber)

        textViewChannel.text =
            ConstantHelper.Text.getChannelByChannelId(beneficiaryDetailDto.channelId)

        textViewReceivingBank.text =
            if (beneficiaryDetailDto.channelId == ChannelBankEnum.UBP_TO_UBP.getChannelId())
                formatString(R.string.value_receiving_bank_ubp)
            else
                beneficiaryDetailDto.bankDetails?.name

        textViewEmailAddress.text = beneficiaryDetailDto.emailAddress.notEmpty()
        textViewMobileNumber.text = beneficiaryDetailDto.mobileNumber.notEmpty()
        imageViewFlag.setImageResource(
            viewUtil.getDrawableById("ic_flag_${beneficiaryDetailDto.countryCode?.code?.toLowerCase()}")
        )
        val isHasEmail =
            beneficiaryDetailDto.emailAddress != null && beneficiaryDetailDto.emailAddress != ""
        textViewEmailAddressTitle.visibility(isHasEmail)
        textViewEmailAddress.visibility(isHasEmail)
        textViewMobileNumberTitle.visibility(beneficiaryDetailDto.mobileNumber != null)
        textViewMobileNumber.visibility(beneficiaryDetailDto.mobileNumber != null)
        imageViewFlag.visibility(beneficiaryDetailDto.mobileNumber != null)
        viewBorderMobileNumber.visibility(beneficiaryDetailDto.mobileNumber != null && isHasEmail)

        beneficiaryDetailDto.swiftBankDetails?.let {
            textViewSwiftCode.text = it.swiftBicCode
            textViewSwiftReceivingBank.text = it.bankName
            textViewSwiftReceivingAddressBank.text = it.let {
                if (it.address1 == null && it.address2 == null) {
                    Constant.EMPTY
                } else {
                    it.address1.notNullable() + it.address2.notNullable()
                }
            }
            textViewSwiftCountry.text = it.country.notEmpty()
        }

        linearLayoutAllowedSourceAccounts.removeAllViews()
        if (beneficiaryDetailDto.accounts?.size ?: 0 > 3) {
            val viewAllowedSourceAccount =
                LayoutInflater.from(context)
                    .inflate(R.layout.item_edittext_allowed_source_account, null)
            val textViewTitle =
                viewAllowedSourceAccount.findViewById<TextView>(R.id.textViewTitle)
            val textView =
                viewAllowedSourceAccount.findViewById<TextView>(R.id.textView)
            val viewBorder =
                viewAllowedSourceAccount.findViewById<View>(R.id.viewBorder)
            viewBorder.visibility = View.GONE
            textViewTitle.visibility = View.GONE
            textView.gravity = Gravity.START
            textView.setTypeface(textView.typeface, Typeface.BOLD)
            textView.text = formatString(
                R.string.params_allowed_source_accounts,
                formatString(
                    R.string.param_color,
                    convertColorResourceToHex(getAccentColor()),
                    beneficiaryDetailDto.accounts?.size.toString()
                ),
                formatString(R.string.title_beneficiary)
            ).toHtmlSpan()
            cardViewAllowedSourceAccounts.setOnClickListener {
                navigateSourceAccountScreen(beneficiaryDetailDto)
            }
            linearLayoutAllowedSourceAccounts.addView(viewAllowedSourceAccount)
        } else {
            beneficiaryDetailDto.accounts?.forEachIndexed { index, account ->
                val viewAllowedSourceAccount = LayoutInflater.from(context).inflate(
                    R.layout.item_edittext_allowed_source_account,
                    null
                )
                val textViewTitle =
                    viewAllowedSourceAccount.findViewById<TextView>(R.id.textViewTitle)
                val textView =
                    viewAllowedSourceAccount.findViewById<TextView>(R.id.textView)
                val viewBorder =
                    viewAllowedSourceAccount.findViewById<View>(R.id.viewBorder)
                textViewTitle.text = account.name
                textView.text = viewUtil.getAccountNumberFormat(account.accountNumber)
                viewBorder.visibility =
                    if (index == beneficiaryDetailDto.accounts?.size?.minus(1))
                        View.GONE
                    else
                        View.VISIBLE
                linearLayoutAllowedSourceAccounts.addView(viewAllowedSourceAccount)
                cardViewAllowedSourceAccounts.setOnClickListener(null)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
    }

    override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
        deleteConfirmationBottomSheet?.dismiss()
        viewModel.deleteBeneficiary(
            beneficiaryDetailDto.id.toString()
        )
    }

    override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
        deleteConfirmationBottomSheet?.dismiss()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonViewEditLog -> {
                navigateActivityLogScreen()
            }
            R.id.buttonEditDetails -> {
                navigateBeneficiaryFormScreen(beneficiaryDetailDto)
            }
            R.id.buttonDelete -> {
                showDeleteScheduledTransferBottomSheet(beneficiaryDetailDto)
            }
        }
    }

    private fun navigateSourceAccountScreen(beneficiaryDetailDto: BeneficiaryDetailDto) {
        val bundle = Bundle()
        val sourceAccountForm = SourceAccountForm()
        sourceAccountForm.selectedAccounts = beneficiaryDetailDto.accounts.notNullable()
        sourceAccountForm.totalSelected = beneficiaryDetailDto.accounts.notNullable().size
        sourceAccountForm.allAccountsSelected = false
        bundle.putParcelable(
            SourceAccountActivity.EXTRA_SOURCE_ACCOUNT_FORM,
            sourceAccountForm
        )
        bundle.putString(
            SourceAccountActivity.EXTRA_PAGE,
            SourceAccountActivity.PAGE_BENEFICIARY_DETAIL
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

    private fun navigateBeneficiaryFormScreen(beneficiaryDetailDto: BeneficiaryDetailDto) {
        val bundle = Bundle()
        bundle.putParcelable(
            ManageBeneficiaryFormActivity.EXTRA_BENEFICIARY,
            beneficiaryDetailDto
        )
        bundle.putString(
            ManageBeneficiaryFormActivity.EXTRA_TYPE,
            ManageBeneficiaryFormActivity.TYPE_UPDATE
        )
        navigator.navigate(
            this@ManageBeneficiaryDetailActivity,
            ManageBeneficiaryFormActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateActivityLogScreen() {
        val bundle = Bundle()
        bundle.putString(
            ActivityLogActivity.EXTRA_PAGE,
            ActivityLogActivity.PAGE_BENEFICIARY_DETAIL
        )
        bundle.putString(
            ActivityLogActivity.EXTRA_ID,
            beneficiaryDetailDto.id.toString()
        )
        navigator.navigate(
            this@ManageBeneficiaryDetailActivity,
            ActivityLogActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    private fun deleteBeneficiary() {
        eventBus.actionSyncEvent.emmit(BaseEvent(ActionSyncEvent.ACTION_DELETE_BENEFICIARY))
        val bundle = Bundle()
        bundle.putString(
            ResultLandingPageActivity.EXTRA_PAGE,
            ResultLandingPageActivity.PAGE_BENEFICIARY_DETAIL
        )
        bundle.putString(
            ResultLandingPageActivity.EXTRA_TITLE,
            getString(R.string.title_beneficiary_deleted)
        )
        bundle.putString(
            ResultLandingPageActivity.EXTRA_DESC,
            formatString(
                R.string.params_desc_beneficiary_deleted,
                beneficiaryDetailDto.name
            )
        )
        bundle.putString(
            ResultLandingPageActivity.EXTRA_BUTTON,
            getString(R.string.action_close)
        )
        navigator.navigate(
            this,
            ResultLandingPageActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun showDeleteScheduledTransferBottomSheet(beneficiaryDetailDto: BeneficiaryDetailDto) {
        deleteConfirmationBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_delete_beneficiary),
            formatString(
                R.string.params_delete_beneficiary,
                beneficiaryDetailDto.name
            ),
            formatString(R.string.action_yes),
            formatString(R.string.action_no)
        )
        deleteConfirmationBottomSheet?.setOnConfirmationPageCallBack(this)
        deleteConfirmationBottomSheet?.show(
            supportFragmentManager,
            TAG_DELETE_BENEFICIARY_DIALOG
        )
    }

    companion object {
        const val EXTRA_ID = "id"
        const val TAG_DELETE_BENEFICIARY_DIALOG = "delete_beneficiary_dialog"
    }
}
