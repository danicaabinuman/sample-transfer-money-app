package com.unionbankph.corporate.payment_link.presentation.billing_details

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.payment_link.domain.model.PaymentLogsModel
import timber.log.Timber
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class PaymentHistoryAdapter : RecyclerView.Adapter<PaymentHistoryAdapter.PaymentHistoryViewHolder> {

    var mData = mutableListOf<PaymentLogsModel>()
    var mContext : Context? = null

            constructor(context: Context){
                mContext = context
                mData = mutableListOf()
            }

    var onItemClick: ((PaymentLogsModel) -> Unit)? = null

    fun clearData(){
        mData = mutableListOf()
        notifyDataSetChanged()
    }

    fun appendData(data : List<PaymentLogsModel>){
        mData.addAll(data)
        notifyDataSetChanged()
    }

    inner class PaymentHistoryViewHolder(view : View): RecyclerView.ViewHolder(view) {

        val tvPaymentType: TextView = view.findViewById(R.id.tv_payment_type)
        val tvTransactionId: TextView = view.findViewById(R.id.tv_transaction_id)
        val tvStatus: TextView = view.findViewById(R.id.tv_payment_history_status)
        val tvDate: TextView = view.findViewById(R.id.tv_payment_history_date)

        init {
            view.setOnClickListener {
//                onItemClick?.invoke(mData(adapterPosition))
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentHistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment_history, parent, false)

        return PaymentHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentHistoryViewHolder, position: Int) {
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
                Timber.e("Date Error on Payment Logs")
            }
        }
        holder.tvDate.text = dateCreated
        holder.tvStatus.text = item.status
        holder.tvPaymentType.text = item.paymentMethodName
        holder.tvTransactionId.text = item.transactionId
    }

    override fun getItemCount(): Int {
        return mData.size
    }
}