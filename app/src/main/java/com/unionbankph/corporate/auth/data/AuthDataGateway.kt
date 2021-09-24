package com.unionbankph.corporate.auth.data

import com.unionbankph.corporate.app.util.SettingsUtil
import com.unionbankph.corporate.auth.data.form.*
import com.unionbankph.corporate.auth.data.model.*
import com.unionbankph.corporate.auth.data.source.local.AuthCache
import com.unionbankph.corporate.auth.data.source.remote.AuthRemote
import com.unionbankph.corporate.common.data.form.VerifyOTPForm
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.common.presentation.constant.PromptTypeEnum
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.user_creation.data.form.UcNominatePasswordForm
import com.unionbankph.corporate.user_creation.data.form.ValidateContactInfoForm
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import javax.inject.Inject

class AuthDataGateway
@Inject
constructor(
    private val responseProvider: ResponseProvider,
    private val authRemote: AuthRemote,
    private val authCache: AuthCache,
    private val settingsGateway: SettingsGateway,
    private val settingsUtil: SettingsUtil
) : AuthGateway {

    override fun isLaunched(): Observable<Boolean> {
        return authCache.isLaunched()
    }

    override fun isLoggedIn(): Observable<Boolean> {
        return authCache.isLoggedIn()
    }

    override fun privacyPolicy(): Completable {
        return settingsGateway.getAccessToken()
            .flatMap { authRemote.privacyPolicy(it) }
            .flatMapCompletable { responseProvider.executeResponseCompletable(it) }
    }

    override fun agreeMCDTerms(): Completable {
        return settingsGateway.getAccessToken()
            .flatMap { authRemote.agreeMCDTerms(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
            .flatMapCompletable { authCache.readMCDTerms() }
    }

    override fun clearCredential(): Completable {
        return authCache.clearCredential()
    }

    override fun clearLoginCredential(): Completable {
        return authCache.clearLoginCredential()
    }

    override fun saveCredential(userDetails: UserDetails): Completable {
        return authCache.saveCredential(userDetails)
    }

    override fun login(loginForm: LoginForm): Single<Auth> {
        return settingsGateway.getNotificationToken()
            .zipWith(settingsGateway.getUdid().toSingle())
            .flatMap {
                loginForm.registrationToken = it.first
                loginForm.udid = it.second
                return@flatMap authRemote.login(loginForm)
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun eBankingResendOTPMigration(
        temporaryCorporateUserId: String,
        eBankingResendOTPForm: EBankingResendOTPForm
    ): Single<Auth> {
        return authRemote.eBankingResendOTPMigration(
            temporaryCorporateUserId,
            eBankingResendOTPForm
        )
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun loginEBankingMigration(loginEBankingMigrationForm: LoginEBankingMigrationForm): Single<LoginMigrationDto> {
        return authRemote.loginEBankingMigration(loginEBankingMigrationForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun loginECreditingMigration(
        loginECreditingMigrationForm: LoginECreditingMigrationForm
    ): Single<ECredLoginDto> {
        return authRemote.loginECreditingMigration(loginECreditingMigrationForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun loginFingerprint(loginFingerprintForm: LoginFingerprintForm): Single<UserDetails> {
        return settingsGateway.getFingerprintEmail()
            .zipWith(settingsGateway.getUdid())
            .flatMapSingle {
                loginFingerprintForm.username = it.first
                loginFingerprintForm.udid = it.second
                return@flatMapSingle settingsGateway.getNotificationToken()
            }
            .flatMap {
                loginFingerprintForm.registrationToken = it
                return@flatMap authRemote.loginFingerprint(loginFingerprintForm)
            }
            .flatMap {
                responseProvider.executeResponseCustomSingle(it) { apiError ->
                    val error = apiError.errors[0]
                    if (error.message == "Invalid Fingerprint.") {
                        loginFingerprintForm.udid = settingsUtil.getUdId()
                        authRemote.loginFingerprint(loginFingerprintForm)
                            .flatMap {
                                settingsGateway.deleteFingerPrint().andThen(
                                    settingsGateway.updateRecentUser(
                                        PromptTypeEnum.BIOMETRIC, false
                                    )
                                ).toSingle { it }
                            }
                            .flatMap { responseProvider.executeResponseSingle(it) }
                    } else {
                        responseProvider.handleOnError(apiError)
                    }
                }
            }

    }

    override fun loginOTP(loginOTPForm: LoginOTPForm): Single<UserDetails> {
        return settingsGateway.getNotificationToken()
            .flatMap {
                loginOTPForm.registrationToken = it
                authRemote.loginOTP(loginOTPForm)
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun loginResendOTP(resendOTPForm: ResendOTPForm): Single<Auth> {
        return authRemote.loginResendOTP(resendOTPForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun resetPass(passwordRecoveryForm: PasswordRecoveryForm): Single<Auth> {
        return authRemote.resetPass(passwordRecoveryForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun resetPassOTP(resetPasswordOTPForm: ResetPasswordOTPForm): Single<Message> {
        return authRemote.resetPassOTP(resetPasswordOTPForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun resetPassResendOTP(
        resetPasswordResendOTPForm: ResetPasswordResendOTPForm
    ): Single<Auth> {
        return authRemote.resetPassResendOTP(resetPasswordResendOTPForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun nominatePasswordActivation(
        activationPasswordForm: ActivationPasswordForm
    ): Single<PasswordToken> {
        return authRemote.nominatePasswordActivation(activationPasswordForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun nominatePasswordOTP(nominatePasswordOTPForm: NominatePasswordOTPForm): Single<Message> {
        return authRemote.nominatePasswordOTP(nominatePasswordOTPForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun nominatePasswordResentOTP(
        nominatePasswordResendOTPForm: NominatePasswordResendOTPForm
    ): Single<Auth> {
        return authRemote.nominatePasswordResentOTP(nominatePasswordResendOTPForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun nominatePassword(nominatePasswordForm: NominatePasswordForm): Single<Auth> {
        return authRemote.nominatePassword(nominatePasswordForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun resetPassVerify(
        resetPasswordVerifyForm: ResetPasswordVerifyForm
    ): Single<VerifyResetPass> {
        return authRemote.resetPassVerify(resetPasswordVerifyForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun resetPassNew(
        resetPasswordForm: ResetPasswordForm
    ): Single<Message> {
        return authRemote.resetPassNew(resetPasswordForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun userCreationValidateContact(
        validateContactInfoForm: ValidateContactInfoForm
    ): Single<Auth> {
        return authRemote.userCreationValidateContact(validateContactInfoForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun userCreationValidateOTP(verifyOTPForm: VerifyOTPForm): Single<UserCreationOTPVerified> {
        return authRemote.userCreationValidateOTP(verifyOTPForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun userCreationNominatePassword(
        form: UcNominatePasswordForm
    ): Single<UserCreationOTPVerified> {
        return authRemote.userCreationNominatePassword(form)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun userCreationResendOTP(
        form: ResendOTPForm
    ): Single<Auth> {
        return authRemote.userCreationResendOTP(form)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun nominateEmailMigration(
        temporaryCorporateUserId: String,
        migrationNominateEmailForm: MigrationNominateEmailForm
    ): Single<LoginMigrationDto> {
        return authRemote.nominateEmailMigration(
            temporaryCorporateUserId,
            migrationNominateEmailForm
        )
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun nominatePasswordMigration(
        temporaryCorporateUserId: String,
        migrationNominatePasswordForm: MigrationNominatePasswordForm
    ): Single<LoginMigrationDto> {
        return authRemote.nominatePasswordMigration(
            temporaryCorporateUserId,
            migrationNominatePasswordForm
        )
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun nominateMobileNumberMigration(
        temporaryCorporateUserId: String,
        migrationNominateMobileNumberForm: MigrationNominateMobileNumberForm
    ): Single<Auth> {
        return authRemote.nominateMobileNumberMigration(
            temporaryCorporateUserId,
            migrationNominateMobileNumberForm
        )
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun migrationMergeAccount(
        migrationMergeAccountForm: MigrationMergeAccountForm
    ): Single<MigrationSubmitDto> {
        return authRemote.migrationMergeAccount(migrationMergeAccountForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun submitMigration(migrationForm: MigrationForm): Single<MigrationSubmitDto> {
        return authRemote.submitMigration(migrationForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun migrationConfirmEmail(
        emailConfirmationForm: EmailConfirmationForm
    ): Single<ConfirmationEmailDto> {
        return authRemote.migrationConfirmEmail(emailConfirmationForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun loginMigrationOTP(eCredOTPForm: ECredOTPForm): Single<ECredLoginOTPDto> {
        return authRemote.loginOTPMigration(eCredOTPForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun loginResendOTPMigration(
        resendOTPForm: ResendOTPForm
    ): Single<com.unionbankph.corporate.common.data.model.Auth> {
        return authRemote.loginResendOTPMigration(resendOTPForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun corpLoginResendOTPMigration(
        accessToken: String,
        resendOTPForm: ResendOTPForm
    ): Single<com.unionbankph.corporate.common.data.model.Auth> {
        return authRemote.corpLoginResendOTPMigration(accessToken, resendOTPForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun updateDetailsResendOTPMigration(
        accessToken: String,
        resendOTPForm: ResendOTPForm
    ): Single<com.unionbankph.corporate.common.data.model.Auth> {
        return authRemote.updateDetailsResendOTPMigration(accessToken, resendOTPForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun nominateECreditingEmailAddress(
        accessToken: String,
        emailAddress: String
    ): Single<NominateEmailDto> {
        return authRemote.nominateECreditingEmailAddress(accessToken, emailAddress)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun saveECredPayload(eCredForm: ECredForm): Completable {
        return authCache.saveECredPayload(eCredForm)
    }

    override fun nominateECredForm(accessToken: String): Single<ECredSubmitDto> {
        return authCache.getECredPayload()
            .flatMap { authRemote.nominateECredForm(accessToken, it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun nominateECredFormOTP(
        accessToken: String,
        eCredOTPForm: ECredOTPForm
    ): Single<ECredSubmitOTPDto> {
        return authRemote.nominateECredFormOTP(accessToken, eCredOTPForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun resendECredEmailMigration(accessToken: String): Single<Message> {
        return authRemote.resendECredEmailMigration(accessToken)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun migrationECred(
        eCredEmailConfirmationForm: ECredEmailConfirmationForm
    ): Single<ECredSubmitOTPDto> {
        return authRemote.migrationECred(eCredEmailConfirmationForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun mergeECredAccount(
        accessToken: String,
        eCredMergeAccountForm: ECredMergeAccountForm
    ): Single<ECredMergeSubmitDto> {
        return authRemote.mergeECredAccount(accessToken, eCredMergeAccountForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun mergeECredAccountOTP(
        accessToken: String,
        eCredMergeAccountOTPForm: ECredMergeAccountOTPForm
    ): Single<ECredSubmitOTPDto> {
        return authRemote.mergeECredAccountOTP(accessToken, eCredMergeAccountOTPForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }
}
