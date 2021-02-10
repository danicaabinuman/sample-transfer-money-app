package com.unionbankph.corporate.dao.domain.interactor

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.CompletableUseCase
import com.unionbankph.corporate.dao.domain.gateway.DaoGateway
import io.reactivex.Completable
import javax.inject.Inject

class SaveTokenDeepLink
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val daoGateway: DaoGateway
) : CompletableUseCase<String?>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: String?): Completable {
        return daoGateway.saveAccessToken(params)
            .andThen(daoGateway.saveReferenceNumber(""))
    }

}
