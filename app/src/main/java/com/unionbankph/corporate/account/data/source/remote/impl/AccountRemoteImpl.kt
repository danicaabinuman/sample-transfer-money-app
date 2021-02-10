package com.unionbankph.corporate.account.data.source.remote.impl

import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.data.model.AccountTransactionHistoryDetails
import com.unionbankph.corporate.account.data.model.RecentTransaction
import com.unionbankph.corporate.account.data.model.Record
import com.unionbankph.corporate.account.data.source.remote.AccountRemote
import com.unionbankph.corporate.account.data.source.remote.client.AccountApiClient
import com.unionbankph.corporate.account.domain.form.GetAccountsBalances
import com.unionbankph.corporate.common.data.constant.ApiVersionEnum
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import io.reactivex.Single
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * Created by herald25santos on 2020-01-14
 */
class AccountRemoteImpl
@Inject
constructor(
    retrofit: Retrofit
) : AccountRemote {

    private val accountApiClient: AccountApiClient =
        retrofit.create(AccountApiClient::class.java)

    override fun getAccountsPermission(
        accessToken: String,
        channelId: String?,
        permissionId: String?,
        exceptCurrency: String?
    ): Single<Response<MutableList<Account>>> {
        return accountApiClient.getAccountsPermission(
            accessToken,
            ApiVersionEnum.V3.value,
            channelId,
            permissionId,
            exceptCurrency
        )
    }

    override fun getAccountsPaginated(
        accessToken: String,
        channelId: String?,
        permissionId: String?,
        exceptCurrency: String?,
        exceptAccount: String?,
        pageable: Pageable
    ): Single<Response<PagedDto<Account>>> {
        return accountApiClient.getAccountsPaginated(
            accessToken,
            ApiVersionEnum.V4.value,
            channelId,
            permissionId,
            exceptCurrency,
            exceptAccount,
            pageable.page,
            pageable.size,
            pageable.filter
        )
    }

    override fun getAccountsBalances(
        accessToken: String,
        getAccountsBalances: GetAccountsBalances
    ): Single<Response<MutableList<Account>>> {
        return accountApiClient.getAccountsBalances(
            accessToken,
            ApiVersionEnum.V1.value,
            getAccountsBalances
        )
    }

    override fun getCorporateUserAccountDetail(
        accessToken: String,
        id: String
    ): Single<Response<Account>> {
        return accountApiClient.getCorporateUserAccountDetail(
            accessToken,
            ApiVersionEnum.V3.value,
            id
        )
    }

    override fun getRecentTransactionsWithPagination(
        accessToken: String,
        id: String,
        limit: Int
    ): Single<Response<RecentTransaction>> {
        return accountApiClient.getRecentTransactionsWithPagination(
            accessToken,
            ApiVersionEnum.V3.value,
            id,
            limit
        )
    }

    override fun getRecentTransactions(
        accessToken: String,
        id: String,
        pageable: Pageable,
        lastItemRecord: Record?
    ): Single<Response<RecentTransaction>> {
        return if (lastItemRecord != null) {
            accountApiClient.getRecentTransactions(
                accessToken,
                ApiVersionEnum.V3.value,
                id,
                lastItemRecord.tranId,
                lastItemRecord.balance,
                lastItemRecord.postedDate,
                lastItemRecord.serialNo,
                pageable.page + 1,
                pageable.size
            )
        } else {
            accountApiClient.getRecentTransactions(
                accessToken,
                BuildConfig.CLIENT_API_VERSION,
                id
            )
        }
    }

    override fun getAccountTransactionHistoryDetails(
        accessToken: String,
        id: String,
        referenceNumber: String,
        serialNo: String?,
        transactionDate: String?
    ): Single<Response<AccountTransactionHistoryDetails>> {
        return accountApiClient.getAccountTransactionHistoryDetails(
            accessToken,
            ApiVersionEnum.V4.value,
            id,
            referenceNumber,
            serialNo,
            transactionDate
        )
    }
}
