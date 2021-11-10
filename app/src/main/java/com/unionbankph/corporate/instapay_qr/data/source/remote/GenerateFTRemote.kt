package com.unionbankph.corporate.instapay_qr.data.source.remote

import com.unionbankph.corporate.instapay_qr.domain.model.form.GenerateFTForm
import com.unionbankph.corporate.instapay_qr.domain.model.response.GenerateFTResponse
import io.reactivex.Single
import retrofit2.Response

interface GenerateFTRemote {

    fun generateFT(accessToken: String, generateFTForm: GenerateFTForm) : Single<Response<GenerateFTResponse>>
}