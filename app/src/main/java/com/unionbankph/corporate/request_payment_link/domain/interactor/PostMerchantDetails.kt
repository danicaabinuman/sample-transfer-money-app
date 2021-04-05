package com.unionbankph.corporate.request_payment_link.domain.interactor

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.link_details.data.LinkDetailsResponse
import com.unionbankph.corporate.link_details.data.form.LinkDetailsForm
import com.unionbankph.corporate.link_details.data.LinkDetailsGateway
import com.unionbankph.corporate.request_payment_link.data.form.CreateMerchantForm
import com.unionbankph.corporate.request_payment_link.data.model.CreateMerchantResponse
import com.unionbankph.corporate.request_payment_link.domain.CreateMerchantGateway
import io.reactivex.Single
import javax.inject.Inject

class PostMerchantDetails
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val createMerchantGateway: CreateMerchantGateway
) : SingleUseCase<CreateMerchantResponse, CreateMerchantForm>(
    threadExecutor,
    postExecutionThread
) {

    override fun buildUseCaseObservable(
        params: CreateMerchantForm?
    ): Single<CreateMerchantResponse> {
        return createMerchantGateway.createMerchant(
            CreateMerchantForm(
                params!!.organizationId,
                params.merchantName,
                params.uniqueStoreHandle,
                params.accountNo,
                params.accountName,
                params.website,
                params.productsAndServices
            )
        )
    }

}
