package com.unionbankph.corporate.approval.presentation.di

import com.unionbankph.corporate.app.di.scope.PerApplication
import com.unionbankph.corporate.approval.domain.gateway.ApprovalGateway
import com.unionbankph.corporate.approval.domain.interactor.GetCheckWriterActivityLogs
import com.unionbankph.corporate.approval.domain.mapper.CheckWriterActivityLogsMapper
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import dagger.Module
import dagger.Provides

@Module
class ApprovalModule {

    @Provides
    @PerApplication
    fun getCheckWriterActivityLogs(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        responseProvider: ResponseProvider,
        approvalGateway: ApprovalGateway,
        settingsGateway: SettingsGateway,
        getCheckWriterActivityLogsMapper: CheckWriterActivityLogsMapper
    ): GetCheckWriterActivityLogs =
        GetCheckWriterActivityLogs(
            threadExecutor,
            postExecutionThread,
            responseProvider,
            approvalGateway,
            settingsGateway,
            getCheckWriterActivityLogsMapper
        )

}