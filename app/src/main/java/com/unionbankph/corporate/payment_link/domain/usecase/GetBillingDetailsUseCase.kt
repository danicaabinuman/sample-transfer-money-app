package com.unionbankph.corporate.payment_link.domain.usecase

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.payment_link.data.gateway.PaymentLinkGateway
import com.unionbankph.corporate.payment_link.presentation.billing_details.BillingDetailsState
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import javax.inject.Inject

class GetBillingDetailsUseCase
@Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val paymentLinkGateway: PaymentLinkGateway
) : SingleUseCase<BillingDetailsState, String>(
    threadExecutor,
    postExecutionThread
) {

    override fun buildUseCaseObservable(params: String?): Single<BillingDetailsState> {
        return  paymentLinkGateway.getPaymentLinkByReferenceId(params!!)
            .zipWith(paymentLinkGateway.getPaymentLogs(params))
            .map {
                BillingDetailsState().apply {
                    this.paymentDetails  = it.first
                    this.paymentLogs = it.second.records?.toMutableList()
                }
            }
    }
}