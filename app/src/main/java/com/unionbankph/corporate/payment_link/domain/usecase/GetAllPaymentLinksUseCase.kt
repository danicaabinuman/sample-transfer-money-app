package com.unionbankph.corporate.payment_link.domain.usecase

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.payment_link.data.gateway.PaymentLinkGateway
import com.unionbankph.corporate.payment_link.domain.model.form.GetPaymentLinkListPaginatedForm
import com.unionbankph.corporate.payment_link.domain.model.response.GetPaymentLinkListPaginatedResponse
import io.reactivex.Single
import javax.inject.Inject

class GetAllPaymentLinksUseCase
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val paymentLinkGateway: PaymentLinkGateway
) : SingleUseCase<GetPaymentLinkListPaginatedResponse, GetPaymentLinkListPaginatedForm>(
    threadExecutor,
    postExecutionThread
) {

    override fun buildUseCaseObservable(
            getPaymentLinkListPaginatedForm: GetPaymentLinkListPaginatedForm?
    ): Single<GetPaymentLinkListPaginatedResponse> {
        return paymentLinkGateway.getPaymentLinkListPaginated(
                getPaymentLinkListPaginatedForm!!.page.toString(),
                getPaymentLinkListPaginatedForm.itemsPerPage.toString()
        )
    }


}
