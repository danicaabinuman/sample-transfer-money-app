package com.unionbankph.corporate.common.data.source.local.sharedpref

import com.f2prateek.rx.preferences2.RxSharedPreferences
import javax.inject.Inject

class SharedPreferenceUtil
@Inject
constructor(private val rxSharedPreferences: RxSharedPreferences) {

    private val notificationTokenPref = "notificationToken"

    private val launchSharedPref = "isLaunched"

    private val requestPaymentLaunchSharedPref = "isRequestPaymentLaunched"

    private val trustedDevicePref = "trustedDevice"

    private val isReadMCDTerms = "isReadMCDTerms"

    private val isNewUserDetectedSharedPref = "isNewUserDetected"

    private val authSharedPref = "isLoggedIn"

    private val fingerPrintTokenSharedPref = "fingerPrintToken"

    private val totpTokenPref = "totpToken"

    private val fullNameSharedPref = "fullName"

    private val emailSharedPref = "email"

    private val recentUsersSharedPref = "recentUsers"

    private val tutorialIntroductionSharedPref = "tutorialIntroduction"

    private val tutorialAccountSharedPref = "tutorialAccount"

    private val tutorialAccountDetailSharedPref = "tutorialAccountDetail"

    private val tutorialTransactSharedPref = "tutorialTransact"

    private val tutorialApprovalSharedPref = "tutorialApproval"

    private val tutorialApprovalDetailSharedPref = "tutorialApprovalDetail"

    private val tutorialUserSharedPref = "tutorialUser"

    private val tutorialAlertsSharedPref = "tutorialAlerts"

    private val tutorialSettingsSharedPref = "tutorialSettings"

    private val tutorialFundTransferSharedPref = "tutorialFundTransfer"

    private val tutorialBillsPaymentSharedPref = "tutorialBillsPayment"

    private val tutorialCheckDepositSharedPref = "tutorialCheckDeposit"

    private val tutorialChannelSharedPref = "tutorialChannel"

    private val tutorialUBPFormSharedPref = "tutorialUBPForm"

    private val tutorialUBPConfirmationSharedPref = "tutorialUBPConfirmation"

    private val tutorialUBPSummarySharedPref = "tutorialUBPSummary"

    private val tutorialPesoNetFormSharedPref = "tutorialPesoNetForm"

    private val tutorialPesoNetConfirmationSharedPref = "tutorialPesoNetConfirmation"

    private val tutorialPesoNetSummarySharedPref = "tutorialPesoNetSummary"

    private val tutorialPDDTSFormSharedPref = "tutorialPDDTSForm"

    private val tutorialPDDTSConfirmationSharedPref = "tutorialPDDTSConfirmation"

    private val tutorialPDDTSSummarySharedPref = "tutorialPDDTSSummary"

    private val tutorialInstapayFormSharedPref = "tutorialInstapayForm"

    private val tutorialInstapayConfirmationSharedPref = "tutorialInstapayConfirmation"

    private val tutorialInstapaySummarySharedPref = "tutorialInstapaySummary"

    private val tutorialSwiftFormSharedPref = "tutorialSwiftForm"

    private val tutorialSwiftConfirmationSharedPref = "tutorialSwiftConfirmation"

    private val tutorialSwiftSummarySharedPref = "tutorialSwiftSummary"

    private val tutorialBillsPaymentFormSharedPref = "tutorialBillsPaymentForm"

    private val tutorialBillsPaymentConfirmationSharedPref = "tutorialBillsPaymentConfirmation"

    private val tutorialBillsPaymentSummarySharedPref = "tutorialBillsPaymentSummary"

    private val hasPendingEmailChangedSharedPref = "hasPendingEmailChanged"

    private val badgeCountApprovalsSharedPref = "badgeCountApprovals"

    private val badgeCountAlertsSharedPref = "badgeCountAlerts"

    private val firstDepositCheckSharedPref = "firstDepositCheck"

    private val isTableView = "isTableView"

    private val udidSharedPref = "udid"

    private val isTrialMode = "isTrialMode"

    private val trialModeDaysRemainingSharedPref = "trialModeDaysRemainingSharedPref"


    fun notificationTokenPref() = rxSharedPreferences.getString(notificationTokenPref, "")

    fun isLoggedIn() = rxSharedPreferences.getBoolean(authSharedPref, false)

    fun isLaunched() = rxSharedPreferences.getBoolean(launchSharedPref, false)

    fun isRequestPaymentLaunched() = rxSharedPreferences.getBoolean(requestPaymentLaunchSharedPref, false)

    fun isTrustedDevice() = rxSharedPreferences.getBoolean(trustedDevicePref, false)

    fun isReadMCDTerms() = rxSharedPreferences.getBoolean(isReadMCDTerms, false)

    fun isNewUserDetectedSharedPref() =
        rxSharedPreferences.getBoolean(isNewUserDetectedSharedPref, false)

    fun fingerPrintTokenSharedPref() = rxSharedPreferences.getString(fingerPrintTokenSharedPref, "")

    fun totpTokenPref() = rxSharedPreferences.getString(totpTokenPref, "")

    fun fullNameSharedPref() = rxSharedPreferences.getString(fullNameSharedPref, "")

    fun emailSharedPref() = rxSharedPreferences.getString(emailSharedPref, "")

    fun recentUsersSharedPref() = rxSharedPreferences.getString(recentUsersSharedPref)

    fun badgeCountApprovalsSharedPref() =
        rxSharedPreferences.getInteger(badgeCountApprovalsSharedPref, 0)

    fun badgeCountAlertsSharedPref() = rxSharedPreferences.getInteger(badgeCountAlertsSharedPref, 0)

    fun tutorialIntroductionSharedPref() =
        rxSharedPreferences.getBoolean(tutorialIntroductionSharedPref, true)

    fun tutorialAccountSharedPref() =
        rxSharedPreferences.getBoolean(tutorialAccountSharedPref, true)

    fun tutorialAccountDetailSharedPref() =
        rxSharedPreferences.getBoolean(tutorialAccountDetailSharedPref, true)

    fun tutorialTransactSharedPref() =
        rxSharedPreferences.getBoolean(tutorialTransactSharedPref, true)

    fun tutorialApprovalSharedPref() =
        rxSharedPreferences.getBoolean(tutorialApprovalSharedPref, true)

    fun tutorialApprovalDetailSharedPref() =
        rxSharedPreferences.getBoolean(tutorialApprovalDetailSharedPref, true)

    fun tutorialUserSharedPref() = rxSharedPreferences.getBoolean(tutorialUserSharedPref, true)

    fun tutorialAlertsSharedPref() = rxSharedPreferences.getBoolean(tutorialAlertsSharedPref, true)

    fun tutorialSettingsSharedPref() =
        rxSharedPreferences.getBoolean(tutorialSettingsSharedPref, true)

    fun tutorialFundTransferSharedPref() =
        rxSharedPreferences.getBoolean(tutorialFundTransferSharedPref, true)

    fun tutorialBillsPaymentSharedPref() =
        rxSharedPreferences.getBoolean(tutorialBillsPaymentSharedPref, true)

    fun tutorialCheckDepositSharedPref() =
        rxSharedPreferences.getBoolean(tutorialCheckDepositSharedPref, true)

    fun tutorialChannelSharedPref() =
        rxSharedPreferences.getBoolean(tutorialChannelSharedPref, true)

    fun tutorialUBPFormSharedPref() =
        rxSharedPreferences.getBoolean(tutorialUBPFormSharedPref, true)

    fun tutorialUBPConfirmationSharedPref() =
        rxSharedPreferences.getBoolean(tutorialUBPConfirmationSharedPref, true)

    fun tutorialUBPSummarySharedPref() =
        rxSharedPreferences.getBoolean(tutorialUBPSummarySharedPref, true)

    fun tutorialPesoNetFormSharedPref() =
        rxSharedPreferences.getBoolean(tutorialPesoNetFormSharedPref, true)

    fun tutorialPesoNetConfirmationSharedPref() =
        rxSharedPreferences.getBoolean(tutorialPesoNetConfirmationSharedPref, true)

    fun tutorialPesoNetSummarySharedPref() =
        rxSharedPreferences.getBoolean(tutorialPesoNetSummarySharedPref, true)

    fun tutorialPDDTSFormSharedPref() =
        rxSharedPreferences.getBoolean(tutorialPDDTSFormSharedPref, true)

    fun tutorialPDDTSConfirmationSharedPref() =
        rxSharedPreferences.getBoolean(tutorialPDDTSConfirmationSharedPref, true)

    fun tutorialPDDTSSummarySharedPref() =
        rxSharedPreferences.getBoolean(tutorialPDDTSSummarySharedPref, true)

    fun tutorialInstapayFormSharedPref() =
        rxSharedPreferences.getBoolean(tutorialInstapayFormSharedPref, true)

    fun tutorialInstapayConfirmationSharedPref() =
        rxSharedPreferences.getBoolean(tutorialInstapayConfirmationSharedPref, true)

    fun tutorialInstapaySummarySharedPref() =
        rxSharedPreferences.getBoolean(tutorialInstapaySummarySharedPref, true)

    fun tutorialSwiftFormSharedPref() =
        rxSharedPreferences.getBoolean(tutorialSwiftFormSharedPref, true)

    fun tutorialSwiftConfirmationSharedPref() =
        rxSharedPreferences.getBoolean(tutorialSwiftConfirmationSharedPref, true)

    fun tutorialSwiftSummarySharedPref() =
        rxSharedPreferences.getBoolean(tutorialSwiftSummarySharedPref, true)

    fun tutorialBillsPaymentFormSharedPref() =
        rxSharedPreferences.getBoolean(tutorialBillsPaymentFormSharedPref, true)

    fun tutorialBillsPaymentConfirmationSharedPref() =
        rxSharedPreferences.getBoolean(tutorialBillsPaymentConfirmationSharedPref, true)

    fun tutorialBillsPaymentSummarySharedPref() =
        rxSharedPreferences.getBoolean(tutorialBillsPaymentSummarySharedPref, true)

    fun hasPendingEmailChangedSharedPref() =
        rxSharedPreferences.getBoolean(hasPendingEmailChangedSharedPref, false)

    fun firstDepositCheckSharedPref() =
        rxSharedPreferences.getBoolean(firstDepositCheckSharedPref, true)

    fun isTableView() = rxSharedPreferences.getBoolean(isTableView, false)

    fun udidSharedPref() = rxSharedPreferences.getString(udidSharedPref, "")

    fun isTrialMode() = rxSharedPreferences.getBoolean(isTrialMode, false)

    fun trialModeDaysRemainingSharedPref() = rxSharedPreferences.getString(trialModeDaysRemainingSharedPref,"")
}
