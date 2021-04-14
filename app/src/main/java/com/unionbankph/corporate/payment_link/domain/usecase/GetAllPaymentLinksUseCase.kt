package com.unionbankph.corporate.payment_link.domain.usecase

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.payment_link.data.gateway.PaymentLinkGateway
import com.unionbankph.corporate.payment_link.domain.model.form.GetAllPaymentLinksForm
import io.reactivex.Single
import javax.inject.Inject

class GetAllPaymentLinksUseCase
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val paymentLinkGateway: PaymentLinkGateway
) : SingleUseCase<Any, GetAllPaymentLinksForm>(
    threadExecutor,
    postExecutionThread
) {

    override fun buildUseCaseObservable(
        getAllPaymentLinksForm: GetAllPaymentLinksForm?
    ): Single<Any> {
        return paymentLinkGateway.getPaymentLinkList(
            getAllPaymentLinksForm!!.page.toString(),
            getAllPaymentLinksForm.itemsPerPage.toString(),
            getAllPaymentLinksForm.referenceNumber!!
        )
    }


}
