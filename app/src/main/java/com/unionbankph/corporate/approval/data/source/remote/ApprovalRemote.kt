package com.unionbankph.corporate.approval.data.source.remote

import com.unionbankph.corporate.approval.data.model.ApprovalForm
import com.unionbankph.corporate.approval.data.model.ApprovalHierarchyDto
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.approval.data.model.CheckWriterActivityLogDto
import com.unionbankph.corporate.branch.data.model.BranchVisitDto
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import io.reactivex.Single
import retrofit2.Response

/**
 * Created by herald25santos on 2020-01-15
 */
interface ApprovalRemote {

    fun getApprovalList(
        accessToken: String,
        pageable: Pageable,
        status: String
    ): Single<Response<PagedDto<Transaction>>>

    fun getApprovalHierarchy(
        accessToken: String,
        id: String,
        status: String? = ""
    ): Single<Response<ApprovalHierarchyDto>>

    fun approval(accessToken: String, approvalForm: ApprovalForm): Single<Response<ApprovalForm>>

    fun getCheckWriterDetails(
        accessToken: String,
        id: String
    ): Single<Response<Transaction>>

    fun getCashWithdrawalDetails(
        accessToken: String,
        id: String
    ): Single<Response<BranchVisitDto>>

    fun getCheckWriterActivityLogs(
        accessToken: String,
        id: String
    ): Single<Response<MutableList<CheckWriterActivityLogDto>>>

}