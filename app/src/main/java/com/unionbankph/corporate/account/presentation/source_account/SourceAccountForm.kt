package com.unionbankph.corporate.account.presentation.source_account

import android.os.Parcelable
import com.unionbankph.corporate.account.data.model.Account
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald on 11/26/20
 */
@Parcelize
@Serializable
data class SourceAccountForm(
    @SerialName("all_accounts_selected")
    var allAccountsSelected: Boolean = true,
    @SerialName("total_selected")
    var totalSelected: Int = 0,
    @SerialName("excluded_account_ids")
    var excludedAccountIds: MutableList<Int> = mutableListOf(),
    @SerialName("selected_accounts")
    var selectedAccounts: MutableList<Account> = mutableListOf()
) : Parcelable