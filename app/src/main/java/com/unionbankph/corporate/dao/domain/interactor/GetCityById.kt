package com.unionbankph.corporate.dao.domain.interactor

import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.nullable
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.dao.domain.form.GetCityForm
import com.unionbankph.corporate.dao.domain.gateway.DaoGateway
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import javax.inject.Inject

class GetCityById
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val responseProvider: ResponseProvider,
    private val daoGateway: DaoGateway
) : SingleUseCase<Selector, String?>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: String?): Single<Selector> {
        val getCityForm = JsonHelper.fromJson<GetCityForm>(params)
        return daoGateway.getAccessToken()
            .zipWith(daoGateway.getReferenceNumber())
            .flatMap {
                daoGateway.getCities(
                    it.first.nullable(),
                    it.second.nullable(),
                    getCityForm.provinceId.notNullable()
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
            .map { list ->
                val city = list.find { it.id == getCityForm.cityId }
                Selector(id = city?.id, value = city?.value)
            }
    }

}
