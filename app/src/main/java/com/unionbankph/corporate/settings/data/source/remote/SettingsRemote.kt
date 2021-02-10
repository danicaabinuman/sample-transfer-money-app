package com.unionbankph.corporate.settings.data.source.remote

import com.unionbankph.corporate.auth.data.form.ChangePasswordForm
import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.common.data.form.Pageable
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

/**
 * Created by herald25santos on 2020-01-14
 */
interface SettingsRemote {

    fun changePassword(
        accessToken: String,
        changePasswordForm: ChangePasswordForm
    ): Single<Response<Message>>

    fun changeEmailAddress(
        accessToken: String,
        changeEmailAddressParams: HashMap<String, String>
    ): Single<Response<Message>>

    fun changeMobileNumber(
        accessToken: String,
        changeMobileNumberParams: HashMap<String, String>
    ): Single<Response<Auth>>

    fun changeMobileNumberOTP(
        accessToken: String,
        changeMobileNumberOTPParams: HashMap<String, String>
    ): Single<Response<Message>>

    fun changeMobileNumberResendOTP(
        accessToken: String,
        resendOTPForm: com.unionbankph.corporate.auth.data.form.ResendOTPForm
    ): Single<Response<Auth>>

    fun getCountryCode(
        accessToken: String,
        id: String,
        mobileNumber: String
    ): Single<Response<CountryCode>>

    fun getCountryCodes(accessToken: String): Single<Response<MutableList<CountryCode>>>

    fun getRecurrenceTypes(accessToken: String): Single<Response<MutableList<RecurrenceTypes>>>

    fun verifyEmailAddress(verifyEmailAddressForm: VerifyEmailAddressForm): Single<Response<Message>>

    fun oTPSettings(accessToken: String, otpSettingsForm: OTPSettingsForm): Single<Response<Auth>>

    fun oTPSettingsVerify(
        accessToken: String,
        verifyOTPForm: VerifyOTPForm
    ): Single<Response<OTPSettingsDto>>

    fun oTPSettingsVerifyResend(
        accessToken: String,
        resendOTPForm: com.unionbankph.corporate.auth.data.form.ResendOTPForm
    ): Single<Response<Auth>>

    fun getOTPSettings(accessToken: String): Single<Response<OTPSettingsDto>>

    fun setOTPType(accessToken: String, otpTypeForm: OTPTypeForm): Single<Response<OTPTypeDto>>

    fun getOTPTypes(accessToken: String): Single<Response<OTPTypeDto>>

    fun totpSubscribe(
        accessToken: String,
        manageDeviceForm: ManageDeviceForm
    ): Single<Response<TOTPSubscribeDto>>

    fun unTrustDevice(
        accessToken: String,
        manageDeviceForm: ManageDeviceForm
    ): Single<Response<Message>>

    fun forgetDevice(accessToken: String, id: String): Single<Response<Message>>

    fun getManageDevicesList(accessToken: String): Single<Response<ManageDevicesDto>>

    fun getManageDeviceDetail(
        accessToken: String,
        id: String
    ): Single<Response<ManageDeviceDetailDto>>

    fun getLoginHistory(
        accessToken: String,
        id: String,
        pageable: Pageable
    ): Single<Response<PagedDto<LastAccessed>>>

    fun setFingerprint(accessToken: String): Single<Response<FingerPrintToken>>

    fun logoutUser(accessToken: String): Single<Response<FingerPrintToken>>

    fun getEnabledFeatures(): Single<Response<EnabledFeaturesDto>>
}
