package com.unionbankph.corporate.branch.data.source.remote.impl

import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.branch.data.model.Branch
import com.unionbankph.corporate.branch.data.model.BranchVisitDto
import com.unionbankph.corporate.branch.data.model.BranchVisitSubmitDto
import com.unionbankph.corporate.branch.data.source.remote.BranchVisitRemote
import com.unionbankph.corporate.branch.data.source.remote.client.BranchVisitApiClient
import com.unionbankph.corporate.branch.presentation.model.BranchVisitForm
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import io.reactivex.Single
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

class BranchVisitRemoteImpl
@Inject
constructor(
    retrofit: Retrofit
) : BranchVisitRemote {

    private val branchVisitApiClient: BranchVisitApiClient =
        retrofit.create(BranchVisitApiClient::class.java)

    override fun getBranchVisits(
        accessToken: String,
        pageable: Pageable
    ): Single<Response<PagedDto<BranchVisitDto>>> {
        return branchVisitApiClient.getBranchVisits(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            pageable.page,
            pageable.size,
            pageable.filter
        )
    }

    override fun getBranchVisit(accessToken: String, id: String): Single<Response<BranchVisitDto>> {
        return branchVisitApiClient.getBranchVisit(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }

    override fun getBranches(accessToken: String): Single<Response<MutableList<Branch>>> {
        return branchVisitApiClient.getBranches(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun submitBranchVisitTransaction(
        accessToken: String,
        branchVisitForm: BranchVisitForm
    ): Single<Response<BranchVisitSubmitDto>> {
        return branchVisitApiClient.submitBranchVisitTransaction(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            branchVisitForm
        )
    }
}
