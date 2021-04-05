package com.unionbankph.corporate.request_payment_link.domain.interactor

import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.link_details.data.LinkDetailsResponse
import com.unionbankph.corporate.link_details.data.form.LinkDetailsForm
import com.unionbankph.corporate.link_details.data.LinkDetailsGateway
import com.unionbankph.corporate.request_payment_link.data.form.CreateMerchantForm
import com.unionbankph.corporate.request_payment_link.data.model.CreateMerchantResponse
import com.unionbankph.corporate.request_payment_link.domain.CreateMerchantGateway
import io.reactivex.Single
import javax.inject.Inject

class GetAccounts
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
