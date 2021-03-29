package com.unionbankph.corporate.link_details.domain.interactor

import com.unionbankph.corporate.account.data.model.AccountTransactionHistoryDetails
import com.unionbankph.corporate.account.domain.form.GetAccountTransactionHistoryDetailsForm
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.link_details.data.LinkDetailsResponse
import com.unionbankph.corporate.link_details.data.form.LinkDetailsForm
import com.unionbankph.corporate.link_details.presentation.LinkDetailsGateway
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
        return linkDetailsGateway.linkGateway(
            LinkDetailsForm(
                params!!.totalAmount,
                params.description,
                params.notes,
                params.paymentLinkExpiry
            )
        )
    }

}
