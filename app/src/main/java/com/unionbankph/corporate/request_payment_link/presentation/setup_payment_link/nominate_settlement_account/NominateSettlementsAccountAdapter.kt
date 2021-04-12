package com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link.nominate_settlement_account

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account;

var data = listOf<Account>()
class NominateSettlementAccountsAdapter(data: MutableList<Account>): RecyclerView.Adapter<NominateSettlementAccountsAdapter.NominateSettlementAccountsViewHolder>(){

    class NominateSettlementAccountsViewHolder(view : View): RecyclerView.ViewHolder(view){
        val tvCorporateName: AppCompatTextView = view.findViewById(R.id.textViewCorporateName)
        val tvAccountName: AppCompatTextView = view.findViewById(R.id.textViewAccountName)
        val tvAccountNumber: AppCompatTextView = view.findViewById(R.id.textViewAccountNumber)
        val tvAvailableBalance: AppCompatTextView = view.findViewById(R.id.textViewAvailableBalance)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NominateSettlementAccountsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_nominate_account, parent, false)

        return NominateSettlementAccountsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NominateSettlementAccountsViewHolder, position: Int) {
        val item = data[position]
        holder.tvAccountNumber.text = item.accountNumber
        holder.tvCorporateName.text = item.name
        holder.tvAccountName.text = item.productCodeDesc
    }

    override fun getItemCount(): Int {
        return data.size
    }
}

