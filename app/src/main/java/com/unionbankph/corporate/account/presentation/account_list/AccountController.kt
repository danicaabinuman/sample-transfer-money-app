package com.unionbankph.corporate.account.presentation.account_list

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.airbnb.epoxy.*
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.presentation.constant.AccountBalanceTypeEnum
import com.unionbankph.corporate.app.common.extension.ACCOUNT_TYPE_ODA
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ErrorAccountFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoadingFooterModel_
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import io.supercharge.shimmerlayout.ShimmerLayout
import kotlinx.android.synthetic.main.item_account.view.*
import kotlinx.android.synthetic.main.row_account.view.*

class AccountsController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil,
    private val autoFormatUtil: AutoFormatUtil
) : Typed3EpoxyController<MutableList<Account>, Pageable, Boolean>() {

    @AutoModel
    lateinit var loadingFooterModel: LoadingFooterModel_

    @AutoModel
    lateinit var errorAccountFooterModel: ErrorAccountFooterModel_


    private lateinit var accountAdapterCallback: AccountAdapterCallback

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(
        accounts: MutableList<Account>,
        pageable: Pageable,
        isTableView: Boolean
    ) {
        accounts.forEachIndexed { position, account ->
            if (isTableView) {
                accountRow {
                    id("${account.id?.toString()}")
                    accountString(JsonHelper.toJson(account))
                    loadingAccount(account.isLoading)
                    errorBool(account.isError)
                    position(position)
                    callbacks(accountAdapterCallback)
                    viewUtil(viewUtil)
                    context(context)
                    autoFormatUtil(autoFormatUtil)
                }
            } else {
                accountItem {
                    id("${account.id?.toString()}")
                    accountString(JsonHelper.toJson(account))
                    loadingAccount(account.isLoading)
                    errorBool(account.isError)
                    balanceViewable(account.isViewableBalance)
                    position(position)
                    callbacks(accountAdapterCallback)
                    viewUtil(viewUtil)
                    context(context)
                    autoFormatUtil(autoFormatUtil)
                }
            }
        }

        loadingFooterModel.loading(pageable.isLoadingPagination)
            .addIf(pageable.isLoadingPagination && !isTableView, this)
        errorAccountFooterModel.title(pageable.errorMessage)
            .callbacks(accountAdapterCallback)
            .addIf(pageable.isFailed, this)
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    fun setAdapterCallbacks(accountAdapterCallback: AccountAdapterCallback) {
        this.accountAdapterCallback = accountAdapterCallback
    }

}

@EpoxyModelClass(layout = R.layout.item_account)
abstract class AccountItemModel : EpoxyModelWithHolder<AccountItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @EpoxyAttribute
    lateinit var accountString: String

    @EpoxyAttribute
    var position: Int = 0

    @EpoxyAttribute
    var errorBool: Boolean = false

    @EpoxyAttribute
    var loadingAccount: Boolean = false

    @EpoxyAttribute
    var balanceViewable: Boolean = false

    @EpoxyAttribute
    lateinit var callbacks: AccountAdapterCallback

    override fun bind(holder: Holder) {
        super.bind(holder)
        val account = JsonHelper.fromJson<Account>(accountString)

        holder.corporateName.text = viewUtil.getStringOrDefaultString(
            account.name,
            context.getString(R.string.title_account_name)
        )
        holder.accountNumber.text = viewUtil.getStringOrEmpty(
            viewUtil.getAccountNumberFormat(account.accountNumber)
        )
        holder.accountType.text = viewUtil.getStringOrEmpty(account.productCodeDesc)

        if (account.productType.equals(ACCOUNT_TYPE_ODA)) {
            val availableCredit = autoFormatUtil.getBalance(
                AccountBalanceTypeEnum.AVAILABLE_CREDIT.value,
                account.headers
            )
            holder.textViewAvailableBalanceTitle.text = availableCredit?.display
            holder.textViewAvailableBalance.text = availableCredit?.value ?: Constant.EMPTY
            holder.cardViewAccount.setBackgroundResource(R.drawable.bg_card_view_gradient_orange)
        } else {
            val availableBalance = autoFormatUtil.getBalance(
                AccountBalanceTypeEnum.AVAILABLE.value,
                account.headers
            )
            holder.textViewAvailableBalanceTitle.text = availableBalance?.display
            holder.textViewAvailableBalance.text = availableBalance?.value ?: Constant.EMPTY
            holder.cardViewAccount.setBackgroundResource(R.drawable.bg_card_view_gradient_gray)
        }

        holder.cardViewAccount.setOnClickListener {
            if (account.permissionCollection.hasAllowToBTRViewTransaction &&
                account.permissionCollection.hasAllowToBTRViewBalance &&
                !errorBool
            ) {
                callbacks.onClickItem(accountString, position)
            }
        }

        if (loadingAccount) {
            //callbacks.onRequestAccountDetail(account.id.toString(), position)
            holder.shimmerLayoutAmount.post {
                holder.shimmerLayoutAmount.startShimmerAnimation()
            }
            holder.viewShimmer.visibility = View.VISIBLE
            holder.textViewAvailableBalanceTitle.visibility = View.VISIBLE
            holder.textViewAvailableBalance.visibility = View.INVISIBLE
            holder.textViewAvailableBalance.text =
                context.formatString(R.string.value_default_zero_balance)
            if (holder.viewError.visibility == View.VISIBLE) {
                holder.viewError.visibility = View.GONE
            }
        } else {
            holder.shimmerLayoutAmount.post {
                holder.shimmerLayoutAmount.stopShimmerAnimation()
            }
            holder.viewShimmer.visibility = View.GONE
            if (account.permissionCollection.hasAllowToBTRViewBalance) {
                holder.textViewAvailableBalanceTitle.visibility = View.VISIBLE
                holder.textViewAvailableBalance.visibility = View.VISIBLE
            } else {
                holder.textViewAvailableBalanceTitle.visibility = View.INVISIBLE
                holder.textViewAvailableBalance.visibility = View.INVISIBLE
            }
        }
        if (errorBool) {
            holder.viewError.visibility = View.VISIBLE
            holder.textViewAvailableBalanceTitle.visibility = View.INVISIBLE
            holder.textViewAvailableBalance.visibility = View.INVISIBLE
            holder.viewError.setOnClickListener {
                callbacks.onTapErrorRetry(account.id.toString(), position)
            }
        } else {
            holder.viewError.visibility = View.GONE
        }
    }

    override fun onViewAttachedToWindow(holder: Holder) {
        if (loadingAccount) {
            holder.shimmerLayoutAmount.post {
                holder.shimmerLayoutAmount.startShimmerAnimation()
            }
        } else {
            holder.shimmerLayoutAmount.post {
                holder.shimmerLayoutAmount.stopShimmerAnimation()
            }
        }
        super.onViewAttachedToWindow(holder)
    }

    class Holder : EpoxyHolder() {
        lateinit var corporateName: TextView
        lateinit var accountNumber: TextView
        lateinit var textViewAvailableBalance: TextView
        lateinit var textViewAvailableBalanceTitle: TextView
        lateinit var accountType: TextView
        lateinit var cardViewAccount: CardView
        lateinit var shimmerLayoutAmount: ShimmerLayout
        lateinit var viewShimmer: View
        lateinit var viewError: View

        override fun bindView(itemView: View) {
            corporateName = itemView.textViewCorporateName
            accountNumber = itemView.textViewAccountNumber
            textViewAvailableBalance = itemView.textViewAvailableBalance
            textViewAvailableBalanceTitle = itemView.textViewAvailableBalanceTitle
            accountType = itemView.textViewProductType
            cardViewAccount = itemView.card_view_account
            shimmerLayoutAmount = itemView.shimmerLayoutAmount
            viewShimmer = itemView.viewShimmer
            viewError = itemView.viewError
        }
    }
}

