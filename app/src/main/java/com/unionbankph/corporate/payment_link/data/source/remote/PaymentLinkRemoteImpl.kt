package com.unionbankph.corporate.payment_link.data.source.remote

import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.payment_link.data.source.api.PaymentLinkApiClient
import com.unionbankph.corporate.payment_link.domain.model.form.CreateMerchantForm
import com.unionbankph.corporate.payment_link.domain.model.form.GeneratePaymentLinkForm
import com.unionbankph.corporate.payment_link.domain.model.form.PutPaymentLinkStatusForm
import com.unionbankph.corporate.payment_link.domain.model.form.RMOBusinessInformationForm
import com.unionbankph.corporate.payment_link.domain.model.response.*
import io.reactivex.Single
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

class PaymentLinkRemoteImpl
@Inject constructor(
        retrofit: Retrofit
) : PaymentLinkRemote {
    private val paymentLinkApiClient: PaymentLinkApiClient =
            retrofit.create(PaymentLinkApiClient::class.java)

    override fun generatePaymentLink(
        accessToken: String,
        generatePaymentLinkForm: GeneratePaymentLinkForm
    ): Single<Response<GeneratePaymentLinkResponse>> {
        return paymentLinkApiClient.generatePaymentLink(accessToken,
            BuildConfig.MSME_CLIENT_ID,
            BuildConfig.MSME_CLIENT_SECRET,
            BuildConfig.MSME_CLIENT_API_VERSION,
            generatePaymentLinkForm
        )
    }

    override fun createMerchant(
        accessToken: String,
        createMerchantForm: CreateMerchantForm
    ): Single<Response<CreateMerchantResponse>> {
        return paymentLinkApiClient.createMerchant(
            accessToken,
            BuildConfig.MSME_CLIENT_ID,
            BuildConfig.MSME_CLIENT_SECRET,
            BuildConfig.MSME_CLIENT_API_VERSION,
            createMerchantForm
        )
    }

    override fun putBusinessInformation(
        accessToken: String,
        rmoBusinessInformationForm: RMOBusinessInformationForm
    ): Single<Response<RMOBusinessInformationResponse>> {
        return paymentLinkApiClient.putBusinessInformation(
            accessToken,
            BuildConfig.MSME_CLIENT_ID,
            BuildConfig.MSME_CLIENT_SECRET,
            BuildConfig.MSME_CLIENT_API_VERSION,
            rmoBusinessInformationForm
        )
    }

    override fun getPaymentLinkListPaginated(
            accessToken: String,
            page: String,
            itemsPerPage: String
    ): Single<Response<GetPaymentLinkListPaginatedResponse>> {
        return paymentLinkApiClient.getPaymentLinkListPaginated(
                accessToken,
                BuildConfig.MSME_CLIENT_ID,
                BuildConfig.MSME_CLIENT_SECRET,
                BuildConfig.MSME_CLIENT_API_VERSION,
                page,
                itemsPerPage
        )
    }

    override fun getPaymentLinkListByReferenceNumber(
            accessToken: String,
            page: String,
            itemsPerPage: String,
            referenceNumber: String
    ): Single<Response<GetPaymentLinkListPaginatedResponse>> {
        return paymentLinkApiClient.getPaymentLinkByReferenceNumber(
                accessToken,
                BuildConfig.MSME_CLIENT_ID,
                BuildConfig.MSME_CLIENT_SECRET,
                BuildConfig.MSME_CLIENT_API_VERSION,
                page,
                itemsPerPage,
                referenceNumber
        )
    }

    override fun getPaymentLinkByReferenceId(
            accessToken: String,
            referenceId: String

    ): Single<Response<GetPaymentLinkByReferenceIdResponse>> {
        return paymentLinkApiClient.getPaymentLinkByReferenceId(
                accessToken,
                BuildConfig.MSME_CLIENT_ID,
                BuildConfig.MSME_CLIENT_SECRET,
                BuildConfig.MSME_CLIENT_API_VERSION,
                referenceId
        )
    }

    override fun putPaymentLinkStatus(
            accessToken: String,
            transactionId: String,
            putPaymentLinkStatusForm: PutPaymentLinkStatusForm
    ): Single<Response<PutPaymentLinkStatusResponse>> {
        return paymentLinkApiClient.putPaymentLinkStatus(
                accessToken,
                BuildConfig.MSME_CLIENT_ID,
                BuildConfig.MSME_CLIENT_SECRET,
                BuildConfig.MSME_CLIENT_API_VERSION,
                transactionId,
                putPaymentLinkStatusForm
        )
    }

    override fun validateMerchantByOrganization(
        accessToken: String
    ): Single<Response<ValidateMerchantByOrganizationResponse>> {
        return paymentLinkApiClient.validateMerchantByOrganization(
            accessToken,
            BuildConfig.MSME_CLIENT_ID,
            BuildConfig.MSME_CLIENT_SECRET,
            BuildConfig.MSME_CLIENT_API_VERSION
        )
    }


}