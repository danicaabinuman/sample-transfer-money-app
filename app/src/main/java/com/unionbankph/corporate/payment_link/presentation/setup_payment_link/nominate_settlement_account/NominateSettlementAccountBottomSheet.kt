package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account

import android.os.Bundle
import com.unionbankph.corporate.app.base.BaseBottomSheetDialog
import com.unionbankph.corporate.R


import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback

class NominateSettlementAccountBottomSheet : BaseBottomSheetDialog<NominateSettlementViewModel>(R.layout.bottom_sheet_nominate_settlement_account),
    AccountAdapterCallback {



    companion object {

        const val RESULT_DATA = "RESULT_DATA"
        const val EXTRA_ACCOUNTS_ARRAY = "EXTRA_ACCOUNTS_ARRAY"

        fun newInstance(
            accountsArray: String? = null
        ): NominateSettlementAccountBottomSheet {
            val fragment =
                NominateSettlementAccountBottomSheet()
            val bundle = Bundle()
            bundle.putString(EXTRA_ACCOUNTS_ARRAY, accountsArray)
            fragment.arguments = bundle
            return fragment
        }
    }
}