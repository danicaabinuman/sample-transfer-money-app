package com.unionbankph.corporate.payment_link.domain.usecase

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.payment_link.data.gateway.PaymentLinkGateway
import com.unionbankph.corporate.payment_link.domain.model.form.UpdateSettlementOnRequestPaymentForm
import com.unionbankph.corporate.payment_link.domain.model.response.UpdateSettlementOnRequestPaymentResponse
import io.reactivex.Single
import javax.inject.Inject

class UpdateSettlementOnRequestPaymentUseCase
@Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val paymentLinkGateway: PaymentLinkGateway
) : SingleUseCase<UpdateSettlementOnRequestPaymentResponse, UpdateSettlementOnRequestPaymentForm>(
    threadExecutor,
    postExecutionThread
){
    override fun buildUseCaseObservable(
        params: UpdateSettlementOnRequestPaymentForm?
    ): Single<UpdateSettlementOnRequestPaymentResponse> {
        return paymentLinkGateway.updateSettlementOnRequestPayment(
            UpdateSettlementOnRequestPaymentForm(
                params!!.accountNumber
            )
        )
    }
}