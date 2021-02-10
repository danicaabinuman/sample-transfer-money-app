package com.unionbankph.corporate.approval.domain.gateway

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
interface ApprovalGateway {

    fun getApprovalList(pageable: Pageable, status: String): Single<PagedDto<Transaction>>

    fun getApprovalHierarchy(id: String, status: String? = ""): Single<ApprovalHierarchyDto>

    fun approval(approvalForm: ApprovalForm): Single<ApprovalForm>

    fun getCheckWriterDetails(id: String): Single<Transaction>

    fun getCashWithdrawalDetails(id: String): Single<BranchVisitDto>

    fun getCheckWriterActivityLogs(
        accessToken: String,
        id: String
    ): Single<Response<MutableList<CheckWriterActivityLogDto>>>

}