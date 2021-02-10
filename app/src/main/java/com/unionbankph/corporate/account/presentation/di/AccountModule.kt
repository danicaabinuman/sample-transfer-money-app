package com.unionbankph.corporate.account.presentation.di

import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.domain.interactor.GetAccountTransactionHistoryDetails
import com.unionbankph.corporate.app.di.scope.PerApplication
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import dagger.Module
import dagger.Provides

@Module
class AccountModule  {

    @Provides
    @PerApplication
    fun getAccountTransactionHistoryDetails(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        accountGateway: AccountGateway
    ): GetAccountTransactionHistoryDetails =
        GetAccountTransactionHistoryDetails(
            threadExecutor,
            postExecutionThread,
            accountGateway
        )

}