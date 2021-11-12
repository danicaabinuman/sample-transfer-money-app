package com.unionbankph.corporate.app.dashboard.fragment

import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.GenericMenuItem
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.payment_link.data.gateway.PaymentLinkGateway
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.Single
import javax.inject.Inject

class GetMegaMenuUseCase
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val settingsGateway: SettingsGateway
) : SingleUseCase<MutableList<GenericMenuItem>, Unit?>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: Unit?): Single<MutableList<GenericMenuItem>> {
        return settingsGateway.getDashboardMegaMenu()
            .map {



                mutableListOf<GenericMenuItem>()
            }
    }
}