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
import kotlinx.android.synthetic.main.item_account_error.view.*

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

        holder.textViewErrorMessage.text = when (errorMessage.isNotEmpty()) {
            true -> errorMessage
            else -> holder.textViewErrorMessage.context.getString(R.string.error_something_went_wrong)
        }

        holder.cardViewTapToRetry.setOnClickListener {
            callbacks.onTapToRetry()
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var cardViewTapToRetry : CardView
        lateinit var textViewErrorMessage : TextView

        override fun bindView(itemView: View) {
            cardViewTapToRetry = itemView.cardViewTapToRetry
            textViewErrorMessage = itemView.textViewErrorMessage
        }
    }
}