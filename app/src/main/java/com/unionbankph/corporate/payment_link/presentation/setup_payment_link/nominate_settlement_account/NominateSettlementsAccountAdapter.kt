package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account;
import io.supercharge.shimmerlayout.ShimmerLayout
import javax.inject.Inject



var mData = listOf<Account>()
class NominateSettlementAccountsAdapter : RecyclerView.Adapter<NominateSettlementAccountsAdapter.NominateSettlementAccountsViewHolder> {

    constructor(accounts: MutableList<Account>) {
        mData = accounts
    }

    var onItemClick: ((Account) -> Unit)? = null


    inner class NominateSettlementAccountsViewHolder(view : View): RecyclerView.ViewHolder(view){



        val tvCorporateName: AppCompatTextView = view.findViewById(R.id.textViewCorporateName)
        val tvAccountName: AppCompatTextView = view.findViewById(R.id.textViewAccountName)
        val tvAccountNumber: AppCompatTextView = view.findViewById(R.id.textViewAccountNumber)
        val tvAvailableBalance: AppCompatTextView = view.findViewById(R.id.textViewAvailableBalance)
        val slAmount: ShimmerLayout = view.findViewById(R.id.shimmerLayoutAmount)
        val viewShimmer: View = view.findViewById(R.id.viewShimmer)

        init {
            view.setOnClickListener {
                onItemClick?.invoke(mData[adapterPosition])
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NominateSettlementAccountsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_nominate_account, parent, false)

        return NominateSettlementAccountsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NominateSettlementAccountsViewHolder, position: Int) {
        val item = mData[position]
        holder.tvAccountNumber.text = item.accountNumber
        holder.tvCorporateName.text = item.name
        holder.tvAccountName.text = item.productCodeDesc

        item.headers.forEach{ header ->
            header.name?.let { headerName ->
                if(headerName.equals("CURBAL",true)){
                    header.value?.let{ headerValue ->
                        holder.slAmount.stopShimmerAnimation()
                        holder.viewShimmer.visibility = View.GONE
                        holder.tvAvailableBalance.visibility = View.VISIBLE
                        holder.tvAvailableBalance.text = headerValue
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }
}

