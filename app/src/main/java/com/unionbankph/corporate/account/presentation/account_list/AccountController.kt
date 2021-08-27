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
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.AccountItemModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ErrorAccountFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoadingFooterModel_
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ItemAccountBinding
import com.unionbankph.corporate.databinding.RowAccountBinding
import io.supercharge.shimmerlayout.ShimmerLayout

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
                    callbacks(this@AccountsController.accountAdapterCallback)
                    viewUtil(this@AccountsController.viewUtil)
                    context(this@AccountsController.context)
                    autoFormatUtil(this@AccountsController.autoFormatUtil)
                }
            } else {
                AccountItemModel_()
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

        holder.binding.apply {
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

            root.setOnClickListener {
                if (account.permissionCollection.hasAllowToBTRViewTransaction &&
                    account.permissionCollection.hasAllowToBTRViewBalance &&
                    !errorBool
                ) {
                    callbacks.onClickItem(accountString, position)
                }
            }

            if (loadingAccount) {
                //callbacks.onRequestAccountDetail(account.id.toString(), position)
                shimmerLayoutRowAmount.post {
                    shimmerLayoutRowAmount.startShimmerAnimation()
                }
                viewRowShimmer.visibility = View.VISIBLE
                textViewRowAvailableBalance.visibility = View.INVISIBLE
                textViewRowAvailableBalance.text =
                    context.formatString(R.string.value_default_zero_balance)
                if (viewRowError.root.visibility == View.VISIBLE) {
                    viewRowError.root.visibility = View.GONE
                }
            } else {
                shimmerLayoutRowAmount.post {
                    shimmerLayoutRowAmount.stopShimmerAnimation()
                }
                viewRowShimmer.visibility = View.GONE
                if (account.permissionCollection.hasAllowToBTRViewBalance) {
                    textViewRowAvailableBalance.visibility = View.VISIBLE
                } else {
                    textViewRowAvailableBalance.visibility = View.INVISIBLE
                }
            }
            if (errorBool) {
                viewRowError.root.visibility = View.VISIBLE
                viewRowError.root.setOnClickListener {
                    callbacks.onTapErrorRetry(account.id.toString(), position)
                }
            } else {
                viewRowError.root.visibility = View.GONE
            }
        }
    }

    override fun onViewAttachedToWindow(holder: Holder) {
        if (loadingAccount) {
            holder.binding.shimmerLayoutRowAmount.post {
                holder.binding.shimmerLayoutRowAmount.startShimmerAnimation()
            }
        } else {
            holder.binding.shimmerLayoutRowAmount.post {
                holder.binding.shimmerLayoutRowAmount.stopShimmerAnimation()
            }
        }
        super.onViewAttachedToWindow(holder)
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: RowAccountBinding

        override fun bindView(itemView: View) {
            binding = RowAccountBinding.bind(itemView)
        }
    }
}
