package com.unionbankph.corporate.payment_link.presentation.request_payment.fee_calculator

data class FeeCalculatorValueModel(
    var fee: Double = 0.00,
    var percentageFee: Double = 0.00

){
    var ubOnlineFee: Double = 0.00
    var instapay: Double = 15.00
    var card: Double = 15.00
    var eWallet: Double = 10.00
    var otc: Double = 20.00
    var cardPercentageFee: Double = 3.50
    var eWalletPercentageFee: Double = 2.00
}
