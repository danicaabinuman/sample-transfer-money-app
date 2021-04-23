package com.unionbankph.corporate.payment_link.domain.usecase

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.payment_link.data.gateway.PaymentLinkGateway
import com.unionbankph.corporate.payment_link.domain.model.response.ValidateMerchantByOrganizationResponse
import io.reactivex.Single
import javax.inject.Inject

class ValidateMerchantUseCase
@Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val paymentLinkGateway: PaymentLinkGateway
) : SingleUseCase<ValidateMerchantByOrganizationResponse, Unit>(
    threadExecutor,
    postExecutionThread
) {
    override fun buildUseCaseObservable(params: Unit?): Single<ValidateMerchantByOrganizationResponse> {
        return paymentLinkGateway.validateMerchantByOrganization()
    }

}