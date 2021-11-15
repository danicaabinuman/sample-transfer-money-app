package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel

import android.content.Context
import android.view.View
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
import com.unionbankph.corporate.databinding.ItemDashboardAccountBinding

@EpoxyModelClass
abstract class DashboardAccountItemModel :
    EpoxyModelWithHolder<DashboardAccountItemModel.Holder>() {

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

        holder.binding.apply {
            textViewCorporateName.text = viewUtil.getStringOrDefaultString(
                account.name,
                context.getString(R.string.title_account_name)
            )
            textViewAccountNumber.text = viewUtil.getStringOrEmpty(
                viewUtil.getAccountNumberFormat(account.accountNumber)
            )
            textViewProductType.text = viewUtil.getStringOrEmpty(account.productCodeDesc)

            if (account.productType.equals(ACCOUNT_TYPE_ODA)) {
                val availableCredit = autoFormatUtil.getBalance(
                    AccountBalanceTypeEnum.AVAILABLE_CREDIT.value,
                    account.headers
                )
                textViewAvailableBalanceTitle.text = availableCredit?.display
                textViewAvailableBalance.text = availableCredit?.value ?: Constant.EMPTY
                cardViewAccount.setBackgroundResource(R.drawable.bg_card_view_gradient_orange)
            } else {
                val availableBalance = autoFormatUtil.getBalance(
                    AccountBalanceTypeEnum.AVAILABLE.value,
                    account.headers
                )
                textViewAvailableBalanceTitle.text = availableBalance?.display
                textViewAvailableBalance.text = availableBalance?.value ?: Constant.EMPTY
                cardViewAccount.setBackgroundResource(R.drawable.bg_gradient_orange_radius8)
            }

            cardViewAccount.setOnClickListener {
                if (account.permissionCollection.hasAllowToBTRViewTransaction &&
                    account.permissionCollection.hasAllowToBTRViewBalance &&
                    !errorBool
                ) {
                    callbacks.onClickItem(accountString, position)
                }
            }

            if (loadingAccount) {
                //callbacks.onRequestAccountDetail(account.id.toString(), position)
                shimmerLayoutAmount.post {
                    shimmerLayoutAmount.startShimmerAnimation()
                }
                viewShimmer.visibility = View.VISIBLE
                textViewAvailableBalanceTitle.visibility = View.VISIBLE
                textViewAvailableBalance.visibility = View.INVISIBLE
                textViewAvailableBalance.text =
                    context.formatString(R.string.value_default_zero_balance)
                if (viewError.root.visibility == View.VISIBLE) {
                    viewError.root.visibility = View.GONE
                }
            } else {
                shimmerLayoutAmount.post {
                    shimmerLayoutAmount.stopShimmerAnimation()
                }
                viewShimmer.visibility = View.GONE
                if (account.permissionCollection.hasAllowToBTRViewBalance) {
                    textViewAvailableBalanceTitle.visibility = View.VISIBLE
                    textViewAvailableBalance.visibility = View.VISIBLE
                } else {
                    textViewAvailableBalanceTitle.visibility = View.INVISIBLE
                    textViewAvailableBalance.visibility = View.INVISIBLE
                }
            }
            if (errorBool) {
                viewError.root.visibility = View.VISIBLE
                textViewAvailableBalanceTitle.visibility = View.INVISIBLE
                textViewAvailableBalance.visibility = View.INVISIBLE
                viewError.root.setOnClickListener {
                    callbacks.onTapErrorRetry(account.id.toString(), position)
                }
            } else {
                viewError.root.visibility = View.GONE
            }
        }
    }

    override fun onViewAttachedToWindow(holder: Holder) {
        if (loadingAccount) {
            holder.binding.shimmerLayoutAmount.post {
                holder.binding.shimmerLayoutAmount.startShimmerAnimation()
            }
        } else {
            holder.binding.shimmerLayoutAmount.post {
                holder.binding.shimmerLayoutAmount.stopShimmerAnimation()
            }
        }
        super.onViewAttachedToWindow(holder)
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: ItemDashboardAccountBinding

        override fun bindView(itemView: View) {
            binding = ItemDashboardAccountBinding.bind(itemView)
        }
    }
}