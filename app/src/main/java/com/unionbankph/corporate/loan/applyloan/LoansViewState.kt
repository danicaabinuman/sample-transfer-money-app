package com.unionbankph.corporate.loan.applyloan

data class LoansViewState(
    var isScreenRefreshed: Boolean = true,
    var hasInitialFetchError: Boolean = true,
    var commonQuestionEligible: Boolean = true,
    var commonQuestionRequirement: Boolean = true,
    var commonQuestionBusiness: Boolean = true,
    var errorMessage: String? = null,
    var commonQuestions: List<CommonQuestions>
)
