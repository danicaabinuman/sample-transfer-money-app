package com.unionbankph.corporate.corporate.data.source.remote

import com.unionbankph.corporate.auth.data.model.Role
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.corporate.data.form.ValidateAccountNumberForm
import com.unionbankph.corporate.corporate.data.model.Channel
import com.unionbankph.corporate.corporate.data.model.ChannelDto
import com.unionbankph.corporate.corporate.data.model.CorporateUsers
import com.unionbankph.corporate.corporate.data.model.TransactionStatusDto
import io.reactivex.Single
import retrofit2.Response

interface CorporateRemote {

    fun getChannels(
        accessToken: String,
        roleId: String,
        productId: String
    ): Single<Response<MutableList<Channel>>>

    fun getCorporateUserOrganization(accessToken: String): Single<Response<MutableList<CorporateUsers>>>

    fun switchOrganization(accessToken: String, roleId: String): Single<Response<Role>>

    fun validateAccountNumber(
        accessToken: String,
        validateAccountNumberForm: ValidateAccountNumberForm
    ): Single<Response<Message>>

    fun getChannelsLite(
        accessToken: String,
        productId: String
    ): Single<Response<MutableList<ChannelDto>>>

    fun getTransactionStatuses(
        accessToken: String,
        productId: String
    ): Single<Response<MutableList<TransactionStatusDto>>>
}
