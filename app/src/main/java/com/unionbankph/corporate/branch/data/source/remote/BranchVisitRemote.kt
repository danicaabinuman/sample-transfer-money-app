package com.unionbankph.corporate.branch.data.source.remote

import com.unionbankph.corporate.branch.data.model.Branch
import com.unionbankph.corporate.branch.data.model.BranchVisitDto
import com.unionbankph.corporate.branch.data.model.BranchVisitSubmitDto
import com.unionbankph.corporate.branch.presentation.model.BranchVisitForm
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import io.reactivex.Single
import retrofit2.Response

interface BranchVisitRemote {

    fun getBranchVisits(
        accessToken: String,
        pageable: Pageable
    ): Single<Response<PagedDto<BranchVisitDto>>>

    fun getBranchVisit(
        accessToken: String,
        id: String
    ): Single<Response<BranchVisitDto>>

    fun getBranches(accessToken: String): Single<Response<MutableList<Branch>>>

    fun submitBranchVisitTransaction(
        accessToken: String,
        branchVisitForm: BranchVisitForm
    ): Single<Response<BranchVisitSubmitDto>>
}
