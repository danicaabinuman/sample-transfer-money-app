package com.unionbankph.corporate.branch.data.source.local.impl

import com.unionbankph.corporate.branch.data.model.BranchVisitDto
import com.unionbankph.corporate.branch.data.source.local.BranchVisitCache
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.data.source.local.tutorial.TestDataTutorial
import io.reactivex.Single
import javax.inject.Inject

class BranchVisitCacheImpl
@Inject
constructor(
    private val testDataTutorial: TestDataTutorial
) : BranchVisitCache {

    override fun getBranchVisitsTestData(): Single<PagedDto<BranchVisitDto>> {
        return Single.fromCallable {
            testDataTutorial.getBranchVisits()
        }
    }
}
