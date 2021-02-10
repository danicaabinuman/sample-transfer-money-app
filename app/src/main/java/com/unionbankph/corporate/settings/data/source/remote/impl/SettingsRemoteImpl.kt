package com.unionbankph.corporate.settings.data.source.remote.impl

import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.auth.data.form.ChangePasswordForm
import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.common.data.constant.ApiVersionEnum
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
import com.unionbankph.corporate.settings.data.source.remote.SettingsRemote
import com.unionbankph.corporate.settings.data.source.remote.client.SettingsApiClient
import io.reactivex.Single
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * Created by herald25santos on 2020-01-14
 */
class SettingsRemoteImpl
@Inject
constructor(retrofit: Retrofit) : SettingsRemote {

    private val settingsApiClient: SettingsApiClient =
        retrofit.create(SettingsApiClient::class.java)

    override fun changePassword(
        accessToken: String,
        changePasswordForm: ChangePasswordForm
    ): Single<Response<Message>> {
        return settingsApiClient.changePassword(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            changePasswordForm
        )
    }

    override fun changeEmailAddress(
        accessToken: String,
        changeEmailAddressParams: HashMap<String, String>
    ): Single<Response<Message>> {
        return settingsApiClient.changeEmailAddress(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            changeEmailAddressParams
        )
    }

    override fun changeMobileNumber(
        accessToken: String,
        changeMobileNumberParams: HashMap<String, String>
    ): Single<Response<Auth>> {
        return settingsApiClient.changeMobileNumber(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            changeMobileNumberParams
        )
    }

    override fun changeMobileNumberOTP(
        accessToken: String,
        changeMobileNumberOTPParams: HashMap<String, String>
    ): Single<Response<Message>> {
        return settingsApiClient.changeMobileNumberOTP(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            changeMobileNumberOTPParams
        )
    }

    override fun changeMobileNumberResendOTP(
        accessToken: String,
        resendOTPForm: com.unionbankph.corporate.auth.data.form.ResendOTPForm
    ): Single<Response<Auth>> {
        return settingsApiClient.changeMobileNumberResendOTP(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            resendOTPForm
        )
    }

    override fun verifyEmailAddress(verifyEmailAddressForm: VerifyEmailAddressForm): Single<Response<Message>> {
        return settingsApiClient.verifyEmailAddress(
            BuildConfig.CLIENT_API_VERSION,
            verifyEmailAddressForm
        )
    }

    override fun getCountryCodes(accessToken: String): Single<Response<MutableList<CountryCode>>> {
        return settingsApiClient.getCountryCodes(
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun getCountryCode(
        accessToken: String,
        id: String,
        mobileNumber: String
    ): Single<Response<CountryCode>> {
        return settingsApiClient.getCountryCode(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }

    override fun setFingerprint(accessToken: String): Single<Response<FingerPrintToken>> {
        return settingsApiClient.setFingerPrint(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun oTPSettings(
        accessToken: String,
        otpSettingsForm: OTPSettingsForm
    ): Single<Response<Auth>> {
        return settingsApiClient.oTPSettings(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            otpSettingsForm
        )
    }

    override fun getOTPSettings(accessToken: String): Single<Response<OTPSettingsDto>> {
        return settingsApiClient.getOTPSettings(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun oTPSettingsVerify(
        accessToken: String,
        verifyOTPForm: VerifyOTPForm
    ): Single<Response<OTPSettingsDto>> {
        return settingsApiClient.oTPSettingsVerify(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            verifyOTPForm
        )
    }

    override fun oTPSettingsVerifyResend(
        accessToken: String,
        resendOTPForm: com.unionbankph.corporate.auth.data.form.ResendOTPForm
    ): Single<Response<Auth>> {
        return settingsApiClient.oTPSettingsVerifyResend(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            resendOTPForm
        )
    }

    override fun totpSubscribe(
        accessToken: String,
        manageDeviceForm: ManageDeviceForm
    ): Single<Response<TOTPSubscribeDto>> {
        return settingsApiClient.totpSubscribe(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            manageDeviceForm
        )
    }

    override fun getOTPTypes(accessToken: String): Single<Response<OTPTypeDto>> {
        return settingsApiClient.getOTPTypes(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun setOTPType(
        accessToken: String,
        otpTypeForm: OTPTypeForm
    ): Single<Response<OTPTypeDto>> {
        return settingsApiClient.setOTPType(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            otpTypeForm
        )
    }

    override fun unTrustDevice(
        accessToken: String,
        manageDeviceForm: ManageDeviceForm
    ): Single<Response<Message>> {
        return settingsApiClient.unTrustDevice(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            manageDeviceForm
        )
    }

    override fun forgetDevice(accessToken: String, id: String): Single<Response<Message>> {
        return settingsApiClient.forgetDevice(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }

    override fun getManageDevicesList(accessToken: String): Single<Response<ManageDevicesDto>> {
        return settingsApiClient.getManageDevicesList(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun getManageDeviceDetail(
        accessToken: String,
        id: String
    ): Single<Response<ManageDeviceDetailDto>> {
        return settingsApiClient.getManageDeviceDetail(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id
        )
    }

    override fun getLoginHistory(
        accessToken: String,
        id: String,
        pageable: Pageable
    ): Single<Response<PagedDto<LastAccessed>>> {
        return settingsApiClient.getLoginHistory(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            id,
            pageable.page,
            pageable.size
        )
    }

    override fun getRecurrenceTypes(accessToken: String): Single<Response<MutableList<RecurrenceTypes>>> {
        return settingsApiClient.getRecurrenceTypes(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun logoutUser(accessToken: String): Single<Response<FingerPrintToken>> {
        return settingsApiClient.logoutUser(accessToken, BuildConfig.CLIENT_API_VERSION)
    }

    override fun getEnabledFeatures(): Single<Response<EnabledFeaturesDto>> {
        return settingsApiClient.getEnabledFeatures(ApiVersionEnum.V4.value)
    }
}
