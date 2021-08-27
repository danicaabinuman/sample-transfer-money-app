package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback
import com.unionbankph.corporate.databinding.ItemAccountErrorBinding

@EpoxyModelClass
abstract class AccountItemErrorModel : EpoxyModelWithHolder<AccountItemErrorModel.Holder>() {

    @EpoxyAttribute
    lateinit var errorMessage: String

    @EpoxyAttribute
    lateinit var callbacks: AccountAdapterCallback

    override fun getDefaultLayout(): Int {
        return R.layout.item_account_error
    }

    override fun bind(holder: Holder) {
        super.bind(holder)

        holder.binding.textViewErrorMessage.text = when (errorMessage.isNotEmpty()) {
            true -> errorMessage
            else -> holder.binding.textViewErrorMessage.context.getString(R.string.error_something_went_wrong)
        }

        holder.binding.cardViewTapToRetry.setOnClickListener {
            callbacks.onTapToRetry()
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: ItemAccountErrorBinding

        override fun bindView(itemView: View) {
            binding = ItemAccountErrorBinding.bind(itemView)
        }
    }
}