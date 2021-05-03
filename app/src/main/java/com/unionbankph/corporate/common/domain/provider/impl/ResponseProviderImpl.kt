package com.unionbankph.corporate.common.domain.provider.impl

import android.content.Context
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.common.data.constant.ResponseApiCodeEnum
import com.unionbankph.corporate.common.data.model.ApiError
import com.unionbankph.corporate.common.domain.exception.ApiErrorException
import com.unionbankph.corporate.common.domain.exception.InvalidTokenException
import com.unionbankph.corporate.common.domain.exception.SessionExpiredException
import com.unionbankph.corporate.common.domain.exception.SomethingWentWrongException
import com.unionbankph.corporate.common.domain.exception.UnderMaintenanceException
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.payment_link.data.model.SMEApiError
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class ResponseProviderImpl
@Inject
constructor(
    private val context: Context
) : ResponseProvider {

    override fun <T> executeResponseSingle(response: Response<T>): Single<T> {
        return if (response.isSuccessful) {
            Single.just(response.body())
        } else {
            if(BuildConfig.FLAVOR.contains("SME")){
                handleSMEErrorResponse(response)
            }else{
                handleErrorResponse(response)
            }
        }
    }

    override fun <T> executeResponseCustomSingle(
        response: Response<T>,
        errorAction: ((apiError: ApiError) -> Single<T>)
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
            if(BuildConfig.FLAVOR.contains("SME")){
                handleSMEErrorResponse(response).flatMapCompletable {
                    Completable.complete()
                }
            }else{
                handleErrorResponse(response).flatMapCompletable {
                    Completable.complete()
                }
            }
        }
    }

    override fun <T> handleOnError(apiError: ApiError): Single<T> {
        return Single.error(throwException(apiError))
    }

    private fun <T> handleErrorResponse(response: Response<T>): Single<T> {
        val errorResponse = response.errorBody()?.string()
        val throwable = try {
            val apiError = JsonHelper.fromJson<ApiError>(errorResponse)
            throwException(apiError)
        } catch (e: Exception) {
            SomethingWentWrongException(context)
        }
        return Single.error(throwable)
    }

    private fun <T> handleSMEErrorResponse(response: Response<T>): Single<T> {
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
        return if (!smeApiError.message.isNullOrEmpty()) {
            ApiErrorException(smeApiError.message.notNullable())
        } else {
            SomethingWentWrongException(context)
        }
    }

    private fun throwException(apiError: ApiError): IOException {
        return if (!apiError.errors.isNullOrEmpty()) {
            val errorMessage = apiError.errors[0].message.notNullable()
            if (errorMessage.contains(
                    context.formatString(R.string.error_msg_under_maintenance),
                    true
                )
            ) {
                UnderMaintenanceException(context)
            } else if (errorMessage == "Invalid Access Token" ||
                apiError.code == ResponseApiCodeEnum.LOGOUT_USER.value
            ) {
                InvalidTokenException(errorMessage)
            } else {
                ApiErrorException(JsonHelper.toJson(apiError.errors[0]))
            }
        } else if (apiError.error != null &&
            apiError.error.equals("invalid_token", true)
        ) {
            SessionExpiredException(context)
        } else if (apiError.errorCode != null) {
            ApiErrorException(apiError.message.notNullable())
        } else {
            SomethingWentWrongException(context)
        }
    }

    private fun <T> handleCustomError(
        it: Response<T>,
        errorAction: ((apiError: ApiError) -> Single<T>)
    ): Single<T> {
        return if (!it.isSuccessful) {
            val errorResponse = it.errorBody()?.string()
            try {
                val apiError = JsonHelper.fromJson<ApiError>(errorResponse)
                errorAction.invoke(apiError)
            } catch (e: Exception) {
                if(BuildConfig.FLAVOR.contains("SME")){
                    handleSMEErrorResponse(it)
                }else{
                    handleErrorResponse(it)
                }
            }
        } else {
            if(BuildConfig.FLAVOR.contains("SME")){
                handleSMEErrorResponse(it)
            }else{
                handleErrorResponse(it)
            }
        }
    }

}
