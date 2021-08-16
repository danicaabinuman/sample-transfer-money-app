package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.presentation.constant.AccountBalanceTypeEnum
import com.unionbankph.corporate.app.common.extension.ACCOUNT_TYPE_ODA
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import io.supercharge.shimmerlayout.ShimmerLayout
import kotlinx.android.synthetic.main.item_account.view.*

@EpoxyModelClass
abstract class DashboardAccountItemModel : EpoxyModelWithHolder<DashboardAccountItemModel.Holder>() {

    override fun getDefaultLayout(): Int {
        return R.layout.item_dashboard_account
    }

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