package com.unionbankph.corporate.mcd.data.source.remote.impl

import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.common.data.constant.ApiVersionEnum
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.fund_transfer.data.model.Bank
import com.unionbankph.corporate.mcd.data.form.CheckDepositForm
import com.unionbankph.corporate.mcd.data.model.CheckDeposit
import com.unionbankph.corporate.mcd.data.model.CheckDepositActivityLog
import com.unionbankph.corporate.mcd.data.model.CheckDepositUpload
import com.unionbankph.corporate.mcd.data.source.remote.CheckDepositRemote
import com.unionbankph.corporate.mcd.data.source.remote.client.CheckDepositApiClient
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import javax.inject.Inject

class CheckDepositRemoteImpl
@Inject
constructor(
    retrofit: Retrofit
) : CheckDepositRemote {

    private val checkDepositApiClient: CheckDepositApiClient =
        retrofit.create(CheckDepositApiClient::class.java)

    override fun checkDepositUploadFile(
        accessToken: String,
        file: File,
        fileKey: String,
        id: String?
    ): Single<Response<CheckDepositUpload>> {
        val fileRequestBody = RequestBody.create(MediaType.parse("image/*"), file)
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM).apply {
            addFormDataPart("file", file.name, fileRequestBody)
        }.build()
        return checkDepositApiClient.checkDepositUploadFile(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            fileKey,
            id,
            requestBody
        )
    }

    override fun checkDeposit(
        accessToken: String,
        checkDepositForm: CheckDepositForm
    ): Single<Response<CheckDeposit>> {
        return checkDepositApiClient.checkDeposit(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            checkDepositForm
        )
    }

    override fun getCheckDeposits(
        accessToken: String,
        pageable: Pageable,
        checkNumber: String?,
        amount: String?,
        dateOnCheckFrom: String?,
        dateOnCheckTo: String?,
        depositAccount: String?,
        status: String?,
        dateCreatedFrom: String?,
        dateCreatedTo: String?
    ): Single<Response<PagedDto<CheckDeposit>>> {
        return checkDepositApiClient.getCheckDeposits(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            pageable.page,
            pageable.size,
            pageable.filter,
            checkNumber,
            amount,
            dateOnCheckFrom,
            dateOnCheckTo,
            depositAccount,
            status,
            dateCreatedFrom,
            dateCreatedTo
        )
    }

    override fun getCheckDeposit(accessToken: String, id: String): Single<Response<CheckDeposit>> {
        return checkDepositApiClient.getCheckDeposit(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }

    override fun getCheckDepositActivityLogs(
        accessToken: String,
        id: String
    ): Single<Response<MutableList<CheckDepositActivityLog>>> {
        return checkDepositApiClient.getCheckDepositActivityLogs(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }

    override fun getCheckDepositBanks(
        accessToken: String,
        remitType: String?
    ): Single<Response<MutableList<Bank>>> {
        return checkDepositApiClient.getCheckDepositBanks(
            accessToken,
            ApiVersionEnum.V4.value,
            remitType
        )
    }

}
