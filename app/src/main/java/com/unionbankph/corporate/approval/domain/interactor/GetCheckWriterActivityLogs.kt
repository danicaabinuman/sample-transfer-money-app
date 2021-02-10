package com.unionbankph.corporate.approval.domain.interactor

import com.unionbankph.corporate.app.common.extension.convertDateToDesireFormat
import com.unionbankph.corporate.approval.domain.gateway.ApprovalGateway
import com.unionbankph.corporate.approval.domain.mapper.CheckWriterActivityLogsMapper
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.fund_transfer.data.model.ActivityLogDto
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by herald25santos on 6/24/20
 */
class GetCheckWriterActivityLogs
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val responseProvider: ResponseProvider,
    private val approvalGateway: ApprovalGateway,
    private val settingsGateway: SettingsGateway,
    private val checkWriterActivityLogsMapper: CheckWriterActivityLogsMapper
) : SingleUseCase<Pair<MutableList<ActivityLogDto>, MutableList<ActivityLogDto>>, String?>(
    threadExecutor,
    postExecutionThread
) {

    override fun buildUseCaseObservable(
        params: String?
    ): Single<Pair<MutableList<ActivityLogDto>, MutableList<ActivityLogDto>>> {
        return settingsGateway.getAccessToken()
            .flatMap {
                params?.let { params ->
                    approvalGateway.getCheckWriterActivityLogs(it, params)
                }
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
            .map { checkWriterActivityLogsMapper.map(it) }
            .map { list ->
                val header = list.distinctBy {
                    Pair(
                        it.createdDate.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_WITHOUT_TIME),
                        it.createdDate.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_WITHOUT_TIME)
                    )
                }.toMutableList()
                return@map Pair(header, list)
            }
    }

}
