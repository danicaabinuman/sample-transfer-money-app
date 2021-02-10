package com.unionbankph.corporate.bills_payment.data.source.remote.impl

import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.auth.data.form.ResendOTPForm
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.bills_payment.data.form.BillsPaymentForm
import com.unionbankph.corporate.bills_payment.data.form.CancelBillsPaymentTransactionForm
import com.unionbankph.corporate.bills_payment.data.form.FrequentBillerForm
import com.unionbankph.corporate.bills_payment.data.model.Biller
import com.unionbankph.corporate.bills_payment.data.model.BillerField
import com.unionbankph.corporate.bills_payment.data.model.BillsPaymentValidate
import com.unionbankph.corporate.bills_payment.data.model.BillsPaymentVerify
import com.unionbankph.corporate.bills_payment.data.model.CancelBillsPaymentTransactionResponse
import com.unionbankph.corporate.bills_payment.data.model.CreationFrequentBillerDto
import com.unionbankph.corporate.bills_payment.data.model.FrequentBiller
import com.unionbankph.corporate.bills_payment.data.source.remote.BillsPaymentRemote
import com.unionbankph.corporate.bills_payment.data.source.remote.client.BillsPaymentApiClient
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.fund_transfer.data.model.ActivityLogDto
import io.reactivex.Single
import retrofit2.Response
import retrofit2.Retrofit
import javax.annotation.concurrent.ThreadSafe
import javax.inject.Inject

/**
 * Created by herald25santos on 2020-01-16
 */
class BillsPaymentRemoteImpl
@Inject
constructor(
    retrofit: Retrofit
) : BillsPaymentRemote {

    private val billsPaymentApiClient: BillsPaymentApiClient =
        retrofit.create(BillsPaymentApiClient::class.java)

    override fun getOrganizationPayments(
        accessToken: String,
        startDate: String?,
        endDate: String?,
        status: String?,
        biller: String?,
        pageable: Pageable
    ): Single<Response<PagedDto<Transaction>>> {
        return billsPaymentApiClient.getOrganizationPayments(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            pageable.page,
            pageable.size,
            pageable.filter,
            startDate,
            endDate,
            status,
            biller
        )
    }

    override fun getBillsPaymentDetail(
        accessToken: String,
        id: String
    ): Single<Response<Transaction>> {
        return billsPaymentApiClient.getBillsPaymentDetail(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }

    override fun getBillsPaymentActivityLogs(
        accessToken: String,
        id: String
    ): Single<Response<MutableList<ActivityLogDto>>> {
        return billsPaymentApiClient.getBillsPaymentActivityLogs(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }

    override fun validateBillsPayment(
        accessToken: String,
        billsPaymentForm: BillsPaymentForm
    ): Single<Response<BillsPaymentValidate>> {
        return billsPaymentApiClient.validateBillsPayment(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            billsPaymentForm
        )
    }

    override fun billsPayment(
        accessToken: String,
        billsPaymentForm: BillsPaymentForm
    ): Single<Response<BillsPaymentVerify>> {
        return billsPaymentApiClient.submitBillsPayment(
            accessToken,
            API_V4,
            billsPaymentForm
        )
    }

    override fun billsPaymentOTP(
        accessToken: String,
        otp: HashMap<String, String>
    ): Single<Response<BillsPaymentVerify>> {
        return billsPaymentApiClient.billsPaymentOTP(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            otp
        )
    }

    override fun resendOTPBillsPayment(
        accessToken: String,
        resendOTPForm: ResendOTPForm
    ): Single<Response<Auth>> {
        return billsPaymentApiClient.resendOTPBillsPayment(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            resendOTPForm
        )
    }

    override fun getBillerFields(
        accessToken: String,
        billerId: String
    ): Single<Response<MutableList<BillerField>>> {
        return billsPaymentApiClient.getBillerFields(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            billerId
        )
    }

    override fun getBillers(
        accessToken: String,
        type: String
    ): Single<Response<MutableList<Biller>>> {
        return billsPaymentApiClient.getBillers(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            type
        )
    }

    override fun getFrequentBillers(
        accessToken: String,
        accountId: String?,
        pageable: Pageable
    ): Single<Response<PagedDto<FrequentBiller>>> {
        return billsPaymentApiClient.getFrequentBillers(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            accountId,
            pageable.page,
            pageable.size,
            pageable.filter
        )
    }

    override fun getFrequentBillerActivityLogs(
        accessToken: String,
        id: String
    ): Single<Response<MutableList<ActivityLogDto>>> {
        return billsPaymentApiClient.getFrequentBillerActivityLogs(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }

    override fun createFrequentBiller(
        accessToken: String,
        frequentBillerForm: FrequentBillerForm
    ): Single<Response<CreationFrequentBillerDto>> {
        return billsPaymentApiClient.createFrequentBiller(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            frequentBillerForm
        )
    }

    override fun updateFrequentBiller(
        accessToken: String,
        id: String,
        frequentBillerForm: FrequentBillerForm
    ): Single<Response<CreationFrequentBillerDto>> {
        return billsPaymentApiClient.updateFrequentBiller(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id,
            frequentBillerForm
        )
    }

    override fun getFrequentBillerDetail(
        accessToken: String,
        id: String
    ): Single<Response<FrequentBiller>> {
        return billsPaymentApiClient.getFrequentBillerDetail(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }

    override fun deleteFrequentBiller(accessToken: String, id: String): Single<Response<Message>> {
        return billsPaymentApiClient.deleteFrequentBiller(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }

    override fun cancelBillsPaymentTransaction(
        accessToken: String,
        cancelBillsPaymentTransactionForm: CancelBillsPaymentTransactionForm
    ): Single<Response<CancelBillsPaymentTransactionResponse>> {
        return billsPaymentApiClient.cancelFundTransferTransaction(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            cancelBillsPaymentTransactionForm
        )
    }

    @ThreadSafe
    companion object {
        const val API_V4 = "v4"
    }
}
