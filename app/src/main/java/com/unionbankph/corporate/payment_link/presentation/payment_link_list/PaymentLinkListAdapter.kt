package com.unionbankph.corporate.payment_link.presentation.payment_link_list

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.payment_link.domain.model.PaymentLinkModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*



class PaymentLinkListAdapter : RecyclerView.Adapter<PaymentLinkListAdapter.PaymentLinkViewHolder> {

    var mData = mutableListOf<PaymentLinkModel>()
    var mContext : Context? = null

    constructor(context: Context) {
        mContext = context
        mData = mutableListOf()
    }

    var onItemClick: ((PaymentLinkModel) -> Unit)? = null

    fun appendData(data : List<PaymentLinkModel>){
        mData.addAll(data)
        notifyDataSetChanged()
    }

    inner class PaymentLinkViewHolder(view : View): RecyclerView.ViewHolder(view){



        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
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

        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val formatter = SimpleDateFormat("MMM dd, yyyy, hh:mm aa", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getDefault()

        var dateCreated = "UNAVAILABLE"
        item.createdDate?.let {
            dateCreated = it
            try {
                dateCreated = formatter.format(parser.parse(item.createdDate))
            } catch (e: Exception){
                Timber.e(e.toString()) // this never gets called either
            }
        }
        holder.tvDateTime.text = dateCreated


        holder.tvReferenceNumber.text = item.referenceNo
        holder.tvAmount.text = item.amount
        holder.tvDescription.text = item.paymentFor

        holder.tvStatus.text = item.status

        if(item.status?.contains("ARCHIVE",true) == true){

            holder.tvStatus.background = mContext?.getDrawable(R.drawable.bg_archived)
            holder.tvStatus.setTextColor(Color.parseColor("#4A4A4A"))

        }else if(item.status?.equals("PAID",true) == true){

            holder.tvStatus.background = mContext?.getDrawable(R.drawable.bg_paid)
            holder.tvStatus.setTextColor(Color.parseColor("#5CA500"))

        }else if(item.status?.contains("EXPIRE",true) == true){

            holder.tvStatus.background = mContext?.getDrawable(R.drawable.bg_expired)
            holder.tvStatus.setTextColor(Color.parseColor("#E83C18"))

        }else if(item.status?.contains("PENDING",true) == true){

            holder.tvStatus.background = mContext?.getDrawable(R.drawable.bg_pending)
            holder.tvStatus.setTextColor(Color.parseColor("#FF8200"))

        }else if(item.status?.equals("UNPAID",true) == true){

            holder.tvStatus.background = mContext?.getDrawable(R.drawable.bg_unpaid)
            holder.tvStatus.setTextColor(Color.parseColor("#F6B000"))

        }

    }

    override fun getItemCount(): Int {
        return mData.size
    }
}