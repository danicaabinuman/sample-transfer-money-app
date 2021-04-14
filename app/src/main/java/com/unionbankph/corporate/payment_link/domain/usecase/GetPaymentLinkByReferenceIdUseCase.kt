package com.unionbankph.corporate.payment_link.domain.usecase

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.payment_link.data.gateway.PaymentLinkGateway
import io.reactivex.Single
import javax.inject.Inject

class GetPaymentLinkByReferenceIdUseCase
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val paymentLinkGateway: PaymentLinkGateway
) : SingleUseCase<Any, String>(
    threadExecutor,
    postExecutionThread
) {

    override fun buildUseCaseObservable(
        referenceId: String?
    ): Single<Any> {
        return paymentLinkGateway.getPaymentLinkByReferenceId(
            referenceId!!
        )
    }


}
