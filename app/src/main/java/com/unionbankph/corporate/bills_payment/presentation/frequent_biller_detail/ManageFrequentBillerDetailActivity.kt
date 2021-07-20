package com.unionbankph.corporate.bills_payment.presentation.frequent_biller_detail

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
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
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.approval.presentation.approval_activity_log.ActivityLogActivity
import com.unionbankph.corporate.bills_payment.data.model.FrequentBiller
import com.unionbankph.corporate.bills_payment.presentation.frequent_biller_form.ManageFrequentBillerFormActivity
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ActivityFrequentBillerDetailBinding
import com.unionbankph.corporate.general.presentation.result.ResultLandingPageActivity
import io.reactivex.rxkotlin.addTo

class ManageFrequentBillerDetailActivity :
    BaseActivity<ActivityFrequentBillerDetailBinding, ManageFrequentBillerDetailViewModel>(),
    OnConfirmationPageCallBack, View.OnClickListener {

    private var deleteBottomSheet: ConfirmationBottomSheet? = null

    private lateinit var frequentBiller: FrequentBiller

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setToolbarTitle(binding.viewToolbar.tvToolbar, getString(R.string.title_frequent_biller_details))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.buttonViewEditLog.setOnClickListener(this)
        binding.buttonEditDetails.setOnClickListener(this)
        binding.buttonDelete.setOnClickListener(this)
        eventBus.actionSyncEvent.flowable.subscribe {
            if (it.eventType == ActionSyncEvent.ACTION_UPDATE_FREQUENT_BILLER_DETAIL) {
                binding.scrollViewFrequentBillerDetail.fullScroll(ScrollView.FOCUS_UP)
                val frequentBiller = JsonHelper.fromJson<FrequentBiller>(it.payload)
                this.frequentBiller = frequentBiller
                initViews()
            }
        }.addTo(disposables)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowFrequentBillerDetailLoading -> {
                    binding.constraintLayout.visibility(false)
                    binding.viewLoadingState.root.visibility(true)
                }
                is ShowFrequentBillerDetailDismissLoading -> {
                    binding.viewLoadingState.root.visibility(false)
                }
                is ShowFrequentBillerDetailProgressBarLoading -> {
                    showProgressAlertDialog(ManageFrequentBillerDetailActivity::class.java.simpleName)
                }
                is ShowFrequentBillerDetailDismissProgressBarLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowBeneficiaryDetailGetFrequentBillerDetail -> {
                    binding.constraintLayout.visibility(true)
                    frequentBiller = it.data
                    initViews()
                }
                is ShowFrequentBillerDetailDeleteFrequentBiller -> {
                    deleteBeneficiary()
                }
                is ShowFrequentBillerDetailError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.getBeneficiaryDetail(intent.getStringExtra(EXTRA_ID).notNullable())
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
        deleteBottomSheet?.dismiss()
        viewModel.deleteFrequentBiller(
            frequentBiller.id.toString()
        )
    }

    override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
        deleteBottomSheet?.dismiss()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonViewEditLog -> {
                navigateActivityLogScreen()
            }
            R.id.buttonEditDetails -> {
                navigateBeneficiaryFormScreen(frequentBiller)
            }
            R.id.buttonDelete -> {
                showDeleteScheduledTransferBottomSheet(frequentBiller)
            }
        }
    }

    private fun initViews() {
        binding.linearLayoutAllowedSourceAccounts.removeAllViews()
        if (frequentBiller.accounts?.size.notNullable() > 3) {
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
                    frequentBiller.accounts?.size.toString()
                ),
                formatString(R.string.title_frequent_biller)
            ).toHtmlSpan()
            textView.setOnClickListener {
                navigateSourceAccountScreen(frequentBiller)
            }
            binding.linearLayoutAllowedSourceAccounts.addView(viewAllowedSourceAccount)
        } else {
            frequentBiller.accounts?.forEachIndexed { index, account ->
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
                    if (index == frequentBiller.accounts?.size?.minus(1))
                        View.GONE
                    else
                        View.VISIBLE
                binding.linearLayoutAllowedSourceAccounts.addView(viewAllowedSourceAccount)
            }
        }

        binding.linearLayoutBillerFields.removeAllViews()
        frequentBiller.fields.sortedWith(compareBy { it.index }).forEach {
            val viewFields =
                LayoutInflater.from(context).inflate(R.layout.item_textview_biller, null)
            val textViewTitle = viewFields.findViewById<TextView>(R.id.textViewTitle)
            val textView = viewFields.findViewById<TextView>(R.id.tvReferenceNo)
            textViewTitle.text = it.name
            textView.text = it.value
            if (viewUtil.isValidDateFormat(ViewUtil.DATE_FORMAT_DATE_SLASH, it.value) &&
                it.name.equals("Return Period", true)) {
                textView.text = viewUtil.getDateFormatByDateString(
                    it.value,
                    ViewUtil.DATE_FORMAT_DATE_SLASH,
                    ViewUtil.DATE_FORMAT_DATE
                )
            } else {
                textView.text = it.value
            }
            binding.linearLayoutBillerFields.addView(viewFields)
        }

        binding.textViewCreatedDate.text = viewUtil.getDateFormatByDateString(
            frequentBiller.createdDate,
            ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
            ViewUtil.DATE_FORMAT_DEFAULT
        )
        binding.textViewBillerAlias.text = frequentBiller.name
        binding.textViewCreatedBy.text = frequentBiller.createdBy
        binding.textViewBiller.text = frequentBiller.billerName
    }

    private fun navigateSourceAccountScreen(frequentBiller: FrequentBiller) {
        val bundle = Bundle()
        val sourceAccountForm = SourceAccountForm()
        sourceAccountForm.selectedAccounts = frequentBiller.accounts.notNullable()
        sourceAccountForm.totalSelected = frequentBiller.accounts.notNullable().size
        sourceAccountForm.allAccountsSelected = false
        bundle.putParcelable(
            SourceAccountActivity.EXTRA_SOURCE_ACCOUNT_FORM,
            sourceAccountForm
        )
        bundle.putString(
            SourceAccountActivity.EXTRA_PAGE,
            SourceAccountActivity.PAGE_FREQUENT_BILLER_DETAIL
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

    private fun navigateBeneficiaryFormScreen(frequentBiller: FrequentBiller) {
        val bundle = Bundle()
        bundle.putParcelable(
            ManageFrequentBillerFormActivity.EXTRA_FREQUENT_BILLER,
            frequentBiller
        )
        bundle.putString(
            ManageFrequentBillerFormActivity.EXTRA_TYPE,
            ManageFrequentBillerFormActivity.TYPE_UPDATE
        )
        navigator.navigate(
            this@ManageFrequentBillerDetailActivity,
            ManageFrequentBillerFormActivity::class.java,
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
            ActivityLogActivity.PAGE_FREQUENT_BILLER_DETAIL
        )
        bundle.putString(
            ActivityLogActivity.EXTRA_ID,
            frequentBiller.id.toString()
        )
        navigator.navigate(
            this@ManageFrequentBillerDetailActivity,
            ActivityLogActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    private fun deleteBeneficiary() {
        eventBus.actionSyncEvent.emmit(BaseEvent(ActionSyncEvent.ACTION_DELETE_FREQUENT_BILLER))
        val bundle = Bundle()
        bundle.putString(
            ResultLandingPageActivity.EXTRA_PAGE,
            ResultLandingPageActivity.PAGE_FREQUENT_BILLER_DETAIL
        )
        bundle.putString(
            ResultLandingPageActivity.EXTRA_TITLE,
            getString(R.string.title_frequent_biller_deleted)
        )
        bundle.putString(
            ResultLandingPageActivity.EXTRA_DESC,
            String.format(
                getString(R.string.params_desc_frequent_biller_deleted),
                frequentBiller.name
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

    private fun showDeleteScheduledTransferBottomSheet(frequentBiller: FrequentBiller) {
        deleteBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_delete_frequent_biller),
            formatString(
                R.string.params_delete_frequent_biller,
                frequentBiller.name
            ),
            formatString(R.string.action_yes),
            formatString(R.string.action_no)
        )
        deleteBottomSheet?.setOnConfirmationPageCallBack(this)
        deleteBottomSheet?.show(
            supportFragmentManager,
            TAG_DELETE_FREQUENT_BILLER_DIALOG
        )
    }

    companion object {
        const val EXTRA_ID = "id"
        const val TAG_DELETE_FREQUENT_BILLER_DIALOG = "delete_frequent_biller_dialog"
    }

    override val layoutId: Int
        get() = R.layout.activity_frequent_biller_detail

    override val viewModelClassType: Class<ManageFrequentBillerDetailViewModel>
        get() = ManageFrequentBillerDetailViewModel::class.java
}
