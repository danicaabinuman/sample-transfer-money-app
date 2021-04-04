package com.unionbankph.corporate.account.data.gateway

import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.data.model.AccountTransactionHistoryDetails
import com.unionbankph.corporate.account.data.model.RecentTransaction
import com.unionbankph.corporate.account.data.model.Record
import com.unionbankph.corporate.account.domain.form.GetAccountsBalances
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import io.reactivex.Single

/**
 * Created by herald25santos on 2020-01-14
 */
interface AccountGateway {

    fun getAccounts(
    ): Single<MutableList<Account>>

    fun getAccountsPermission(
        channelId: String? = null,
        permissionId: String? = null,
        exceptCurrency: String? = null
    ): Single<MutableList<Account>>

    fun getAccountsPaginated(
        channelId: String? = null,
        permissionId: String? = null,
        exceptCurrency: String? = null,
        exceptAccount: String? = null,
        pageable: Pageable
    ): Single<PagedDto<Account>>

    fun getAccountsBalances(
        getAccountsBalances: GetAccountsBalances
    ): Single<MutableList<Account>>

    fun getCorporateUserAccountDetail(id: String): Single<Account>

    fun getRecentTransactionsWithPagination(id: String, limit: Int): Single<RecentTransaction>

    fun getRecentTransactions(
        id: String,
        pageable: Pageable,
        lastItemRecord: Record?
    ): Single<RecentTransaction>

    fun getAccountTransactionHistoryDetails(
        id: String,
        referenceNumber: String,
        serialNo: String?,
        transactionDate: String?
    ): Single<AccountTransactionHistoryDetails>
}
