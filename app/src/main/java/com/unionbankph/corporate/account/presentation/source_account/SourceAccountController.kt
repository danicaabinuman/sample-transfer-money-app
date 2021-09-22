package com.unionbankph.corporate.account.presentation.source_account

import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.*
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.setContextCompatBackgroundColor
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ErrorAccountFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoadingFooterModel_
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ItemSourceAccountBinding

class SourceAccountController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil
) : Typed2EpoxyController<MutableList<Account>, Pageable>() {

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

    override fun buildModels(accounts: MutableList<Account>, pageable: Pageable) {
        accounts.forEachIndexed { index, account ->
            sourceAccountItem {
                id(account.id)
                position(index)
                accountString(JsonHelper.toJson(account))
                hasSelected(account.isSelected)
                callbacks(this@SourceAccountController.accountAdapterCallback)
                viewUtil(this@SourceAccountController.viewUtil)
                context(this@SourceAccountController.context)
            }
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

    fun setAccountAdapterCallback(accountAdapterCallback: AccountAdapterCallback) {
        this.accountAdapterCallback = accountAdapterCallback
    }
}

@EpoxyModelClass(layout = R.layout.item_source_account)
abstract class SourceAccountItemModel : EpoxyModelWithHolder<SourceAccountItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var accountString: String

    @EpoxyAttribute
    lateinit var callbacks: AccountAdapterCallback

    @EpoxyAttribute
    var hasSelected: Boolean = false

    @EpoxyAttribute
    var position: Int = 0

    val account by lazyFast { JsonHelper.fromJson<Account>(accountString) }

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.apply {
            account.isSelected = hasSelected
            if (account.isViewableCheckBox) {
                checkBoxSourceAccount.isChecked = hasSelected
                checkBoxSourceAccount.visibility = View.VISIBLE
            } else {
                checkBoxSourceAccount.isChecked = false
                checkBoxSourceAccount.visibility = View.GONE
            }
            textViewAccountName.text = account.name
            textViewAccountNumber.text = viewUtil.getAccountNumberFormat(account.accountNumber)
            constraintLayoutSourceAccount.setContextCompatBackgroundColor(
                if (hasSelected) {
                    R.color.colorOrangeSelectedSourceAccount
                } else {
                    R.color.colorTransparent
                }
            )
            if (account.isViewableCheckBox) {
                constraintLayoutSourceAccount.setOnClickListener {
                    callbacks.onClickItem(JsonHelper.toJson(account), position)
                }
            }
            checkBoxSourceAccount.setOnClickListener {
                callbacks.onClickItem(JsonHelper.toJson(account), position)
            }
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemSourceAccountBinding

        override fun bindView(itemView: View) {
            binding = ItemSourceAccountBinding.bind(itemView)
        }
    }
}
