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
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.DashboardActionCarousel
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ErrorAccountFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoadingFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.SMETextViewModel_
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import kotlinx.android.synthetic.main.item_dashboard_action_group.view.*
import kotlinx.android.synthetic.main.item_dashboard_actions.view.*
import kotlinx.android.synthetic.main.item_dashboard_header.view.*
import timber.log.Timber

class DashboardController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil,
    private val autoFormatUtil: AutoFormatUtil
) : Typed2EpoxyController<DashboardViewState, Pageable>() {

    @AutoModel
    lateinit var loadingFooterModel: LoadingFooterModel_

    @AutoModel
    lateinit var errorAccountFooterModel: ErrorAccountFooterModel_

    private lateinit var adapterCallback: DashboardAdapterCallback

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(dashboardViewState: DashboardViewState, pageable: Pageable) {
        Timber.e("hohoho buildModels " + dashboardViewState.name)

        DashboardHeaderModel_()
            .id("dashboard-header")
            .context(context)
            .helloName(dashboardViewState.name!!)
            .buttonText(dashboardViewState.accountButtonText!!)
            .callbacks(adapterCallback)
            .spanSizeOverride { _, _, _ -> 2 }
            .addTo(this)


//        dashboardViewState.accounts.forEachIndexed { position, account ->
//            accountItem {
//                id("${account.id?.toString()}")
//                accountString(JsonHelper.toJson(account))
//                loadingAccount(account.isLoading)
//                errorBool(account.isError)
//                balanceViewable(account.isViewableBalance)
//                position(position)
//                callbacks(accountAdapterCallback)
//                viewUtil(viewUtil)
//                context(context)
//                autoFormatUtil(autoFormatUtil)
//            }
//        }

//        loadingFooterModel.loading(pageable.isLoadingPagination)
//            .addIf(pageable.isLoadingPagination, this)
//        errorAccountFooterModel.title(pageable.errorMessage)
//            .callbacks(accountAdapterCallback)
//            .addIf(pageable.isFailed, this)


        actionGroup {
            id("dashboard-group")
            dashboardItemsString(JsonHelper.toJson(dashboardViewState.actionList))
            callbacks(adapterCallback)
            spanSizeOverride { _, _, _ -> 2 }
        }
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    fun setAdapterCallbacks(dashboardAdapterCallback: DashboardAdapterCallback) {
        this.adapterCallback = dashboardAdapterCallback
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
        holder.buttonAccountAction.text = buttonText

        val action = when (buttonText) {
            context.getString(R.string.action_add) -> Constant.DASHBOARD_ACTION_ADD_ACCOUNT
            else -> Constant.DASHBOARD_ACTION_VIEW_ALL_ACCOUNTS
        }

        holder.buttonAccountAction.setOnClickListener {
            callbacks.onDashboardActionEmit(action)
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

        Timber.e("hohoho ActionsList String " + dashboardItemsString)

        val dashboardActionList = JsonHelper.fromListJson<ActionItem>(dashboardItemsString)
        val dashboardActionModelList : MutableList<ActionModel_> = mutableListOf()

        dashboardActionList.forEach {
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
            callbacks.onDashboardActionEmit(dashboardItem.id.toString())
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





































