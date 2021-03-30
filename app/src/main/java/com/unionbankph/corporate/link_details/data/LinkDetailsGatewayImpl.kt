package com.unionbankph.corporate.link_details.data

import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.link_details.data.form.LinkDetailsForm
import com.unionbankph.corporate.link_details.data.source.remote.LinkDetailsRemote
import com.unionbankph.corporate.link_details.presentation.LinkDetailsGateway
import com.unionbankph.corporate.settings.data.source.local.SettingsCache
import io.reactivex.Single
import javax.inject.Inject

class LinkDetailsGatewayImpl
@Inject constructor(
    private val responseProvider: ResponseProvider,
    private val linkDetailsRemote: LinkDetailsRemote,
    private val settingsCache: SettingsCache
) : LinkDetailsGateway {

    override fun getPaymentLink(linkDetailsRes: LinkDetailsForm): Single<LinkDetailsResponse> {
        return settingsCache.getAccessToken()
            .flatMap { linkDetailsRemote.generateLink(it, linkDetailsRes) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }


}