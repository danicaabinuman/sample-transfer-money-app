package com.unionbankph.corporate.auth.data

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
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.auth.data.model.ConfirmationEmailDto
import com.unionbankph.corporate.auth.data.model.ECredLoginDto
import com.unionbankph.corporate.auth.data.model.ECredLoginOTPDto
import com.unionbankph.corporate.auth.data.model.ECredMergeSubmitDto
import com.unionbankph.corporate.auth.data.model.ECredSubmitDto
import com.unionbankph.corporate.auth.data.model.ECredSubmitOTPDto
import com.unionbankph.corporate.auth.data.model.LoginMigrationDto
import com.unionbankph.corporate.auth.data.model.MigrationSubmitDto
import com.unionbankph.corporate.auth.data.model.NominateEmailDto
import com.unionbankph.corporate.auth.data.model.PasswordToken
import com.unionbankph.corporate.auth.data.model.UserDetails
import com.unionbankph.corporate.auth.data.model.VerifyResetPass
import com.unionbankph.corporate.common.data.model.Message
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface AuthGateway {

    fun isLoggedIn(): Observable<Boolean>

    fun isLaunched(): Observable<Boolean>

    fun clearCredential(): Completable

    fun clearLoginCredential(): Completable

    fun privacyPolicy(): Completable

    fun agreeMCDTerms(): Completable

    fun saveCredential(userDetails: UserDetails): Completable

    fun login(loginForm: LoginForm): Single<Auth>
    fun eBankingResendOTPMigration(
        temporaryCorporateUserId: String,
        eBankingResendOTPForm: EBankingResendOTPForm
    ): Single<Auth>

    fun loginEBankingMigration(loginEBankingMigrationForm: LoginEBankingMigrationForm): Single<LoginMigrationDto>
    fun loginFingerprint(loginFingerprintForm: LoginFingerprintForm): Single<UserDetails>
    fun loginOTP(loginOTPForm: LoginOTPForm): Single<UserDetails>
    fun loginResendOTP(resendOTPForm: ResendOTPForm): Single<Auth>

    fun resetPassVerify(resetPasswordVerifyForm: ResetPasswordVerifyForm): Single<VerifyResetPass>
    fun resetPass(passwordRecoveryForm: PasswordRecoveryForm): Single<Auth>
    fun resetPassOTP(resetPasswordOTPForm: ResetPasswordOTPForm): Single<Message>
    fun resetPassResendOTP(resetPasswordResendOTPForm: ResetPasswordResendOTPForm): Single<Auth>
    fun resetPassNew(resetPasswordForm: ResetPasswordForm): Single<Message>

    fun nominatePasswordActivation(activationPasswordForm: ActivationPasswordForm): Single<PasswordToken>
    fun nominatePasswordOTP(nominatePasswordOTPForm: NominatePasswordOTPForm): Single<Message>
    fun nominatePasswordResentOTP(nominatePasswordResendOTPForm: NominatePasswordResendOTPForm): Single<Auth>
    fun nominatePassword(nominatePasswordForm: NominatePasswordForm): Single<Auth>

    fun nominateEmailMigration(
        temporaryCorporateUserId: String,
        migrationNominateEmailForm: MigrationNominateEmailForm
    ): Single<LoginMigrationDto>

    fun nominatePasswordMigration(
        temporaryCorporateUserId: String,
        migrationNominatePasswordForm: MigrationNominatePasswordForm
    ): Single<LoginMigrationDto>

    fun nominateMobileNumberMigration(
        temporaryCorporateUserId: String,
        migrationNominateMobileNumberForm: MigrationNominateMobileNumberForm
    ): Single<Auth>

    fun migrationMergeAccount(
        migrationMergeAccountForm: MigrationMergeAccountForm
    ): Single<MigrationSubmitDto>

    fun submitMigration(
        migrationForm: MigrationForm
    ): Single<MigrationSubmitDto>

    fun migrationConfirmEmail(
        emailConfirmationForm: EmailConfirmationForm
    ): Single<ConfirmationEmailDto>

    fun loginECreditingMigration(loginECreditingMigrationForm: LoginECreditingMigrationForm): Single<ECredLoginDto>

    fun loginMigrationOTP(eCredOTPForm: ECredOTPForm): Single<ECredLoginOTPDto>

    fun loginResendOTPMigration(
        resendOTPForm: ResendOTPForm
    ): Single<com.unionbankph.corporate.common.data.model.Auth>

    fun corpLoginResendOTPMigration(
        accessToken: String,
        resendOTPForm: ResendOTPForm
    ): Single<com.unionbankph.corporate.common.data.model.Auth>

    fun updateDetailsResendOTPMigration(
        accessToken: String,
        resendOTPForm: ResendOTPForm
    ): Single<com.unionbankph.corporate.common.data.model.Auth>

    fun saveECredPayload(eCredForm: ECredForm): Completable

    fun nominateECreditingEmailAddress(
        accessToken: String,
        emailAddress: String
    ): Single<NominateEmailDto>

    fun nominateECredForm(
        accessToken: String
    ): Single<ECredSubmitDto>

    fun nominateECredFormOTP(
        accessToken: String,
        eCredOTPForm: ECredOTPForm
    ): Single<ECredSubmitOTPDto>

    fun resendECredEmailMigration(
        accessToken: String
    ): Single<Message>

    fun migrationECred(
        eCredEmailConfirmationForm: ECredEmailConfirmationForm
    ): Single<ECredSubmitOTPDto>

    fun mergeECredAccount(
        accessToken: String,
        eCredMergeAccountForm: ECredMergeAccountForm
    ): Single<ECredMergeSubmitDto>

    fun mergeECredAccountOTP(
        accessToken: String,
        eCredMergeAccountOTPForm: ECredMergeAccountOTPForm
    ): Single<ECredSubmitOTPDto>
}
