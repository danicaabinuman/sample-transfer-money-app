package com.unionbankph.corporate.mcd.data.source.local

import com.unionbankph.corporate.mcd.data.model.CheckDeposit
import com.unionbankph.corporate.common.data.model.PagedDto
import io.reactivex.Single

interface CheckDepositCache {

    fun getCheckDepositsTestData(): Single<PagedDto<CheckDeposit>>
}
