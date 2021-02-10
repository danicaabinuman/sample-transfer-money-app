package com.unionbankph.corporate.dao.domain.interactor

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.CompletableUseCase
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.dao.data.form.PersonalInformationStepOneForm
import com.unionbankph.corporate.dao.domain.gateway.DaoGateway
import io.reactivex.Completable
import io.reactivex.rxkotlin.zipWith
import javax.inject.Inject

class SubmitPersonalInformationStepOne
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val responseProvider: ResponseProvider,
    private val daoGateway: DaoGateway
) : CompletableUseCase<PersonalInformationStepOneForm?>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: PersonalInformationStepOneForm?): Completable {
        return daoGateway.getAccessToken()
            .zipWith(daoGateway.getReferenceNumber())
            .flatMap {
                params?.let { params ->
                    daoGateway.submitPersonalInformationStepOne(
                        it.first,
                        it.second,
                        params
                    )
                }
            }
            .flatMapCompletable { responseProvider.executeResponseCompletable(it) }
    }
}
