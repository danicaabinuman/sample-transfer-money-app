package com.unionbankph.corporate.corporate.domain.interactor

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.corporate.domain.gateway.CorporateGateway
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by herald25santos on 6/24/20
 */
open class GetTransactionStatuses
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val responseProvider: ResponseProvider,
    private val settingsGateway: SettingsGateway,
    private val corporateGateway: CorporateGateway
) : SingleUseCase<MutableList<Selector>, String?>(
    threadExecutor,
    postExecutionThread
) {

    public override fun buildUseCaseObservable(params: String?): Single<MutableList<Selector>> {
        return settingsGateway.getAccessToken()
            .flatMap {
                params?.let { params ->
                    corporateGateway.getTransactionStatuses(it, params)
                }
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
            .map { list ->
                list.map {
                    val selector = Selector()
                    selector.id = it.type
                    selector.value = it.description
                    selector
                }.toMutableList()
            }
    }

}
