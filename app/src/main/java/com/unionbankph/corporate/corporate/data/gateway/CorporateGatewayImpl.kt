package com.unionbankph.corporate.corporate.data.gateway

import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.auth.data.model.CorporateUser
import com.unionbankph.corporate.auth.data.model.Role
import com.unionbankph.corporate.auth.data.model.RoleAccountPermissions
import com.unionbankph.corporate.auth.data.model.UserDetails
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.corporate.data.form.ValidateAccountNumberForm
import com.unionbankph.corporate.corporate.data.model.Channel
import com.unionbankph.corporate.corporate.data.model.ChannelDto
import com.unionbankph.corporate.corporate.data.model.CorporateUsers
import com.unionbankph.corporate.corporate.data.model.TransactionStatusDto
import com.unionbankph.corporate.corporate.data.source.local.CorporateCache
import com.unionbankph.corporate.corporate.data.source.remote.CorporateRemote
import com.unionbankph.corporate.corporate.domain.gateway.CorporateGateway
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import retrofit2.Response
import javax.inject.Inject

class CorporateGatewayImpl
@Inject
constructor(
    private val responseProvider: ResponseProvider,
    private val corporateRemote: CorporateRemote,
    private val corporateCache: CorporateCache,
    private val settingsGateway: SettingsGateway
) : CorporateGateway {

    override fun getProductPermissions(): Single<MutableList<RoleAccountPermissions>> {
        return corporateCache.getProductPermissions()
    }

    override fun getRole(): Single<Role> {
        return corporateCache.getRole()
    }

    override fun getCorporateUser(): Single<CorporateUser> {
        return corporateCache.getCorporateUser()
    }

    override fun getChannels(productId: String): Single<MutableList<Channel>> {
        return corporateCache.getRole()
            .zipWith(settingsGateway.getAccessToken())
            .flatMap {
                corporateRemote.getChannels(
                    it.second,
                    it.first.organizationId.notNullable(),
                    productId
                )
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getUserDetails(): Single<UserDetails> {
        return corporateCache.getUserDetails()
    }

    override fun switchOrganization(roleId: String): Completable {
        return settingsGateway.getAccessToken()
            .flatMap { corporateRemote.switchOrganization(it, roleId) }
            .flatMap { responseProvider.executeResponseSingle(it) }
            .flatMapCompletable { corporateCache.saveRole(it) }
    }

    override fun getCorporateUserOrganization(): Single<MutableList<CorporateUsers>> {
        return settingsGateway.getAccessToken()
            .flatMap { corporateRemote.getCorporateUserOrganization(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun validateAccountNumber(validateAccountNumberForm: ValidateAccountNumberForm): Single<Message> {
        return settingsGateway.getAccessToken()
            .flatMap { corporateRemote.validateAccountNumber(it, validateAccountNumberForm) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getChannelsLite(productId: String): Single<MutableList<ChannelDto>> {
        return settingsGateway.getAccessToken()
            .flatMap { corporateRemote.getChannelsLite(it, productId) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getTransactionStatuses(
        accessToken: String,
        productId: String
    ): Single<Response<MutableList<TransactionStatusDto>>> {
        return corporateRemote.getTransactionStatuses(accessToken, productId)
    }
}
