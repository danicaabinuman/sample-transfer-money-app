package com.unionbankph.corporate.branch.data.source.local

import com.unionbankph.corporate.auth.data.model.CorporateUser
import io.reactivex.Single

interface CorporateUserCache {

    fun getCorporateUser(): Single<CorporateUser>
}
