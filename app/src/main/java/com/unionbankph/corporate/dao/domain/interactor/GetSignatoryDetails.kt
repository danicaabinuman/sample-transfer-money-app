package com.unionbankph.corporate.dao.domain.interactor

import com.unionbankph.corporate.app.common.extension.nullable
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.dao.domain.constant.DaoErrorCodeEnum
import com.unionbankph.corporate.dao.domain.exception.VerificationCompletedException
import com.unionbankph.corporate.dao.domain.form.DaoGetSignatoryForm
import com.unionbankph.corporate.dao.domain.gateway.DaoGateway
import com.unionbankph.corporate.dao.domain.mapper.SignatoryMapper
import com.unionbankph.corporate.dao.domain.model.SignatoryDetail
import io.reactivex.Single
import javax.inject.Inject

class GetSignatoryDetails
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val responseProvider: ResponseProvider,
    private val daoGateway: DaoGateway,
    private val signatoryMapper: SignatoryMapper
) : SingleUseCase<SignatoryDetail, String?>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: String?): Single<SignatoryDetail> {
        return daoGateway.getAccessToken()
            .flatMap {
                // null params if the dao application is via deep link
                if (params != null) {
                    daoGateway.getSignatoryDetails(DaoGetSignatoryForm(null, params.nullable()))
                } else {
                    daoGateway.getSignatoryDetails(DaoGetSignatoryForm(it.nullable(), null))
                }
            }
            .flatMap {
                responseProvider.executeResponseCustomSingle(it) { apiError ->
                    val error = apiError.errors[0]
                    if (error.code == DaoErrorCodeEnum.DAO_REACH_OUT_PAGE.name) {
                        if (error.details != null) {
                            val details = JsonHelper.toJson(error.details)
                            Single.error(VerificationCompletedException(details))
                        } else {
                            responseProvider.handleOnError(apiError)
                        }
                    } else {
                        responseProvider.handleOnError(apiError)
                    }
                }
            }
            .map { signatoryMapper.map(it) }
            .flatMap {
                daoGateway.saveSignatoryDetails(it).toSingle { it }
            }
    }

}
