package com.unionbankph.corporate.common.domain.provider.impl

import android.content.Context
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.common.domain.exception.SMEApiErrorException
import com.unionbankph.corporate.common.domain.exception.SomethingWentWrongException
import com.unionbankph.corporate.common.domain.provider.SMEResponseProvider
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.payment_link.data.model.SMEApiError
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class SMEResponseProviderImpl
@Inject
constructor(
    private val context: Context
) : SMEResponseProvider {

    override fun <T> executeResponseSingle(response: Response<T>): Single<T> {
        return if (response.isSuccessful) {
            Single.just(response.body())
        } else {
            handleErrorResponse(response)
        }
    }

    override fun <T> executeResponseCustomSingle(
        response: Response<T>,
        errorAction: ((smeApiError: SMEApiError) -> Single<T>)
    ): Single<T> {
        return if (response.isSuccessful) {
            Single.just(response.body())
        } else {
            handleCustomError(response, errorAction)
        }
    }

    override fun <T> executeResponseCompletable(response: Response<T>): Completable {
        return if (response.isSuccessful) {
            Completable.complete()
        } else {
            handleErrorResponse(response).flatMapCompletable {
                Completable.complete()
            }
        }
    }

    override fun <T> handleOnError(smeApiError: SMEApiError): Single<T> {
        return Single.error(throwSMEException(smeApiError))
    }

    private fun <T> handleErrorResponse(response: Response<T>): Single<T> {
        val errorResponse = response.errorBody()?.string()
        val throwable = try {
            val smeApiError = JsonHelper.fromJson<SMEApiError>(errorResponse)
            throwSMEException(smeApiError)
        } catch (e: Exception) {
            SomethingWentWrongException(context)
        }
        return Single.error(throwable)
    }

    private fun throwSMEException(smeApiError: SMEApiError): IOException {
        val errorMessage = smeApiError.message + ""
        return SMEApiErrorException(errorMessage)
    }

    private fun <T> handleCustomError(
        it: Response<T>,
        errorAction: ((smeApiError: SMEApiError) -> Single<T>)
    ): Single<T> {
        return if (!it.isSuccessful) {
            val errorResponse = it.errorBody()?.string()
            try {
                val apiError = JsonHelper.fromJson<SMEApiError>(errorResponse)
                errorAction.invoke(apiError)
            } catch (e: Exception) {
                handleErrorResponse(it)
            }
        } else {
            handleErrorResponse(it)
        }
    }

}
