package com.unionbankph.corporate.common.domain.provider

import com.unionbankph.corporate.common.data.model.ApiError
import com.unionbankph.corporate.payment_link.data.model.SMEApiError
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Response

interface SMEResponseProvider {

    fun <T> executeResponseSingle(response: Response<T>): Single<T>

    fun <T> executeResponseCustomSingle(
        response: Response<T>,
        errorAction: ((smeApiError: SMEApiError) -> Single<T>)
    ): Single<T>

    fun <T> executeResponseCompletable(response: Response<T>): Completable

    fun <T> handleOnError(smeApiError: SMEApiError): Single<T>

}
