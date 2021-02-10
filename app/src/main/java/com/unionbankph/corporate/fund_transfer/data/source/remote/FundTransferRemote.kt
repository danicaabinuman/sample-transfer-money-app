package com.unionbankph.corporate.fund_transfer.data.source.remote

import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.auth.data.form.ResendOTPForm
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.fund_transfer.data.form.*
import com.unionbankph.corporate.fund_transfer.data.model.*
import io.reactivex.Single
import retrofit2.Response

/**
 * Created by herald25santos on 2020-01-15
 */
interface FundTransferRemote {

    fun getOrganizationTransfers(
        accessToken: String,
        startDate: String?,
        endDate: String?,
        channelIds: String?,
        status: String?,
        pageable: Pageable
    ): Single<Response<PagedDto<Transaction>>>

    fun getFundTransferDetail(accessToken: String, id: String): Single<Response<Transaction>>

    fun getFundTransferActivityLogs(
        accessToken: String,
        id: String
    ): Single<Response<MutableList<ActivityLogDto>>>

    fun getBatchTransaction(
        accessToken: String,
        pageable: Pageable,
        id: String
    ): Single<Response<PagedDto<Batch>>>

    fun fundTransferOTP(
        accessToken: String,
        otp: HashMap<String, String>
    ): Single<Response<FundTransferVerify>>

    fun resendOTPFundTransfer(
        accessToken: String,
        resendOTPForm: ResendOTPForm
    ): Single<Response<Auth>>

    fun cancelFundTransferTransaction(
        accessToken: String,
        cancelFundTransferTransactionForm: CancelFundTransferTransactionForm
    ): Single<Response<CancelFundTransferTransactionResponse>>

    /* UBP */
    fun fundTransferUBP(
        accessToken: String,
        fundTransferUBPForm: FundTransferUBPForm
    ): Single<Response<FundTransferVerify>>

    /* PESONET */
    fun fundTransferPesoNet(
        accessToken: String,
        fundTransferPesoNetForm: FundTransferPesoNetForm
    ): Single<Response<FundTransferVerify>>

    fun getPurposes(accessToken: String): Single<Response<MutableList<Purpose>>>

    /* INSTAPAY */
    fun fundTransferInstapay(
        accessToken: String,
        fundTransferInstaPayForm: FundTransferInstaPayForm
    ): Single<Response<FundTransferVerify>>

    fun getInstaPayPurposes(accessToken: String): Single<Response<MutableList<Purpose>>>

    /* SWIFT */
    fun fundTransferSwift(
        accessToken: String,
        fundTransferSwiftForm: FundTransferSwiftForm
    ): Single<Response<FundTransferVerify>>

    /* BENEFICIARY */
    fun getBeneficiaries(
        accessToken: String,
        channelId: String?,
        accountId: String?,
        pageable: Pageable
    ): Single<Response<PagedDto<Beneficiary>>>

    fun getBeneficiaryDetail(
        accessToken: String,
        id: String
    ): Single<Response<BeneficiaryDetailDto>>

    fun createBeneficiary(
        accessToken: String,
        apiVersion: String,
        beneficiaryForm: BeneficiaryForm
    ): Single<Response<CreationBeneficiaryDto>>

    fun updateBeneficiary(
        accessToken: String,
        apiVersion: String,
        id: String,
        beneficiaryForm: BeneficiaryForm
    ): Single<Response<CreationBeneficiaryDto>>

    fun deleteBeneficiary(accessToken: String, id: String): Single<Response<Message>>

    fun getBeneficiaryDetailActivityLogs(
        accessToken: String,
        id: String
    ): Single<Response<MutableList<ActivityLogDto>>>

    /* BANKS */
    fun getPesoNetBanks(accessToken: String): Single<Response<MutableList<Bank>>>

    fun getInstaPayBanks(accessToken: String): Single<Response<MutableList<Bank>>>

    fun getPDDTSBanks(accessToken: String): Single<Response<MutableList<Bank>>>

    fun getSwiftBanks(
        accessToken: String,
        pageable: Pageable
    ): Single<Response<PagedDto<SwiftBank>>>

    /* SCHEDULED */
    fun getManageScheduledTransfers(
        accessToken: String,
        status: String,
        pageable: Pageable
    ): Single<Response<PagedDto<Transaction>>>

    fun deleteScheduledTransfer(
        accessToken: String,
        scheduledTransferDeletionForm: ScheduledTransferDeletionForm
    ): Single<Response<Message>>

    /* CWT */
    fun getFundTransferCWTHeader(accessToken: String): Single<Response<MutableList<CWTDetail>>>

    fun getFundTransferCWT(
        accessToken: String,
        pageable: Pageable,
        id: String
    ): Single<Response<PagedDto<MutableList<CWTDetail>>>>

    fun getFundTransferINVHeader(accessToken: String): Single<Response<MutableList<CWTDetail>>>

    fun getFundTransferINV(
        accessToken: String,
        pageable: Pageable,
        id: String
    ): Single<Response<PagedDto<MutableList<CWTDetail>>>>
}
