package com.unionbankph.corporate.dao.domain.interactor

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.dao.data.model.Results
import com.unionbankph.corporate.dao.domain.form.ValidateNominatedUserForm
import com.unionbankph.corporate.dao.domain.gateway.DaoGateway
import io.reactivex.Single
import javax.inject.Inject

class ValidateNominatedUser
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val responseProvider: ResponseProvider,
    private val daoGateway: DaoGateway
) : SingleUseCase<Map<String, Results>, ValidateNominatedUserForm?>(
    threadExecutor,
    postExecutionThread
) {

    override fun buildUseCaseObservable(params: ValidateNominatedUserForm?): Single<Map<String, Results>> {
        return daoGateway.getAccessToken()
            .flatMap {
                params?.let { params ->
                    daoGateway.validateNominatedUser(it, params)
                }
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
            .map { it.results }
    }

}
