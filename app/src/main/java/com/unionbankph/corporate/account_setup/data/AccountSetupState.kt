package com.unionbankph.corporate.account_setup.data

import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.GenericMenuItem
import kotlinx.serialization.Serializable

@Serializable
data class AccountSetupState(
    var businessType: Int? = null,
    var businessAccountType: Int? = null,
    var debitCardType: Int? = null,

    var hasPersonalInfoInput: Boolean = false,
    var personalInfoInput: PersonalInfoInput? = null
)

@Serializable
data class ToolbarState(
    var isButtonShow: Boolean? = null,
    var buttonType: Int? = null,
    var backButtonType: Int? = null
)

@Serializable
data class DebitCardState(
    var lastCardSelected: Int? = 0,
    var cards: MutableList<GenericMenuItem> = mutableListOf()
)
