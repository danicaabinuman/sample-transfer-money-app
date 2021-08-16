package com.unionbankph.corporate.app.dashboard.fragment

import android.content.Context
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.*
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.*
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ErrorFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.DashboardAccountItemModel_
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
import kotlinx.android.synthetic.main.item_dashboard_action_group.view.*
import kotlinx.android.synthetic.main.item_dashboard_actions.view.*
import kotlinx.android.synthetic.main.item_dashboard_header.view.*

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
        val accountButtonText = ConstantHelper.Text.getDashboardAccountButtonText(context, accountSize, isRefreshed)

        DashboardHeaderModel_()
            .id("dashboard-header")
            .context(context)
            .helloName(dashboardViewState.name ?: "null")
            .buttonText(accountButtonText)
            .callbacks(dashboardAdapterCallback)
            .addTo(this)

        initialAccountLoadingModel
            .addIf(pageable.isInitialLoad && isRefreshed && !hasInitialFetchError, this)

        noAccountModel
            .callbacks(dashboardAdapterCallback)
            .addIf(!isRefreshed &&
                    !hasInitialFetchError &&
                            accountSize == 0, this)

        dashboardViewState.accounts.forEachIndexed { position, account ->
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
            callbacks(dashboardAdapterCallback)
        }

        loansModel
            .addTo(this)

        earningsModel
            .addTo(this)

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

        holder.textViewHello.text = context.formatString(R.string.hello_name, helloName)

        if (buttonText.isNotEmpty()) {
            holder.buttonAccountAction.apply {
                text = buttonText
                visibility = View.VISIBLE
            }
        } else {
            holder.buttonAccountAction.visibility = View.INVISIBLE
        }

        val action = when (buttonText) {
            context.getString(R.string.action_add) -> {
                Constant.DASHBOARD_ACTION_ADD_ACCOUNT
            }
            else -> Constant.DASHBOARD_ACTION_VIEW_ALL_ACCOUNTS
        }

        holder.buttonAccountAction.setOnClickListener {
            callbacks.onDashboardActionEmit(action, true)
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var textViewHello: AppCompatTextView
        lateinit var buttonAccountAction: AppCompatButton

        override fun bindView(itemView: View) {
            textViewHello = itemView.textViewHello
            buttonAccountAction = itemView.buttonAccountAction
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

        holder.dashboardActionContainer
            .setModels(dashboardActionModelList)
    }

    class Holder : EpoxyHolder() {

        lateinit var dashboardActionContainer: DashboardActionCarousel

        override fun bindView(itemView: View) {
            dashboardActionContainer = itemView.dashboardGroupContainer
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

        holder.constraintLayoutRoot.setOnClickListener {
            callbacks.onDashboardActionEmit(
                dashboardItem.id.toString(),
                dashboardItem.isEnabled)
        }
        holder.textViewLabel.text = dashboardItem.label
        holder.imageViewIcon.setImageResource(
            ConstantHelper.Drawable.getDashboardActionIcon(
                dashboardItem.id!!
            )
        )
    }

    class Holder : EpoxyHolder() {
        lateinit var constraintLayoutRoot: ConstraintLayout
        lateinit var textViewLabel: AppCompatTextView
        lateinit var imageViewIcon: AppCompatImageView

        override fun bindView(itemView: View) {
            constraintLayoutRoot = itemView.constraintLayoutRoot
            textViewLabel = itemView.textViewLabel
            imageViewIcon = itemView.imageViewIcon
        }
    }
}





































