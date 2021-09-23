package com.unionbankph.corporate.payment_link.domain.usecase

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.payment_link.data.gateway.PaymentLinkGateway
import com.unionbankph.corporate.payment_link.domain.model.response.GetPaymentLogsResponse
import io.reactivex.Single
import javax.inject.Inject

class GetPaymentLogsUseCase
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val paymentLinkGateway: PaymentLinkGateway
) : SingleUseCase<GetPaymentLogsResponse, String>(
    threadExecutor,
    postExecutionThread
) {

    override fun buildUseCaseObservable(
        referenceId: String?
    ): Single<GetPaymentLogsResponse> {
        return paymentLinkGateway.getPaymentLogs(
            referenceId!!
        )
    }
}