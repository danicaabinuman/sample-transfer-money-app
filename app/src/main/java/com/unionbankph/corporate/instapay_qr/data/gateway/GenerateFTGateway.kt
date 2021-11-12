package com.unionbankph.corporate.instapay_qr.data.gateway

import com.unionbankph.corporate.instapay_qr.domain.model.form.GenerateFTForm
import com.unionbankph.corporate.instapay_qr.domain.model.response.GenerateFTResponse
import io.reactivex.Single

interface GenerateFTGateway {

    fun generateFT(generateFTForm: GenerateFTForm) : Single<GenerateFTResponse>
}