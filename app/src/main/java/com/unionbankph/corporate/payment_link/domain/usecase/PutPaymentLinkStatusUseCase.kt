package com.unionbankph.corporate.payment_link.domain.usecase

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.payment_link.data.gateway.PaymentLinkGateway
import com.unionbankph.corporate.payment_link.domain.model.form.PutPaymentLinkStatusContainerForm
import com.unionbankph.corporate.payment_link.domain.model.form.PutPaymentLinkStatusForm
import com.unionbankph.corporate.payment_link.domain.model.response.PutPaymentLinkStatusResponse
import io.reactivex.Single
import javax.inject.Inject

class PutPaymentLinkStatusUseCase
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val paymentLinkGateway: PaymentLinkGateway
) : SingleUseCase<PutPaymentLinkStatusResponse, PutPaymentLinkStatusContainerForm>(
    threadExecutor,
    postExecutionThread
) {

    override fun buildUseCaseObservable(
        putPaymentLinkStatusContainerForm: PutPaymentLinkStatusContainerForm?
    ): Single<PutPaymentLinkStatusResponse> {
        return paymentLinkGateway.putPaymentLinkStatus(
            putPaymentLinkStatusContainerForm?.transactionId!!,
            putPaymentLinkStatusContainerForm?.putPaymentLinkStatusForm
        )
    }


}
