package com.unionbankph.corporate.account.presentation.account_selection

import android.content.Context
import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.*
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.presentation.constant.AccountBalanceTypeEnum
import com.unionbankph.corporate.app.common.extension.ACCOUNT_TYPE_ODA
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ErrorAccountFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoadingFooterModel_
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ItemAccountSelectionBinding
import io.supercharge.shimmerlayout.ShimmerLayout

class AccountSelectionController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil,
    private val autoFormatUtil: AutoFormatUtil
) : Typed2EpoxyController<MutableList<Account>, Pageable>() {

    private lateinit var accountAdapterCallback: AccountAdapterCallback

    @AutoModel
    lateinit var loadingFooterModel: LoadingFooterModel_

    @AutoModel
    lateinit var errorAccountFooterModel: ErrorAccountFooterModel_


    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(accounts: MutableList<Account>, pageable: Pageable) {
        accounts.forEachIndexed { position, account ->
            AccountSelectionItemModel_()
                .id(account.id?.toLong())
                .accountString(JsonHelper.toJson(account))
                .loadingAccount(account.isLoading)
                .selected(account.isSelected)
                .balanceViewable(account.isViewableBalance)
                .errorBool(account.isError)
                .position(position)
                .callbacks(accountAdapterCallback)
                .viewUtil(viewUtil)
                .context(context)
                .autoFormatUtil(autoFormatUtil)
                .addTo(this)
        }
        loadingFooterModel.loading(pageable.isLoadingPagination)
            .addIf(pageable.isLoadingPagination, this)
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

    interface AdapterCallbacks {
        fun onClickItem(account: String)
        fun onRequestAccountDetail(id: String, position: Int)
        fun onTapRetry(id: String, position: Int)
    }
}

@EpoxyModelClass(layout = R.layout.item_account_selection)
abstract class AccountSelectionItemModel :
    EpoxyModelWithHolder<AccountSelectionItemModel.Holder>() {

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
    var selected: Boolean = false

    @EpoxyAttribute
    lateinit var callbacks: AccountAdapterCallback

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.apply {
            val account = JsonHelper.fromJson<Account>(accountString)
            textViewCorporateName.text = viewUtil.getStringOrDefaultString(
                account.name,
                context.getString(R.string.title_account_name)
            )
            textViewAccountNumber.text = viewUtil.getStringOrEmpty(
                viewUtil.getAccountNumberFormat(account.accountNumber)
            )

            if (account.productType.equals(ACCOUNT_TYPE_ODA)) {
                val availableCredit = autoFormatUtil.getBalance(
                    AccountBalanceTypeEnum.AVAILABLE_CREDIT.value,
                    account.headers
                )
                textViewAvailableBalanceTitle.text = availableCredit?.display
                textViewAvailableBalance.text = availableCredit?.value
                cardViewAccount.setBackgroundResource(
                    if (selected)
                        R.drawable.bg_card_view_gradient_orange
                    else
                        R.drawable.bg_card_view_selected_gradient_orange
                )
            } else {
                val availableBalance = autoFormatUtil.getBalance(
                    AccountBalanceTypeEnum.AVAILABLE.value,
                    account.headers
                )
                textViewAvailableBalanceTitle.text = availableBalance?.display
                textViewAvailableBalance.text = availableBalance?.value
                cardViewAccount.setBackgroundResource(
                    if (selected)
                        R.drawable.bg_card_view_gradient_gray
                    else
                        R.drawable.bg_card_view_selected_gradient_gray
                )
            }

            if (loadingAccount) {
                shimmerLayoutAmount.post {
                    shimmerLayoutAmount.startShimmerAnimation()
                }
                viewShimmer.visibility = View.VISIBLE
                textViewAvailableBalance.visibility = View.INVISIBLE
                textViewAvailableBalance.text = context.formatString(R.string.value_default_zero_balance)
                if (viewError.root.visibility == View.VISIBLE) {
                    viewError.root.visibility = View.GONE
                }
            } else {
                shimmerLayoutAmount.post {
                    shimmerLayoutAmount.stopShimmerAnimation()
                }
                viewShimmer.visibility = View.GONE
                if (balanceViewable) {
                    textViewAvailableBalanceTitle.visibility = View.VISIBLE
                    textViewAvailableBalance.visibility = View.VISIBLE
                } else {
                    textViewAvailableBalanceTitle.visibility = View.INVISIBLE
                    textViewAvailableBalance.visibility = View.INVISIBLE
                }
            }

            cardViewAccount.setOnClickListener {
                callbacks.onClickItem(JsonHelper.toJson(account), position)
            }

            if (errorBool) {
                viewError.root.visibility = View.VISIBLE
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

        lateinit var binding: ItemAccountSelectionBinding

        override fun bindView(itemView: View) {
            binding = ItemAccountSelectionBinding.bind(itemView)
        }
    }
}
