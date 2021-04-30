package com.unionbankph.corporate.account.data.gateway.impl

import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.data.model.AccountTransactionHistoryDetails
import com.unionbankph.corporate.account.data.model.RecentTransaction
import com.unionbankph.corporate.account.data.model.Record
import com.unionbankph.corporate.account.data.source.local.AccountCache
import com.unionbankph.corporate.account.data.source.remote.AccountRemote
import com.unionbankph.corporate.account.domain.form.GetAccountsBalances
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by herald25santos on 2020-01-14
 */
class AccountGatewayImpl
@Inject
constructor(
    private val responseProvider: ResponseProvider,
    private val accountRemote: AccountRemote,
    private val accountCache: AccountCache,
    private val settingsGateway: SettingsGateway
) : AccountGateway {

    override fun getNominateSettlementAccounts(): Single<PagedDto<Account>> {
        return settingsGateway.getAccessToken()
            .flatMap {
                accountRemote.getAccountsPaginated(
                    it,
                    "3",
                    "10",
                    null,
                    null,
                    Pageable()

                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getAccountsPermission(
        channelId: String?,
        permissionId: String?,
        exceptCurrency: String?
    ): Single<MutableList<Account>> {
        return settingsGateway.getAccessToken()
            .flatMap {
                accountRemote.getAccountsPermission(
                    it,
                    channelId,
                    permissionId,
                    exceptCurrency
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getAccountsPaginated(
        channelId: String?,
        permissionId: String?,
        exceptCurrency: String?,
        exceptAccount: String?,
        pageable: Pageable
    ): Single<PagedDto<Account>> {
        return settingsGateway.getAccessToken()
            .flatMap {
                accountRemote.getAccountsPaginated(
                    it,
                    channelId,
                    permissionId,
                    exceptCurrency,
                    exceptAccount,
                    pageable
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getAccountsBalances(
        getAccountsBalances: GetAccountsBalances
    ): Single<MutableList<Account>> {
        return settingsGateway.getAccessToken()
            .flatMap {
                accountRemote.getAccountsBalances(
                    it,
                    getAccountsBalances
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getCorporateUserAccountDetail(id: String): Single<Account> {
        return settingsGateway.getAccessToken()
            .flatMap { accountRemote.getCorporateUserAccountDetail(it, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getRecentTransactionsWithPagination(
        id: String,
        limit: Int
    ): Single<RecentTransaction> {
        return settingsGateway.getAccessToken()
            .flatMap { accountRemote.getRecentTransactionsWithPagination(it, id, limit) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getRecentTransactions(
        id: String,
        pageable: Pageable,
        lastItemRecord: Record?
    ): Single<RecentTransaction> {
        return settingsGateway.getAccessToken()
            .flatMap { accountRemote.getRecentTransactions(it, id, pageable, lastItemRecord) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getAccountTransactionHistoryDetails(
        id: String,
        referenceNumber: String,
        serialNo: String?,
        transactionDate: String?
    ): Single<AccountTransactionHistoryDetails> {
        return settingsGateway.getAccessToken()
            .flatMap {
                accountRemote.getAccountTransactionHistoryDetails(
                    it,
                    id,
                    referenceNumber,
                    serialNo,
                    transactionDate
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }
}
