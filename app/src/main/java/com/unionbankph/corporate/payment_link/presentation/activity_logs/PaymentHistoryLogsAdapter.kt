package com.unionbankph.corporate.payment_link.presentation.activity_logs

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.convertDateToDesireFormat
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.payment_link.domain.model.PaymentLogsModel
import timber.log.Timber
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class PaymentHistoryLogsAdapter(
    context: Context,
    adapterType: String
): RecyclerView.Adapter<PaymentHistoryLogsAdapter.PaymentHistoryViewHolder>() {

    private var mData = mutableListOf<PaymentLogsModel>()
    private var mContext : Context? = context

    private var adapterType = adapterType

    fun appendData(data : List<PaymentLogsModel>){
        mData.addAll(data)

        // to delete
//        if (adapterType != BILLING_ADAPTER) {
//            appendDummyData()
//        }

        notifyDataSetChanged()
    }

    //FOR MOCK DATA

//    private fun appendDummyData(){
//        val templist = mutableListOf<PaymentLogsModel>()
//        templist.add(
//            PaymentLogsModel().apply {
//                this.logId = 262
//                this.paymentMethodName = "Created by Roger Mango"
//                this.modifiedDate = "2021-09-21T05:55:11Z"
//                this.status = "FAILED"
//                this.transactionId = "2109210143550"
//            }
//        )

//        templist.add(
//            PaymentLogsModel().apply {
//                this.logId = 263
//                this.paymentMethodName = "UB Online"
//                this.modifiedDate = "2021-09-23T07:30:11Z"
//                this.status = "PAID"
//                this.transactionId = "XASDS@#@#"
//            }
//        )
//        templist.add(
//            PaymentLogsModel().apply {
//                this.logId = 263
//                this.paymentMethodName = "Online Sabong"
//                this.modifiedDate = "2021-09-30T06:55:11Z"
//            }
//        )
//
//        mData.addAll(templist)
//    }

    inner class PaymentHistoryViewHolder(view : View): RecyclerView.ViewHolder(view) {

        val tvPaymentType: TextView = view.findViewById(R.id.tv_payment_type)
        val tvTransactionId: TextView = view.findViewById(R.id.tv_transaction_id)
        val tvStatus: TextView = view.findViewById(R.id.tv_payment_history_status)
        val tvDate: TextView = view.findViewById(R.id.tv_payment_history_date)
        val tvDateLog: TextView = view.findViewById(R.id.tv_log_date)
        val viewBorder: View = view.findViewById(R.id.viewBorder)

        init {
            view.setOnClickListener {
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentHistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment_history_with_date, parent, false)
        return PaymentHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentHistoryViewHolder, position: Int) {
        val item = mData[position]

        holder.tvPaymentType.text = item.paymentMethodName

        when (adapterType) {
            LOGS_ADAPTER ->{
                holder.viewBorder.visibility = View.GONE
            }

            BILLING_ADAPTER -> {
                if (position == (mData.size - 1)){
                    holder.viewBorder.visibility = View.GONE
                }
            }
        }

        item.status?.let { status ->
            holder.tvStatus.visibility = View.VISIBLE
            holder.tvStatus.text = status
        }

        item.transactionId?.let { transactionId ->
            holder.tvTransactionId.visibility = View.VISIBLE
            holder.tvTransactionId.text = transactionId
        }

        dateGroup(holder, item, position)
    }

    @SuppressLint("SetTextI18n")
    private fun dateGroup(holder : PaymentHistoryViewHolder, item: PaymentLogsModel, pos: Int) {
        val formattedThreeDate = item.modifiedDate
            .convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_THREE_DATE)

        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val formatter = SimpleDateFormat("hh:mm aa", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getDefault()


        var formattedTime = "UNAVAILABLE"

        item.modifiedDate?.let {
            formattedTime = it
            try {
                formattedTime = formatter.format(parser.parse(item.modifiedDate))
            } catch (e: Exception){
                Timber.e("Date Error on Payment Logs")
            }
        }

        val formattedFullDate = item.modifiedDate
            .convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE)

        Timber.e("Date group " + formattedTime )

        holder.tvDate.text = "$formattedThreeDate at $formattedTime"
        holder.tvDateLog.text = formattedFullDate

        if (adapterType == BILLING_ADAPTER) {
            holder.tvDateLog.visibility = View.GONE
            return
        }

        if (pos == 0){
            holder.tvDateLog.visibility = View.VISIBLE
        } else {
            val previousItemFullDate = mData[pos - 1].createdDate
                .convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE)
            if (formattedFullDate == previousItemFullDate) {
                holder.tvDateLog.visibility = View.GONE
            } else {
                holder.tvDateLog.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    companion object {
        const val BILLING_ADAPTER = "billing"
        const val LOGS_ADAPTER = "logs"
    }
}