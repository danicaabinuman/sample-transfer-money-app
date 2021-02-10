package com.unionbankph.corporate.corporate.presentation.di

import com.unionbankph.corporate.app.di.scope.PerApplication
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.corporate.domain.gateway.CorporateGateway
import com.unionbankph.corporate.corporate.domain.interactor.GetTransactionStatuses
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import dagger.Module
import dagger.Provides

@Module
class CorporateModule {

    @Provides
    @PerApplication
    fun getTransactionStatuses(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        responseProvider: ResponseProvider,
        settingsGateway: SettingsGateway,
        corporateGateway: CorporateGateway
    ): GetTransactionStatuses =
        GetTransactionStatuses(
            threadExecutor,
            postExecutionThread,
            responseProvider,
            settingsGateway,
            corporateGateway
        )

}
