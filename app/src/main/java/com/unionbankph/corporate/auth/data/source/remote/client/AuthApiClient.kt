package com.unionbankph.corporate.auth.data.source.remote.client

import com.unionbankph.corporate.auth.data.form.ActivationPasswordForm
import com.unionbankph.corporate.auth.data.form.EBankingResendOTPForm
import com.unionbankph.corporate.auth.data.form.ECredEmailConfirmationForm
import com.unionbankph.corporate.auth.data.form.ECredForm
import com.unionbankph.corporate.auth.data.form.ECredMergeAccountForm
import com.unionbankph.corporate.auth.data.form.ECredMergeAccountOTPForm
import com.unionbankph.corporate.auth.data.form.ECredOTPForm
import com.unionbankph.corporate.auth.data.form.EmailConfirmationForm
import com.unionbankph.corporate.auth.data.form.LoginEBankingMigrationForm
import com.unionbankph.corporate.auth.data.form.LoginECreditingMigrationForm
import com.unionbankph.corporate.auth.data.form.LoginFingerprintForm
import com.unionbankph.corporate.auth.data.form.LoginForm
import com.unionbankph.corporate.auth.data.form.LoginOTPForm
import com.unionbankph.corporate.auth.data.form.MigrationForm
import com.unionbankph.corporate.auth.data.form.MigrationMergeAccountForm
import com.unionbankph.corporate.auth.data.form.MigrationNominateEmailForm
import com.unionbankph.corporate.auth.data.form.MigrationNominateMobileNumberForm
import com.unionbankph.corporate.auth.data.form.MigrationNominatePasswordForm
import com.unionbankph.corporate.auth.data.form.NominatePasswordForm
import com.unionbankph.corporate.auth.data.form.NominatePasswordOTPForm
import com.unionbankph.corporate.auth.data.form.NominatePasswordResendOTPForm
import com.unionbankph.corporate.auth.data.form.PasswordRecoveryForm
import com.unionbankph.corporate.auth.data.form.ResendOTPForm
import com.unionbankph.corporate.auth.data.form.ResetPasswordForm
import com.unionbankph.corporate.auth.data.form.ResetPasswordOTPForm
import com.unionbankph.corporate.auth.data.form.ResetPasswordResendOTPForm
import com.unionbankph.corporate.auth.data.form.ResetPasswordVerifyForm
import com.unionbankph.corporate.auth.data.model.*
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.open_account.data.OpenAccountForm
import com.unionbankph.corporate.open_account.data.form.ValidateContactInfoForm
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthApiClient {

    @POST("api/{api_version}/corporate/login")
    fun login(
        @Path("api_version")
        apiVersion: String,
        @Body
        loginForm: LoginForm
    ): Single<Response<Auth>>

    @POST("api/{api_version}/corporate/login/fingerprint")
    fun userLoginFingerprint(
        @Path("api_version")
        apiVersion: String,
        @Body
        loginFingerprintForm: LoginFingerprintForm
    ): Single<Response<UserDetails>>

    @POST("api/{api_version}/corporate/login/otp")
    fun userOTP(
        @Path("api_version")
        apiVersion: String,
        @Body
        loginOTPForm: LoginOTPForm
    ): Single<Response<UserDetails>>

    @POST("api/{api_version}/corporate-users/password")
    fun nominatePassword(
        @Path("api_version")
        apiVersion: String,
        @Body
        nominatePasswordForm: NominatePasswordForm
    ): Single<Response<Auth>>

    @POST("api/{api_version}/corporate/reset-password")
    fun resetPass(
        @Path("api_version")
        apiVersion: String,
        @Body
        passwordRecoveryForm: PasswordRecoveryForm
    ): Single<Response<Auth>>

    @POST("api/{api_version}/corporate/reset-password/otp")
    fun resetPassOTP(
        @Path("api_version")
        apiVersion: String,
        @Body
        resetPasswordOTPForm: ResetPasswordOTPForm
    ): Single<Response<Message>>

    @POST("api/{api_version}/corporate-users/password/otp")
    fun nominatePasswordOTP(
        @Path("api_version")
        apiVersion: String,
        @Body
        nominatePasswordOTPForm: NominatePasswordOTPForm
    ): Single<Response<Message>>

    @POST("api/{api_version}/corporate/login/otp/resend")
    fun resendOTP(
        @Path("api_version")
        apiVersion: String,
        @Body
        resendOTPForm: ResendOTPForm
    ): Single<Response<Auth>>

    @POST("api/{api_version}/corporate/reset-password/otp/resend")
    fun resetPassResendOTP(
        @Path("api_version")
        apiVersion: String,
        @Body
        resetPasswordResendOTPForm: ResetPasswordResendOTPForm
    ): Single<Response<Auth>>

    @POST("api/{api_version}/corporate-users/password/otp/resend")
    fun nominatePasswordResendOTP(
        @Path("api_version")
        apiVersion: String,
        @Body
        nominatePasswordResendOTPForm: NominatePasswordResendOTPForm
    ): Single<Response<Auth>>

    @POST("api/{api_version}/corporate/reset-password/verify")
    fun resetPassVerify(
        @Path("api_version")
        apiVersion: String,
        @Body
        resetPasswordVerifyForm: ResetPasswordVerifyForm
    ): Single<Response<VerifyResetPass>>

    @POST("api/{api_version}/corporate-users/activation")
    fun activateAccount(
        @Path("api_version")
        apiVersion: String,
        @Body
        activationPasswordForm: ActivationPasswordForm
    ): Single<Response<PasswordToken>>

    @POST("api/{api_version}/corporate/reset-password/new")
    fun resetPassNew(
        @Path("api_version")
        apiVersion: String,
        @Body
        resetPasswordForm: ResetPasswordForm
    ): Single<Response<Message>>

    @POST("msme/api/{api_version}/corporate-users/validate-corporate-user")
    fun validateContactInfo(
        @Path("api_version")
        apiVersion: String,
        @Body
        validateContactInfoForm: ValidateContactInfoForm
    ): Single<Response<ContactValidityResponse>>

    @POST("api/{api_version}/config/policy")
    fun privacyPolicy(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<Message>>

    @POST("api/{api_version}/config/updated-terms-and-conditions")
    fun agreeMCDTerms(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<Message>>

    @POST("api/{api_version}/migrations/login")
    fun loginEBankingMigration(
        @Path("api_version")
        apiVersion: String,
        @Body
        loginEBankingMigrationForm: LoginEBankingMigrationForm
    ): Single<Response<LoginMigrationDto>>

    @POST("api/{api_version}/migrations/mobile-number/{temporary-corporate-userid}/resend")
    fun eBankingResendOTPMigration(
        @Path("api_version")
        apiVersion: String,
        @Path("temporary-corporate-userid")
        temporaryCorporateUserId: String,
        @Body
        eBankingResendOTPForm: EBankingResendOTPForm
    ): Single<Response<Auth>>

    @POST("api/{api_version}/migrations/email-address/{temporary_corporate_user_id}")
    fun migrationNominateEmailAddress(
        @Path("api_version")
        apiVersion: String,
        @Path("temporary_corporate_user_id")
        temporaryCorporateUserId: String,
        @Body
        migrationNominateEmailForm: MigrationNominateEmailForm
    ): Single<Response<LoginMigrationDto>>

    @POST("api/{api_version}/migrations/password/{temporary_corporate_user_id}")
    fun migrationNominatePassword(
        @Path("api_version")
        apiVersion: String,
        @Path("temporary_corporate_user_id")
        temporaryCorporateUserId: String,
        @Body
        migrationNominatePasswordForm: MigrationNominatePasswordForm
    ): Single<Response<LoginMigrationDto>>

    @POST("api/{api_version}/migrations/mobile-number/{temporary_corporate_user_id}")
    fun migrationNominateMobileNumber(
        @Path("api_version")
        apiVersion: String,
        @Path("temporary_corporate_user_id")
        temporaryCorporateUserId: String,
        @Body
        migrationNominateMobileNumberForm: MigrationNominateMobileNumberForm
    ): Single<Response<Auth>>

    @POST("api/{api_version}/migrations/submit")
    fun submitMigration(
        @Path("api_version")
        apiVersion: String,
        @Body
        migrationForm: MigrationForm
    ): Single<Response<MigrationSubmitDto>>

    @POST("api/{api_version}/migrations/merge-account")
    fun migrationMergeAccount(
        @Path("api_version")
        apiVersion: String,
        @Body
        migrationMergeAccountForm: MigrationMergeAccountForm
    ): Single<Response<MigrationSubmitDto>>

    @POST("api/{api_version}/migrations/email-confirmation")
    fun migrationConfirmEmail(
        @Path("api_version")
        apiVersion: String,
        @Body
        emailConfirmationForm: EmailConfirmationForm
    ): Single<Response<ConfirmationEmailDto>>

    @POST("ecred-migration/api/{api_version}/auth")
    fun loginECreditingMigration(
        @Path("api_version")
        apiVersion: String,
        @Body
        loginECreditingMigrationForm: LoginECreditingMigrationForm
    ): Single<Response<ECredLoginDto>>

    @POST("ecred-migration/api/{api_version}/auth/otp-validation")
    fun loginOTPMigration(
        @Path("api_version")
        apiVersion: String,
        @Body
        eCredOTPForm: ECredOTPForm
    ): Single<Response<ECredLoginOTPDto>>

    @POST("ecred-migration/api/{api_version}/auth/otp-resend")
    fun loginResendOTPMigration(
        @Path("api_version")
        apiVersion: String,
        @Body
        resendOTPForm: ResendOTPForm
    ): Single<Response<com.unionbankph.corporate.common.data.model.Auth>>

    @POST("ecred-migration/api/{api_version}/users/corporate/auth/otp-resend")
    fun corpLoginResendOTPMigration(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        resendOTPForm: ResendOTPForm
    ): Single<Response<com.unionbankph.corporate.common.data.model.Auth>>

    @POST("ecred-migration/api/{api_version}/users/me/otp-resend")
    fun updateDetailsResendOTPMigration(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        resendOTPForm: ResendOTPForm
    ): Single<Response<com.unionbankph.corporate.common.data.model.Auth>>

    @GET("ecred-migration/api/{api_version}/users/email-address/{email_address}/available")
    fun nominateECreditingEmailAddress(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("email_address")
        emailAddress: String
    ): Single<Response<NominateEmailDto>>

    @PUT("ecred-migration/api/{api_version}/users/me")
    fun nominateECredForm(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        eCredForm: ECredForm
    ): Single<Response<ECredSubmitDto>>

    @POST("ecred-migration/api/{api_version}/users/me/otp-validation")
    fun nominateECredFormOTP(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        eCredOTPForm: ECredOTPForm
    ): Single<Response<ECredSubmitOTPDto>>

    @POST("ecred-migration/api/{api_version}/migrations/email")
    fun resendECredEmailMigration(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<Message>>

    @POST("ecred-migration/api/{api_version}/migrations")
    fun migrationECred(
        @Path("api_version")
        apiVersion: String,
        @Body
        eCredEmailConfirmationForm: ECredEmailConfirmationForm
    ): Single<Response<ECredSubmitOTPDto>>

    @POST("ecred-migration/api/{api_version}/users/corporate/auth")
    fun mergeECredAccount(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        eCredMergeAccountForm: ECredMergeAccountForm
    ): Single<Response<ECredMergeSubmitDto>>

    @POST("ecred-migration/api/{api_version}/users/corporate/auth/otp-validation")
    fun mergeECredAccountOTP(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        eCredMergeAccountOTPForm: ECredMergeAccountOTPForm
    ): Single<Response<ECredSubmitOTPDto>>
}
