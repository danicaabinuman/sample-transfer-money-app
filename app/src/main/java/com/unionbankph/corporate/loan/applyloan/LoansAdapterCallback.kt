package com.unionbankph.corporate.loan.applyloan

interface LoansAdapterCallback {

    fun onApplyNow()
    fun onSeeFullList()
    fun onReferenceCode()
    fun onKeyFeatures(features: String?)
    fun onCommonQuestions(item: CommonQuestions)
    fun onCommonQuestionsEligible(expand: Boolean?)
    fun onCommonQuestionRequirements(expand: Boolean?)
    fun oncCommonQuestionBusiness(expand: Boolean?)
}