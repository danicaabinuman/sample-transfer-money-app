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
import com.unionbankph.corporate.databinding.ActivityBeneficiaryDetailBinding
import com.unionbankph.corporate.fund_transfer.data.model.BeneficiaryDetailDto
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_form.ManageBeneficiaryFormActivity
import com.unionbankph.corporate.general.presentation.result.ResultLandingPageActivity
import io.reactivex.rxkotlin.addTo

class ManageBeneficiaryDetailActivity :
    BaseActivity<ActivityBeneficiaryDetailBinding, ManageBeneficiaryDetailViewModel>(),
    OnConfirmationPageCallBack, View.OnClickListener {

    private var deleteConfirmationBottomSheet: ConfirmationBottomSheet? = null

    private lateinit var beneficiaryDetailDto: BeneficiaryDetailDto

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setToolbarTitle(binding.viewToolbar.tvToolbar, getString(R.string.title_beneficiary_details))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.buttonViewEditLog.setOnClickListener(this)
        binding.buttonEditDetails.setOnClickListener(this)
        binding.buttonDelete.setOnClickListener(this)
        eventBus.actionSyncEvent.flowable.subscribe {
            if (it.eventType == ActionSyncEvent.ACTION_UPDATE_BENEFICIARY_DETAIL) {
                binding.scrollViewBeneficiaryDetail.fullScroll(ScrollView.FOCUS_UP)
                val beneficiaryDetailDto =
                    JsonHelper.fromJson<BeneficiaryDetailDto>(it.payload)
                this.beneficiaryDetailDto = beneficiaryDetailDto
                initView()
            }
        }.addTo(disposables)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.beneficiaryDetailState.observe(this, Observer {
            when (it) {
                is ShowBeneficiaryDetailLoading -> {
                    binding.constraintLayout.visibility(false)
                    binding.viewLoadingState.root.visibility(true)
                }
                is ShowBeneficiaryDetailDismissLoading -> {
                    binding.viewLoadingState.root.visibility(false)
                }
                is ShowBeneficiaryDetailProgressBarLoading -> {
                    showProgressAlertDialog(ManageBeneficiaryDetailActivity::class.java.simpleName)
                }
                is ShowBeneficiaryDetailDismissProgressBarLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowBeneficiaryDetailGetBeneficiaryDetail -> {
                    binding.constraintLayout.visibility(true)
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
        constraintSet.clone(binding.constraintLayout)
        if (beneficiaryDetailDto.swiftBankDetails != null) {
            constraintSet.connect(
                binding.textViewHeaderContactDetails.id,
                ConstraintSet.TOP,
                binding.cardViewSwiftBankDetails.id,
                ConstraintSet.BOTTOM,
                resources.getDimension(R.dimen.content_spacing).toInt()
            )
        } else {
            constraintSet.connect(
                binding.textViewHeaderContactDetails.id,
                ConstraintSet.TOP,
                binding.cardViewReceivingBankDetails.id,
                ConstraintSet.BOTTOM,
                resources.getDimension(R.dimen.content_spacing).toInt()
            )
        }
        constraintSet.applyTo(binding.constraintLayout)

        binding.cardViewReceivingBankDetails.visibility(beneficiaryDetailDto.swiftBankDetails == null)
        binding.cardViewSwiftBankDetails.visibility(beneficiaryDetailDto.swiftBankDetails != null)

        val isVisibleContactDetails =
            (beneficiaryDetailDto.emailAddress != null
                    && beneficiaryDetailDto.emailAddress != "")
                    || (beneficiaryDetailDto.mobileNumber != null
                    && beneficiaryDetailDto.mobileNumber != "")

        binding.textViewHeaderContactDetails.visibility(isVisibleContactDetails)
        binding.cardViewContactDetails.visibility(isVisibleContactDetails)

        binding.textViewBeneficiaryAddressTitle.visibility(
            beneficiaryDetailDto.channelId == ChannelBankEnum.SWIFT.getChannelId()
        )
        binding.textViewBeneficiaryAddress.visibility(
            beneficiaryDetailDto.channelId == ChannelBankEnum.SWIFT.getChannelId()
        )
        binding.viewBorderBeneficiaryAddress.visibility(
            beneficiaryDetailDto.channelId == ChannelBankEnum.SWIFT.getChannelId()
        )
        binding.textViewBeneficiaryAddress.text = beneficiaryDetailDto.address.notEmpty()

        binding.textViewBeneficiaryName.text = beneficiaryDetailDto.name
        binding.textViewBeneficiaryCode.text = beneficiaryDetailDto.code
        binding.textViewBeneficiaryAccountNumber.text =
            viewUtil.getAccountNumberFormat(beneficiaryDetailDto.accountNumber)

        binding.textViewChannel.text =
            ConstantHelper.Text.getChannelByChannelId(beneficiaryDetailDto.channelId)

        binding.textViewReceivingBank.text =
            if (beneficiaryDetailDto.channelId == ChannelBankEnum.UBP_TO_UBP.getChannelId())
                formatString(R.string.value_receiving_bank_ubp)
            else
                beneficiaryDetailDto.bankDetails?.name

        binding.textViewEmailAddress.text = beneficiaryDetailDto.emailAddress.notEmpty()
        binding.textViewMobileNumber.text = beneficiaryDetailDto.mobileNumber.notEmpty()
        binding.imageViewFlag.setImageResource(
            viewUtil.getDrawableById("ic_flag_${beneficiaryDetailDto.countryCode?.code?.toLowerCase()}")
        )
        val isHasEmail =
            beneficiaryDetailDto.emailAddress != null && beneficiaryDetailDto.emailAddress != ""
        binding.textViewEmailAddressTitle.visibility(isHasEmail)
        binding.textViewEmailAddress.visibility(isHasEmail)
        binding.textViewMobileNumberTitle.visibility(beneficiaryDetailDto.mobileNumber != null)
        binding.textViewMobileNumber.visibility(beneficiaryDetailDto.mobileNumber != null)
        binding.imageViewFlag.visibility(beneficiaryDetailDto.mobileNumber != null)
        binding.viewBorderMobileNumber.visibility(beneficiaryDetailDto.mobileNumber != null && isHasEmail)

        beneficiaryDetailDto.swiftBankDetails?.let {
            binding.textViewSwiftCode.text = it.swiftBicCode
            binding.textViewSwiftReceivingBank.text = it.bankName
            binding.textViewSwiftReceivingAddressBank.text = it.let {
                if (it.address1 == null && it.address2 == null) {
                    Constant.EMPTY
                } else {
                    it.address1.notNullable() + it.address2.notNullable()
                }
            }
            binding.textViewSwiftCountry.text = it.country.notEmpty()
        }

        binding.linearLayoutAllowedSourceAccounts.removeAllViews()
        if (beneficiaryDetailDto.accounts?.size ?: 0 > 3) {
            val viewAllowedSourceAccount =
                LayoutInflater.from(context)
                    .inflate(R.layout.item_edittext_allowed_source_account, null)
            val textViewTitle =
                viewAllowedSourceAccount.findViewById<TextView>(R.id.textViewTitle)
            val textView =
                viewAllowedSourceAccount.findViewById<TextView>(R.id.tvReferenceNo)
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
            binding.cardViewAllowedSourceAccounts.setOnClickListener {
                navigateSourceAccountScreen(beneficiaryDetailDto)
            }
            binding.linearLayoutAllowedSourceAccounts.addView(viewAllowedSourceAccount)
        } else {
            beneficiaryDetailDto.accounts?.forEachIndexed { index, account ->
                val viewAllowedSourceAccount = LayoutInflater.from(context).inflate(
                    R.layout.item_edittext_allowed_source_account,
                    null
                )
                val textViewTitle =
                    viewAllowedSourceAccount.findViewById<TextView>(R.id.textViewTitle)
                val textView =
                    viewAllowedSourceAccount.findViewById<TextView>(R.id.tvReferenceNo)
                val viewBorder =
                    viewAllowedSourceAccount.findViewById<View>(R.id.viewBorder)
                textViewTitle.text = account.name
                textView.text = viewUtil.getAccountNumberFormat(account.accountNumber)
                viewBorder.visibility =
                    if (index == beneficiaryDetailDto.accounts?.size?.minus(1))
                        View.GONE
                    else
                        View.VISIBLE
                binding.linearLayoutAllowedSourceAccounts.addView(viewAllowedSourceAccount)
                binding.cardViewAllowedSourceAccounts.setOnClickListener(null)
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

    override val viewModelClassType: Class<ManageBeneficiaryDetailViewModel>
        get() = ManageBeneficiaryDetailViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityBeneficiaryDetailBinding
        get() = ActivityBeneficiaryDetailBinding::inflate
}
