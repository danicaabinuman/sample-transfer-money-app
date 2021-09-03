package com.unionbankph.corporate.dao.domain.interactor

import android.content.Context
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.nullable
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.dao.data.form.DaoForm
import com.unionbankph.corporate.dao.domain.constant.DaoErrorCodeEnum
import com.unionbankph.corporate.dao.domain.exception.ExpiredIDException
import com.unionbankph.corporate.dao.domain.exception.MismatchIDException
import com.unionbankph.corporate.dao.domain.exception.ReachOutPageException
import com.unionbankph.corporate.dao.domain.exception.RetakeIDException
import com.unionbankph.corporate.dao.domain.exception.VerificationProcessingException
import com.unionbankph.corporate.dao.domain.gateway.DaoGateway
import com.unionbankph.corporate.dao.domain.model.DaoHit
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import javax.inject.Inject

class SubmitDao
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val responseProvider: ResponseProvider,
    private val daoGateway: DaoGateway,
    private val context: Context
) : SingleUseCase<DaoHit, DaoForm?>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: DaoForm?): Single<DaoHit> {
        return daoGateway.getAccessToken()
            .zipWith(daoGateway.getReferenceNumber())
            .flatMap {
                params?.let { params ->
                    daoGateway.submitDao(
                        it.first.nullable(),
                        it.second.nullable(),
                        params
                    )
                }
            }
            .flatMap {
                responseProvider.executeResponseCustomSingle(it) { apiError ->
                    val error = apiError.errors[0]
                    when (error.code) {
                        DaoErrorCodeEnum.DAO_JUMIO_NEED_MORE_TIME.name -> {
                            Single.error(VerificationProcessingException(apiError.message))
                        }
                        DaoErrorCodeEnum.DAO_REACH_OUT_PAGE.name -> {
                            if (error.details != null) {
                                val details = JsonHelper.toJson(error.details)
                                Single.error(ReachOutPageException(details))
                            } else {
                                responseProvider.handleOnError(apiError)
                            }
                        }
                        DaoErrorCodeEnum.DAO_JUMIO_SCAN_RETRY.name -> {
                            if (error.mismatchedIdDetails != null) {
                                val mismatchedIdErrors = JsonHelper.toJson(error.mismatchedIdDetails)
                                Single.error(MismatchIDException(mismatchedIdErrors))
                            } else {
                                Single.error(MismatchIDException(apiError.message))
                            }
                        }
                        DaoErrorCodeEnum.DAO_JUMIO_EXPIRED_DOCUMENT_SCAN_RETRY.name -> {
                            Single.error(ExpiredIDException(apiError.message))
                        }
                        DaoErrorCodeEnum.DAO_JUMIO_UNREADABLE_ID_SCAN_RETRY.name -> {
                            Single.error(RetakeIDException(apiError.message))
                        }
                        else -> {
                            responseProvider.handleOnError(apiError)
                        }
                    }
                }
            }
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
