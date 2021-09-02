package com.unionbankph.corporate.app.di.module

import com.unionbankph.corporate.account.presentation.account_detail.AccountDetailActivity
import com.unionbankph.corporate.account.presentation.account_history.AccountTransactionHistoryActivity
import com.unionbankph.corporate.account.presentation.account_history_detail.AccountTransactionHistoryDetailsActivity
import com.unionbankph.corporate.account.presentation.account_selection.AccountSelectionActivity
import com.unionbankph.corporate.account.presentation.source_account.SourceAccountActivity
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.app.di.scope.PerActivity
import com.unionbankph.corporate.approval.presentation.approval_activity_log.ActivityLogActivity
import com.unionbankph.corporate.approval.presentation.approval_batch_detail.BatchDetailActivity
import com.unionbankph.corporate.approval.presentation.approval_batch_list.BatchTransferActivity
import com.unionbankph.corporate.approval.presentation.approval_cwt.BatchCWTActivity
import com.unionbankph.corporate.approval.presentation.approval_cwt_detail.BatchCWTDetailActivity
import com.unionbankph.corporate.approval.presentation.approval_detail.ApprovalDetailActivity
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.auth.presentation.migration.MigrationMainActivity
import com.unionbankph.corporate.auth.presentation.migration.migration_form.MigrationFormActivity
import com.unionbankph.corporate.auth.presentation.migration.migration_merge.MigrationMergeActivity
import com.unionbankph.corporate.auth.presentation.migration.migration_selection.MigrationSelectionActivity
import com.unionbankph.corporate.auth.presentation.migration.nominate_migration_success.NominateMigrationSuccessActivity
import com.unionbankph.corporate.auth.presentation.otp.OTPActivity
import com.unionbankph.corporate.auth.presentation.password_recovery.PasswordRecoveryActivity
import com.unionbankph.corporate.auth.presentation.policy.PrivacyPolicyActivity
import com.unionbankph.corporate.bills_payment.presentation.biller.BillerMainActivity
import com.unionbankph.corporate.bills_payment.presentation.bills_payment_confirmation.BillsPaymentConfirmationActivity
import com.unionbankph.corporate.bills_payment.presentation.bills_payment_form.BillsPaymentFormActivity
import com.unionbankph.corporate.bills_payment.presentation.bills_payment_summary.BillsPaymentSummaryActivity
import com.unionbankph.corporate.bills_payment.presentation.frequent_biller_detail.ManageFrequentBillerDetailActivity
import com.unionbankph.corporate.bills_payment.presentation.frequent_biller_form.ManageFrequentBillerFormActivity
import com.unionbankph.corporate.bills_payment.presentation.frequent_biller_list.ManageFrequentBillerActivity
import com.unionbankph.corporate.bills_payment.presentation.organization_payment.OrganizationPaymentActivity
import com.unionbankph.corporate.branch.presentation.branch.BranchActivity
import com.unionbankph.corporate.branch.presentation.channel.BranchVisitChannelActivity
import com.unionbankph.corporate.branch.presentation.confirmation.BranchVisitConfirmationActivity
import com.unionbankph.corporate.branch.presentation.detail.BranchVisitDetailActivity
import com.unionbankph.corporate.branch.presentation.form.BranchVisitFormActivity
import com.unionbankph.corporate.branch.presentation.list.BranchVisitActivity
import com.unionbankph.corporate.branch.presentation.summary.BranchVisitSummaryActivity
import com.unionbankph.corporate.branch.presentation.transaction.BranchVisitTransactionFormActivity
import com.unionbankph.corporate.branch.presentation.transactiondetail.BranchTransactionDetailActivity
import com.unionbankph.corporate.branch.presentation.transactionlist.BranchTransactionActivity
import com.unionbankph.corporate.corporate.presentation.channel.ChannelActivity
import com.unionbankph.corporate.corporate.presentation.organization.OrganizationActivity
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.dao.presentation.DaoBasicInfoReminderActivity
import com.unionbankph.corporate.dao.presentation.selection.DaoSelectionActivity
import com.unionbankph.corporate.dao.presentation.signature_preview.DaoSignaturePreviewActivity
import com.unionbankph.corporate.ebilling.presentation.confirmation.EBillingConfirmationActivity
import com.unionbankph.corporate.ebilling.presentation.form.EBillingFormActivity
import com.unionbankph.corporate.ebilling.presentation.generate.EBillingGenerateActivity
import com.unionbankph.corporate.fund_transfer.presentation.bank.BankActivity
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_detail.ManageBeneficiaryDetailActivity
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_form.ManageBeneficiaryFormActivity
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_list.ManageBeneficiaryActivity
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_selection.BeneficiaryActivity
import com.unionbankph.corporate.fund_transfer.presentation.instapay.InstaPayConfirmationActivity
import com.unionbankph.corporate.fund_transfer.presentation.instapay.InstaPayFormActivity
import com.unionbankph.corporate.fund_transfer.presentation.instapay.InstaPaySummaryActivity
import com.unionbankph.corporate.fund_transfer.presentation.organization_transfer.OrganizationTransferActivity
import com.unionbankph.corporate.fund_transfer.presentation.pddts.PDDTSConfirmationActivity
import com.unionbankph.corporate.fund_transfer.presentation.pddts.PDDTSFormActivity
import com.unionbankph.corporate.fund_transfer.presentation.pddts.PDDTSSummaryActivity
import com.unionbankph.corporate.fund_transfer.presentation.pesonet.PesoNetConfirmationActivity
import com.unionbankph.corporate.fund_transfer.presentation.pesonet.PesoNetFormActivity
import com.unionbankph.corporate.fund_transfer.presentation.pesonet.PesoNetSummaryActivity
import com.unionbankph.corporate.fund_transfer.presentation.proposed_transfer.ProposedTransferDateActivity
import com.unionbankph.corporate.fund_transfer.presentation.scheduled.ManageScheduledTransferActivity
import com.unionbankph.corporate.fund_transfer.presentation.swift.SwiftConfirmationActivity
import com.unionbankph.corporate.fund_transfer.presentation.swift.SwiftFormActivity
import com.unionbankph.corporate.fund_transfer.presentation.swift.SwiftSummaryActivity
import com.unionbankph.corporate.fund_transfer.presentation.swift_bank.SwiftBankActivity
import com.unionbankph.corporate.fund_transfer.presentation.ubp.UBPConfirmationActivity
import com.unionbankph.corporate.fund_transfer.presentation.ubp.UBPFormActivity
import com.unionbankph.corporate.fund_transfer.presentation.ubp.UBPSummaryActivity
import com.unionbankph.corporate.general.presentation.link.DeepLinkLandingActivity
import com.unionbankph.corporate.general.presentation.result.ResultLandingPageActivity
import com.unionbankph.corporate.general.presentation.result.ResultLandingWithoutAuthActivity
import com.unionbankph.corporate.general.presentation.transaction_filter.TransactionFilterActivity
import com.unionbankph.corporate.payment_link.presentation.payment_link_details.LinkDetailsActivity
import com.unionbankph.corporate.mcd.presentation.camera.CheckDepositCameraActivity
import com.unionbankph.corporate.mcd.presentation.confirmation.CheckDepositConfirmationActivity
import com.unionbankph.corporate.mcd.presentation.detail.CheckDepositDetailActivity
import com.unionbankph.corporate.mcd.presentation.filter.CheckDepositFilterActivity
import com.unionbankph.corporate.mcd.presentation.form.CheckDepositFormActivity
import com.unionbankph.corporate.mcd.presentation.list.CheckDepositActivity
import com.unionbankph.corporate.mcd.presentation.log.CheckDepositActivityLogActivity
import com.unionbankph.corporate.mcd.presentation.onboarding.CheckDepositOnBoardingActivity
import com.unionbankph.corporate.mcd.presentation.preview.CheckDepositPreviewActivity
import com.unionbankph.corporate.mcd.presentation.summary.CheckDepositSummaryActivity
import com.unionbankph.corporate.payment_link.presentation.activity_logs.ActivityLogsActivity
import com.unionbankph.corporate.payment_link.presentation.billing_details.BillingDetailsActivity
import com.unionbankph.corporate.payment_link.presentation.onboarding.OnboardingUploadPhotosActivity
import com.unionbankph.corporate.payment_link.presentation.onboarding.RequestPaymentSplashActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_channels.PaymentLinkChannelsActivity
import com.unionbankph.corporate.payment_link.presentation.request_payment.RequestForPaymentActivity
import com.unionbankph.corporate.payment_link.presentation.request_payment.fee_calculator.FeeCalculatorActivity
import com.unionbankph.corporate.payment_link.presentation.setup_business_information.BusinessInformationActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.SetupPaymentLinkActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.CardAcceptanceOptionActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.NotNowCardPaymentsActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.YesAcceptCardPaymentsActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.upload_documents.CardAcceptanceUploadDocumentsActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account.NominateSettlementActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_success.SetupPaymentLinkSuccessfulActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.terms_of_service.TermsOfServiceActivity
import com.unionbankph.corporate.settings.presentation.country.CountryActivity
import com.unionbankph.corporate.settings.presentation.email.UpdateEmailActivity
import com.unionbankph.corporate.settings.presentation.fingerprint.BiometricActivity
import com.unionbankph.corporate.settings.presentation.learn_more.LearnMoreActivity
import com.unionbankph.corporate.settings.presentation.mobile.ChangeMobileNumberActivity
import com.unionbankph.corporate.settings.presentation.password.PasswordActivity
import com.unionbankph.corporate.settings.presentation.selector.SelectorActivity
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorActivity
import com.unionbankph.corporate.settings.presentation.splash.SplashFrameActivity
import com.unionbankph.corporate.settings.presentation.splash.SplashFrameOnboardingActivity
import com.unionbankph.corporate.settings.presentation.splash.SplashStartedScreenActivity
import com.unionbankph.corporate.settings.presentation.totp.TOTPActivity
import com.unionbankph.corporate.settings.presentation.update_password.UpdatePasswordActivity
import com.unionbankph.corporate.open_account.presentation.OpenAccountActivity
import com.unionbankph.corporate.open_account.presentation.trial_account.TrialAccountActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindingModule {

    @PerActivity
    @ContributesAndroidInjector
    abstract fun splashStartedScreenActivity(): SplashStartedScreenActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun splashFrameActivity(): SplashFrameActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun loginActivity(): LoginActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun passwordRecoveryActivity(): PasswordRecoveryActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun otpActivity(): OTPActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun updatePasswordActivity(): UpdatePasswordActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun dashboardActivity(): DashboardActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun accountDetailActivity(): AccountDetailActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun accountTransactionHistoryDetailsActivity(): AccountTransactionHistoryDetailsActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun accountTransactionHistoryActivity(): AccountTransactionHistoryActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun passwordActivity(): PasswordActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun changeMobileNumberActivity(): ChangeMobileNumberActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun updateEmailActivity(): UpdateEmailActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun approvalDetailActivity(): ApprovalDetailActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun activityLogActivity(): ActivityLogActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun biometricActivity(): BiometricActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun learnMoreActivity(): LearnMoreActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun privacyPolicyActivity(): PrivacyPolicyActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun channelActivity(): ChannelActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun pesoNetFormActivity(): PesoNetFormActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun uBPFormActivity(): UBPFormActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun proposedTransferDateActivity(): ProposedTransferDateActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun accountSelectionActivity(): AccountSelectionActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun uBPConfirmationActivity(): UBPConfirmationActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun pesoNetConfirmationActivity(): PesoNetConfirmationActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun uBPSummaryActivity(): UBPSummaryActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun pesoNetSummaryActivity(): PesoNetSummaryActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun bankActivity(): BankActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun beneficiaryActivity(): BeneficiaryActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun pDDTSFormActivity(): PDDTSFormActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun pDDTSConfirmationActivity(): PDDTSConfirmationActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun pDDTSSummaryActivity(): PDDTSSummaryActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun organizationTransferActivity(): OrganizationTransferActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun selectorActivity(): SelectorActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun transactionFilterActivity(): TransactionFilterActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun swiftBankActivity(): SwiftBankActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun swiftFormActivity(): SwiftFormActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun swiftConfirmationActivity(): SwiftConfirmationActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun swiftSummaryActivity(): SwiftSummaryActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun billsPaymentFormActivity(): BillsPaymentFormActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun billerMainActivity(): BillerMainActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun billsPaymentConfirmationActivity(): BillsPaymentConfirmationActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun billsPaymentSummaryActivity(): BillsPaymentSummaryActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun organizationPaymentActivity(): OrganizationPaymentActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun batchTransferActivity(): BatchTransferActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun batchDetailActivity(): BatchDetailActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun instaPayFormActivity(): InstaPayFormActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun instaPayConfirmationActivity(): InstaPayConfirmationActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun instaPaySummaryActivity(): InstaPaySummaryActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun resultLandingPageActivity(): ResultLandingPageActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun manageBeneficiaryActivity(): ManageBeneficiaryActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun manageBeneficiaryFormActivity(): ManageBeneficiaryFormActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun sourceAccountActivity(): SourceAccountActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun manageBeneficiaryDetailActivity(): ManageBeneficiaryDetailActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun manageScheduledTransferActivity(): ManageScheduledTransferActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun manageFrequentBillerActivity(): ManageFrequentBillerActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun manageFrequentBillerFormActivity(): ManageFrequentBillerFormActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun manageFrequentBillerDetailActivity(): ManageFrequentBillerDetailActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun countryActivity(): CountryActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun migrationSelectionActivity(): MigrationSelectionActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun migrationFormActivity(): MigrationFormActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun migrationMainActivity(): MigrationMainActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun migrationMergeActivity(): MigrationMergeActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun nominateMigrationSuccessActivity(): NominateMigrationSuccessActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun generateTOTPActivity(): TOTPActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun deepLinkLandingActivity(): DeepLinkLandingActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun resultLandingWithoutAuthActivity(): ResultLandingWithoutAuthActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun organizationActivity(): OrganizationActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun batchCWTActivity(): BatchCWTActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun batchCWTDetailActivity(): BatchCWTDetailActivity

    // Check Deposit
    @PerActivity
    @ContributesAndroidInjector
    abstract fun checkDepositCameraActivity(): CheckDepositCameraActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun checkDepositPreviewActivity(): CheckDepositPreviewActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun checkDepositOnBoardingActivity(): CheckDepositOnBoardingActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun checkDepositFormActivity(): CheckDepositFormActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun checkDepositConfirmationActivity(): CheckDepositConfirmationActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun checkDepositSummaryActivity(): CheckDepositSummaryActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun checkDepositActivity(): CheckDepositActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun checkDepositDetailActivity(): CheckDepositDetailActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun checkDepositActivityLogActivity(): CheckDepositActivityLogActivity

    // Branch Visit
    @PerActivity
    @ContributesAndroidInjector
    abstract fun branchVisitActivity(): BranchVisitActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun branchVisitChannelActivity(): BranchVisitChannelActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun branchVisitFormActivity(): BranchVisitFormActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun branchActivity(): BranchActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun branchVisitTransactionActivity(): BranchVisitTransactionFormActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun branchTransactionActivity(): BranchTransactionActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun branchVisitConfirmationActivity(): BranchVisitConfirmationActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun branchVisitSummaryActivity(): BranchVisitSummaryActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun branchVisitDetailActivity(): BranchVisitDetailActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun branchTransactionDetailActivity(): BranchTransactionDetailActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoActivity(): DaoActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun singleSelectorActivity(): SingleSelectorActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoSignaturePreviewActivity(): DaoSignaturePreviewActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun checkDepositFilterActivity(): CheckDepositFilterActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun eBillingFormActivity(): EBillingFormActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun eBillingConfirmationActivity(): EBillingConfirmationActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun eBillingGenerateActivity(): EBillingGenerateActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoSelectionActivity(): DaoSelectionActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun linkDetailsActivity(): LinkDetailsActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun requestPaymentActivity(): RequestForPaymentActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun setupPaymentLinkActivity(): SetupPaymentLinkActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun termsOfServiceActivity(): TermsOfServiceActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun nominateSettlementActivity(): NominateSettlementActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun billingDetailsActivity(): BillingDetailsActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun activityLogsActivity(): ActivityLogsActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun requestPaymentSplashActivity(): RequestPaymentSplashActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun setupPaymentLinkSuccessfulActivity(): SetupPaymentLinkSuccessfulActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun feeCalculatorActivity(): FeeCalculatorActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun onboardingUploadPhotosActivity(): OnboardingUploadPhotosActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun businessInformationActivity(): BusinessInformationActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun paymentLinkChannelsActivity(): PaymentLinkChannelsActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun cardAcceptanceOptionActivity(): CardAcceptanceOptionActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun splashFrameOnboardingActivity(): SplashFrameOnboardingActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun userCreationActivity(): OpenAccountActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun yesAcceptCardPaymentsActivity(): YesAcceptCardPaymentsActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun cardAcceptanceUploadDocumentsActivity(): CardAcceptanceUploadDocumentsActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun notNowCardPaymentsActivity(): NotNowCardPaymentsActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoBasicInfoReminderActivity(): DaoBasicInfoReminderActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun trialAccountActivity(): TrialAccountActivity


}
