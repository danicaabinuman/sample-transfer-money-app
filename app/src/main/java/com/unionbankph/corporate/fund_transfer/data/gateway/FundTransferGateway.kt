package com.unionbankph.corporate.fund_transfer.data.gateway

import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.auth.data.form.ResendOTPForm
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.fund_transfer.data.form.*
import com.unionbankph.corporate.fund_transfer.data.model.*
import io.reactivex.Single

/**
 * Created by herald25santos on 2020-01-15
 */
interface FundTransferGateway {

    fun getOrganizationTransfers(
        startDate: String?,
        endDate: String?,
        channelIds: String?,
        status: String?,
        pageable: Pageable
    ): Single<PagedDto<Transaction>>

    fun getFundTransferDetail(id: String): Single<Transaction>

    fun getFundTransferActivityLogs(id: String): Single<MutableList<ActivityLogDto>>

    fun getBatchTransaction(pageable: Pageable, id: String): Single<PagedDto<Batch>>

    fun getOrganizationTransfersTestData(): Single<PagedDto<Transaction>>

    fun fundTransferOTP(otp: HashMap<String, String>): Single<FundTransferVerify>

    fun resendOTPFundTransfer(resendOTPForm: ResendOTPForm): Single<Auth>

    fun cancelFundTransferTransaction(
        cancelFundTransferTransactionForm: CancelFundTransferTransactionForm
    ): Single<CancelFundTransferTransactionResponse>

    /* UBP */
    fun fundTransferUBP(fundTransferUBPForm: FundTransferUBPForm): Single<FundTransferVerify>

    /* PESONET */
    fun fundTransferPesoNet(
        fundTransferPesoNetForm: FundTransferPesoNetForm
    ): Single<FundTransferVerify>

    fun getPurposes(): Single<MutableList<Purpose>>

    /* INSTAPAY */
    fun fundTransferInstaPay(
        fundTransferInstaPayForm: FundTransferInstaPayForm
    ): Single<FundTransferVerify>

    fun getInstaPayPurposes(): Single<MutableList<Purpose>>

    /* SWIFT */
    fun fundTransferSwift(fundTransferSwiftForm: FundTransferSwiftForm): Single<FundTransferVerify>

    /* BENEFICIARY */
    fun getBeneficiaries(
        channelId: String?,
        accountId: String?,
        pageable: Pageable
    ): Single<PagedDto<Beneficiary>>

    fun getBeneficiaryDetail(id: String): Single<BeneficiaryDetailDto>

    fun createBeneficiary(beneficiaryForm: BeneficiaryForm): Single<CreationBeneficiaryDto>

    fun updateBeneficiary(
        id: String,
        beneficiaryForm: BeneficiaryForm
    ): Single<CreationBeneficiaryDto>

    fun deleteBeneficiary(id: String): Single<Message>

    fun getBeneficiaryDetailActivityLogs(id: String): Single<MutableList<ActivityLogDto>>

    fun getBeneficiariesTestData(): Single<PagedDto<Beneficiary>>

    /* BANKS */
    fun getPesoNetBanks(): Single<MutableList<Bank>>

    fun getInstaPayBanks(): Single<MutableList<Bank>>

    fun getPDDTSBanks(): Single<MutableList<Bank>>

    fun getSwiftBanks(pageable: Pageable): Single<PagedDto<SwiftBank>>

    /* SCHEDULED */
    fun getManageScheduledTransfers(
        status: String,
        pageable: Pageable
    ): Single<PagedDto<Transaction>>

    fun getScheduledTransferTutorialTestData(): Single<PagedDto<Transaction>>

    fun deleteScheduledTransfer(
        scheduledTransferDeletionForm: ScheduledTransferDeletionForm
    ): Single<Message>

    /* CWT */
    fun getFundTransferCWTHeader(): Single<MutableList<CWTDetail>>

    fun getFundTransferCWT(pageable: Pageable, id: String): Single<PagedDto<MutableList<CWTDetail>>>

    fun getFundTransferINVHeader(): Single<MutableList<CWTDetail>>

    fun getFundTransferINV(pageable: Pageable, id: String): Single<PagedDto<MutableList<CWTDetail>>>
}
