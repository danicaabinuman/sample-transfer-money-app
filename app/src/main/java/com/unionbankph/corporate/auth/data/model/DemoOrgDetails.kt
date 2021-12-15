package com.unionbankph.corporate.auth.data.model

import kotlinx.serialization.SerialName

data class DemoOrgDetails(

    @SerialName("trial_days_remaining")
    var trialDaysRemaining: Int? = null,

    @SerialName("trial_mode")
    var trialMode: Boolean? = null
)
