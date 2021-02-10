package com.unionbankph.corporate.corporate.domain.gateway

import com.unionbankph.corporate.auth.data.model.CorporateUser
import com.unionbankph.corporate.auth.data.model.Role
import com.unionbankph.corporate.auth.data.model.RoleAccountPermissions
import com.unionbankph.corporate.auth.data.model.UserDetails
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.corporate.data.form.ValidateAccountNumberForm
import com.unionbankph.corporate.corporate.data.model.Channel
import com.unionbankph.corporate.corporate.data.model.ChannelDto
import com.unionbankph.corporate.corporate.data.model.CorporateUsers
import com.unionbankph.corporate.corporate.data.model.TransactionStatusDto
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Response

interface CorporateGateway {

    fun getRole(): Single<Role>

    fun getUserDetails(): Single<UserDetails>

    fun getCorporateUser(): Single<CorporateUser>

    fun getProductPermissions(): Single<MutableList<RoleAccountPermissions>>

    fun getChannels(productId: String): Single<MutableList<Channel>>

    fun getCorporateUserOrganization(): Single<MutableList<CorporateUsers>>

    fun switchOrganization(roleId: String): Completable

    fun validateAccountNumber(validateAccountNumberForm: ValidateAccountNumberForm): Single<Message>

    fun getChannelsLite(productId: String): Single<MutableList<ChannelDto>>

    fun getTransactionStatuses(accessToken: String, productId: String): Single<Response<MutableList<TransactionStatusDto>>>
}
