package com.unionbankph.corporate.approval.data.gateway

import com.unionbankph.corporate.approval.domain.gateway.ApprovalGateway
import com.unionbankph.corporate.approval.data.model.ApprovalForm
import com.unionbankph.corporate.approval.data.model.ApprovalHierarchyDto
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.approval.data.source.remote.ApprovalRemote
import com.unionbankph.corporate.approval.data.model.CheckWriterActivityLogDto
import com.unionbankph.corporate.branch.data.model.BranchVisitDto
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.Single
import retrofit2.Response
import javax.inject.Inject

/**
 * Created by herald25santos on 2020-01-15
 */
class ApprovalGatewayImpl
@Inject
constructor(
    private val responseProvider: ResponseProvider,
    private val approvalRemote: ApprovalRemote,
    private val settingsGateway: SettingsGateway
) : ApprovalGateway {

    override fun getApprovalList(
        pageable: Pageable,
        status: String
    ): Single<PagedDto<Transaction>> {
        return settingsGateway.getAccessToken()
            .flatMap { approvalRemote.getApprovalList(it, pageable, status) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getApprovalHierarchy(id: String, status: String?): Single<ApprovalHierarchyDto> {
        return settingsGateway.getAccessToken()
            .flatMap { approvalRemote.getApprovalHierarchy(it, id, status) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun approval(approvalForm: ApprovalForm): Single<ApprovalForm> {
        return settingsGateway.getAccessToken()
            .flatMap { approvalRemote.approval(it, approvalForm) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getCheckWriterDetails(id: String): Single<Transaction> {
        return settingsGateway.getAccessToken()
            .flatMap { approvalRemote.getCheckWriterDetails(it, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getCashWithdrawalDetails(id: String): Single<BranchVisitDto> {
        return settingsGateway.getAccessToken()
            .flatMap { approvalRemote.getCashWithdrawalDetails(it, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getCheckWriterActivityLogs(
        accessToken: String,
        id: String
    ): Single<Response<MutableList<CheckWriterActivityLogDto>>> {
        return approvalRemote.getCheckWriterActivityLogs(accessToken, id)
    }

}