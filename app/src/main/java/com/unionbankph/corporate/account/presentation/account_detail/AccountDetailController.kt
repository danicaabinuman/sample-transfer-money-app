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
import com.unionbankph.corporate.databinding.HeaderAccountDetailsBinding
import com.unionbankph.corporate.databinding.ItemRecentTransactionBinding
import io.supercharge.shimmerlayout.ShimmerLayout

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
            viewUtil(this@AccountDetailController.viewUtil)
            autoFormatUtil(this@AccountDetailController.autoFormatUtil)
            context(this@AccountDetailController.context)
            callbacks(this@AccountDetailController.callbacks)
        }
        records
            .take(3)
            .forEachIndexed { position, record ->
                recentTransactionItem {
                    id("${record.tranId}_$position")
                    position(position)
                    size(records.take(3).size)
                    record(record)
                    viewUtil(this@AccountDetailController.viewUtil)
                    autoFormatUtil(this@AccountDetailController.autoFormatUtil)
                    context(this@AccountDetailController.context)
                    callbacks(this@AccountDetailController.callbacks)
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

    @field:EpoxyAttribute
    lateinit var context: Context

    @field:EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @field:EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @field:EpoxyAttribute
    lateinit var account: Account

    @field:EpoxyAttribute
    var loadingAccount: Boolean? = null

    @field:EpoxyAttribute
    lateinit var callbacks: AccountDetailController.AdapterCallbacks

    override fun bind(holder: Holder) {
        super.bind(holder)

        holder.binding.apply {

            viewAccountHeader.textViewAccountNumber.text = viewUtil.getStringOrEmpty(
                viewUtil.getAccountNumberFormat(account.accountNumber)
            )
            viewAccountHeader.textViewProductType.text = viewUtil.getStringOrDefaultString(
                account.productCodeDesc,
                context.getString(R.string.title_product_code_desc)
            )
            viewAccountDetails.textViewAccountNameDetail.text = viewUtil.getStringOrEmpty(account.name)
            viewAccountDetails.textViewAccountNumberDetail.text = viewUtil.getStringOrEmpty(
                viewUtil.getAccountNumberFormat(account.accountNumber)
            )
            viewAccountDetails.textViewAccountStatus.text = account.status?.description
            viewAccountDetails.textViewAccountType.text = viewUtil.getStringOrEmpty(account.productCodeDesc)
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
                viewAccountHeader.textViewAvailableBalanceTitle.text = availableCredit?.display
                viewAccountDetails.textViewAvailableBalanceDetailTitle.text = availableCredit?.display
                viewAccountDetails.textViewCurrentBalanceTitle.text = outstandingBalance?.display

                viewAccountHeader.textViewAvailableBalance.text = availableCredit?.value ?: Constant.EMPTY
                viewAccountDetails.textViewAvailableBalanceDetail.text = availableCredit?.value ?: Constant.EMPTY
                viewAccountDetails.textViewCurrentBalance.text = outstandingBalance?.value ?: Constant.EMPTY
                viewAccountHeader.cardViewAccount.setBackgroundResource(R.drawable.bg_card_view_gradient_orange)
                viewAccountDetails.textViewAccountStatusTitle.visibility(false)
                viewAccountDetails.textViewAccountStatus.visibility(false)
                viewAccountDetails.view3.visibility(false)
            } else {
                val availableBalance = autoFormatUtil.getBalance(
                    AccountBalanceTypeEnum.AVAILABLE.value,
                    account.headers
                )
                val currentBalance = autoFormatUtil.getBalance(
                    AccountBalanceTypeEnum.CURRENT.value,
                    account.headers
                )
                viewAccountHeader.textViewAvailableBalanceTitle.text = availableBalance?.display
                viewAccountDetails.textViewAvailableBalanceDetailTitle.text = availableBalance?.display
                viewAccountDetails.textViewCurrentBalanceTitle.text = currentBalance?.display

                viewAccountHeader.textViewAvailableBalance.text = availableBalance?.value ?: Constant.EMPTY
                viewAccountDetails.textViewAvailableBalanceDetail.text = availableBalance?.value ?: Constant.EMPTY
                viewAccountDetails.textViewCurrentBalance.text = currentBalance?.value ?: Constant.EMPTY
                viewAccountHeader.cardViewAccount.setBackgroundResource(R.drawable.bg_card_view_gradient_gray)
                viewAccountDetails.textViewAccountStatusTitle.visibility(true)
                viewAccountDetails.textViewAccountStatus.visibility(true)
                viewAccountDetails.view3.visibility(true)
            }

            viewAccountDetails.textViewFundsClearingTitle.text = fundsInClearingBalance?.display
            viewAccountDetails.textViewFundsClearing.text = fundsInClearingBalance?.value ?: Constant.EMPTY

            if (loadingAccount!!) {
                viewAccountHeader.shimmerLayoutAmount.startShimmerAnimation()
                viewAccountHeader.viewShimmer.visibility = View.VISIBLE
                viewAccountHeader.textViewAvailableBalance.visibility = View.INVISIBLE
                viewAccountHeader.textViewAvailableBalance.text =
                    context.formatString(R.string.value_default_zero_balance)
            } else {
                viewAccountHeader.shimmerLayoutAmount.stopShimmerAnimation()
                viewAccountHeader.viewShimmer.visibility = View.GONE
                viewAccountHeader.textViewAvailableBalance.visibility = View.VISIBLE
            }

            cardViewAll.textViewAll.setOnClickListener {
                callbacks.onClickViewAll(account.id.toString())
            }
        }
    }

    override fun onViewAttachedToWindow(holder: Holder) {
        holder.binding.apply {
            if (loadingAccount!!) {
                viewAccountHeader.shimmerLayoutAmount.startShimmerAnimation()
            } else {
                viewAccountHeader.shimmerLayoutAmount.stopShimmerAnimation()
            }
        }
        super.onViewAttachedToWindow(holder)
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: HeaderAccountDetailsBinding
            private set

        override fun bindView(itemView: View) {
            binding = HeaderAccountDetailsBinding.bind(itemView)
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

        holder.binding.apply {
            if (position == 0) {
                if (size == 1) {
                    viewBorder.visibility = View.GONE
                    viewBorder2.visibility = View.GONE
                    constraintLayoutItemRecent.background =
                        ContextCompat.getDrawable(
                            constraintLayoutItemRecent.context,
                            R.drawable.bg_shadow
                        )
                } else {
                    viewBorder.visibility = View.GONE
                    viewBorder2.visibility = View.VISIBLE
                    constraintLayoutItemRecent.background =
                        ContextCompat.getDrawable(
                            constraintLayoutItemRecent.context,
                            R.drawable.bg_shadow_top
                        )
                }
            } else if (position == (size?.minus(1))) {
                viewBorder.visibility = View.GONE
                viewBorder2.visibility = View.GONE
                constraintLayoutItemRecent.background =
                    ContextCompat.getDrawable(
                        constraintLayoutItemRecent.context,
                        R.drawable.bg_shadow_bottom
                    )
            } else {
                viewBorder.visibility = View.GONE
                viewBorder2.visibility = View.VISIBLE
                constraintLayoutItemRecent.background =
                    ContextCompat.getDrawable(
                        constraintLayoutItemRecent.context,
                        R.drawable.bg_shadow_center
                    )
            }

            textViewAmount.text = autoFormatUtil.formatWithTwoDecimalPlaces(
                record.amount,
                record.currency
            )

            textViewDate.text = viewUtil.getStringOrEmpty(
                viewUtil.getDateFormatByDateString(
                    record.postedDate,
                    ViewUtil.DATE_FORMAT_ISO,
                    ViewUtil.DATE_FORMAT_DEFAULT
                )
            )

            textViewTitle.text = viewUtil.getStringOrEmpty(record.tranDescription)

            imageViewTransferType.setImageResource(
                ConstantHelper.Drawable.getAccountTransactionType(
                    record.transactionClass
                )
            )

            constraintLayoutItemRecent.setOnClickListener {
                callbacks.onClickItem(JsonHelper.toJson(record))
            }
        }

        super.bind(holder)
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: ItemRecentTransactionBinding

        override fun bindView(itemView: View) {
           binding = ItemRecentTransactionBinding.bind(itemView)
        }
    }
}
