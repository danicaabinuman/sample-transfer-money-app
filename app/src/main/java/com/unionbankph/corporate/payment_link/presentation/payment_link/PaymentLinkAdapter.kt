package com.unionbankph.corporate.payment_link.presentation.payment_link

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.payment_link.domain.model.PaymentLinkModel
import java.text.SimpleDateFormat


var mData = listOf<PaymentLinkModel>()
class PaymentLinkAdapter : RecyclerView.Adapter<PaymentLinkAdapter.PaymentLinkViewHolder> {

    constructor(paymentLinks: MutableList<PaymentLinkModel>) {
        mData = paymentLinks
    }

    var onItemClick: ((PaymentLinkModel) -> Unit)? = null


    inner class PaymentLinkViewHolder(view : View): RecyclerView.ViewHolder(view){



        val tvStatusUnpaid: TextView = view.findViewById(R.id.tvStatusUnpaid)
        val tvStatusArchived: TextView = view.findViewById(R.id.tvStatusArchived)
        val tvStatusPaid: TextView = view.findViewById(R.id.tvStatusPaid)
        val tvStatusExpired: TextView = view.findViewById(R.id.tvStatusExpired)
        val tvReferenceNumber: TextView = view.findViewById(R.id.tvReferenceNumber)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvDateTime: TextView = view.findViewById(R.id.tvDateTime)

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

        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val formatter = SimpleDateFormat("MMM dd, yyyy hh:mm:aa")

        var expiryDate = formatter.format(parser.parse(item.expireDate))

        holder.tvDateTime.text = expiryDate


        holder.tvReferenceNumber.text = item.referenceNo
        holder.tvAmount.text = item.amount
        holder.tvDescription.text = item.paymentFor

        holder.tvStatusUnpaid.text = item.status
        if(item.status.equals("ARCHIVED",true)){

            holder.tvStatusArchived.visibility = View.VISIBLE
            holder.tvStatusUnpaid.visibility = View.GONE
            holder.tvStatusExpired.visibility = View.GONE
            holder.tvStatusPaid.visibility = View.GONE

        }else if(item.status.equals("PAID", true)){
            holder.tvStatusArchived.visibility = View.GONE
            holder.tvStatusUnpaid.visibility = View.GONE
            holder.tvStatusExpired.visibility = View.GONE
            holder.tvStatusPaid.visibility = View.VISIBLE
        }else if (item.status.equals("EXPIRED", true)){
            holder.tvStatusArchived.visibility = View.GONE
            holder.tvStatusUnpaid.visibility = View.GONE
            holder.tvStatusExpired.visibility = View.VISIBLE
            holder.tvStatusPaid.visibility = View.GONE
        }else{
            holder.tvStatusArchived.visibility = View.GONE
            holder.tvStatusUnpaid.visibility = View.VISIBLE
            holder.tvStatusExpired.visibility = View.GONE
            holder.tvStatusPaid.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
        return mData.size
    }
}