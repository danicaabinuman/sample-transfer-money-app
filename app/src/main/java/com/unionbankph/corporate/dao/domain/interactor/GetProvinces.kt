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

class GetProvinces
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
                daoGateway.getProvinces(
                    it.first /*,
                    it.second.nullable()*/
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
            .map { response ->
                response.records.let { provinces ->
                    provinces.map {
                        val selector = Selector()
                        selector.id = it.id
                        selector.value = it.value
                        selector
                    }.toMutableList()
                }
            }
    }
}
