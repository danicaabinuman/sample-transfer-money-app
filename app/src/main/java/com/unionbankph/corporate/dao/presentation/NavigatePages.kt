package com.unionbankph.corporate.dao.presentation

/**
 * Created by herald on 9/7/20
 */
data class NavigatePages(
    var hasEditPersonalInformationOneInput: Boolean = false,
    var hasEditPersonalInformationTwoInput: Boolean = false,
    var hasEditPersonalInformationThreeInput: Boolean = false,
    var hasEditPersonalInformationFourInput: Boolean = false,
    var hasEditJumioVerificationInput: Boolean = false,
    var hasEditSignatureInput: Boolean = false,
    var hasEditConfirmationInput: Boolean = false
)