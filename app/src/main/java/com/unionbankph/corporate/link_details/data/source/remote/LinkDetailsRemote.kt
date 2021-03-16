package com.unionbankph.corporate.link_details.data.source.remote

import com.unionbankph.corporate.link_details.data.LinkDetailsResponse
import com.unionbankph.corporate.link_details.data.form.LinkDetailsForm
import io.reactivex.Single
import retrofit2.Response

interface LinkDetailsRemote {

    fun generateLink(linkDetails: LinkDetailsForm) : Single<Response<LinkDetailsResponse>>
}