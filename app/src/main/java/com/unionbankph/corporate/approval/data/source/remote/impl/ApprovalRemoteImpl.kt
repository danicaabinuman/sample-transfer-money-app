package com.unionbankph.corporate.approval.data.source.remote.impl

import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.approval.data.model.ApprovalForm
import com.unionbankph.corporate.approval.data.model.ApprovalHierarchyDto
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.approval.data.source.remote.ApprovalRemote
import com.unionbankph.corporate.approval.data.source.remote.client.ApprovalApiClient
import com.unionbankph.corporate.approval.data.model.CheckWriterActivityLogDto
import com.unionbankph.corporate.branch.data.model.BranchVisitDto
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import io.reactivex.Single
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * Created by herald25santos on 2020-01-15
 */
class ApprovalRemoteImpl
@Inject
constructor(
    retrofit: Retrofit
) : ApprovalRemote {

    private val approvalApiClient: ApprovalApiClient =
        retrofit.create(ApprovalApiClient::class.java)

    override fun getApprovalList(
        accessToken: String,
        pageable: Pageable,
        status: String
    ): Single<Response<PagedDto<Transaction>>> {
        return approvalApiClient.getApprovalList(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            status,
            pageable.page,
            pageable.size,
            pageable.filter.notNullable()
        )
    }

    override fun getApprovalHierarchy(
        accessToken: String,
        id: String,
        status: String?
    ): Single<Response<ApprovalHierarchyDto>> {
        return approvalApiClient.getApprovalHierarchy(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }


    override fun approval(
        accessToken: String,
        approvalForm: ApprovalForm
    ): Single<Response<ApprovalForm>> {
        return approvalApiClient.approval(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            approvalForm
        )
    }

    override fun getCheckWriterDetails(
        accessToken: String,
        id: String
    ): Single<Response<Transaction>> {
        return approvalApiClient.getCheckWriterDetails(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }

    override fun getCashWithdrawalDetails(
        accessToken: String,
        id: String
    ): Single<Response<BranchVisitDto>> {
        return approvalApiClient.getCashWithdrawalDetails(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }

    override fun getCheckWriterActivityLogs(
        accessToken: String,
        id: String
    ): Single<Response<MutableList<CheckWriterActivityLogDto>>> {
        return approvalApiClient.getCheckWriterActivityLogs(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }
}