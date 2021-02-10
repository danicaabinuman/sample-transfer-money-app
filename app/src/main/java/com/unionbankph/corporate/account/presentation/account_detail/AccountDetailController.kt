package com.unionbankph.corporate.account.presentation.account_detail

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.AutoModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.Typed3EpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.data.model.Record
import com.unionbankph.corporate.account.presentation.constant.AccountBalanceTypeEnum
import com.unionbankph.corporate.app.common.extension.ACCOUNT_TYPE_ODA
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ItemStateModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoadingFooterModel_
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import io.supercharge.shimmerlayout.ShimmerLayout
import kotlinx.android.synthetic.main.header_account_details.view.*
import kotlinx.android.synthetic.main.item_account.view.*
import kotlinx.android.synthetic.main.item_recent_transaction.view.*
import kotlinx.android.synthetic.main.widget_account_detail_fields.view.*

class AccountDetailController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil,
    private val autoFormatUtil: AutoFormatUtil
) : Typed3EpoxyController<MutableList<Record>, Account, Boolean>() {

    @AutoModel
    lateinit var loadingFooterModel: LoadingFooterModel_

    @AutoModel
    lateinit var itemStateModel: ItemStateModel_

    private lateinit var callbacks: AdapterCallbacks

    interface AdapterCallbacks {
        fun onClickItem(record: String?)
        fun onClickViewAll(id: String?)
    }

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(records: MutableList<Record>, account: Account, isLoading: Boolean) {

        accountDetailHeader {
            id(account.id)
            loadingAccount(account.isLoading)
            account(account)
            viewUtil(viewUtil)
            autoFormatUtil(autoFormatUtil)
            context(context)
            callbacks(callbacks)
        }
        records
            .take(3)
            .forEachIndexed { position, record ->
                recentTransactionItem {
                    id("${record.tranId}_$position")
                    position(position)
                    size(records.take(3).size)
                    record(record)
                    viewUtil(viewUtil)
                    autoFormatUtil(autoFormatUtil)
                    context(context)
                    callbacks(callbacks)
                }
            }

        loadingFooterModel
            .loading(isLoading)
            .addIf(isLoading, this)

        itemStateModel
            .message(context.getString(R.string.msg_no_recent_transaction_found))
            .addIf(records.isEmpty() && !isLoading, this)
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    fun setAdapterCallbacks(callbacks: AdapterCallbacks) {
        this.callbacks = callbacks
    }
}

@EpoxyModelClass(layout = R.layout.header_account_details)
abstract class AccountDetailHeaderModel :
    EpoxyModelWithHolder<AccountDetailHeaderModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @EpoxyAttribute
    lateinit var account: Account

    @EpoxyAttribute
    var loadingAccount: Boolean? = null

    @EpoxyAttribute
    lateinit var callbacks: AccountDetailController.AdapterCallbacks

    override fun bind(holder: Holder) {
        super.bind(holder)

        holder.textViewButton.text = viewUtil.getStringOrEmpty(account.name)
        holder.textViewAccountNumber.text = viewUtil.getStringOrEmpty(
            viewUtil.getAccountNumberFormat(account.accountNumber)
        )
        holder.textViewProductType.text = viewUtil.getStringOrDefaultString(
            account.productCodeDesc,
            context.getString(R.string.title_product_code_desc)
        )

        holder.textViewAccountNameDetail.text = viewUtil.getStringOrEmpty(account.name)
        holder.textViewAccountNumberDetail.text = viewUtil.getStringOrEmpty(
            viewUtil.getAccountNumberFormat(account.accountNumber)
        )
        holder.textViewAccountStatus.text = account.status?.description
        holder.textViewAccountType.text = viewUtil.getStringOrEmpty(account.productCodeDesc)

        val fundsInClearingBalance = autoFormatUtil.getBalance(
            AccountBalanceTypeEnum.FUND_IN_CLEARING.value,
            account.headers
        )
        if (account.productType.equals(ACCOUNT_TYPE_ODA)) {
            val availableCredit = autoFormatUtil.getBalance(
                AccountBalanceTypeEnum.AVAILABLE_CREDIT.value,
                account.headers
            )
            val outstandingBalance = autoFormatUtil.getBalance(
                AccountBalanceTypeEnum.OUTSTANDING_BALANCE.value,
                account.headers
            )
            holder.textViewAvailableBalanceTitle.text = availableCredit?.display
            holder.textViewAvailableBalanceTitleDetail.text = availableCredit?.display
            holder.textViewCurrentBalanceTitleDetail.text = outstandingBalance?.display

            holder.textViewAvailableBalance.text = availableCredit?.value ?: Constant.EMPTY
            holder.textViewAvailableBalanceDetail.text = availableCredit?.value ?: Constant.EMPTY
            holder.textViewCurrentBalanceDetail.text = outstandingBalance?.value ?: Constant.EMPTY
            holder.cardViewAccount.setBackgroundResource(R.drawable.bg_card_view_gradient_orange)
            holder.textViewAccountStatusTitle.visibility(false)
            holder.textViewAccountStatus.visibility(false)
            holder.view3.visibility(false)
        } else {
            val availableBalance = autoFormatUtil.getBalance(
                AccountBalanceTypeEnum.AVAILABLE.value,
                account.headers
            )
            val currentBalance = autoFormatUtil.getBalance(
                AccountBalanceTypeEnum.CURRENT.value,
                account.headers
            )
            holder.textViewAvailableBalanceTitle.text = availableBalance?.display
            holder.textViewAvailableBalanceTitleDetail.text = availableBalance?.display
            holder.textViewCurrentBalanceTitleDetail.text = currentBalance?.display

            holder.textViewAvailableBalance.text = availableBalance?.value ?: Constant.EMPTY
            holder.textViewAvailableBalanceDetail.text = availableBalance?.value ?: Constant.EMPTY
            holder.textViewCurrentBalanceDetail.text = currentBalance?.value ?: Constant.EMPTY
            holder.cardViewAccount.setBackgroundResource(R.drawable.bg_card_view_gradient_gray)
            holder.textViewAccountStatusTitle.visibility(true)
            holder.textViewAccountStatus.visibility(true)
            holder.view3.visibility(true)
        }

        holder.textViewFundsClearingTitle.text = fundsInClearingBalance?.display
        holder.textViewFundsClearing.text = fundsInClearingBalance?.value ?: Constant.EMPTY

        if (loadingAccount!!) {
            holder.shimmerLayoutAmount.startShimmerAnimation()
            holder.viewShimmer.visibility = View.VISIBLE
            holder.textViewAvailableBalance.visibility = View.INVISIBLE
            holder.textViewAvailableBalance.text =
                context.formatString(R.string.value_default_zero_balance)
        } else {
            holder.shimmerLayoutAmount.stopShimmerAnimation()
            holder.viewShimmer.visibility = View.GONE
            holder.textViewAvailableBalance.visibility = View.VISIBLE
        }

        holder.cardViewAll.setOnClickListener {
            callbacks.onClickViewAll(account.id.toString())
        }
    }

    override fun onViewAttachedToWindow(holder: Holder) {
        if (loadingAccount!!) {
            holder.shimmerLayoutAmount.startShimmerAnimation()
        } else {
            holder.shimmerLayoutAmount.stopShimmerAnimation()
        }
        super.onViewAttachedToWindow(holder)
    }

    class Holder : EpoxyHolder() {
        lateinit var cardViewAccount: View
        lateinit var textViewButton: TextView
        lateinit var textViewAccountNumber: TextView
        lateinit var textViewAvailableBalanceTitle: TextView
        lateinit var textViewAvailableBalance: TextView
        lateinit var imageViewEdit: ImageView
        lateinit var shimmerLayoutAmount: ShimmerLayout

        lateinit var textViewAccountNameDetail: TextView
        lateinit var textViewAccountNumberDetail: TextView
        lateinit var textViewAccountType: TextView
        lateinit var textViewProductType: TextView
        lateinit var textViewAccountStatusTitle: TextView
        lateinit var textViewAccountStatus: TextView

        lateinit var textViewAvailableBalanceTitleDetail: TextView
        lateinit var textViewCurrentBalanceTitleDetail: TextView
        lateinit var textViewFundsClearingTitle: TextView
        lateinit var textViewCurrentBalanceDetail: TextView
        lateinit var textViewAvailableBalanceDetail: TextView
        lateinit var textViewFundsClearing: TextView
        lateinit var cardViewAll: View
        lateinit var viewShimmer: View
        lateinit var view3: View

        override fun bindView(itemView: View) {
            cardViewAccount = itemView.viewAccountHeader
            cardViewAll = itemView.cardViewAll
            textViewButton = itemView.textViewCorporateName
            textViewAccountNumber = itemView.textViewAccountNumber
            textViewAvailableBalanceTitle = itemView.textViewAvailableBalanceTitle
            textViewAvailableBalance = itemView.textViewAvailableBalance
            imageViewEdit = itemView.imageViewEdit
            shimmerLayoutAmount = itemView.shimmerLayoutAmount

            textViewAccountNameDetail = itemView.textViewAccountNameDetail
            textViewAccountNumberDetail = itemView.textViewAccountNumberDetail
            textViewCurrentBalanceDetail = itemView.textViewCurrentBalance
            textViewAvailableBalanceTitleDetail = itemView.textViewAvailableBalanceDetailTitle
            textViewCurrentBalanceTitleDetail = itemView.textViewCurrentBalanceTitle
            textViewFundsClearingTitle = itemView.textViewFundsClearingTitle
            textViewAccountType = itemView.textViewAccountType
            textViewProductType = itemView.textViewProductType
            textViewAccountStatusTitle = itemView.textViewAccountStatusTitle
            textViewAccountStatus = itemView.textViewAccountStatus
            view3 = itemView.view3
            textViewAvailableBalanceDetail = itemView.textViewAvailableBalanceDetail
            textViewFundsClearing = itemView.textViewFundsClearing
            viewShimmer = itemView.viewShimmer
        }
    }
}

