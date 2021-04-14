package com.unionbankph.corporate.payment_link.domain.usecase

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.payment_link.data.gateway.PaymentLinkGateway
import com.unionbankph.corporate.payment_link.domain.model.form.CreateMerchantForm
import com.unionbankph.corporate.payment_link.domain.model.form.GeneratePaymentLinkForm
import com.unionbankph.corporate.payment_link.domain.model.response.CreateMerchantResponse
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkResponse
import io.reactivex.Single
import javax.inject.Inject

class GeneratePaymentLinkUseCase
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val paymentLinkGateway: PaymentLinkGateway
) : SingleUseCase<GeneratePaymentLinkResponse, GeneratePaymentLinkForm>(
    threadExecutor,
    postExecutionThread
) {

    override fun buildUseCaseObservable(
        params: GeneratePaymentLinkForm?
    ): Single<GeneratePaymentLinkResponse> {
        return paymentLinkGateway.generatePaymentLink(params!!)
    }

}
