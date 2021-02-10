package com.unionbankph.corporate.corporate.data.source.remote.impl

import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.auth.data.model.Role
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.corporate.data.form.ValidateAccountNumberForm
import com.unionbankph.corporate.corporate.data.model.Channel
import com.unionbankph.corporate.corporate.data.model.ChannelDto
import com.unionbankph.corporate.corporate.data.model.CorporateUsers
import com.unionbankph.corporate.corporate.data.model.TransactionStatusDto
import com.unionbankph.corporate.corporate.data.source.remote.CorporateRemote
import com.unionbankph.corporate.corporate.data.source.remote.client.CorporateApiClient
import io.reactivex.Single
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * Created by herald25santos on 2020-01-16
 */
class CorporateRemoteImpl
@Inject
constructor(
    retrofit: Retrofit
) : CorporateRemote {

    private val corporateApiClient: CorporateApiClient =
        retrofit.create(CorporateApiClient::class.java)

    override fun getChannels(
        accessToken: String,
        roleId: String,
        productId: String
    ): Single<Response<MutableList<Channel>>> {
        return corporateApiClient.getChannels(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            roleId,
            productId
        )
    }

    override fun getCorporateUserOrganization(accessToken: String): Single<Response<MutableList<CorporateUsers>>> {
        return corporateApiClient.getCorporateUserOrganization(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun switchOrganization(accessToken: String, roleId: String): Single<Response<Role>> {
        val switchOrgParam = java.util.HashMap<String, String>()
        switchOrgParam["role_id"] = roleId
        return corporateApiClient.switchOrganization(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            switchOrgParam
        )
    }

    override fun validateAccountNumber(
        accessToken: String,
        validateAccountNumberForm: ValidateAccountNumberForm
    ): Single<Response<Message>> {
        return corporateApiClient.validateAccountNumber(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            validateAccountNumberForm
        )
    }

    override fun getChannelsLite(
        accessToken: String,
        productId: String
    ): Single<Response<MutableList<ChannelDto>>> {
        return corporateApiClient.getChannelsLite(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            productId
        )
    }

    override fun getTransactionStatuses(
        accessToken: String,
        productId: String
    ): Single<Response<MutableList<TransactionStatusDto>>> {
        return corporateApiClient.getTransactionStatuses(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            productId
        )
    }
}
