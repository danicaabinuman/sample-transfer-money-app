package com.unionbankph.corporate.payment_link.domain.usecase

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.payment_link.data.gateway.PaymentLinkGateway
import com.unionbankph.corporate.payment_link.domain.model.form.GetPaymentLinkListByReferenceNumberForm
import com.unionbankph.corporate.payment_link.domain.model.form.GetPaymentLinkListPaginatedForm
import com.unionbankph.corporate.payment_link.domain.model.response.GetPaymentLinkListPaginatedResponse
import io.reactivex.Single
import javax.inject.Inject

class GetAllPaymentLinksByReferenceNumberUseCase
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val paymentLinkGateway: PaymentLinkGateway
) : SingleUseCase<GetPaymentLinkListPaginatedResponse, GetPaymentLinkListByReferenceNumberForm>(
    threadExecutor,
    postExecutionThread
) {

    override fun buildUseCaseObservable(
            getPaymentLinkListByReferenceNumberForm: GetPaymentLinkListByReferenceNumberForm?
    ): Single<GetPaymentLinkListPaginatedResponse> {
        return paymentLinkGateway.getPaymentLinkListByReferenceNumber(
                getPaymentLinkListByReferenceNumberForm!!.page.toString(),
                getPaymentLinkListByReferenceNumberForm.itemsPerPage.toString(),
                getPaymentLinkListByReferenceNumberForm.referenceNumber!!
        )
    }


}
