package com.unionbankph.corporate.dao.domain.interactor

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.dao.domain.gateway.DaoGateway
import com.unionbankph.corporate.dao.domain.model.SignatoryDetail
import io.reactivex.Single
import javax.inject.Inject

class GetSignatoryDetailsFromCache
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val daoGateway: DaoGateway
) : SingleUseCase<SignatoryDetail, String?>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: String?): Single<SignatoryDetail> {
        return daoGateway.getSignatoryDetailsFromCache()
    }

}
