package com.unionbankph.corporate.app.dashboard.fragment

import android.content.Context
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.*
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.*
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoadingFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.DashboardAccountItemModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.AccountItemLoadingModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ErrorAccountFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoansDashboardItemModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.EarningsDashboardItemModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.NoAccountItemModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.AccountItemErrorModel_
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ItemDashboardActionGroupBinding
import com.unionbankph.corporate.databinding.ItemDashboardActionsBinding
import com.unionbankph.corporate.databinding.ItemDashboardHeaderBinding
import com.unionbankph.corporate.databinding.ItemFeatureCardBinding

class DashboardFragmentController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil,
    private val autoFormatUtil: AutoFormatUtil
) : Typed2EpoxyController<DashboardViewState, Pageable>() {

    @AutoModel
    lateinit var loadingFooterModel: LoadingFooterModel_

    @AutoModel
    lateinit var errorAccountFooterModel: ErrorAccountFooterModel_

    @AutoModel
    lateinit var initialAccountLoadingModel: AccountItemLoadingModel_

    @AutoModel
    lateinit var initialAccountErrorModel: AccountItemErrorModel_

    @AutoModel
    lateinit var noAccountModel: NoAccountItemModel_

    @AutoModel
    lateinit var loansModel: LoansDashboardItemModel_

    @AutoModel
    lateinit var earningsModel: EarningsDashboardItemModel_

    private lateinit var dashboardAdapterCallback: DashboardAdapterCallback
    private lateinit var accountAdapterCallback: AccountAdapterCallback

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(dashboardViewState: DashboardViewState, pageable: Pageable) {
        val isRefreshed = dashboardViewState.isScreenRefreshed
        val accountSize = dashboardViewState.accounts.size
        val hasInitialFetchError = dashboardViewState.hasInitialFetchError
        val isOnTrialMode = dashboardViewState.isOnTrialMode
        val accountButtonText = ConstantHelper.Text.getDashboardAccountButtonText(
            context,
            accountSize,
            isOnTrialMode,
            isRefreshed
        )

        DashboardHeaderModel_()
            .id("dashboard-header")
            .context(context)
            .helloName(dashboardViewState.name ?: "null")
            .buttonText(accountButtonText)
            .callbacks(dashboardAdapterCallback)
            .addTo(this)

        initialAccountLoadingModel
            .addIf(pageable.isInitialLoad && isRefreshed && !hasInitialFetchError, this)


        if (!isRefreshed &&
            !hasInitialFetchError &&
            isOnTrialMode) {

            noAccountModel
                .callbacks(dashboardAdapterCallback)
                .addTo(this)

        } else {
            dashboardViewState.accounts.take(2).forEachIndexed { position, account ->
                DashboardAccountItemModel_()
                    .id("${account.id?.toString()}")
                    .accountString(JsonHelper.toJson(account))
                    .loadingAccount(account.isLoading)
                    .errorBool(account.isError)
                    .balanceViewable(account.isViewableBalance)
                    .position(position)
                    .callbacks(accountAdapterCallback)
                    .viewUtil(viewUtil)
                    .context(context)
                    .autoFormatUtil(autoFormatUtil)
                    .addTo(this)
            }
        }

        loadingFooterModel.loading(pageable.isLoadingPagination)
            .addIf(pageable.isLoadingPagination && !isRefreshed, this)

        errorAccountFooterModel.title(pageable.errorMessage)
            .callbacks(accountAdapterCallback)
            .addIf(pageable.isFailed && !isRefreshed, this)

        initialAccountErrorModel
            .errorMessage(dashboardViewState.errorMessage ?: "")
            .callbacks(accountAdapterCallback)
            .addIf(isRefreshed && hasInitialFetchError, this)

        actionGroup {
            id("dashboard-action-group")
            dashboardItemsString(JsonHelper.toJson(dashboardViewState.actionList))
            callbacks(this@DashboardFragmentController.dashboardAdapterCallback)
        }

        if (dashboardViewState.hasLoans) {
            loansModel.addTo(this)
        } else {
            featureCard {
                id("loans-default")
                item(this@DashboardFragmentController.defaultLoansItem())
                callbacks(this@DashboardFragmentController.dashboardAdapterCallback)
            }
        }

        if (dashboardViewState.hasEarnings) {
            earningsModel.addTo(this)
        } else {
            featureCard {
                id("earnings-default")
                item(this@DashboardFragmentController.defaultEarningsItem())
                callbacks(this@DashboardFragmentController.dashboardAdapterCallback)
            }
        }

        val banners = mutableListOf<DashboardBannerItemModel>()
        for (x in 0..4) {
            banners.add(
                DashboardBannerItemModel_().id(x)
            )
        }

        CarouselIndicatorModel()
            .id("dashboard-banners")
            .numViewsToShowOnScreen(1f)
            .models(banners)
            .addTo(this)
    }

    private fun defaultLoansItem() = JsonHelper.toJson(
        FeatureCardItem().apply {
            id = "default-loans-item"
            featureTitle = context.getString(R.string.title_loans)
            cardTitle = context.getString(R.string.loans_card_title)
            cardContent = context.getString(R.string.loans_card_desc)
            action = Constant.DASHBOARD_ACTION_DEFAULT_LOANS
            cardFooter = context.getString(R.string.loans_card_button)
        }
    )

    private fun defaultEarningsItem() = JsonHelper.toJson(
        FeatureCardItem().apply {
            id = "default-earnings-item"
            featureTitle = context.getString(R.string.title_earnings)
            cardTitle = context.getString(R.string.earnings_card_title)
            cardContent = context.getString(R.string.earnings_card_desc)
            action = Constant.DASHBOARD_ACTION_DEFAULT_EARNINGS
            cardFooter = context.getString(R.string.earnings_card_button)
        }
    )

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    fun setDashboardAdapterCallbacks(dashboardAdapterCallback: DashboardAdapterCallback) {
        this.dashboardAdapterCallback = dashboardAdapterCallback
    }

    fun setAccountAdapterCallbacks(accountAdapterCallback: AccountAdapterCallback) {
        this.accountAdapterCallback = accountAdapterCallback
    }
}

