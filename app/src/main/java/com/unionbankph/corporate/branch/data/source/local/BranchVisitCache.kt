package com.unionbankph.corporate.branch.data.source.local

import com.unionbankph.corporate.branch.data.model.BranchVisitDto
import com.unionbankph.corporate.common.data.model.PagedDto
import io.reactivex.Single

interface BranchVisitCache {

    fun getBranchVisitsTestData(): Single<PagedDto<BranchVisitDto>>
}
