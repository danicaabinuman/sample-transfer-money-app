package com.unionbankph.corporate.bills_payment.data.gateway.impl

import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.auth.data.form.ResendOTPForm
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.bills_payment.data.form.BillsPaymentForm
import com.unionbankph.corporate.bills_payment.data.form.CancelBillsPaymentTransactionForm
import com.unionbankph.corporate.bills_payment.data.form.FrequentBillerForm
import com.unionbankph.corporate.bills_payment.data.gateway.BillsPaymentGateway
import com.unionbankph.corporate.bills_payment.data.model.Biller
import com.unionbankph.corporate.bills_payment.data.model.BillerField
import com.unionbankph.corporate.bills_payment.data.model.BillsPaymentValidate
import com.unionbankph.corporate.bills_payment.data.model.BillsPaymentVerify
import com.unionbankph.corporate.bills_payment.data.model.CancelBillsPaymentTransactionResponse
import com.unionbankph.corporate.bills_payment.data.model.CreationFrequentBillerDto
import com.unionbankph.corporate.bills_payment.data.model.FrequentBiller
import com.unionbankph.corporate.bills_payment.data.source.local.BillsPaymentCache
import com.unionbankph.corporate.bills_payment.data.source.remote.BillsPaymentRemote
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.corporate.domain.gateway.CorporateGateway
import com.unionbankph.corporate.fund_transfer.data.model.ActivityLogDto
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import javax.inject.Inject

/**
 * Created by herald25santos on 2020-01-16
 */
class BillsPaymentGatewayImpl
@Inject
constructor(
    private val responseProvider: ResponseProvider,
    private val billsPaymentRemote: BillsPaymentRemote,
    private val billsPaymentCache: BillsPaymentCache,
    private val settingsGateway: SettingsGateway,
    private val corporateGateway: CorporateGateway
) : BillsPaymentGateway {

    override fun getOrganizationPayments(
        startDate: String?,
        endDate: String?,
        status: String?,
        biller: String?,
        pageable: Pageable
    ): Single<PagedDto<Transaction>> {
        return settingsGateway.getAccessToken()
            .flatMap {
                billsPaymentRemote.getOrganizationPayments(
                    it,
                    startDate,
                    endDate,
                    status,
                    biller,
                    pageable
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getBillsPaymentDetail(id: String): Single<Transaction> {
        return settingsGateway.getAccessToken()
            .flatMap { billsPaymentRemote.getBillsPaymentDetail(it, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getBillsPaymentActivityLogs(id: String): Single<MutableList<ActivityLogDto>> {
        return settingsGateway.getAccessToken()
            .flatMap { billsPaymentRemote.getBillsPaymentActivityLogs(it, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun validateBillsPayment(billsPaymentForm: BillsPaymentForm): Single<BillsPaymentValidate> {
        return corporateGateway.getRole()
            .zipWith(settingsGateway.getAccessToken())
            .flatMap {
                billsPaymentForm.organizationId = it.first.organizationId
                billsPaymentRemote.validateBillsPayment(it.second, billsPaymentForm)
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun billsPayment(billsPaymentForm: BillsPaymentForm): Single<BillsPaymentVerify> {
        return corporateGateway.getRole()
            .zipWith(settingsGateway.getAccessToken())
            .flatMap {
                billsPaymentForm.organizationId = it.first.organizationId
                billsPaymentRemote.billsPayment(
                    it.second,
                    billsPaymentForm
                )
            }.flatMap { responseProvider.executeResponseSingle(it) }
            .flatMap { billsPaymentVerify ->
                corporateGateway.getCorporateUser().map {
                    billsPaymentVerify.mobileNumber = it.mobileNumber
                    billsPaymentVerify
                }
            }
    }

    override fun billsPaymentOTP(otp: HashMap<String, String>): Single<BillsPaymentVerify> {
        return settingsGateway.getAccessToken()
            .flatMap { billsPaymentRemote.billsPaymentOTP(it, otp) }
            .flatMap { responseProvider.executeResponseSingle(it) }
            .flatMap { billsPaymentVerify ->
                corporateGateway.getCorporateUser().map {
                    billsPaymentVerify.mobileNumber = it.mobileNumber
                    billsPaymentVerify
                }
            }
    }

    override fun resendOTPBillsPayment(resendOTPForm: ResendOTPForm): Single<Auth> {
        return settingsGateway.getAccessToken()
            .flatMap { billsPaymentRemote.resendOTPBillsPayment(it, resendOTPForm) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getOrganizationPaymentsTestData(): Single<PagedDto<Transaction>> {
        return billsPaymentCache.getOrganizationPaymentsTestData()
    }

    override fun cancelBillsPaymentTransaction(
        cancelBillsPaymentTransactionForm: CancelBillsPaymentTransactionForm
    ): Single<CancelBillsPaymentTransactionResponse> {
        return settingsGateway.getAccessToken()
            .flatMap {
                billsPaymentRemote.cancelBillsPaymentTransaction(
                    it,
                    cancelBillsPaymentTransactionForm
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getBillers(type: String): Single<MutableList<Biller>> {
        return settingsGateway.getAccessToken()
            .flatMap { billsPaymentRemote.getBillers(it, type) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getBillerFields(billerId: String): Single<MutableList<BillerField>> {
        return settingsGateway.getAccessToken()
            .flatMap { billsPaymentRemote.getBillerFields(it, billerId) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getFrequentBillers(
        accountId: String?,
        pageable: Pageable
    ): Single<PagedDto<FrequentBiller>> {
        return settingsGateway.getAccessToken()
            .flatMap { billsPaymentRemote.getFrequentBillers(it, accountId, pageable) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getFrequentBillerDetail(id: String): Single<FrequentBiller> {
        return settingsGateway.getAccessToken()
            .flatMap { billsPaymentRemote.getFrequentBillerDetail(it, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getFrequentBillerActivityLogs(id: String): Single<MutableList<ActivityLogDto>> {
        return settingsGateway.getAccessToken()
            .flatMap { billsPaymentRemote.getFrequentBillerActivityLogs(it, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getFrequentBillersTestData(): Single<PagedDto<FrequentBiller>> {
        return billsPaymentCache.getFrequentBillersTestData()
    }

    override fun createFrequentBiller(frequentBillerForm: FrequentBillerForm): Single<CreationFrequentBillerDto> {
        return settingsGateway.getAccessToken()
            .flatMap { billsPaymentRemote.createFrequentBiller(it, frequentBillerForm) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun updateFrequentBiller(
        id: String,
        frequentBillerForm: FrequentBillerForm
    ): Single<CreationFrequentBillerDto> {
        return settingsGateway.getAccessToken()
            .flatMap { billsPaymentRemote.updateFrequentBiller(it, id, frequentBillerForm) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun deleteFrequentBiller(id: String): Single<Message> {
        return settingsGateway.getAccessToken()
            .flatMap { billsPaymentRemote.deleteFrequentBiller(it, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }
}
