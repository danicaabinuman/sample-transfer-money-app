package com.unionbankph.corporate.app.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.unionbankph.corporate.account.presentation.account_detail.AccountDetailViewModel
import com.unionbankph.corporate.account.presentation.account_history.AccountTransactionHistoryViewModel
import com.unionbankph.corporate.account.presentation.account_history_detail.AccountTransactionHistoryDetailsViewModel
import com.unionbankph.corporate.account.presentation.account_list.AccountViewModel
import com.unionbankph.corporate.account.presentation.account_selection.AccountSelectionViewModel
import com.unionbankph.corporate.account.presentation.own_account.OwnAccountViewModel
import com.unionbankph.corporate.account.presentation.source_account.SourceAccountViewModel
import com.unionbankph.corporate.app.dashboard.DashboardViewModel
import com.unionbankph.corporate.app.di.ViewModelFactory
import com.unionbankph.corporate.app.di.ViewModelKey
import com.unionbankph.corporate.approval.presentation.approval_activity_log.ActivityLogViewModel
import com.unionbankph.corporate.approval.presentation.approval_batch_list.BatchTransferViewModel
import com.unionbankph.corporate.approval.presentation.approval_cwt.BatchCWTViewModel
import com.unionbankph.corporate.approval.presentation.approval_detail.ApprovalDetailViewModel
import com.unionbankph.corporate.approval.presentation.approval_done.ApprovalDoneViewModel
import com.unionbankph.corporate.approval.presentation.approval_ongoing.ApprovalOngoingViewModel
import com.unionbankph.corporate.auth.presentation.login.LoginViewModel
import com.unionbankph.corporate.auth.presentation.migration.MigrationViewModel
import com.unionbankph.corporate.auth.presentation.otp.OTPViewModel
import com.unionbankph.corporate.auth.presentation.password_recovery.PasswordRecoveryViewModel
import com.unionbankph.corporate.auth.presentation.policy.PrivacyPolicyViewModel
import com.unionbankph.corporate.bills_payment.presentation.biller.BillerMainViewModel
import com.unionbankph.corporate.bills_payment.presentation.biller.biller_all.AllBillerViewModel
import com.unionbankph.corporate.bills_payment.presentation.biller.frequent_biller.FrequentBillerViewModel
import com.unionbankph.corporate.bills_payment.presentation.bills_payment_confirmation.BillsPaymentConfirmationViewModel
import com.unionbankph.corporate.bills_payment.presentation.bills_payment_form.BillsPaymentFormViewModel
import com.unionbankph.corporate.bills_payment.presentation.bills_payment_summary.BillsPaymentSummaryViewModel
import com.unionbankph.corporate.bills_payment.presentation.frequent_biller_detail.ManageFrequentBillerDetailViewModel
import com.unionbankph.corporate.bills_payment.presentation.frequent_biller_form.ManageFrequentBillerFormViewModel
import com.unionbankph.corporate.bills_payment.presentation.organization_payment.OrganizationPaymentViewModel
import com.unionbankph.corporate.branch.presentation.branch.BranchViewModel
import com.unionbankph.corporate.branch.presentation.channel.BranchVisitChannelViewModel
import com.unionbankph.corporate.branch.presentation.confirmation.BranchVisitConfirmationViewModel
import com.unionbankph.corporate.branch.presentation.detail.BranchVisitDetailViewModel
import com.unionbankph.corporate.branch.presentation.form.BranchVisitFormViewModel
import com.unionbankph.corporate.branch.presentation.list.BranchVisitViewModel
import com.unionbankph.corporate.branch.presentation.summary.BranchVisitSummaryViewModel
import com.unionbankph.corporate.branch.presentation.transaction.BranchVisitTransactionFormViewModel
import com.unionbankph.corporate.branch.presentation.transactiondetail.BranchTransactionDetailViewModel
import com.unionbankph.corporate.branch.presentation.transactionlist.BranchTransactionViewModel
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.corporate.presentation.channel.ChannelViewModel
import com.unionbankph.corporate.corporate.presentation.organization.OrganizationViewModel
import com.unionbankph.corporate.dao.presentation.DaoViewModel
import com.unionbankph.corporate.dao.presentation.business_registration_papers.DaoBusinessRegistrationPapersViewModel
import com.unionbankph.corporate.dao.presentation.checking_account.DaoCheckingAccountTypeViewModel
import com.unionbankph.corporate.dao.presentation.company_info_1.DaoCompanyInformationStepOneViewModel
import com.unionbankph.corporate.dao.presentation.company_info_2.DaoCompanyInformationStepTwoViewModel
import com.unionbankph.corporate.dao.presentation.confirmation.DaoConfirmationViewModel
import com.unionbankph.corporate.dao.presentation.home_default.DaoDefaultViewModel
import com.unionbankph.corporate.dao.presentation.jumio_verification.DaoJumioVerificationViewModel
import com.unionbankph.corporate.dao.presentation.online_banking_products.DaoOnlineBankingProductsViewModel
import com.unionbankph.corporate.dao.presentation.personal_info_1.DaoPersonalInformationStepOneViewModel
import com.unionbankph.corporate.dao.presentation.personal_info_2.DaoPersonalInformationStepTwoViewModel
import com.unionbankph.corporate.dao.presentation.personal_info_3.DaoPersonalInformationStepThreeViewModel
import com.unionbankph.corporate.dao.presentation.personal_info_4.DaoPersonalInformationStepFourViewModel
import com.unionbankph.corporate.dao.presentation.preferred_branch.DaoPreferredBranchViewModel
import com.unionbankph.corporate.dao.presentation.reminders.DaoRemindersViewModel
import com.unionbankph.corporate.dao.presentation.result.DaoResultViewModel
import com.unionbankph.corporate.dao.presentation.selection.DaoSelectionViewModel
import com.unionbankph.corporate.dao.presentation.signature.DaoSignatureViewModel
import com.unionbankph.corporate.dao.presentation.signature_preview.DaoSignaturePreviewViewModel
import com.unionbankph.corporate.dao.presentation.type_of_business.DaoTypeOfBusinessViewModel
import com.unionbankph.corporate.dao.presentation.welcome.DaoWelcomeViewModel
import com.unionbankph.corporate.dao.presentation.welcome_enter.DaoWelcomeEnterViewModel
import com.unionbankph.corporate.ebilling.presentation.confirmation.EBillingConfirmationViewModel
import com.unionbankph.corporate.ebilling.presentation.form.EBillingFormViewModel
import com.unionbankph.corporate.ebilling.presentation.generate.EBillingGenerateViewModel
import com.unionbankph.corporate.fund_transfer.presentation.bank.BankViewModel
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_detail.ManageBeneficiaryDetailViewModel
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_form.ManageBeneficiaryFormViewModel
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_list.ManageBeneficiaryViewModel
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_selection.BeneficiaryViewModel
import com.unionbankph.corporate.fund_transfer.presentation.instapay.InstaPayViewModel
import com.unionbankph.corporate.fund_transfer.presentation.organization_transfer.OrganizationTransferViewModel
import com.unionbankph.corporate.fund_transfer.presentation.pddts.PDDTSViewModel
import com.unionbankph.corporate.fund_transfer.presentation.pesonet.PesoNetViewModel
import com.unionbankph.corporate.fund_transfer.presentation.proposed_transfer.ProposedTransferDateViewModel
import com.unionbankph.corporate.fund_transfer.presentation.scheduled.ManageScheduledTransferViewModel
import com.unionbankph.corporate.fund_transfer.presentation.swift.SwiftViewModel
import com.unionbankph.corporate.fund_transfer.presentation.swift_bank.SwiftBankViewModel
import com.unionbankph.corporate.fund_transfer.presentation.ubp.UBPViewModel
import com.unionbankph.corporate.general.presentation.transaction_filter.TransactionFilterViewModel
import com.unionbankph.corporate.payment_link.presentation.payment_link_details.LinkDetailsViewModel
import com.unionbankph.corporate.mcd.presentation.camera.CheckDepositCameraViewModel
import com.unionbankph.corporate.mcd.presentation.confirmation.CheckDepositConfirmationViewModel
import com.unionbankph.corporate.mcd.presentation.detail.CheckDepositDetailViewModel
import com.unionbankph.corporate.mcd.presentation.filter.CheckDepositFilterViewModel
import com.unionbankph.corporate.mcd.presentation.form.CheckDepositFormViewModel
import com.unionbankph.corporate.mcd.presentation.list.CheckDepositViewModel
import com.unionbankph.corporate.mcd.presentation.log.CheckDepositActivityLogViewModel
import com.unionbankph.corporate.mcd.presentation.onboarding.CheckDepositOnBoardingViewModel
import com.unionbankph.corporate.mcd.presentation.preview.CheckDepositPreviewViewModel
import com.unionbankph.corporate.mcd.presentation.summary.CheckDepositSummaryViewModel
import com.unionbankph.corporate.notification.presentation.notification_log.NotificationLogViewModel
import com.unionbankph.corporate.payment_link.presentation.activity_logs.ActivityLogsViewModel
import com.unionbankph.corporate.payment_link.presentation.billing_details.BillingDetailsViewModel
import com.unionbankph.corporate.payment_link.presentation.onboarding.RequestPaymentSplashViewModel
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_channels.PaymentLinkChannelsViewModel
import com.unionbankph.corporate.payment_link.presentation.payment_link_list.PaymentLinkListViewModel
import com.unionbankph.corporate.payment_link.presentation.request_payment.RequestForPaymentViewModel
import com.unionbankph.corporate.payment_link.presentation.request_payment.fee_calculator.FeeCalculatorViewModel
import com.unionbankph.corporate.payment_link.presentation.setup_business_information.BusinessInformationViewModel
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.SetupPaymentLinkViewModel
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.CardAcceptanceOptionViewModel
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account.NominateSettlementViewModel
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_success.SetupPaymentLinkSuccessfulViewModel
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.terms_of_service.TermsOfServiceViewModel
import com.unionbankph.corporate.settings.presentation.SettingsViewModel
import com.unionbankph.corporate.settings.presentation.country.CountryViewModel
import com.unionbankph.corporate.settings.presentation.display.SettingsDisplayViewModel
import com.unionbankph.corporate.settings.presentation.fingerprint.BiometricViewModel
import com.unionbankph.corporate.settings.presentation.general.GeneralSettingsViewModel
import com.unionbankph.corporate.settings.presentation.learn_more.LearnMoreViewModel
import com.unionbankph.corporate.settings.presentation.notification.NotificationViewModel
import com.unionbankph.corporate.settings.presentation.security.device.ManageDevicesViewModel
import com.unionbankph.corporate.settings.presentation.selector.SelectorViewModel
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorViewModel
import com.unionbankph.corporate.settings.presentation.splash.SplashStartedScreenViewModel
import com.unionbankph.corporate.settings.presentation.update_password.UpdatePasswordViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    abstract fun viewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(SplashStartedScreenViewModel::class)
    abstract fun splashStartedScreenViewModel(viewModel: SplashStartedScreenViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GeneralViewModel::class)
    abstract fun generalViewModel(viewModel: GeneralViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TutorialViewModel::class)
    abstract fun tutorialViewModel(viewModel: TutorialViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun loginViewModel(viewModel: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OTPViewModel::class)
    abstract fun oTPViewModel(viewModel: OTPViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PasswordRecoveryViewModel::class)
    abstract fun passwordRecoveryViewModel(viewModel: PasswordRecoveryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UpdatePasswordViewModel::class)
    abstract fun updatePasswordViewModel(viewModel: UpdatePasswordViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DashboardViewModel::class)
    abstract fun dashboardViewModel(viewModel: DashboardViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AccountViewModel::class)
    abstract fun accountViewModel(viewModel: AccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OwnAccountViewModel::class)
    abstract fun ownAccountViewModel(viewModel: OwnAccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AccountSelectionViewModel::class)
    abstract fun accountSelectionViewModel(viewModel: AccountSelectionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AccountDetailViewModel::class)
    abstract fun accountDetailViewModel(viewModel: AccountDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AccountTransactionHistoryViewModel::class)
    abstract fun accountTransactionHistoryViewModel(
        viewModel: AccountTransactionHistoryViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AccountTransactionHistoryDetailsViewModel::class)
    abstract fun accountTransactionDetailsViewModel(
        viewModel: AccountTransactionHistoryDetailsViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SourceAccountViewModel::class)
    abstract fun sourceAccountViewModel(viewModel: SourceAccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OrganizationViewModel::class)
    abstract fun organizationViewModel(viewModel: OrganizationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ApprovalOngoingViewModel::class)
    abstract fun approvalOngoingViewModel(viewModel: ApprovalOngoingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ApprovalDoneViewModel::class)
    abstract fun approvalDoneViewModel(viewModel: ApprovalDoneViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun settingsViewModel(viewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ApprovalDetailViewModel::class)
    abstract fun approvalDetailViewModel(viewModel: ApprovalDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ActivityLogViewModel::class)
    abstract fun activityLogViewModel(viewModel: ActivityLogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BiometricViewModel::class)
    abstract fun biometricViewModel(viewModel: BiometricViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GeneralSettingsViewModel::class)
    abstract fun generalSettingsViewModel(viewModel: GeneralSettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PrivacyPolicyViewModel::class)
    abstract fun privacyPolicyViewModel(viewModel: PrivacyPolicyViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChannelViewModel::class)
    abstract fun channelViewModel(viewModel: ChannelViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UBPViewModel::class)
    abstract fun uBPViewModel(viewModel: UBPViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PesoNetViewModel::class)
    abstract fun pesoNetViewModel(viewModel: PesoNetViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BankViewModel::class)
    abstract fun bankViewModel(viewModel: BankViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BeneficiaryViewModel::class)
    abstract fun beneficiaryViewModel(viewModel: BeneficiaryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PDDTSViewModel::class)
    abstract fun pDDTSViewModel(viewModel: PDDTSViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OrganizationTransferViewModel::class)
    abstract fun organizationTransferViewModel(viewModel: OrganizationTransferViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OrganizationPaymentViewModel::class)
    abstract fun organizationPaymentViewModel(viewModel: OrganizationPaymentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TransactionFilterViewModel::class)
    abstract fun transactionFilterViewModel(viewModel: TransactionFilterViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SelectorViewModel::class)
    abstract fun selectorViewModel(viewModel: SelectorViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProposedTransferDateViewModel::class)
    abstract fun proposedTransferDateViewModel(viewModel: ProposedTransferDateViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SwiftViewModel::class)
    abstract fun swiftViewModel(viewModel: SwiftViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AllBillerViewModel::class)
    abstract fun allBillerViewModel(viewModel: AllBillerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BillerMainViewModel::class)
    abstract fun billerMainViewModel(viewModel: BillerMainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FrequentBillerViewModel::class)
    abstract fun frequentBillerViewModel(viewModel: FrequentBillerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BillsPaymentFormViewModel::class)
    abstract fun billsPaymentFormViewModel(viewModel: BillsPaymentFormViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BillsPaymentConfirmationViewModel::class)
    abstract fun billsPaymentConfirmationViewModel(viewModel: BillsPaymentConfirmationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BillsPaymentSummaryViewModel::class)
    abstract fun billsPaymentSummaryViewModel(viewModel: BillsPaymentSummaryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BatchTransferViewModel::class)
    abstract fun batchTransferViewModel(viewModel: BatchTransferViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(InstaPayViewModel::class)
    abstract fun instaPayViewModel(viewModel: InstaPayViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotificationViewModel::class)
    abstract fun notificationViewModel(viewModel: NotificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ManageBeneficiaryViewModel::class)
    abstract fun manageBeneficiaryViewModel(viewModel: ManageBeneficiaryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ManageBeneficiaryFormViewModel::class)
    abstract fun manageBeneficiaryFormViewModel(viewModel: ManageBeneficiaryFormViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ManageBeneficiaryDetailViewModel::class)
    abstract fun manageBeneficiaryDetailViewModel(viewModel: ManageBeneficiaryDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ManageScheduledTransferViewModel::class)
    abstract fun manageScheduledTransferViewModel(viewModel: ManageScheduledTransferViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ManageFrequentBillerFormViewModel::class)
    abstract fun manageFrequentBillerFormViewModel(viewModel: ManageFrequentBillerFormViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ManageFrequentBillerDetailViewModel::class)
    abstract fun manageFrequentBillerDetailViewModel(viewModel: ManageFrequentBillerDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CountryViewModel::class)
    abstract fun countryViewModel(viewModel: CountryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MigrationViewModel::class)
    abstract fun migrationViewModel(viewModel: MigrationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ManageDevicesViewModel::class)
    abstract fun manageDevicesViewModel(viewModel: ManageDevicesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotificationLogViewModel::class)
    abstract fun notificationLogViewModel(viewModel: NotificationLogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BatchCWTViewModel::class)
    abstract fun batchCWTViewModel(viewModel: BatchCWTViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CheckDepositViewModel::class)
    abstract fun checkDepositViewModel(viewModel: CheckDepositViewModel): ViewModel

    // Check Deposit
    @Binds
    @IntoMap
    @ViewModelKey(CheckDepositFormViewModel::class)
    abstract fun checkDepositFormViewModel(viewModel: CheckDepositFormViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CheckDepositDetailViewModel::class)
    abstract fun checkDepositDetailViewModel(viewModel: CheckDepositDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CheckDepositOnBoardingViewModel::class)
    abstract fun checkDepositOnBoardingViewModel(viewModel: CheckDepositOnBoardingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CheckDepositConfirmationViewModel::class)
    abstract fun checkDepositConfirmationViewModel(viewModel: CheckDepositConfirmationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CheckDepositSummaryViewModel::class)
    abstract fun checkDepositSummaryViewModel(viewModel: CheckDepositSummaryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CheckDepositCameraViewModel::class)
    abstract fun checkDepositCameraViewModel(viewModelCheckDeposit: CheckDepositCameraViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CheckDepositPreviewViewModel::class)
    abstract fun checkDepositPreviewViewModel(viewModel: CheckDepositPreviewViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CheckDepositActivityLogViewModel::class)
    abstract fun checkDepositActivityLogViewModel(viewModel: CheckDepositActivityLogViewModel): ViewModel

    // Branch Visit
    @Binds
    @IntoMap
    @ViewModelKey(BranchVisitViewModel::class)
    abstract fun branchVisitViewModel(viewModel: BranchVisitViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BranchVisitChannelViewModel::class)
    abstract fun branchVisitChannelViewModel(viewModel: BranchVisitChannelViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BranchVisitFormViewModel::class)
    abstract fun branchVisitFormViewModel(viewModel: BranchVisitFormViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BranchViewModel::class)
    abstract fun branchViewModel(viewModel: BranchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BranchVisitTransactionFormViewModel::class)
    abstract fun branchVisitTransactionViewModel(viewModel: BranchVisitTransactionFormViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BranchTransactionViewModel::class)
    abstract fun branchTransactionViewModel(viewModel: BranchTransactionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BranchVisitConfirmationViewModel::class)
    abstract fun branchVisitConfirmationViewModel(viewModel: BranchVisitConfirmationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BranchVisitSummaryViewModel::class)
    abstract fun branchVisitSummaryViewModel(viewModel: BranchVisitSummaryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BranchVisitDetailViewModel::class)
    abstract fun branchVisitDetailViewModel(viewModel: BranchVisitDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BranchTransactionDetailViewModel::class)
    abstract fun branchTransactionDetailViewModel(viewModel: BranchTransactionDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsDisplayViewModel::class)
    abstract fun settingsDisplayViewModel(viewModel: SettingsDisplayViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoViewModel::class)
    abstract fun daoViewModel(viewModel: DaoViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SingleSelectorViewModel::class)
    abstract fun singleSelectorViewModel(viewModel: SingleSelectorViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoWelcomeViewModel::class)
    abstract fun daoWelcomeViewModel(viewModel: DaoWelcomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoTypeOfBusinessViewModel::class)
    abstract fun daoTypeOfBusinessViewModel(viewModel: DaoTypeOfBusinessViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoCompanyInformationStepOneViewModel::class)
    abstract fun daoCompanyInformationStepOneViewModel(
        viewModel: DaoCompanyInformationStepOneViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoCompanyInformationStepTwoViewModel::class)
    abstract fun daoCompanyInformationStepTwoViewModel(
        viewModel: DaoCompanyInformationStepTwoViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoBusinessRegistrationPapersViewModel::class)
    abstract fun daoBusinessRegistrationPapersViewModel(
        viewModel: DaoBusinessRegistrationPapersViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoCheckingAccountTypeViewModel::class)
    abstract fun daoCheckingAccountTypeViewModel(viewModel: DaoCheckingAccountTypeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoRemindersViewModel::class)
    abstract fun daoRemindersViewModel(viewModel: DaoRemindersViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoPreferredBranchViewModel::class)
    abstract fun daoPreferredBranchViewModel(viewModel: DaoPreferredBranchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoPersonalInformationStepOneViewModel::class)
    abstract fun daoPersonalInformationStepOneViewModel(
        viewModel: DaoPersonalInformationStepOneViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoPersonalInformationStepTwoViewModel::class)
    abstract fun daoPersonalInformationStepTwoViewModel(
        viewModel: DaoPersonalInformationStepTwoViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoPersonalInformationStepThreeViewModel::class)
    abstract fun daoPersonalInformationStepThreeModel(
        viewModel: DaoPersonalInformationStepThreeViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoPersonalInformationStepFourViewModel::class)
    abstract fun daoPersonalInformationStepFourViewModel(
        viewModel: DaoPersonalInformationStepFourViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoOnlineBankingProductsViewModel::class)
    abstract fun daoOnlineBankingProductsViewModel(viewModel: DaoOnlineBankingProductsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoJumioVerificationViewModel::class)
    abstract fun daoJumioVerificationViewModel(viewModel: DaoJumioVerificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoWelcomeEnterViewModel::class)
    abstract fun daoWelcomeEnterViewModel(viewModel: DaoWelcomeEnterViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoSignatureViewModel::class)
    abstract fun daoSignatureViewModel(viewModel: DaoSignatureViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoSignaturePreviewViewModel::class)
    abstract fun daoSignaturePreviewViewModel(viewModel: DaoSignaturePreviewViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoConfirmationViewModel::class)
    abstract fun daoConfirmationViewModel(viewModel: DaoConfirmationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoResultViewModel::class)
    abstract fun daoResultViewModel(viewModel: DaoResultViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CheckDepositFilterViewModel::class)
    abstract fun checkDepositFilterViewModel(viewModel: CheckDepositFilterViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoDefaultViewModel::class)
    abstract fun daoDefaultViewModel(viewModel: DaoDefaultViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SwiftBankViewModel::class)
    abstract fun swiftBankViewModel(viewModel: SwiftBankViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EBillingFormViewModel::class)
    abstract fun eBillingFormViewModel(viewModel: EBillingFormViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EBillingConfirmationViewModel::class)
    abstract fun eBillingConfirmationViewModel(viewModel: EBillingConfirmationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EBillingGenerateViewModel::class)
    abstract fun eBillingGenerateViewModel(viewModel: EBillingGenerateViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DaoSelectionViewModel::class)
    abstract fun daoSelectionViewModel(viewModel: DaoSelectionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LearnMoreViewModel::class)
    abstract fun learnMoreViewModel(viewModel: LearnMoreViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LinkDetailsViewModel::class)
    abstract fun linkDetailsViewModel(
        viewModel: LinkDetailsViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RequestForPaymentViewModel::class)
    abstract fun requestPaymentViewModel(
        viewModel: RequestForPaymentViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SetupPaymentLinkViewModel::class)
    abstract fun setupPaymentLinkViewModel(
        viewModel: SetupPaymentLinkViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TermsOfServiceViewModel::class)
    abstract fun termsOfServiceViewModel(
            viewModel: TermsOfServiceViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NominateSettlementViewModel::class)
    abstract fun nominateSettlementViewModel(
            viewModel: NominateSettlementViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BillingDetailsViewModel::class)
    abstract fun billingDetailsViewModel(
        viewModel: BillingDetailsViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ActivityLogsViewModel::class)
    abstract fun activityLogsViewModel(
        viewModel: ActivityLogsViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PaymentLinkListViewModel::class)
    abstract fun paymentLinkListViewModel(
            viewModel: PaymentLinkListViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RequestPaymentSplashViewModel::class)
    abstract fun requestPaymentSplashViewModel(
        viewModel: RequestPaymentSplashViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SetupPaymentLinkSuccessfulViewModel::class)
    abstract fun setupPaymentLinkSuccessfulViewModel(
        viewModel: SetupPaymentLinkSuccessfulViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FeeCalculatorViewModel::class)
    abstract fun feeCalculatorViewModel(
        viewModel: FeeCalculatorViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BusinessInformationViewModel::class)
    abstract fun businessInformationViewModel(
        viewModel: BusinessInformationViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PaymentLinkChannelsViewModel::class)
    abstract fun paymentLinkChannelsViewModel(
        viewModel: PaymentLinkChannelsViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CardAcceptanceOptionViewModel::class)
    abstract fun cardAcceptanceOptionViewModel(
        viewModel: CardAcceptanceOptionViewModel
    ): ViewModel
}