@EpoxyModelClass
abstract class DashboardHeaderModel: EpoxyModelWithHolder<DashboardHeaderModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var helloName: String

    @EpoxyAttribute
    lateinit var buttonText: String

    @EpoxyAttribute
    lateinit var callbacks: DashboardAdapterCallback

    override fun getDefaultLayout(): Int {
        return R.layout.item_dashboard_header
    }

    override fun bind(holder: Holder) {
        super.bind(holder)

        holder.binding.textViewHello.text = context.formatString(R.string.hello_name, helloName)

        if (buttonText.isNotEmpty()) {
            holder.binding.buttonAccountAction.apply {
                text = buttonText
                visibility = View.VISIBLE
            }
        } else {
            holder.binding.buttonAccountAction.visibility = View.INVISIBLE
        }

        val action = when (buttonText) {
            context.getString(R.string.action_add) -> Constant.DASHBOARD_ACTION_ADD_ACCOUNT
            else -> Constant.DASHBOARD_ACTION_VIEW_ALL_ACCOUNTS
        }

        holder.binding.buttonAccountAction.setOnClickListener {
            callbacks.onDashboardActionEmit(action, true)
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemDashboardHeaderBinding

        lateinit var textViewHello: AppCompatTextView
        lateinit var buttonAccountAction: AppCompatButton

        override fun bindView(itemView: View) {
            binding = ItemDashboardHeaderBinding.bind(itemView)
        }
    }
}

@EpoxyModelClass
abstract class ActionGroupModel: EpoxyModelWithHolder<ActionGroupModel.Holder>() {

    @EpoxyAttribute
    lateinit var dashboardItemsString: String

    @EpoxyAttribute
    lateinit var callbacks: DashboardAdapterCallback

    override fun getDefaultLayout(): Int {
        return R.layout.item_dashboard_action_group
    }

    override fun bind(holder: Holder) {
        super.bind(holder)

        val dashboardActionList = JsonHelper.fromListJson<ActionItem>(dashboardItemsString)
        val dashboardActionModelList : MutableList<ActionModel_> = mutableListOf()

        dashboardActionList
            .filter { actionItem -> actionItem.isVisible }
            .forEach {
                dashboardActionModelList.add(
                    ActionModel_()
                        .id(it.id)
                        .dashboardItemString(JsonHelper.toJson(it))
                        .callbacks(callbacks)
            )
        }

        holder.binding.dashboardGroupContainer
            .setModels(dashboardActionModelList)
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemDashboardActionGroupBinding

        override fun bindView(itemView: View) {
            binding = ItemDashboardActionGroupBinding.bind(itemView)
        }
    }
}

@EpoxyModelClass
abstract class ActionModel : EpoxyModelWithHolder<ActionModel.Holder>() {

    @EpoxyAttribute
    lateinit var dashboardItemString: String

    @EpoxyAttribute
    lateinit var callbacks: DashboardAdapterCallback

    override fun getDefaultLayout(): Int {
        return R.layout.item_dashboard_actions
    }

    override fun bind(holder: Holder) {
        super.bind(holder)

        val dashboardItem = JsonHelper.fromJson<ActionItem>(dashboardItemString)

        holder.binding.constraintLayoutRoot.setOnClickListener {
            callbacks.onDashboardActionEmit(
                dashboardItem.id.toString(),
                dashboardItem.isEnabled)
        }
        holder.binding.textViewLabel.text = dashboardItem.label
        holder.binding.imageViewIcon.setImageResource(
            ConstantHelper.Drawable.getDashboardActionIcon(
                dashboardItem.id!!
            )
        )
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemDashboardActionsBinding

        override fun bindView(itemView: View) {
            binding = ItemDashboardActionsBinding.bind(itemView)
        }
    }
}

@EpoxyModelClass
abstract class FeatureCardModel : EpoxyModelWithHolder<FeatureCardModel.Holder>()  {

    @EpoxyAttribute
    lateinit var item: String

    @EpoxyAttribute
    lateinit var callbacks: DashboardAdapterCallback

    override fun getDefaultLayout(): Int {
        return R.layout.item_feature_card
    }

    override fun bind(holder: Holder) {
        val featureItem: FeatureCardItem = JsonHelper.fromJson(item)

        holder.binding.apply {
            textViewFeatureTitle.text = featureItem.featureTitle
            textViewCardTitle.text = featureItem.cardTitle
            textViewCardContent.text = featureItem.cardContent
            textViewCardFooter.text = featureItem.cardFooter

            imageViewIcon.setImageResource(
                ConstantHelper.Drawable.getFeatureCardIcon(featureItem.action!!)
            )
            cardViewFeature.setOnClickListener {
                callbacks.onDashboardActionEmit(featureItem.action!!, true)
            }
        }
    }

    class Holder: EpoxyHolder() {

        lateinit var binding: ItemFeatureCardBinding

        override fun bindView(itemView: View) {
            binding = ItemFeatureCardBinding.bind(itemView)
        }
    }
}





































