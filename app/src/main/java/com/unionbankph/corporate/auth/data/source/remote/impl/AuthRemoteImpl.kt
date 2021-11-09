package com.unionbankph.corporate.auth.data.source.remote.impl

import com.unionbankph.corporate.BuildConfig
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
import com.unionbankph.corporate.auth.data.source.remote.AuthRemote
import com.unionbankph.corporate.auth.data.source.remote.client.AuthApiClient
import com.unionbankph.corporate.common.data.form.VerifyOTPForm
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.user_creation.data.form.UcNominatePasswordForm
import com.unionbankph.corporate.user_creation.data.form.ValidateContactInfoForm
import io.reactivex.Single
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * Created by herald25santos on 2020-01-17
 */
class AuthRemoteImpl
@Inject
constructor(
    retrofit: Retrofit
) : AuthRemote {

    private val authApiClient: AuthApiClient =
        retrofit.create(AuthApiClient::class.java)

    override fun privacyPolicy(accessToken: String): Single<Response<Message>> {
        return authApiClient.privacyPolicy(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun agreeMCDTerms(accessToken: String): Single<Response<Message>> {
        return authApiClient.agreeMCDTerms(
            accessToken,
            BuildConfig.CLIENT_API_VERSION
        )
    }

    override fun login(loginForm: LoginForm): Single<Response<Auth>> {
        return authApiClient.login(BuildConfig.CLIENT_API_VERSION, loginForm)
    }

    override fun loginEBankingMigration(
        loginEBankingMigrationForm: LoginEBankingMigrationForm
    ): Single<Response<LoginMigrationDto>> {
        return authApiClient.loginEBankingMigration(
            BuildConfig.CLIENT_API_VERSION,
            loginEBankingMigrationForm
        )
    }

    override fun eBankingResendOTPMigration(
        temporaryCorporateUserId: String,
        eBankingResendOTPForm: EBankingResendOTPForm
    ): Single<Response<Auth>> {
        return authApiClient.eBankingResendOTPMigration(
            BuildConfig.CLIENT_API_VERSION,
            temporaryCorporateUserId,
            eBankingResendOTPForm
        )
    }

    override fun loginECreditingMigration(
        loginECreditingMigrationForm: LoginECreditingMigrationForm
    ): Single<Response<ECredLoginDto>> {
        return authApiClient.loginECreditingMigration(
            VERSION,
            loginECreditingMigrationForm
        )
    }

    override fun loginFingerprint(
        loginFingerprintForm: LoginFingerprintForm
    ): Single<Response<UserDetails>> {
        return authApiClient.userLoginFingerprint(
            BuildConfig.CLIENT_API_VERSION,
            loginFingerprintForm
        )
    }

    override fun loginOTP(loginOTPForm: LoginOTPForm): Single<Response<UserDetails>> {
        return authApiClient.userOTP(BuildConfig.MSME_CLIENT_API_VERSION, loginOTPForm)
    }

    override fun loginResendOTP(
        resendOTPForm: ResendOTPForm
    ): Single<Response<Auth>> {
        return authApiClient.resendOTP(BuildConfig.CLIENT_API_VERSION, resendOTPForm)
    }

    override fun resetPass(
        passwordRecoveryForm: PasswordRecoveryForm
    ): Single<Response<Auth>> {
        return authApiClient.resetPass(BuildConfig.CLIENT_API_VERSION, passwordRecoveryForm)
    }

    override fun resetPassOTP(
        resetPasswordOTPForm: ResetPasswordOTPForm
    ): Single<Response<Message>> {
        return authApiClient.resetPassOTP(BuildConfig.CLIENT_API_VERSION, resetPasswordOTPForm)
    }

    override fun resetPassResendOTP(
        resetPasswordResendOTPForm: ResetPasswordResendOTPForm
    ): Single<Response<Auth>> {
        return authApiClient.resetPassResendOTP(
            BuildConfig.CLIENT_API_VERSION,
            resetPasswordResendOTPForm
        )
    }

    override fun nominatePasswordActivation(
        activationPasswordForm: ActivationPasswordForm
    ): Single<Response<PasswordToken>> {
        return authApiClient.activateAccount(
            BuildConfig.CLIENT_API_VERSION,
            activationPasswordForm
        )
    }

    override fun nominatePasswordOTP(
        nominatePasswordOTPForm: NominatePasswordOTPForm
    ): Single<Response<Message>> {
        return authApiClient.nominatePasswordOTP(
            BuildConfig.CLIENT_API_VERSION,
            nominatePasswordOTPForm
        )
    }

    override fun nominatePasswordResentOTP(
        nominatePasswordResendOTPForm: NominatePasswordResendOTPForm
    ): Single<Response<Auth>> {
        return authApiClient.nominatePasswordResendOTP(
            BuildConfig.CLIENT_API_VERSION,
            nominatePasswordResendOTPForm
        )
    }

    override fun nominatePassword(
        nominatePasswordForm: NominatePasswordForm
    ): Single<Response<Auth>> {
        return authApiClient.nominatePassword(
            BuildConfig.CLIENT_API_VERSION,
            nominatePasswordForm
        )
    }

    override fun resetPassVerify(
        resetPasswordVerifyForm: ResetPasswordVerifyForm
    ): Single<Response<VerifyResetPass>> {
        return authApiClient.resetPassVerify(
            BuildConfig.CLIENT_API_VERSION,
            resetPasswordVerifyForm
        )
    }

    override fun resetPassNew(
        resetPasswordForm: ResetPasswordForm
    ): Single<Response<Message>> {
        return authApiClient.resetPassNew(
            BuildConfig.CLIENT_API_VERSION,
            resetPasswordForm
        )
    }

    override fun userCreationValidateContact(
        validateContactInfoForm: ValidateContactInfoForm
    ): Single<Response<Auth>> {
        return authApiClient.userCreationValidateContact(
            BuildConfig.MSME_CLIENT_API_VERSION,
            validateContactInfoForm
        )
    }

    override fun userCreationValidateOTP(
        verifyOTPForm: VerifyOTPForm
    ): Single<Response<UserCreationOTPVerified>> {
        return authApiClient.userCreationValidateOTP(
            BuildConfig.MSME_CLIENT_API_VERSION,
            verifyOTPForm
        )
    }

    override fun userCreationNominatePassword(
        form: UcNominatePasswordForm
    ): Single<Response<UserCreationAuth>> {
        return authApiClient.userCreationNominatePassword(
            BuildConfig.MSME_CLIENT_API_VERSION,
            form
        )
    }

    override fun userCreationResendOTP(form: ResendOTPForm
    ): Single<Response<Auth>> {
        return authApiClient.userCreationResendOTP(
            BuildConfig.MSME_CLIENT_API_VERSION,
            form
        )
    }

    override fun nominateEmailMigration(
        temporaryCorporateUserId: String,
        migrationNominateEmailForm: MigrationNominateEmailForm
    ): Single<Response<LoginMigrationDto>> {
        return authApiClient.migrationNominateEmailAddress(
            BuildConfig.CLIENT_API_VERSION,
            temporaryCorporateUserId,
            migrationNominateEmailForm
        )
    }

    override fun nominatePasswordMigration(
        temporaryCorporateUserId: String,
        migrationNominatePasswordForm: MigrationNominatePasswordForm
    ): Single<Response<LoginMigrationDto>> {
        return authApiClient.migrationNominatePassword(
            BuildConfig.CLIENT_API_VERSION,
            temporaryCorporateUserId,
            migrationNominatePasswordForm
        )
    }

    override fun nominateMobileNumberMigration(
        temporaryCorporateUserId: String,
        migrationNominateMobileNumberForm: MigrationNominateMobileNumberForm
    ): Single<Response<Auth>> {
        return authApiClient.migrationNominateMobileNumber(
            BuildConfig.CLIENT_API_VERSION,
            temporaryCorporateUserId,
            migrationNominateMobileNumberForm
        )
    }

    override fun migrationMergeAccount(
        migrationMergeAccountForm: MigrationMergeAccountForm
    ): Single<Response<MigrationSubmitDto>> {
        return authApiClient.migrationMergeAccount(
            BuildConfig.CLIENT_API_VERSION,
            migrationMergeAccountForm
        )
    }

    override fun submitMigration(migrationForm: MigrationForm): Single<Response<MigrationSubmitDto>> {
        return authApiClient.submitMigration(
            BuildConfig.CLIENT_API_VERSION,
            migrationForm
        )
    }

    override fun migrationConfirmEmail(
        emailConfirmationForm: EmailConfirmationForm
    ): Single<Response<ConfirmationEmailDto>> {
        return authApiClient.migrationConfirmEmail(
            BuildConfig.CLIENT_API_VERSION,
            emailConfirmationForm
        )
    }

    override fun loginOTPMigration(eCredOTPForm: ECredOTPForm): Single<Response<ECredLoginOTPDto>> {
        return authApiClient.loginOTPMigration(
            VERSION,
            eCredOTPForm
        )
    }

    override fun loginResendOTPMigration(
        resendOTPForm: ResendOTPForm
    ): Single<Response<com.unionbankph.corporate.common.data.model.Auth>> {
        return authApiClient.loginResendOTPMigration(
            VERSION,
            resendOTPForm
        )
    }

    override fun corpLoginResendOTPMigration(
        accessToken: String,
        resendOTPForm: ResendOTPForm
    ): Single<Response<com.unionbankph.corporate.common.data.model.Auth>> {
        return authApiClient.corpLoginResendOTPMigration(
            "Bearer $accessToken",
            "v2",
            resendOTPForm
        )
    }

    override fun updateDetailsResendOTPMigration(
        accessToken: String,
        resendOTPForm: ResendOTPForm
    ): Single<Response<com.unionbankph.corporate.common.data.model.Auth>> {
        return authApiClient.updateDetailsResendOTPMigration(
            "Bearer $accessToken",
            "v2",
            resendOTPForm
        )
    }

    override fun nominateECreditingEmailAddress(
        accessToken: String,
        emailAddress: String
    ): Single<Response<NominateEmailDto>> {
        return authApiClient.nominateECreditingEmailAddress(
            "Bearer $accessToken",
            VERSION,
            emailAddress
        )
    }

    override fun nominateECredForm(
        accessToken: String,
        eCredForm: ECredForm
    ): Single<Response<ECredSubmitDto>> {
        return authApiClient.nominateECredForm(
            "Bearer $accessToken",
            VERSION,
            eCredForm
        )
    }

    override fun nominateECredFormOTP(
        accessToken: String,
        eCredOTPForm: ECredOTPForm
    ): Single<Response<ECredSubmitOTPDto>> {
        return authApiClient.nominateECredFormOTP(
            "Bearer $accessToken",
            VERSION,
            eCredOTPForm
        )
    }

    override fun resendECredEmailMigration(accessToken: String): Single<Response<Message>> {
        return authApiClient.resendECredEmailMigration(
            "Bearer $accessToken",
            VERSION
        )
    }

    override fun migrationECred(
        eCredEmailConfirmationForm: ECredEmailConfirmationForm
    ): Single<Response<ECredSubmitOTPDto>> {
        return authApiClient.migrationECred(
            VERSION,
            eCredEmailConfirmationForm
        )
    }

    override fun mergeECredAccount(
        accessToken: String,
        eCredMergeAccountForm: ECredMergeAccountForm
    ): Single<Response<ECredMergeSubmitDto>> {
        return authApiClient.mergeECredAccount(
            "Bearer $accessToken",
            VERSION,
            eCredMergeAccountForm
        )
    }

    override fun mergeECredAccountOTP(
        accessToken: String,
        eCredMergeAccountOTPForm: ECredMergeAccountOTPForm
    ): Single<Response<ECredSubmitOTPDto>> {
        return authApiClient.mergeECredAccountOTP(
            "Bearer $accessToken",
            VERSION,
            eCredMergeAccountOTPForm
        )
    }

    companion object {
        const val VERSION = "v1"
    }
}
