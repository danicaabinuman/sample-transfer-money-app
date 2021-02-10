package com.unionbankph.corporate.fund_transfer.data.source.remote.impl

import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.auth.data.form.ResendOTPForm
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.fund_transfer.data.form.*
import com.unionbankph.corporate.fund_transfer.data.model.*
import com.unionbankph.corporate.fund_transfer.data.source.remote.FundTransferRemote
import com.unionbankph.corporate.fund_transfer.data.source.remote.client.FundTransferApiClient
import io.reactivex.Single
import retrofit2.Response
import retrofit2.Retrofit
import javax.annotation.concurrent.ThreadSafe
import javax.inject.Inject

/**
 * Created by herald25santos on 2020-01-15
 */
class FundTransferRemoteImpl
@Inject
constructor(
    retrofit: Retrofit
) : FundTransferRemote {

    private val fundTransferApiClient: FundTransferApiClient =
        retrofit.create(FundTransferApiClient::class.java)

    override fun getOrganizationTransfers(
        accessToken: String,
        startDate: String?,
        endDate: String?,
        channelIds: String?,
        status: String?,
        pageable: Pageable
    ): Single<Response<PagedDto<Transaction>>> {
        return fundTransferApiClient.getOrganizationTransfers(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            pageable.page,
            pageable.size,
            pageable.filter,
            startDate,
            endDate,
            status,
            channelIds
        )
    }

    override fun getFundTransferDetail(
        accessToken: String,
        id: String
    ): Single<Response<Transaction>> {
        return fundTransferApiClient.getFundTransferDetail(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }

    override fun getFundTransferActivityLogs(
        accessToken: String,
        id: String
    ): Single<Response<MutableList<ActivityLogDto>>> {
        return fundTransferApiClient.getFundTransferActivityLogs(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }

    override fun getBatchTransaction(
        accessToken: String,
        pageable: Pageable,
        id: String
    ): Single<Response<PagedDto<Batch>>> {
        return fundTransferApiClient.getBatchesTransaction(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id,
            pageable.page,
            pageable.size
        )
    }

    override fun fundTransferOTP(
        accessToken: String,
        otp: HashMap<String, String>
    ): Single<Response<FundTransferVerify>> {
        return fundTransferApiClient.fundTransferOTP(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            otp
        )
    }

    override fun resendOTPFundTransfer(
        accessToken: String,
        resendOTPForm: ResendOTPForm
    ): Single<Response<Auth>> {
        return fundTransferApiClient.resendOTPFundTransfer(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            resendOTPForm
        )
    }

    override fun cancelFundTransferTransaction(
        accessToken: String,
        cancelFundTransferTransactionForm: CancelFundTransferTransactionForm
    ): Single<Response<CancelFundTransferTransactionResponse>> {
        return fundTransferApiClient.cancelFundTransferTransaction(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            cancelFundTransferTransactionForm
        )
    }

    override fun fundTransferUBP(
        accessToken: String,
        fundTransferUBPForm: FundTransferUBPForm
    ): Single<Response<FundTransferVerify>> {
        return fundTransferApiClient.fundTransferUBP(
            accessToken,
            API_V4,
            fundTransferUBPForm
        )
    }

    override fun getPurposes(accessToken: String): Single<Response<MutableList<Purpose>>> {
        return fundTransferApiClient.getPurposes(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun getInstaPayPurposes(accessToken: String): Single<Response<MutableList<Purpose>>> {
        return fundTransferApiClient.getInstaPayPurposes(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun fundTransferPesoNet(
        accessToken: String,
        fundTransferPesoNetForm: FundTransferPesoNetForm
    ): Single<Response<FundTransferVerify>> {
        return fundTransferApiClient.fundTransferPesoNet(
            accessToken,
            API_V4,
            fundTransferPesoNetForm
        )
    }

    override fun getPesoNetBanks(accessToken: String): Single<Response<MutableList<Bank>>> {
        return fundTransferApiClient.getPesoNetBanks(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun getInstaPayBanks(accessToken: String): Single<Response<MutableList<Bank>>> {
        return fundTransferApiClient.getInstaPayBanks(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun getPDDTSBanks(accessToken: String): Single<Response<MutableList<Bank>>> {
        return fundTransferApiClient.getPDDTSBanks(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun getSwiftBanks(
        accessToken: String,
        pageable: Pageable
    ): Single<Response<PagedDto<SwiftBank>>> {
        return fundTransferApiClient.getSwiftBanks(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            pageable.page,
            pageable.size,
            pageable.filter
        )
    }

    override fun fundTransferInstapay(
        accessToken: String,
        fundTransferInstaPayForm: FundTransferInstaPayForm
    ): Single<Response<FundTransferVerify>> {
        return fundTransferApiClient.fundTransferInstaPay(
            accessToken,
            API_V4,
            fundTransferInstaPayForm
        )
    }

    override fun fundTransferSwift(
        accessToken: String,
        fundTransferSwiftForm: FundTransferSwiftForm
    ): Single<Response<FundTransferVerify>> {
        return fundTransferApiClient.fundTransferSwift(
            accessToken,
            API_V4,
            fundTransferSwiftForm
        )
    }

    override fun getBeneficiaries(
        accessToken: String,
        channelId: String?,
        accountId: String?,
        pageable: Pageable
    ): Single<Response<PagedDto<Beneficiary>>> {
        return fundTransferApiClient.getBeneficiaries(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            channelId,
            accountId,
            pageable.page,
            pageable.size,
            pageable.filter
        )
    }

    override fun getBeneficiaryDetail(
        accessToken: String,
        id: String
    ): Single<Response<BeneficiaryDetailDto>> {
        return fundTransferApiClient.getBeneficiaryDetail(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }

    override fun createBeneficiary(
        accessToken: String,
        apiVersion: String,
        beneficiaryForm: BeneficiaryForm
    ): Single<Response<CreationBeneficiaryDto>> {
        return fundTransferApiClient.createBeneficiary(
            accessToken,
            apiVersion,
            beneficiaryForm
        )
    }

    override fun updateBeneficiary(
        accessToken: String,
        apiVersion: String,
        id: String,
        beneficiaryForm: BeneficiaryForm
    ): Single<Response<CreationBeneficiaryDto>> {
        return fundTransferApiClient.updateBeneficiary(
            accessToken,
            apiVersion,
            id,
            beneficiaryForm
        )
    }

    override fun deleteBeneficiary(accessToken: String, id: String): Single<Response<Message>> {
        return fundTransferApiClient.deleteBeneficiary(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }

    override fun getBeneficiaryDetailActivityLogs(
        accessToken: String,
        id: String
    ): Single<Response<MutableList<ActivityLogDto>>> {
        return fundTransferApiClient.getBeneficiaryDetailActivityLogs(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }

    override fun getManageScheduledTransfers(
        accessToken: String,
        status: String,
        pageable: Pageable
    ): Single<Response<PagedDto<Transaction>>> {
        return fundTransferApiClient.getManageScheduledTransfers(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            status,
            pageable.page,
            pageable.size,
            pageable.filter
        )
    }

    override fun deleteScheduledTransfer(
        accessToken: String,
        scheduledTransferDeletionForm: ScheduledTransferDeletionForm
    ): Single<Response<Message>> {
        return fundTransferApiClient.deleteScheduledTransfer(
            accessToken,
            "v4",
            scheduledTransferDeletionForm
        )
    }

    override fun getFundTransferCWTHeader(accessToken: String): Single<Response<MutableList<CWTDetail>>> {
        return fundTransferApiClient.getFundTransferCWTHeader(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun getFundTransferCWT(
        accessToken: String,
        pageable: Pageable,
        id: String
    ): Single<Response<PagedDto<MutableList<CWTDetail>>>> {
        return fundTransferApiClient.getFundTransferCWT(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id,
            submitted = true,
            page = pageable.page,
            size = pageable.size
        )
    }

    override fun getFundTransferINVHeader(accessToken: String): Single<Response<MutableList<CWTDetail>>> {
        return fundTransferApiClient.getFundTransferINVHeader(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun getFundTransferINV(
        accessToken: String,
        pageable: Pageable,
        id: String
    ): Single<Response<PagedDto<MutableList<CWTDetail>>>> {
        return fundTransferApiClient.getFundTransferINV(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id,
            submitted = true,
            page = pageable.page,
            size = pageable.size
        )
    }

    @ThreadSafe
    companion object {
        const val API_V4 = "v4"
    }
}
