package com.unionbankph.corporate.dao.domain.interactor

import android.util.Log
import com.unionbankph.corporate.app.common.extension.notNullable
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

class GetListOfIds
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val responseProvider: ResponseProvider,
    private val daoGateway: DaoGateway
) : SingleUseCase<MutableList<Selector>, String?>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: String?): Single<MutableList<Selector>> {
        return daoGateway.getAccessToken()
            .flatMap {
                daoGateway.getListIds(it.nullable())
            }
            .flatMap { responseProvider.executeResponseSingle(it)
                    }

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
