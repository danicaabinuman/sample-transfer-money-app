package com.unionbankph.corporate.payment_link.domain.model.usecase

import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

class GetAccountsUseCase
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val accountGateway: AccountGateway
) : SingleUseCase<MutableList<Account>, Unit>(
    threadExecutor,
    postExecutionThread
) {

    override fun buildUseCaseObservable(params: Unit?): Single<MutableList<Account>> {
        return accountGateway.getAccounts()
    }

}
