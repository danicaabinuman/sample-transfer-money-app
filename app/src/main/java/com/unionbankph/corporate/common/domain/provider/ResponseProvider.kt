package com.unionbankph.corporate.common.domain.provider

import com.unionbankph.corporate.common.data.model.ApiError
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Response

interface ResponseProvider {

    fun <T> executeResponseSingle(response: Response<T>): Single<T>

    fun <T> executeResponseCustomSingle(
        response: Response<T>,
        errorAction: ((apiError: ApiError) -> Single<T>)
    ): Single<T>

    fun <T> executeResponseCompletable(response: Response<T>): Completable

    fun <T> handleOnError(apiError: ApiError): Single<T>

}
