package com.unionbankph.corporate.loan.applyloan

interface LoansAdapterCallback {

    fun onApplyNow()
    fun onReferenceCode()
    fun onKeyFeatures(features: String?)
}