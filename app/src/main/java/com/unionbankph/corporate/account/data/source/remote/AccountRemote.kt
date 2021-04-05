package com.unionbankph.corporate.account.data.source.remote

import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.data.model.AccountTransactionHistoryDetails
import com.unionbankph.corporate.account.data.model.RecentTransaction
import com.unionbankph.corporate.account.data.model.Record
import com.unionbankph.corporate.account.domain.form.GetAccountsBalances
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import io.reactivex.Single
import retrofit2.Response

/**
 * Created by herald25santos on 2020-01-14
 */
interface AccountRemote {

    fun getAccounts(
        accessToken: String
    ): Single<Response<MutableList<Account>>>

    fun getAccountsPermission(
        accessToken: String,
        channelId: String?,
        permissionId: String?,
        exceptCurrency: String?
    ): Single<Response<MutableList<Account>>>

    fun getAccountsPaginated(
        accessToken: String,
        channelId: String?,
        permissionId: String?,
        exceptCurrency: String?,
        exceptAccount: String?,
        pageable: Pageable
    ): Single<Response<PagedDto<Account>>>

    fun getAccountsBalances(
        accessToken: String,
        getAccountsBalances: GetAccountsBalances
    ): Single<Response<MutableList<Account>>>

    fun getCorporateUserAccountDetail(accessToken: String, id: String): Single<Response<Account>>

    fun getRecentTransactionsWithPagination(
        accessToken: String,
        id: String,
        limit: Int
    ): Single<Response<RecentTransaction>>

    fun getRecentTransactions(
        accessToken: String,
        id: String,
        pageable: Pageable,
        lastItemRecord: Record?
    ): Single<Response<RecentTransaction>>

    fun getAccountTransactionHistoryDetails(
        accessToken: String,
        id: String,
        referenceNumber: String,
        serialNo: String?,
        transactionDate: String?
    ): Single<Response<AccountTransactionHistoryDetails>>
}
