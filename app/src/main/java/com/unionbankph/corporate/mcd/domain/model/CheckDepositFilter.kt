package com.unionbankph.corporate.mcd.domain.model

import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.common.data.model.StateData
import com.unionbankph.corporate.settings.presentation.form.Selector
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by herald25santos on 6/23/20
 */
@Serializable
data class CheckDepositFilter(
    @SerialName("check_number")
    var checkNumber: String? = null,
    @SerialName("amount")
    var amount: String? = null,
    @SerialName("check_start_date")
    var checkStartDate: String? = null,
    @SerialName("check_end_date")
    var checkEndDate: String? = null,
    @SerialName("deposit_account")
    var depositAccount: Account? = null,
    @SerialName("status")
    var status: String? = null,
    @SerialName("status_to_display")
    var statusToDisplay: String? = null,
    @SerialName("status_state")
    var statusesState: MutableList<StateData<Selector>>? = null,
    @SerialName("start_date_created")
    var startDateCreated: String? = null,
    @SerialName("end_date_created")
    var endDateCreated: String? = null,
    @SerialName("filter_count")
    var filterCount: Long = 0L
)