@EpoxyModelClass(layout = R.layout.item_recent_transaction)
abstract class RecentTransactionItemModel :
    EpoxyModelWithHolder<RecentTransactionItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @EpoxyAttribute
    lateinit var callbacks: AccountDetailController.AdapterCallbacks

    @EpoxyAttribute
    lateinit var record: Record

    @EpoxyAttribute
    var position: Int? = null

    @EpoxyAttribute
    var size: Int? = null

    override fun bind(holder: Holder) {
        super.bind(holder)

        if (position == 0) {
            if (size == 1) {
                holder.viewBorder.visibility = View.GONE
                holder.viewBorder2.visibility = View.GONE
                holder.constraintLayoutItemRecent.background =
                    ContextCompat.getDrawable(
                        holder.constraintLayoutItemRecent.context,
                        R.drawable.bg_shadow
                    )
            } else {
                holder.viewBorder.visibility = View.GONE
                holder.viewBorder2.visibility = View.VISIBLE
                holder.constraintLayoutItemRecent.background =
                    ContextCompat.getDrawable(
                        holder.constraintLayoutItemRecent.context,
                        R.drawable.bg_shadow_top
                    )
            }
        } else if (position == (size?.minus(1))) {
            holder.viewBorder.visibility = View.GONE
            holder.viewBorder2.visibility = View.GONE
            holder.constraintLayoutItemRecent.background =
                ContextCompat.getDrawable(
                    holder.constraintLayoutItemRecent.context,
                    R.drawable.bg_shadow_bottom
                )
        } else {
            holder.viewBorder.visibility = View.GONE
            holder.viewBorder2.visibility = View.VISIBLE
            holder.constraintLayoutItemRecent.background =
                ContextCompat.getDrawable(
                    holder.constraintLayoutItemRecent.context,
                    R.drawable.bg_shadow_center
                )
        }

        holder.textViewAmount.text = autoFormatUtil.formatWithTwoDecimalPlaces(
            record.amount,
            record.currency
        )
        holder.textViewDate.text = viewUtil.getStringOrEmpty(
            viewUtil.getDateFormatByDateString(
                record.postedDate,
                ViewUtil.DATE_FORMAT_ISO,
                ViewUtil.DATE_FORMAT_DEFAULT
            )
        )
        holder.textViewTitle.text = viewUtil.getStringOrEmpty(record.tranDescription)
        holder.imageViewTransferType.setImageResource(
            ConstantHelper.Drawable.getAccountTransactionType(
                record.transactionClass
            )
        )
        holder.constraintLayoutItemRecent.setOnClickListener {
            callbacks.onClickItem(JsonHelper.toJson(record))
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var viewBorder: View
        lateinit var viewBorder2: View
        lateinit var constraintLayoutItemRecent: ConstraintLayout
        lateinit var textViewAmount: TextView
        lateinit var textViewDate: TextView
        lateinit var textViewTitle: TextView
        lateinit var imageViewTransferType: ImageView

        override fun bindView(itemView: View) {
            viewBorder = itemView.viewBorder
            viewBorder2 = itemView.viewBorder2
            constraintLayoutItemRecent = itemView.constraintLayoutItemRecent
            textViewAmount = itemView.textViewAmount
            textViewDate = itemView.textViewDate
            textViewTitle = itemView.textViewTitle
            imageViewTransferType = itemView.imageViewTransferType
        }
    }
}