@EpoxyModelClass(layout = R.layout.row_account)
abstract class AccountRowModel : EpoxyModelWithHolder<AccountRowModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @EpoxyAttribute
    lateinit var accountString: String

    @EpoxyAttribute
    var position: Int = 0

    @EpoxyAttribute
    var errorBool: Boolean = false

    @EpoxyAttribute
    var loadingAccount: Boolean = false

    @EpoxyAttribute
    lateinit var callbacks: AccountAdapterCallback

    override fun bind(holder: Holder) {
        super.bind(holder)
        val account = JsonHelper.fromJson<Account>(accountString)

        holder.apply {
            viewBorderTop.visibility(position == 0)
            textViewRowAccountName.text = viewUtil.getStringOrDefaultString(
                account.name,
                context.getString(R.string.title_account_name)
            )
            textViewRowAccountNumber.text = viewUtil.getStringOrEmpty(
                viewUtil.getAccountNumberFormat(account.accountNumber)
            )
            if (account.productType.equals(ACCOUNT_TYPE_ODA)) {
                val availableCredit = autoFormatUtil.getBalance(
                    AccountBalanceTypeEnum.AVAILABLE_CREDIT.value,
                    account.headers
                )
                textViewRowAvailableBalance.text = availableCredit?.value.notNullable()
            } else {
                val availableBalance = autoFormatUtil.getBalance(
                    AccountBalanceTypeEnum.AVAILABLE.value,
                    account.headers
                )
                textViewRowAvailableBalance.text = availableBalance?.value.notNullable()
            }

            holder.itemView.setOnClickListener {
                if (account.permissionCollection.hasAllowToBTRViewTransaction &&
                    account.permissionCollection.hasAllowToBTRViewBalance &&
                    !errorBool
                ) {
                    callbacks.onClickItem(accountString, position)
                }
            }

            if (loadingAccount) {
                //callbacks.onRequestAccountDetail(account.id.toString(), position)
                holder.shimmerLayoutRowAmount.post {
                    holder.shimmerLayoutRowAmount.startShimmerAnimation()
                }
                holder.viewRowShimmer.visibility = View.VISIBLE
                holder.textViewRowAvailableBalance.visibility = View.INVISIBLE
                holder.textViewRowAvailableBalance.text =
                    context.formatString(R.string.value_default_zero_balance)
                if (holder.viewRowError.visibility == View.VISIBLE) {
                    holder.viewRowError.visibility = View.GONE
                }
            } else {
                holder.shimmerLayoutRowAmount.post {
                    holder.shimmerLayoutRowAmount.stopShimmerAnimation()
                }
                holder.viewRowShimmer.visibility = View.GONE
                if (account.permissionCollection.hasAllowToBTRViewBalance) {
                    holder.textViewRowAvailableBalance.visibility = View.VISIBLE
                } else {
                    holder.textViewRowAvailableBalance.visibility = View.INVISIBLE
                }
            }
            if (errorBool) {
                holder.viewRowError.visibility = View.VISIBLE
                holder.viewRowError.setOnClickListener {
                    callbacks.onTapErrorRetry(account.id.toString(), position)
                }
            } else {
                holder.viewRowError.visibility = View.GONE
            }
        }
    }

    override fun onViewAttachedToWindow(holder: Holder) {
        if (loadingAccount) {
            holder.shimmerLayoutRowAmount.post {
                holder.shimmerLayoutRowAmount.startShimmerAnimation()
            }
        } else {
            holder.shimmerLayoutRowAmount.post {
                holder.shimmerLayoutRowAmount.stopShimmerAnimation()
            }
        }
        super.onViewAttachedToWindow(holder)
    }

    class Holder : EpoxyHolder() {
        lateinit var textViewRowAccountName: TextView
        lateinit var textViewRowAccountNumber: TextView
        lateinit var textViewRowAvailableBalance: TextView
        lateinit var shimmerLayoutRowAmount: ShimmerLayout
        lateinit var viewRowShimmer: View
        lateinit var viewRowError: View
        lateinit var viewBorderTop: View
        lateinit var itemView: View

        override fun bindView(itemView: View) {
            textViewRowAccountName = itemView.textViewRowAccountName
            textViewRowAccountNumber = itemView.textViewRowAccountNumber
            textViewRowAvailableBalance = itemView.textViewRowAvailableBalance
            shimmerLayoutRowAmount = itemView.shimmerLayoutRowAmount
            viewRowShimmer = itemView.viewRowShimmer
            viewRowError = itemView.viewRowError
            viewBorderTop = itemView.viewBorderTop
            this.itemView = itemView
        }
    }
}
