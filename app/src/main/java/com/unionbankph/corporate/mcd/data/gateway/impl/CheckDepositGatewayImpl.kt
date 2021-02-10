package com.unionbankph.corporate.mcd.data.gateway.impl

import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.fund_transfer.data.model.Bank
import com.unionbankph.corporate.mcd.data.form.CheckDepositForm
import com.unionbankph.corporate.mcd.data.gateway.CheckDepositGateway
import com.unionbankph.corporate.mcd.data.model.CheckDeposit
import com.unionbankph.corporate.mcd.data.model.CheckDepositActivityLog
import com.unionbankph.corporate.mcd.data.model.CheckDepositUpload
import com.unionbankph.corporate.mcd.data.source.local.CheckDepositCache
import com.unionbankph.corporate.mcd.data.source.remote.CheckDepositRemote
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.Single
import java.io.File
import javax.inject.Inject

class CheckDepositGatewayImpl
@Inject
constructor(
    private val responseProvider: ResponseProvider,
    private val checkDepositRemote: CheckDepositRemote,
    private val checkDepositCache: CheckDepositCache,
    private val settingsGateway: SettingsGateway
) : CheckDepositGateway {

    override fun checkDepositUploadFile(
        file: File,
        fileKey: String,
        id: String?
    ): Single<CheckDepositUpload> {
        return settingsGateway.getAccessToken()
            .flatMap { checkDepositRemote.checkDepositUploadFile(it, file, fileKey, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun checkDeposit(checkDepositForm: CheckDepositForm): Single<CheckDeposit> {
        return settingsGateway.getAccessToken()
            .flatMap { checkDepositRemote.checkDeposit(it, checkDepositForm) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getCheckDeposits(
        pageable: Pageable,
        checkNumber: String?,
        amount: String?,
        dateOnCheckFrom: String?,
        dateOnCheckTo: String?,
        depositAccount: String?,
        status: String?,
        dateCreatedFrom: String?,
        dateCreatedTo: String?
    ): Single<PagedDto<CheckDeposit>> {
        return settingsGateway.getAccessToken()
            .flatMap {
                checkDepositRemote.getCheckDeposits(
                    it,
                    pageable,
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
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getCheckDeposit(id: String): Single<CheckDeposit> {
        return settingsGateway.getAccessToken()
            .flatMap { checkDepositRemote.getCheckDeposit(it, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getCheckDepositActivityLogs(id: String): Single<MutableList<CheckDepositActivityLog>> {
        return settingsGateway.getAccessToken()
            .flatMap { checkDepositRemote.getCheckDepositActivityLogs(it, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getCheckDepositsTestData(): Single<PagedDto<CheckDeposit>> {
        return checkDepositCache.getCheckDepositsTestData()
    }

    override fun getCheckDepositBanks(remitType: String?): Single<MutableList<Bank>> {
        return settingsGateway.getAccessToken()
            .flatMap { checkDepositRemote.getCheckDepositBanks(it, remitType) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

}
