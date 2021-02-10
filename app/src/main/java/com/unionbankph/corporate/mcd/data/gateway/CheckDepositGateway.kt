package com.unionbankph.corporate.mcd.data.gateway

import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.fund_transfer.data.model.Bank
import com.unionbankph.corporate.mcd.data.form.CheckDepositForm
import com.unionbankph.corporate.mcd.data.model.CheckDeposit
import com.unionbankph.corporate.mcd.data.model.CheckDepositActivityLog
import com.unionbankph.corporate.mcd.data.model.CheckDepositUpload
import io.reactivex.Single
import java.io.File

interface CheckDepositGateway {

    fun checkDepositUploadFile(file: File, fileKey: String, id: String?): Single<CheckDepositUpload>

    fun checkDeposit(checkDepositForm: CheckDepositForm): Single<CheckDeposit>

    fun getCheckDeposits(
        pageable: Pageable,
        checkNumber: String?,
        amount: String?,
        dateOnCheckFrom: String?,
        dateOnCheckTo: String?,
        depositAccount: String?,
        status: String?,
        dateCreatedFrom: String?,
        dateCreatedTo: String?
    ): Single<PagedDto<CheckDeposit>>

    fun getCheckDeposit(id: String): Single<CheckDeposit>

    fun getCheckDepositActivityLogs(id: String): Single<MutableList<CheckDepositActivityLog>>

    fun getCheckDepositsTestData(): Single<PagedDto<CheckDeposit>>

    fun getCheckDepositBanks(remitType: String? = null): Single<MutableList<Bank>>
}
