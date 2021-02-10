package com.unionbankph.corporate.branch.data.gateway.impl

import com.unionbankph.corporate.branch.data.gateway.BranchVisitGateway
import com.unionbankph.corporate.branch.data.model.Branch
import com.unionbankph.corporate.branch.data.model.BranchVisitDto
import com.unionbankph.corporate.branch.data.model.BranchVisitSubmitDto
import com.unionbankph.corporate.branch.data.model.Client
import com.unionbankph.corporate.branch.data.source.local.BranchVisitCache
import com.unionbankph.corporate.branch.data.source.local.CorporateUserCache
import com.unionbankph.corporate.branch.data.source.remote.BranchVisitRemote
import com.unionbankph.corporate.branch.presentation.model.BranchVisitForm
import com.unionbankph.corporate.branch.presentation.model.Transaction
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import javax.inject.Inject

class BranchVisitGatewayImpl
@Inject
constructor(
    private val responseProvider: ResponseProvider,
    private val branchVisitRemote: BranchVisitRemote,
    private val branchVisitCache: BranchVisitCache,
    private val corporateUserCache: CorporateUserCache,
    private val settingsGateway: SettingsGateway
) : BranchVisitGateway {

    override fun getBranchVisits(pageable: Pageable): Single<PagedDto<BranchVisitDto>> {
        return settingsGateway.getAccessToken()
            .flatMap { branchVisitRemote.getBranchVisits(it, pageable) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getBranchVisit(id: String): Single<BranchVisitDto> {
        return settingsGateway.getAccessToken()
            .flatMap { branchVisitRemote.getBranchVisit(it, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getBranches(): Single<MutableList<Branch>> {
        return settingsGateway.getAccessToken()
            .flatMap { branchVisitRemote.getBranches(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getBranchVisitsTestData(): Single<PagedDto<BranchVisitDto>> {
        return branchVisitCache.getBranchVisitsTestData()
    }

    override fun submitBranchVisitTransaction(
        branchSolId: String,
        branchName: String,
        branchAddress: String,
        transactionDate: String,
        remarks: String,
        transactions: MutableList<Transaction>
    ): Single<BranchVisitSubmitDto> {
        return corporateUserCache.getCorporateUser()
            .zipWith(settingsGateway.getAccessToken()) { t1, t2 -> Pair(t1, t2) }
            .flatMap {
                val corporateUser = it.first
                val accessToken = it.second
                val branchVisitForm = BranchVisitForm()
                val client = Client()
                client.name = corporateUser.firstName
                client.priorityClass = 4
                client.branchSolId = branchSolId
                client.branchName = branchName
                client.branchAddress = branchAddress
                client.phoneNumber = corporateUser.mobileNumber
                client.email = corporateUser.emailAddress
                client.corporationId = corporateUser.id
                client.transactionDate = transactionDate
                client.remarks = remarks
                branchVisitForm.channel = "remotebankingmobile"
                branchVisitForm.client = client
                branchVisitForm.transactions = transactions
                return@flatMap branchVisitRemote.submitBranchVisitTransaction(
                    accessToken,
                    branchVisitForm
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }
}
