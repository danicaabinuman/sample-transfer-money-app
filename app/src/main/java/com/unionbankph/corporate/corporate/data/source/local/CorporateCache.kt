package com.unionbankph.corporate.corporate.data.source.local

import com.unionbankph.corporate.auth.data.model.CorporateUser
import com.unionbankph.corporate.auth.data.model.Role
import com.unionbankph.corporate.auth.data.model.RoleAccountPermissions
import com.unionbankph.corporate.auth.data.model.UserDetails
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created by herald25santos on 2020-01-16
 */
interface CorporateCache {

    fun getProductPermissions(): Single<MutableList<RoleAccountPermissions>>

    fun getRole(): Single<Role>

    fun getCorporateUser(): Single<CorporateUser>

    fun getUserDetails(): Single<UserDetails>

    fun saveRole(role: Role): Completable
}
