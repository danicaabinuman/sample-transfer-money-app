package com.unionbankph.corporate.dao.domain.interactor

import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.nullable
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.dao.domain.form.SignatureForm
import com.unionbankph.corporate.dao.domain.gateway.DaoGateway
import com.unionbankph.corporate.dao.domain.model.DaoHit
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class SubmitSignature
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val responseProvider: ResponseProvider,
    private val daoGateway: DaoGateway
) : SingleUseCase<DaoHit, SignatureForm?>(threadExecutor, postExecutionThread) {

    private var accessToken: String? = null
    private var referenceNumber: String? = null

    override fun buildUseCaseObservable(params: SignatureForm?): Single<DaoHit> {
        val requestBody = params?.file?.let {
            val fileRequestBody = RequestBody.create(MediaType.parse("image/*"), it)
            MultipartBody.Builder().setType(MultipartBody.FORM).apply {
                addFormDataPart("file", it.name, fileRequestBody)
            }.build()
        }
        return daoGateway.getAccessToken()
            .zipWith(daoGateway.getReferenceNumber())
            .flatMap {
                accessToken = it.first
                referenceNumber = it.second
                daoGateway.submitSignature(
                    it.first.nullable(),
                    it.second.nullable(),
                    requestBody!!
                )
            }
            .flatMap {
                params?.daoForm?.let {
                    daoGateway.submitDao(accessToken.nullable(), referenceNumber.nullable(), it)
                }
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
            .map {
                val isHit = it.amlaHit == true
                        || it.nfisHit == true
                        || it.crrHit == true
                        || it.nationalityHit == true
                return@map DaoHit(
                    isHit,
                    it.businessName.notNullable(),
                    it.preferedBranch.notNullable(),
                    it.preferedBranchEmail.notNullable(),
                    it.userReferenceNumber.notNullable()
                )
            }
    }
}
