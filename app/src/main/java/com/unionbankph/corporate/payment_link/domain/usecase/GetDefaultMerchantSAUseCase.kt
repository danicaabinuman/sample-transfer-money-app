package com.unionbankph.corporate.payment_link.domain.usecase

import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.domain.form.GetAccountsBalances
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.payment_link.data.gateway.PaymentLinkGateway
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import javax.inject.Inject

class GetDefaultMerchantSAUseCase
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val paymentLinkGateway: PaymentLinkGateway,
    private val accountGateway: AccountGateway
) : SingleUseCase<MutableList<Account>, String?>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: String?): Single<MutableList<Account>> {
        return paymentLinkGateway.validateMerchantByOrganization()
            .zipWith(accountGateway.getNominateSettlementAccounts())
            .map { pair ->

                val defaultAccount = pair.second.results.find {
                    pair.first.merchantDetails?.settlementAccountNumber == it.accountNumber
                }

                val remainingAccounts: MutableList<Account> =
                    pair.second.results.filter { it != defaultAccount }.toMutableList()

                mutableListOf<Account>().apply {
                    this.add(defaultAccount!!)
                    this.addAll(remainingAccounts)
                }
            }
    }
}