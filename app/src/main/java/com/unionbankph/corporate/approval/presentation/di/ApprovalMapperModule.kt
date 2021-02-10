package com.unionbankph.corporate.approval.presentation.di

import com.unionbankph.corporate.app.di.scope.PerApplication
import com.unionbankph.corporate.approval.domain.mapper.CheckWriterActivityLogsMapper
import dagger.Module
import dagger.Provides

@Module
class ApprovalMapperModule {

    @Provides
    @PerApplication
    fun checkWriterActivityLogsMapper(): CheckWriterActivityLogsMapper =
        CheckWriterActivityLogsMapper()

}
