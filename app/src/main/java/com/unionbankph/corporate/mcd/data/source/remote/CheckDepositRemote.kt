package com.unionbankph.corporate.mcd.data.source.remote

import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.fund_transfer.data.model.Bank
import com.unionbankph.corporate.mcd.data.form.CheckDepositForm
import com.unionbankph.corporate.mcd.data.model.CheckDeposit
import com.unionbankph.corporate.mcd.data.model.CheckDepositActivityLog
import com.unionbankph.corporate.mcd.data.model.CheckDepositUpload
import io.reactivex.Single
import retrofit2.Response
import java.io.File

interface CheckDepositRemote {

    fun checkDepositUploadFile(
        accessToken: String,
        file: File,
        fileKey: String,
        id: String?
    ): Single<Response<CheckDepositUpload>>

    fun checkDeposit(
        accessToken: String,
        checkDepositForm: CheckDepositForm
    ): Single<Response<CheckDeposit>>

    fun getCheckDeposits(
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
    ): Single<Response<PagedDto<CheckDeposit>>>

    fun getCheckDeposit(accessToken: String, id: String): Single<Response<CheckDeposit>>

    fun getCheckDepositActivityLogs(
        accessToken: String,
        id: String
    ): Single<Response<MutableList<CheckDepositActivityLog>>>

    fun getCheckDepositBanks(
        accessToken: String,
        remitType: String?
    ): Single<Response<MutableList<Bank>>>

}
