package com.unionbankph.corporate.payment_link.domain.model.usecase

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.payment_link.domain.model.response.LinkDetailsResponse
import com.unionbankph.corporate.payment_link.domain.model.form.LinkDetailsForm
import com.unionbankph.corporate.payment_link.domain.model.gateway.LinkDetailsGateway
import io.reactivex.Single
import javax.inject.Inject

class GetPaymentLinkDetails
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val linkDetailsGateway: LinkDetailsGateway
) : SingleUseCase<LinkDetailsResponse, LinkDetailsForm>(
    threadExecutor,
    postExecutionThread
) {

    override fun buildUseCaseObservable(
        params: LinkDetailsForm?
    ): Single<LinkDetailsResponse> {
        return linkDetailsGateway.getPaymentLink(
            LinkDetailsForm(
                params!!.totalAmount,
                params.description,
                params.notes,
                params.paymentLinkExpiry,
                params.mobileNumber,
                params.organizationName
            )
        )
    }

}
