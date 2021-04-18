package com.unionbankph.corporate.payment_link.domain.usecase

import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.domain.form.GetAccountsBalances
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

class GetAccountsBalanceUseCase
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val accountGateway: AccountGateway
) : SingleUseCase<MutableList<Account>, GetAccountsBalances>(
    threadExecutor,
    postExecutionThread
) {

    override fun buildUseCaseObservable(params: GetAccountsBalances?): Single<MutableList<Account>> {
        return accountGateway.getAccountsBalances(
                params!!
        )
    }

}
