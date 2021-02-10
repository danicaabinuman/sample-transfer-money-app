package com.unionbankph.corporate.branch.data.gateway

import com.unionbankph.corporate.branch.data.model.Branch
import com.unionbankph.corporate.branch.data.model.BranchVisitDto
import com.unionbankph.corporate.branch.data.model.BranchVisitSubmitDto
import com.unionbankph.corporate.branch.presentation.model.Transaction
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import io.reactivex.Single

interface BranchVisitGateway {

    fun getBranchVisits(pageable: Pageable): Single<PagedDto<BranchVisitDto>>

    fun getBranchVisit(id: String): Single<BranchVisitDto>

    fun getBranches(): Single<MutableList<Branch>>

    fun getBranchVisitsTestData(): Single<PagedDto<BranchVisitDto>>

    fun submitBranchVisitTransaction(
        branchSolId: String,
        branchName: String,
        branchAddress: String,
        transactionDate: String,
        remarks: String,
        transactions: MutableList<Transaction>
    ): Single<BranchVisitSubmitDto>
}
