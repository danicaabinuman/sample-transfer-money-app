package com.unionbankph.corporate.link_details.data

import com.unionbankph.corporate.auth.data.source.local.AuthCache
import com.unionbankph.corporate.auth.data.source.remote.AuthRemote
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.link_details.data.form.LinkDetailsForm
import com.unionbankph.corporate.link_details.data.source.remote.LinkDetailsRemote
import com.unionbankph.corporate.link_details.presentation.LinkDetailsGateway
import io.reactivex.Single
import javax.inject.Inject

class LinkDetailsDataGateway
@Inject constructor(
    private val responseProvider: ResponseProvider,
    private val linkDetailsRemote: LinkDetailsRemote
) : LinkDetailsGateway {
    override fun linkGateway(linkDetailsRes: LinkDetailsForm): Single<LinkDetailsResponse> {
        return linkDetailsRemote.generateLink(linkDetailsRes)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }
}