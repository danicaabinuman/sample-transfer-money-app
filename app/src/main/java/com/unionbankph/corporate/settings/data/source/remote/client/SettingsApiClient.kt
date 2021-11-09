package com.unionbankph.corporate.settings.data.source.remote.client

import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.GenericMenuItem
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.MegaMenuDto
import com.unionbankph.corporate.auth.data.form.ChangePasswordForm
import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.common.data.form.VerifyOTPForm
import com.unionbankph.corporate.common.data.model.Auth
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.corporate.data.model.RecurrenceTypes
import com.unionbankph.corporate.settings.data.form.ManageDeviceForm
import com.unionbankph.corporate.settings.data.form.OTPSettingsForm
import com.unionbankph.corporate.settings.data.form.OTPTypeForm
import com.unionbankph.corporate.settings.data.form.VerifyEmailAddressForm
import com.unionbankph.corporate.settings.data.model.EnabledFeaturesDto
import com.unionbankph.corporate.settings.data.model.FingerPrintToken
import com.unionbankph.corporate.settings.data.model.LastAccessed
import com.unionbankph.corporate.settings.data.model.ManageDeviceDetailDto
import com.unionbankph.corporate.settings.data.model.ManageDevicesDto
import com.unionbankph.corporate.settings.data.model.OTPSettingsDto
import com.unionbankph.corporate.settings.data.model.OTPTypeDto
import com.unionbankph.corporate.settings.data.model.TOTPSubscribeDto
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.nio.channels.Selector

interface SettingsApiClient {

    @GET("api/{api_version}/country-codes")
    fun getCountryCodes(
        @Header("Authorization")
        accessToken: String?,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<MutableList<CountryCode>>>

    @GET("api/{api_version}/country-codes")
    fun getCountryCodes(
        @Path("api_version")
        apiVersion: String
    ): Single<Response<MutableList<CountryCode>>>

    @GET("api/{api_version}/country-codes/{id}")
    fun getCountryCode(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<CountryCode>>

    @POST("api/{api_version}/corporate/fingerprint/link")
    fun setFingerPrint(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<FingerPrintToken>>

    @POST("api/{api_version}/config/security")
    fun oTPSettings(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        otpSettingsForm: OTPSettingsForm
    ): Single<Response<Auth>>

    @POST("api/{api_version}/config/security/otp")
    fun oTPSettingsVerify(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        verifyOTPForm: VerifyOTPForm
    ): Single<Response<OTPSettingsDto>>

    @POST("api/{api_version}/config/security/otp/resend")
    fun oTPSettingsVerifyResend(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        resendOTPForm: com.unionbankph.corporate.auth.data.form.ResendOTPForm
    ): Single<Response<Auth>>

    @GET("api/{api_version}/config/security")
    fun getOTPSettings(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<OTPSettingsDto>>

    @GET("api/{api_version}/config/login")
    fun getOTPTypes(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<OTPTypeDto>>

    @POST("api/{api_version}/config/login")
    fun setOTPType(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        oTPTypeForm: OTPTypeForm
    ): Single<Response<OTPTypeDto>>

    @POST("api/{api_version}/devices/subscribe")
    fun totpSubscribe(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        manageDeviceForm: ManageDeviceForm
    ): Single<Response<TOTPSubscribeDto>>

    @POST("api/{api_version}/devices/untrust")
    fun unTrustDevice(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        manageDeviceForm: ManageDeviceForm
    ): Single<Response<Message>>

    @GET("api/{api_version}/devices/{id}/forget")
    fun forgetDevice(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<Message>>

    @GET("api/{api_version}/devices")
    fun getManageDevicesList(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<ManageDevicesDto>>

    @GET("api/{api_version}/devices/{id}")
    fun getManageDeviceDetail(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<ManageDeviceDetailDto>>

    @GET("api/{api_version}/devices/{id}/history")
    fun getLoginHistory(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String,
        @Query("page")
        page: Int,
        @Query("size")
        size: Int
    ): Single<Response<PagedDto<LastAccessed>>>

    @POST("api/{api_version}/corporate-user-profile/confirm-change-email")
    fun verifyEmailAddress(
        @Path("api_version")
        apiVersion: String,
        @Body
        verifyEmailAddressForm: VerifyEmailAddressForm
    ): Single<Response<Message>>

    @GET("api/{api_version}/scheduler/recurrence-types")
    fun getRecurrenceTypes(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<MutableList<RecurrenceTypes>>>

    @GET("api/{api_version}/corporate/logout")
    fun logoutUser(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<FingerPrintToken>>

    @POST("api/{api_version}/corporate-user-profile/change-password")
    fun changePassword(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        changePasswordForm: ChangePasswordForm
    ): Single<Response<Message>>

    @POST("api/{api_version}/corporate-user-profile/change-email")
    fun changeEmailAddress(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        changeEmailAddressParams: HashMap<String, String>
    ): Single<Response<Message>>

    @POST("api/{api_version}/corporate-user-profile/change-mobile-number")
    fun changeMobileNumber(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        changeMobileNumberParams: HashMap<String, String>
    ): Single<Response<Auth>>

    @POST("api/{api_version}/corporate-user-profile/change-mobile-number/otp")
    fun changeMobileNumberOTP(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        changeMobileNumberOTPParams: HashMap<String, String>
    ): Single<Response<Message>>

    @POST("api/{api_version}/corporate-user-profile/change-mobile-number/otp/resend")
    fun changeMobileNumberResendOTP(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        resendOTPForm: com.unionbankph.corporate.auth.data.form.ResendOTPForm
    ): Single<Response<Auth>>

    @GET("api/{api_version}/enabled-features")
    fun getEnabledFeatures(
        @Path("api_version")
        apiVersion: String
    ): Single<Response<EnabledFeaturesDto>>

    @GET("msme/api/{api_version}/corporate-users/mega-menu")
    fun getDashboardMegaMenu(
        @Header("Authorization")
        accessToken: String,
        @Header("x-client-id")
        clientId: String,
        @Header("x-client-secret")
        clientSecret: String,
        @Path("api_version")
        apiVersion: String,
    ): Single<Response<MutableList<MegaMenuDto>>>
}
