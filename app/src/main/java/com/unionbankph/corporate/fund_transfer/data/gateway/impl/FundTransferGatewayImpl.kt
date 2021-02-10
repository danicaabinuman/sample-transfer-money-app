package com.unionbankph.corporate.fund_transfer.data.gateway.impl

import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.auth.data.form.ResendOTPForm
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.common.data.constant.ApiVersionEnum
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.corporate.domain.gateway.CorporateGateway
import com.unionbankph.corporate.fund_transfer.data.form.*
import com.unionbankph.corporate.fund_transfer.data.gateway.FundTransferGateway
import com.unionbankph.corporate.fund_transfer.data.model.*
import com.unionbankph.corporate.fund_transfer.data.source.local.FundTransferCache
import com.unionbankph.corporate.fund_transfer.data.source.remote.FundTransferRemote
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.settings.domain.constant.FeaturesEnum
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import javax.inject.Inject

/**
 * Created by herald25santos on 2020-01-15
 */
class FundTransferGatewayImpl
@Inject
constructor(
    private val responseProvider: ResponseProvider,
    private val fundTransferRemote: FundTransferRemote,
    private val fundTransferCache: FundTransferCache,
    private val settingsGateway: SettingsGateway,
    private val corporateGateway: CorporateGateway
) : FundTransferGateway {

    override fun getOrganizationTransfers(
        startDate: String?,
        endDate: String?,
        channelIds: String?,
        status: String?,
        pageable: Pageable
    ): Single<PagedDto<Transaction>> {
        return settingsGateway.getAccessToken()
            .flatMap {
                fundTransferRemote.getOrganizationTransfers(
                    it,
                    startDate,
                    endDate,
                    channelIds,
                    status,
                    pageable
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getOrganizationTransfersTestData(): Single<PagedDto<Transaction>> {
        return fundTransferCache.getOrganizationTransfersTestData()
    }

    override fun fundTransferOTP(otp: HashMap<String, String>): Single<FundTransferVerify> {
        return settingsGateway.getAccessToken()
            .flatMap { fundTransferRemote.fundTransferOTP(it, otp) }
            .flatMap { responseProvider.executeResponseSingle(it) }
            .flatMap { fundTransferVerify ->
                corporateGateway.getCorporateUser().map {
                    fundTransferVerify.mobileNumber = it.mobileNumber
                    fundTransferVerify
                }
            }
    }

    override fun resendOTPFundTransfer(resendOTPForm: ResendOTPForm): Single<Auth> {
        return settingsGateway.getAccessToken()
            .flatMap { fundTransferRemote.resendOTPFundTransfer(it, resendOTPForm) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun cancelFundTransferTransaction(
        cancelFundTransferTransactionForm: CancelFundTransferTransactionForm
    ): Single<CancelFundTransferTransactionResponse> {
        return settingsGateway.getAccessToken()
            .flatMap {
                fundTransferRemote.cancelFundTransferTransaction(
                    it,
                    cancelFundTransferTransactionForm
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun fundTransferUBP(
        fundTransferUBPForm: FundTransferUBPForm
    ): Single<FundTransferVerify> {
        return corporateGateway.getRole()
            .zipWith(settingsGateway.getAccessToken())
            .flatMap {
                fundTransferUBPForm.organizationId = it.first.organizationId
                fundTransferRemote.fundTransferUBP(
                    it.second,
                    fundTransferUBPForm
                )
            }.flatMap { responseProvider.executeResponseSingle(it) }
            .flatMap { fundTransferVerify ->
                corporateGateway.getCorporateUser().map {
                    fundTransferVerify.mobileNumber = it.mobileNumber
                    fundTransferVerify
                }
            }
    }

    override fun getPurposes(): Single<MutableList<Purpose>> {
        return settingsGateway.getAccessToken()
            .flatMap { fundTransferRemote.getPurposes(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getInstaPayPurposes(): Single<MutableList<Purpose>> {
        return settingsGateway.getAccessToken()
            .flatMap { fundTransferRemote.getInstaPayPurposes(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun fundTransferPesoNet(
        fundTransferPesoNetForm: FundTransferPesoNetForm
    ): Single<FundTransferVerify> {
        return corporateGateway.getRole()
            .zipWith(settingsGateway.getAccessToken())
            .flatMap {
                fundTransferPesoNetForm.organizationId = it.first.organizationId
                fundTransferRemote.fundTransferPesoNet(
                    it.second,
                    fundTransferPesoNetForm
                )
            }.flatMap { responseProvider.executeResponseSingle(it) }
            .flatMap { fundTransferVerify ->
                corporateGateway.getCorporateUser().map {
                    fundTransferVerify.mobileNumber = it.mobileNumber
                    fundTransferVerify
                }
            }
    }

    override fun fundTransferInstaPay(
        fundTransferInstaPayForm: FundTransferInstaPayForm
    ): Single<FundTransferVerify> {
        return corporateGateway.getRole()
            .zipWith(settingsGateway.getAccessToken())
            .flatMap {
                fundTransferInstaPayForm.organizationId = it.first.organizationId
                fundTransferRemote.fundTransferInstapay(
                    it.second,
                    fundTransferInstaPayForm
                )
            }.flatMap { responseProvider.executeResponseSingle(it) }
            .flatMap { fundTransferVerify ->
                corporateGateway.getCorporateUser().map {
                    fundTransferVerify.mobileNumber = it.mobileNumber
                    fundTransferVerify
                }
            }
    }

    override fun fundTransferSwift(fundTransferSwiftForm: FundTransferSwiftForm): Single<FundTransferVerify> {
        return corporateGateway.getRole()
            .zipWith(settingsGateway.getAccessToken())
            .flatMap {
                fundTransferSwiftForm.organizationId = it.first.organizationId
                fundTransferRemote.fundTransferSwift(
                    it.second,
                    fundTransferSwiftForm
                )
            }.flatMap { responseProvider.executeResponseSingle(it) }
            .flatMap { fundTransferVerify ->
                corporateGateway.getCorporateUser().map {
                    fundTransferVerify.mobileNumber = it.mobileNumber
                    fundTransferVerify
                }
            }
    }

    override fun getFundTransferDetail(id: String): Single<Transaction> {
        return settingsGateway.getAccessToken()
            .flatMap { fundTransferRemote.getFundTransferDetail(it, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getFundTransferActivityLogs(id: String): Single<MutableList<ActivityLogDto>> {
        return settingsGateway.getAccessToken()
            .flatMap { fundTransferRemote.getFundTransferActivityLogs(it, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getBatchTransaction(pageable: Pageable, id: String): Single<PagedDto<Batch>> {
        return settingsGateway.getAccessToken()
            .flatMap { fundTransferRemote.getBatchTransaction(it, pageable, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getBeneficiaries(
        channelId: String?,
        accountId: String?,
        pageable: Pageable
    ): Single<PagedDto<Beneficiary>> {
        return settingsGateway.getAccessToken()
            .flatMap { fundTransferRemote.getBeneficiaries(it, channelId, accountId, pageable) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getBeneficiaryDetail(id: String): Single<BeneficiaryDetailDto> {
        return settingsGateway.getAccessToken()
            .flatMap { fundTransferRemote.getBeneficiaryDetail(it, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun createBeneficiary(beneficiaryForm: BeneficiaryForm): Single<CreationBeneficiaryDto> {
        return settingsGateway.getAccessToken()
            .zipWith(settingsGateway.isEnabledFeature(FeaturesEnum.BENEFICIARY_V4))
            .flatMap {
                val apiVersion: String
                if (it.second) {
                    apiVersion = ApiVersionEnum.V4.value
                } else {
                    apiVersion = BuildConfig.CLIENT_API_VERSION
                    beneficiaryForm.brstnCode = null
                    beneficiaryForm.firmCode = null
                }
                return@flatMap fundTransferRemote.createBeneficiary(
                    it.first,
                    apiVersion,
                    beneficiaryForm
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun updateBeneficiary(
        id: String,
        beneficiaryForm: BeneficiaryForm
    ): Single<CreationBeneficiaryDto> {
        return settingsGateway.getAccessToken()
            .zipWith(settingsGateway.isEnabledFeature(FeaturesEnum.BENEFICIARY_V4))
            .flatMap {
                val apiVersion: String
                if (it.second) {
                    apiVersion = ApiVersionEnum.V4.value
                } else {
                    apiVersion = BuildConfig.CLIENT_API_VERSION
                    beneficiaryForm.brstnCode = null
                    beneficiaryForm.firmCode = null
                }
                return@flatMap fundTransferRemote.updateBeneficiary(
                    it.first,
                    apiVersion,
                    id,
                    beneficiaryForm
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun deleteBeneficiary(id: String): Single<Message> {
        return settingsGateway.getAccessToken()
            .flatMap { fundTransferRemote.deleteBeneficiary(it, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getBeneficiaryDetailActivityLogs(id: String): Single<MutableList<ActivityLogDto>> {
        return settingsGateway.getAccessToken()
            .flatMap { fundTransferRemote.getBeneficiaryDetailActivityLogs(it, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getBeneficiariesTestData(): Single<PagedDto<Beneficiary>> {
        return fundTransferCache.getBeneficiariesTestData()
    }

    override fun getPesoNetBanks(): Single<MutableList<Bank>> {
        return settingsGateway.getAccessToken()
            .flatMap { fundTransferRemote.getPesoNetBanks(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getInstaPayBanks(): Single<MutableList<Bank>> {
        return settingsGateway.getAccessToken()
            .flatMap { fundTransferRemote.getInstaPayBanks(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getPDDTSBanks(): Single<MutableList<Bank>> {
        return settingsGateway.getAccessToken()
            .flatMap { fundTransferRemote.getPDDTSBanks(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getSwiftBanks(pageable: Pageable): Single<PagedDto<SwiftBank>> {
        return settingsGateway.getAccessToken()
            .flatMap { fundTransferRemote.getSwiftBanks(it, pageable) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getManageScheduledTransfers(
        status: String,
        pageable: Pageable
    ): Single<PagedDto<Transaction>> {
        return settingsGateway.getAccessToken()
            .flatMap { fundTransferRemote.getManageScheduledTransfers(it, status, pageable) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getScheduledTransferTutorialTestData(): Single<PagedDto<Transaction>> {
        return fundTransferCache.getScheduledTransferTutorialTestData()
    }

    override fun deleteScheduledTransfer(
        scheduledTransferDeletionForm: ScheduledTransferDeletionForm
    ): Single<Message> {
        return settingsGateway.getAccessToken()
            .flatMap {
                fundTransferRemote.deleteScheduledTransfer(it, scheduledTransferDeletionForm)
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getFundTransferCWTHeader(): Single<MutableList<CWTDetail>> {
        return settingsGateway.getAccessToken()
            .flatMap { fundTransferRemote.getFundTransferCWTHeader(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getFundTransferCWT(
        pageable: Pageable,
        id: String
    ): Single<PagedDto<MutableList<CWTDetail>>> {
        return settingsGateway.getAccessToken()
            .flatMap { fundTransferRemote.getFundTransferCWT(it, pageable, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getFundTransferINVHeader(): Single<MutableList<CWTDetail>> {
        return settingsGateway.getAccessToken()
            .flatMap { fundTransferRemote.getFundTransferINVHeader(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getFundTransferINV(
        pageable: Pageable,
        id: String
    ): Single<PagedDto<MutableList<CWTDetail>>> {
        return settingsGateway.getAccessToken()
            .flatMap { fundTransferRemote.getFundTransferINV(it, pageable, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }
}
