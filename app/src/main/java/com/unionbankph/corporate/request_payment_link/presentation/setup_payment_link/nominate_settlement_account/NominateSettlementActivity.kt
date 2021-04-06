package com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link.nominate_settlement_account

import android.accounts.Account
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity

class NominateSettlementActivity : BaseActivity<NominateSettlementViewModel>(R.layout.activity_nominate_settlement) {

//    private lateinit var linearLayoutManager: LinearLayoutManager




//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_nominate_settlement)
//
//        linearLayoutManager = LinearLayoutManager(this)
////        rvNominateAccounts.layoutManager = linearLayoutManager
//
//    }


    companion object {
        const val EXTRA_ACCOUNTS = "extra_accounts"
    }

    class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.AccountHolder>()  {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecyclerAdapter.AccountHolder {
            TODO("Not yet implemented")
        }

        override fun onBindViewHolder(holder: RecyclerAdapter.AccountHolder, position: Int) {
            TODO("Not implemented")
        }

        override fun getItemCount(): Int {
            TODO("Not yet implemented")
        }


        //1
        class AccountHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
            //2
            private var view: View = v
            private var account: Account? = null

            //3
            init {
                v.setOnClickListener(this)
            }

            //4
            override fun onClick(v: View) {
                Log.d("RecyclerView", "CLICK!")
            }

            companion object {
                //5
                private val PHOTO_KEY = "PHOTO"
            }
        }

    }


}