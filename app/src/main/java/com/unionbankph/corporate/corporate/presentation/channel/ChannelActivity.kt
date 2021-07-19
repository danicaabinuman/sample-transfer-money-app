package com.unionbankph.corporate.corporate.presentation.channel

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.bills_payment.presentation.bills_payment_form.BillsPaymentFormActivity
import com.unionbankph.corporate.common.data.model.SectionedData
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
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
import com.unionbankph.corporate.databinding.ActivityChannelBinding
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_form.ManageBeneficiaryFormActivity
import com.unionbankph.corporate.fund_transfer.presentation.instapay.InstaPayFormActivity
import com.unionbankph.corporate.fund_transfer.presentation.pddts.PDDTSFormActivity
import com.unionbankph.corporate.fund_transfer.presentation.pesonet.PesoNetFormActivity
import com.unionbankph.corporate.fund_transfer.presentation.swift.SwiftFormActivity
import com.unionbankph.corporate.fund_transfer.presentation.ubp.UBPFormActivity
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo

class ChannelActivity :
    BaseActivity<ActivityChannelBinding, ChannelViewModel>(),
    OnTutorialListener, EpoxyAdapterCallback<Channel> {

    private val controller = ChannelController(this)

    private lateinit var helpMenu: MenuItem

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        tutorialEngineUtil.setOnTutorialListener(this)
        initRecyclerView()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initGeneralViewModel()
        initTutorialViewModel()
        initViewModel()
    }

    private fun initViewModel() {
        generalViewModel.getOrgName()
        viewModel = ViewModelProviders.of(this, viewModelFactory)[ChannelViewModel::class.java]
        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    showLoading(
                        binding.viewLoadingState.root,
                        binding.swipeRefreshLayoutChannel,
                        binding.recyclerViewChannel,
                        binding.textViewState
                    )
                }
                is UiState.Complete -> {
                    dismissLoading(
                        binding.viewLoadingState.root,
                        binding.swipeRefreshLayoutChannel,
                        binding.recyclerViewChannel
                    )
                }
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.sectionedChannels.observe(this, Observer {
            helpMenu.isVisible = intent.getStringExtra(EXTRA_PAGE) == PAGE_FUND_TRANSFER
            updateController(it)
        })
        viewModel.setupPage(intent.getStringExtra(EXTRA_PAGE))
        getAccountChannels()
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        binding.viewToolbar.textViewTitle,
                        binding.viewToolbar.textViewCorporationName,
                        if (intent.getStringExtra(EXTRA_PAGE) != null &&
                            intent.getStringExtra(EXTRA_PAGE) == PAGE_BENEFICIARY
                        ) {
                            getString(R.string.title_create_beneficiary)
                        } else if (intent.getStringExtra(EXTRA_PAGE) == PAGE_BILLS_PAYMENT) {
                            getString(R.string.title_bills_payment)
                        } else {
                            getString(R.string.title_fund_transfer)
                        },
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

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.swipeRefreshLayoutChannel.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                getAccountChannels()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        helpMenu = menu.findItem(R.id.menu_help)
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
                binding.recyclerViewChannel.post { binding.recyclerViewChannel.smoothScrollToPosition(0) }
                Handler().postDelayed(
                    {
                        startViewTutorial()
                    }, resources.getInteger(R.integer.time_enter_tutorial_immediate).toLong()
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
    }

    override fun onClickItem(view: View, data: Channel, position: Int) {
        if (data.serviceFee?.currency == null && data.serviceFee?.value == null) {
            data.serviceFee = null
        }
        if (hasPermissionToProceed(data)) {
            val bundle = Bundle()
            if (intent.getStringExtra(EXTRA_PAGE) != null &&
                intent.getStringExtra(EXTRA_PAGE) == PAGE_BENEFICIARY
            ) {
                navigateBeneficiaryFormScreen(bundle, data)
            } else if (intent.getStringExtra(EXTRA_PAGE) == PAGE_BILLS_PAYMENT) {
                navigateBillsPaymentFormScreen(bundle, data)
            } else {
                when (data.id) {
                    ChannelBankEnum.UBP_TO_UBP.getChannelId() -> {
                        navigateUBPFormScreen(bundle, data.permission?.id.toString(), data)
                    }
                    ChannelBankEnum.PESONET.getChannelId() -> {
                        navigatePesoNetFormScreen(bundle, data.permission?.id.toString(), data)
                    }
                    ChannelBankEnum.PDDTS.getChannelId() -> {
                        navigatePDDTSFormScreen(bundle, data.permission?.id.toString(), data)
                    }
                    ChannelBankEnum.SWIFT.getChannelId() -> {
                        navigateSwiftFormScreen(bundle, data.permission?.id.toString(), data)
                    }
                    ChannelBankEnum.INSTAPAY.getChannelId() -> {
                        navigateInstapayFormScreen(bundle, data.permission?.id.toString(), data)
                    }
                }
            }
        } else {
            showRestrictionDialog(data)
        }
    }

    private fun hasPermissionToProceed(data: Channel): Boolean {
        return data.hasSourceAccount &&
                (data.hasApprovalRule ||
                        (intent.getStringExtra(EXTRA_PAGE) != null &&
                                intent.getStringExtra(EXTRA_PAGE) == PAGE_BENEFICIARY)) &&
                (data.hasRulesAllowTransaction ||
                        (intent.getStringExtra(EXTRA_PAGE) != null &&
                                intent.getStringExtra(EXTRA_PAGE) == PAGE_BENEFICIARY)) &&
                data.hasPermission
    }

    private fun initRecyclerView() {
        controller.setAdapterCallbacks(this)
        binding.recyclerViewChannel.setController(controller)
    }

    private fun navigateInstapayFormScreen(
        bundle: Bundle,
        id: String?,
        channel: Channel
    ) {
        bundle.putString(InstaPayFormActivity.EXTRA_ID, id)
        bundle.putString(
            InstaPayFormActivity.EXTRA_CHANNEL,
            JsonHelper.toJson(channel)
        )
        if (channel.serviceFee != null)
            bundle.putString(
                InstaPayFormActivity.EXTRA_SERVICE_FEE,
                JsonHelper.toJson(channel.serviceFee)
            )
        if (channel.customServiceFee != null)
            bundle.putString(
                InstaPayFormActivity.EXTRA_CUSTOM_SERVICE_FEE,
                JsonHelper.toJson(channel.customServiceFee)
            )
        navigator.navigate(
            this,
            InstaPayFormActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateSwiftFormScreen(
        bundle: Bundle,
        id: String?,
        channel: Channel
    ) {
        bundle.putString(SwiftFormActivity.EXTRA_ID, id)
        bundle.putString(
            SwiftFormActivity.EXTRA_CHANNEL,
            JsonHelper.toJson(channel)
        )
        if (channel.serviceFee != null)
            bundle.putString(
                SwiftFormActivity.EXTRA_SERVICE_FEE,
                JsonHelper.toJson(channel.serviceFee)
            )
        if (channel.customServiceFee != null)
            bundle.putString(
                SwiftFormActivity.EXTRA_CUSTOM_SERVICE_FEE,
                JsonHelper.toJson(channel.customServiceFee)
            )
        navigator.navigate(
            this,
            SwiftFormActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigatePDDTSFormScreen(
        bundle: Bundle,
        id: String?,
        channel: Channel
    ) {
        bundle.putString(PDDTSFormActivity.EXTRA_ID, id)
        bundle.putString(
            PDDTSFormActivity.EXTRA_CHANNEL,
            JsonHelper.toJson(channel)
        )
        if (channel.serviceFee != null)
            bundle.putString(
                PDDTSFormActivity.EXTRA_SERVICE_FEE,
                JsonHelper.toJson(channel.serviceFee)
            )
        if (channel.customServiceFee != null)
            bundle.putString(
                PDDTSFormActivity.EXTRA_CUSTOM_SERVICE_FEE,
                JsonHelper.toJson(channel.customServiceFee)
            )
        navigator.navigate(
            this,
            PDDTSFormActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigatePesoNetFormScreen(
        bundle: Bundle,
        id: String?,
        channel: Channel
    ) {
        bundle.putString(PesoNetFormActivity.EXTRA_ID, id)
        bundle.putString(
            PesoNetFormActivity.EXTRA_CHANNEL,
            JsonHelper.toJson(channel)
        )
        if (channel.serviceFee != null)
            bundle.putString(
                PesoNetFormActivity.EXTRA_SERVICE_FEE,
                JsonHelper.toJson(channel.serviceFee)
            )
        if (channel.customServiceFee != null)
            bundle.putString(
                PesoNetFormActivity.EXTRA_CUSTOM_SERVICE_FEE,
                JsonHelper.toJson(channel.customServiceFee)
            )
        navigator.navigate(
            this,
            PesoNetFormActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateUBPFormScreen(
        bundle: Bundle,
        id: String?,
        channel: Channel
    ) {
        bundle.putString(UBPFormActivity.EXTRA_ID, id)
        bundle.putString(
            UBPFormActivity.EXTRA_CHANNEL,
            JsonHelper.toJson(channel)
        )
        if (channel.serviceFee != null)
            bundle.putString(
                UBPFormActivity.EXTRA_SERVICE_FEE,
                JsonHelper.toJson(channel.serviceFee)
            )
        navigator.navigate(
            this,
            UBPFormActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateBillsPaymentFormScreen(
        bundle: Bundle,
        channel: Channel
    ) {
        if (channel.id == ChannelBankEnum.BILLS_PAYMENT.getChannelId()) {
            bundle.putString(
                BillsPaymentFormActivity.EXTRA_CHANNEL,
                JsonHelper.toJson(channel)
            )
            if (channel.serviceFee != null)
                bundle.putString(
                    BillsPaymentFormActivity.EXTRA_SERVICE_FEE,
                    JsonHelper.toJson(channel.serviceFee)
                )
            navigator.navigate(
                this,
                BillsPaymentFormActivity::class.java,
                bundle,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
            )
        } else {
            Observable.fromIterable(viewModel.billers.value)
                .filter {
                    if (channel.id == ChannelBankEnum.BIR.getChannelId()) {
                        it.serviceId == Constant.IntegerValues.BIR_SERVICE_ID
                    } else {
                        it.serviceId == Constant.IntegerValues.SSS_SERVICE_ID
                    }
                }
                .subscribeOn(schedulerProvider.newThread())
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    bundle.putString(
                        BillsPaymentFormActivity.EXTRA_CHANNEL,
                        JsonHelper.toJson(channel)
                    )
                    bundle.putString(
                        BillsPaymentFormActivity.EXTRA_BILLER,
                        JsonHelper.toJson(it)
                    )
                    if (channel.serviceFee != null)
                        bundle.putString(
                            BillsPaymentFormActivity.EXTRA_SERVICE_FEE,
                            JsonHelper.toJson(channel.serviceFee)
                        )
                    navigator.navigate(
                        this,
                        BillsPaymentFormActivity::class.java,
                        bundle,
                        isClear = false,
                        isAnimated = true,
                        transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
                    )
                }.addTo(disposables)
        }
    }

    private fun navigateBeneficiaryFormScreen(
        bundle: Bundle,
        channel: Channel
    ) {
        bundle.putString(
            ManageBeneficiaryFormActivity.EXTRA_TYPE,
            ManageBeneficiaryFormActivity.TYPE_CREATE
        )
        bundle.putString(
            ManageBeneficiaryFormActivity.EXTRA_CHANNEL,
            JsonHelper.toJson(channel)
        )
        navigator.navigate(
            this,
            ManageBeneficiaryFormActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun showRestrictionDialog(channel: Channel) {
        if (!channel.hasSourceAccount) {
            val currencyStringBuilder = StringBuilder()
            channel.currencies.forEachIndexed { index, currency ->
                when (channel.currencies.size) {
                    index.plus(1) -> {
                        if (channel.currencies.size == 1) {
                            currencyStringBuilder.append(currency.name)
                        } else {
                            currencyStringBuilder.append("or " + currency.name)
                        }
                    }
                    else -> currencyStringBuilder.append(currency.name + ", ")
                }
            }
            val title = formatString(
                R.string.title_no_currency_account_enrolled,
                currencyStringBuilder.toString()
            )

            val message = if (channel.currencies.size > 1) {
                formatString(
                    R.string.params_no_currency_account_enrolled_more_than_one,
                    ConstantHelper.Text.getChannelByChannelId(channel.id),
                    currencyStringBuilder.toString()
                )
            } else {
                formatString(
                    R.string.params_no_currency_account_enrolled,
                    ConstantHelper.Text.getChannelByChannelId(channel.id),
                    currencyStringBuilder.toString()
                )
            }
            showMaterialDialogError(title, message)
        } else if (!channel.hasApprovalRule) {
            showMaterialDialogError(
                formatString(R.string.params_title_no_approval_hierarchy_dialog),
                formatString(
                    R.string.params_no_approval_hierarchy,
                    ConstantHelper.Text.getChannelByChannelId(channel.id)
                )
            )
        } else if (!channel.hasRulesAllowTransaction) {
            showMaterialDialogError(
                formatString(
                    R.string.params_title_no_transaction_allowed,
                    ConstantHelper.Text.getChannelByChannelId(channel.id)
                ),
                formatString(
                    R.string.params_no_transaction_allowed,
                    ConstantHelper.Text.getChannelByChannelId(channel.id)
                )
            )
        } else if (!channel.hasPermission) {
            showMaterialDialogError(
                message = formatString(
                    R.string.params_no_permission_channel,
                    ConstantHelper.Text.getChannelByChannelId(channel.id)
                )
            )
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
        isSkipTutorial = false
    }

    override fun onEndedTutorial(view: View?, viewTarget: View) {
        if (!isSkipTutorial && view == binding.recyclerViewChannel.findViewHolderForAdapterPosition(0)?.itemView) {
            val firstSectioned = viewModel.sectionedChannels.value
                ?.filter { it.type == Constant.UNIONBANK_ONLY || it.type == Constant.BILLS_PAYMENT }
                ?.get(0)?.data
            val secondSectioned = viewModel.sectionedChannels.value
                ?.filter { it.type == Constant.OTHER_BANKS || it.type == Constant.EGOV_PAYMENT }
                ?.get(0)?.data
            val firstSectionedSize = firstSectioned?.size ?: 0
            val secondSectionedSize = secondSectioned?.size ?: 0
            val cardViewTop = binding.recyclerViewChannel
                .findViewHolderForAdapterPosition(firstSectionedSize.plus(1))?.itemView
            val cardViewBottom = binding.recyclerViewChannel
                .findViewHolderForAdapterPosition(
                    firstSectionedSize + 1 + secondSectionedSize
                )?.itemView

            val rectTop = Rect()
            cardViewTop?.getGlobalVisibleRect(rectTop)

            val rectBottom = Rect()
            cardViewBottom?.getGlobalVisibleRect(rectBottom)

            var totalHeight = 0
            totalHeight += rectTop.height()

            secondSectioned?.forEachIndexed { index, channel ->
                val rectIterate = Rect()
                binding.recyclerViewChannel
                    .findViewHolderForAdapterPosition(
                        (firstSectionedSize + 1) + (index + 1)
                    )?.itemView?.getGlobalVisibleRect(rectIterate)
                totalHeight += rectIterate.height()
            }
            val cardBottom = rectTop.top + totalHeight
            cardViewTop?.let {
                tutorialEngineUtil.startTutorial(
                    this,
                    cardViewTop,
                    totalHeight,
                    Rect(rectTop.left, rectTop.top, rectTop.right, cardBottom),
                    R.layout.frame_tutorial_lower_left,
                    0f,
                    false,
                    getString(R.string.msg_tutorial_channel_other_banks),
                    GravityEnum.TOP,
                    OverlayAnimationEnum.ANIM_EXPLODE
                )
            }
        }
    }

    private fun updateController(data: MutableList<SectionedData<Channel>>) {
        controller.setData(data)
    }

    private fun startViewTutorial() {
        val rect = Rect()
        val rect2 = Rect()
        val firstSectionedSize = viewModel.sectionedChannels.value
            ?.filter { it.type == Constant.UNIONBANK_ONLY || it.type == Constant.BILLS_PAYMENT }
            ?.get(0)?.data?.size ?: 0
        val cardView = binding.recyclerViewChannel
            .findViewHolderForAdapterPosition(0)?.itemView
        val cardViewEnd = binding.recyclerViewChannel
            .findViewHolderForAdapterPosition(firstSectionedSize)?.itemView
        if (cardView != null && cardViewEnd != null) {
            val totalHeight = cardView.height + (cardViewEnd.height * firstSectionedSize)
            cardView.getGlobalVisibleRect(rect)
            cardViewEnd.getGlobalVisibleRect(rect2)
            tutorialEngineUtil.startTutorial(
                this,
                cardView,
                totalHeight,
                Rect(rect.left, rect.top, rect.right, rect2.bottom),
                R.layout.frame_tutorial_upper_left,
                0f,
                false,
                getString(R.string.msg_tutorial_channel_ubp),
                GravityEnum.BOTTOM,
                OverlayAnimationEnum.ANIM_EXPLODE
            )
        }
    }

    private fun getAccountChannels() {
        viewModel.getAccountChannels()
    }

    companion object {
        const val EXTRA_PAGE = "page"
        const val PAGE_FUND_TRANSFER = "fund_transfer"
        const val PAGE_BILLS_PAYMENT = "bills_payment"
        const val PAGE_BENEFICIARY = "beneficiary"
        const val PAGE_BRANCH_VISIT = "branch_visit"
    }

    override val layoutId: Int
        get() = R.layout.activity_channel

    override val viewModelClassType: Class<ChannelViewModel>
        get() = ChannelViewModel::class.java
}
