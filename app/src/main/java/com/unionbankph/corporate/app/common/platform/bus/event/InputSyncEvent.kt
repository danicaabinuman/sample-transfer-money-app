package com.unionbankph.corporate.app.common.platform.bus.event

import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEventBus

class InputSyncEvent : BaseEventBus<String>() {

    companion object {
        const val ACTION_INPUT_BANK_RECEIVER = "input_bank_receiver"
        const val ACTION_INPUT_BENEFICIARY = "input_beneficiary"
        const val ACTION_INPUT_FREQUENT_BILLER = "input_frequent_biller"
        const val ACTION_INPUT_BILLER = "input_biller"
        const val ACTION_INPUT_SOURCE_ACCOUNTS = "input_source_accounts"
        const val ACTION_SELECT_TRANSFER_FILTER = "select_transfer_filter"
        const val ACTION_SELECT_BENEFICIARY_FILTER = "select_beneficiary_filter"
        const val ACTION_INPUT_PAYMENT_CHANNEL = "input_payment_channel"
        const val ACTION_INPUT_OWN_ACCOUNT = "input_own_account"
        const val ACTION_INPUT_COUNTRY = "input_country"
        const val ACTION_INPUT_BRANCH = "input_branch"
        const val ACTION_ADD_BRANCH_TRANSACTION = "add_branch_transaction"
        const val ACTION_EDIT_BRANCH_TRANSACTION = "edit_branch_transaction"
        const val ACTION_INPUT_FILTER_CHANNEL = "input_filter_channel"
        const val ACTION_INPUT_FILTER_STATUS = "input_filter_status"
    }
}
