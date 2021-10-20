package com.unionbankph.corporate.instapay_qr.domain.usecase

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.corporate.domain.gateway.CorporateGateway
import com.unionbankph.corporate.instapay_qr.data.gateway.GenerateFTGateway
import com.unionbankph.corporate.instapay_qr.domain.model.SuccessQRReference
import com.unionbankph.corporate.instapay_qr.domain.model.form.GenerateFTForm
import com.unionbankph.corporate.instapay_qr.domain.model.response.GenerateFTResponse
import com.unionbankph.corporate.instapay_qr.presentation.constant.QrSwiftCode
import com.unionbankph.corporate.instapay_qr.presentation.constant.QrValidUniqueIdEnum
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import timber.log.Timber
import javax.inject.Inject

class GenerateFTUseCase
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val generateFTGateway: GenerateFTGateway,
    private val corporateGateway: CorporateGateway
) : SingleUseCase<SuccessQRReference, GenerateFTForm>(
    threadExecutor,
    postExecutionThread
){
    override fun buildUseCaseObservable(
        params: GenerateFTForm?
    ): Single<SuccessQRReference> {

        return generateFTGateway.generateFT(params!!)
            .zipWith (corporateGateway.getChannels("2"))
            .map {
                if (isValidUniqueId(it.first.uniqueId!!)) {

                    val qrChannelId = when (it.first.swiftCode) {
                        QrSwiftCode.UBP.value -> ChannelBankEnum.UBP_TO_UBP.getChannelId()
                        else -> ChannelBankEnum.INSTAPAY.getChannelId() // QrSwiftCode.INSTAPAY.value
                    }

                    val channel = it.second.find { it.id == qrChannelId }
                    Timber.e("find: " + JsonHelper.toJson(channel))

                    if(channel?.hasApprovalRule == false &&
                        !channel.hasPermission &&
                        !channel.hasRulesAllowTransaction &&
                        !channel.hasSourceAccount
                    ) {
                        return@map SuccessQRReference().apply {
                            this.channel = channel
                            this.hasValidationError = true
                        }
                    }

                    SuccessQRReference().apply {
                        this.channel = channel
                        this.recipientData = it.first
                    }
                } else {
                    return@map SuccessQRReference().apply {
                        hasValidationError = true
                    }
                }
            }
    }

    private fun isValidUniqueId(id: String) : Boolean {
        Timber.e("idddd $id")
        Timber.e("P2P ${QrValidUniqueIdEnum.P2P.value}")
        return (id.equals(QrValidUniqueIdEnum.P2P.value, true)) ||
                (id.equals(QrValidUniqueIdEnum.P2M.value, true))
    }
}