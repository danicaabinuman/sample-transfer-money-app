package com.unionbankph.corporate.auth.data.source.remote

import com.unionbankph.corporate.auth.data.form.*
import com.unionbankph.corporate.auth.data.model.*
import com.unionbankph.corporate.common.data.form.VerifyOTPForm
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.user_creation.data.form.UcNominatePasswordForm
import com.unionbankph.corporate.user_creation.data.form.ValidateContactInfoForm
import io.reactivex.Single
import retrofit2.Response

/**
 * Created by herald25santos on 2020-01-17
 */
interface AuthRemote {

    fun privacyPolicy(accessToken: String): Single<Response<Message>>

    fun agreeMCDTerms(accessToken: String): Single<Response<Message>>

    fun login(loginForm: LoginForm): Single<Response<Auth>>

    fun loginEBankingMigration(
        loginEBankingMigrationForm: LoginEBankingMigrationForm
    ): Single<Response<LoginMigrationDto>>

    fun eBankingResendOTPMigration(
        temporaryCorporateUserId: String,
        eBankingResendOTPForm: EBankingResendOTPForm
    ): Single<Response<Auth>>

    fun loginFingerprint(
        loginFingerprintForm: LoginFingerprintForm
    ): Single<Response<UserDetails>>

    fun loginOTP(loginOTPForm: LoginOTPForm): Single<Response<UserDetails>>

    fun loginResendOTP(resendOTPForm: ResendOTPForm): Single<Response<Auth>>

    fun resetPassVerify(
        resetPasswordVerifyForm: ResetPasswordVerifyForm
    ): Single<Response<VerifyResetPass>>

    fun resetPass(
        passwordRecoveryForm: PasswordRecoveryForm
    ): Single<Response<Auth>>

    fun resetPassOTP(
        resetPasswordOTPForm: ResetPasswordOTPForm
    ): Single<Response<Message>>

    fun resetPassResendOTP(
        resetPasswordResendOTPForm: ResetPasswordResendOTPForm
    ): Single<Response<Auth>>

    fun resetPassNew(
        resetPasswordForm: ResetPasswordForm
    ): Single<Response<Message>>

    fun userCreationValidateContact(
        validateContactInfoForm: ValidateContactInfoForm
    ): Single<Response<ContactValidityResponse>>

    fun userCreationValidateOTP(
        verifyOTPForm: VerifyOTPForm
    ): Single<Response<UserCreationOTPVerified>>

    fun userCreationNominatePassword(
        form: UcNominatePasswordForm
    ): Single<Response<UserCreationOTPVerified>>

    fun nominatePasswordActivation(
        activationPasswordForm: ActivationPasswordForm
    ): Single<Response<PasswordToken>>

    fun nominatePasswordOTP(
        nominatePasswordOTPForm: NominatePasswordOTPForm
    ): Single<Response<Message>>

    fun nominatePasswordResentOTP(
        nominatePasswordResendOTPForm: NominatePasswordResendOTPForm
    ): Single<Response<Auth>>

    fun nominatePassword(
        nominatePasswordForm: NominatePasswordForm
    ): Single<Response<Auth>>

    fun nominateEmailMigration(
        temporaryCorporateUserId: String,
        migrationNominateEmailForm: MigrationNominateEmailForm
    ): Single<Response<LoginMigrationDto>>

    fun nominatePasswordMigration(
        temporaryCorporateUserId: String,
        migrationNominatePasswordForm: MigrationNominatePasswordForm
    ): Single<Response<LoginMigrationDto>>

    fun nominateMobileNumberMigration(
        temporaryCorporateUserId: String,
        migrationNominateMobileNumberForm: MigrationNominateMobileNumberForm
    ): Single<Response<Auth>>

    fun migrationMergeAccount(
        migrationMergeAccountForm: MigrationMergeAccountForm
    ): Single<Response<MigrationSubmitDto>>

    fun submitMigration(
        migrationForm: MigrationForm
    ): Single<Response<MigrationSubmitDto>>

    fun migrationConfirmEmail(
        emailConfirmationForm: EmailConfirmationForm
    ): Single<Response<ConfirmationEmailDto>>

    fun loginECreditingMigration(
        loginECreditingMigrationForm: LoginECreditingMigrationForm
    ): Single<Response<ECredLoginDto>>

    fun loginOTPMigration(
        eCredOTPForm: ECredOTPForm
    ): Single<Response<ECredLoginOTPDto>>

    fun loginResendOTPMigration(
        resendOTPForm: ResendOTPForm
    ): Single<Response<com.unionbankph.corporate.common.data.model.Auth>>

    fun corpLoginResendOTPMigration(
        accessToken: String,
        resendOTPForm: ResendOTPForm
    ): Single<Response<com.unionbankph.corporate.common.data.model.Auth>>

    fun updateDetailsResendOTPMigration(
        accessToken: String,
        resendOTPForm: ResendOTPForm
    ): Single<Response<com.unionbankph.corporate.common.data.model.Auth>>

    fun nominateECreditingEmailAddress(
        accessToken: String,
        emailAddress: String
    ): Single<Response<NominateEmailDto>>

    fun nominateECredForm(
        accessToken: String,
        eCredForm: ECredForm
    ): Single<Response<ECredSubmitDto>>

    fun nominateECredFormOTP(
        accessToken: String,
        eCredOTPForm: ECredOTPForm
    ): Single<Response<ECredSubmitOTPDto>>

    fun resendECredEmailMigration(
        accessToken: String
    ): Single<Response<Message>>

    fun migrationECred(
        eCredEmailConfirmationForm: ECredEmailConfirmationForm
    ): Single<Response<ECredSubmitOTPDto>>

    fun mergeECredAccount(
        accessToken: String,
        eCredMergeAccountForm: ECredMergeAccountForm
    ): Single<Response<ECredMergeSubmitDto>>

    fun mergeECredAccountOTP(
        accessToken: String,
        eCredMergeAccountOTPForm: ECredMergeAccountOTPForm
    ): Single<Response<ECredSubmitOTPDto>>
}
