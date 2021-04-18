package com.unionbankph.corporate.payment_link.presentation.payment_link

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account;
import com.unionbankph.corporate.payment_link.domain.model.PaymentLinkModel
import io.supercharge.shimmerlayout.ShimmerLayout
import javax.inject.Inject



var mData = listOf<PaymentLinkModel>()
class PaymentLinkAdapter : RecyclerView.Adapter<PaymentLinkAdapter.PaymentLinkViewHolder> {

    constructor(paymentLinks: MutableList<PaymentLinkModel>) {
        mData = paymentLinks
    }

    var onItemClick: ((PaymentLinkModel) -> Unit)? = null


    inner class PaymentLinkViewHolder(view : View): RecyclerView.ViewHolder(view){



        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvReferenceNumber: TextView = view.findViewById(R.id.tvReferenceNumber)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)

        init {
            view.setOnClickListener {
                onItemClick?.invoke(mData[adapterPosition])
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentLinkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment_link_list, parent, false)

        return PaymentLinkViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentLinkViewHolder, position: Int) {
        val item = mData[position]
        holder.tvReferenceNumber.text = item.referenceNo
        holder.tvAmount.text = item.amount
        holder.tvDescription.text = item.expireDate

    }

    override fun getItemCount(): Int {
        return mData.size
    }
}

