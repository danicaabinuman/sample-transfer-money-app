package com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link.nominate_settlement_account

import android.accounts.Account
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unionbankph.corporate.R
import kotlinx.android.synthetic.main.activity_nominate_settlement.*

class NominateSettlementActivity : AppCompatActivity() {

    private lateinit var linearLayoutManager: LinearLayoutManager




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nominate_settlement)

        linearLayoutManager = LinearLayoutManager(this)
//        rvNominateAccounts.layoutManager = linearLayoutManager

        include1.findViewById<TextView>(R.id.textViewCorporateName).text = "UPASS CA001 TEST ACCOUNT"
        include1.findViewById<TextView>(R.id.textViewAccountNumber).text = "0005 9008 0118"
        include1.findViewById<TextView>(R.id.textViewAccountName).text = "RETAIL REGULAR CHECKING"
        include1.findViewById<TextView>(R.id.textViewAccountNumber).text = "PHP 338,989,378.27"

        include2.findViewById<TextView>(R.id.textViewCorporateName).text = "ABC SAMPLE ORGANIZATION"
        include2.findViewById<TextView>(R.id.textViewAccountNumber).text = "0005 9008 0120"
        include2.findViewById<TextView>(R.id.textViewAccountName).text = "RETAIL REGULAR CHECKING"
        include2.findViewById<TextView>(R.id.textViewAccountNumber).text = "PHP 21,142,216.21"

        include3.findViewById<TextView>(R.id.textViewCorporateName).text = "UPASS TEST ACCOUNT 5"
        include3.findViewById<TextView>(R.id.textViewAccountNumber).text = "1021 1002 4433"
        include3.findViewById<TextView>(R.id.textViewAccountName).text = "RETAIL REG.SAVINGS ACCT"
        include3.findViewById<TextView>(R.id.textViewAccountNumber).text = "PHP 1,415,166.80"

        include1.setOnClickListener{finish()}
        include2.setOnClickListener{finish()}
        include3.setOnClickListener{finish()}

    }


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