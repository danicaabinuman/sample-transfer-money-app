package com.unionbankph.corporate.account.domain.interactor

import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.model.AccountTransactionHistoryDetails
import com.unionbankph.corporate.account.domain.form.GetAccountTransactionHistoryDetailsForm
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

class GetAccountTransactionHistoryDetails
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val accountGateway: AccountGateway
) : SingleUseCase<AccountTransactionHistoryDetails, GetAccountTransactionHistoryDetailsForm?>(
    threadExecutor,
    postExecutionThread
) {

    override fun buildUseCaseObservable(
        params: GetAccountTransactionHistoryDetailsForm?
    ): Single<AccountTransactionHistoryDetails> {
        return accountGateway.getAccountTransactionHistoryDetails(
            params?.id.notNullable(),
            params?.referenceNumber.notNullable(),
            params?.serialNo,
            params?.transactionDate
        )
    }

}
