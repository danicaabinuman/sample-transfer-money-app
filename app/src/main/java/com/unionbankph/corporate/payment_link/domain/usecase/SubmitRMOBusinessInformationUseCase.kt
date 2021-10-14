package com.unionbankph.corporate.payment_link.domain.usecase

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.payment_link.data.gateway.PaymentLinkGateway
import com.unionbankph.corporate.payment_link.domain.model.rmo.RMOBusinessInformationForm
import com.unionbankph.corporate.payment_link.domain.model.rmo.RMOBusinessInformationResponse
import io.reactivex.Single
import javax.inject.Inject

class SubmitRMOBusinessInformationUseCase
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val paymentLinkGateway: PaymentLinkGateway
) : SingleUseCase<RMOBusinessInformationResponse, RMOBusinessInformationForm>(
    threadExecutor,
    postExecutionThread
) {
    override fun buildUseCaseObservable(
        params: RMOBusinessInformationForm?
    ): Single<RMOBusinessInformationResponse>{
        return paymentLinkGateway.submitBusinessInformation(
            RMOBusinessInformationForm(
                params!!.businessType,
                params.storeProduct,
                params.infoStatus,
                params.yearsInBusiness,
                params.numberOfBranches,
                params.physicalStore,
                params.website,
                params.lazadaUrl,
                params.shopeeUrl,
                params.facebookUrl,
                params.instagramUrl,
                params.imageUrl1,
                params.imageUrl2,
                params.imageUrl3,
                params.imageUrl4,
                params.imageUrl5,
                params.imageUrl6
            )
        )
    }
}