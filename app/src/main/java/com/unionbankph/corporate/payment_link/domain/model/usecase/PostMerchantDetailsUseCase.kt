package com.unionbankph.corporate.payment_link.domain.model.usecase

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.payment_link.domain.model.form.CreateMerchantForm
import com.unionbankph.corporate.payment_link.domain.model.response.CreateMerchantResponse
import com.unionbankph.corporate.payment_link.domain.model.gateway.CreateMerchantGateway
import io.reactivex.Single
import javax.inject.Inject

class PostMerchantDetailsUseCase
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
