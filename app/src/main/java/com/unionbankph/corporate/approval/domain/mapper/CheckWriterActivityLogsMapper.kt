package com.unionbankph.corporate.approval.domain.mapper

import com.unionbankph.corporate.approval.data.model.CheckWriterActivityLogDto
import com.unionbankph.corporate.common.domain.mapper.Mapper
import com.unionbankph.corporate.fund_transfer.data.model.ActivityLogDto
import javax.inject.Inject

/**
 * Created by herald25santos on 6/24/20
 */
class CheckWriterActivityLogsMapper
@Inject
constructor() : Mapper<ActivityLogDto, CheckWriterActivityLogDto>() {

    override fun map(from: CheckWriterActivityLogDto): ActivityLogDto {
        return ActivityLogDto(
            from.id,
            from.createdDate,
            from.description,
            from.status,
            from.userFullName,
            from.action,
            from.referenceNumber,
            from.reasonForRejection,
            from.errorMessage
        )
    }

    override fun reverse(to: ActivityLogDto): CheckWriterActivityLogDto {
        return CheckWriterActivityLogDto(
            to.id,
            to.createdDate,
            to.description,
            to.status,
            to.userFullName,
            to.action,
            to.referenceNumber,
            to.reasonForRejection,
            to.errorMessage
        )
    }
}