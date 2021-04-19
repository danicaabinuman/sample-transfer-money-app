package com.unionbankph.corporate.payment_link.presentation.payment_link

import android.graphics.Color
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
import kotlinx.android.synthetic.main.activity_link_details.*
import java.text.SimpleDateFormat
import javax.inject.Inject
import kotlin.coroutines.coroutineContext


var mData = listOf<PaymentLinkModel>()
class PaymentLinkAdapter : RecyclerView.Adapter<PaymentLinkAdapter.PaymentLinkViewHolder> {

    constructor(paymentLinks: MutableList<PaymentLinkModel>) {
        mData = paymentLinks
    }

    var onItemClick: ((PaymentLinkModel) -> Unit)? = null


    inner class PaymentLinkViewHolder(view : View): RecyclerView.ViewHolder(view){



        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvStatusArchived: TextView = view.findViewById(R.id.tvStatusArchived)
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

        val parser = SimpleDateFormat("yyyy-MM-dd:HH:mm:ss.SSS")
        val formatter = SimpleDateFormat("MMM dd, yyyy hh:mm:aa  ")

        val expiryDate = formatter.format(parser.parse(item.expireDate))
        holder.tvDateTime.text = expiryDate


        holder.tvReferenceNumber.text = item.referenceNo
        holder.tvAmount.text = item.amount
        holder.tvDescription.text = item.paymentFor

        holder.tvStatus.text = item.status
        if(item.status.equals("ARCHIVED",true)){

            holder.tvStatusArchived.visibility = View.VISIBLE
            holder.tvStatus.visibility = View.GONE
        }else if(false){

        }else{
            holder.tvStatusArchived.visibility = View.GONE
            holder.tvStatus.visibility = View.VISIBLE
        }

    }

    override fun getItemCount(): Int {
        return mData.size
    }
}

