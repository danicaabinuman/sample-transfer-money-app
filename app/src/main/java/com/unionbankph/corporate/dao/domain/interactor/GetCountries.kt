package com.unionbankph.corporate.dao.domain.interactor

import com.unionbankph.corporate.app.common.extension.nullable
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.dao.domain.gateway.DaoGateway
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import javax.inject.Inject

class GetCountries
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val responseProvider: ResponseProvider,
    private val daoGateway: DaoGateway
) : SingleUseCase<MutableList<Selector>, String?>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: String?): Single<MutableList<Selector>> {
        return daoGateway.getAccessToken()
            .zipWith(daoGateway.getReferenceNumber())
            .flatMap {
                daoGateway.getCounties(
                    it.first.nullable(),
                    it.second.nullable()
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
            .map { list ->
                list.map {
                    val selector = Selector()
                    selector.id = it.id
                    selector.value = it.name
                    selector
                }.toMutableList()
            }
    }

}
