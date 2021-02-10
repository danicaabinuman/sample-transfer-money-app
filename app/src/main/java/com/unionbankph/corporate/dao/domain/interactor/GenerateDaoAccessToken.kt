package com.unionbankph.corporate.dao.domain.interactor

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.dao.domain.gateway.DaoGateway
import io.reactivex.Single
import javax.inject.Inject

class GenerateDaoAccessToken
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val responseProvider: ResponseProvider,
    private val daoGateway: DaoGateway
) : SingleUseCase<String, Void?>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: Void?): Single<String> {
        return daoGateway.getDaoToken()
            .flatMap { responseProvider.executeResponseSingle(it) }
            .flatMap { daoGateway.saveAccessToken(it.token).toSingle { it.token } }
    }
}
