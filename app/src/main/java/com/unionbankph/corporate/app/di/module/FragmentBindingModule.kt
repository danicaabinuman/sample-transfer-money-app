package com.unionbankph.corporate.app.di.module

import com.unionbankph.corporate.account.presentation.account_list.AccountFragment
import com.unionbankph.corporate.account.presentation.own_account.OwnAccountFragment
import com.unionbankph.corporate.account_setup.presentation.address.AsAddressFragment
import com.unionbankph.corporate.account_setup.presentation.business_account_type.AsBusinessAccountTypeFragment
import com.unionbankph.corporate.account_setup.presentation.business_info.AsBusinessInformationFragment
import com.unionbankph.corporate.account_setup.presentation.business_type.AsBusinessTypeFragment
import com.unionbankph.corporate.account_setup.presentation.citizenship.AsCitizenshipFragment
import com.unionbankph.corporate.account_setup.presentation.citizenship.non_filipino.AsNonFilipinoCitizenFragment
import com.unionbankph.corporate.account_setup.presentation.personal_info.AsPersonalInformationFragment
import com.unionbankph.corporate.account_setup.presentation.debit_card_type.AsDebitCardTypeFragment
import com.unionbankph.corporate.account_setup.presentation.reminders.AsRemindersFragment
import com.unionbankph.corporate.account_setup.presentation.terms_of_service.AsTermsOfServiceFragment
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.dialog.FileManagerBottomSheet
import com.unionbankph.corporate.app.common.widget.dialog.NewConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.dialog.SessionTimeOutBottomSheet
import com.unionbankph.corporate.app.dashboard.fragment.DashboardFragment
import com.unionbankph.corporate.app.dashboard.fragment.MegaMenuBottomSheet
import com.unionbankph.corporate.app.di.scope.PerActivity
import com.unionbankph.corporate.approval.presentation.ApprovalFragment
import com.unionbankph.corporate.approval.presentation.approval_done.ApprovalDoneFragment
import com.unionbankph.corporate.approval.presentation.approval_ongoing.ApprovalOngoingFragment
import com.unionbankph.corporate.auth.presentation.login.LoginFragment
import com.unionbankph.corporate.auth.presentation.migration.migration_merge.NominateMergeVerifyAccountFragment
import com.unionbankph.corporate.auth.presentation.migration.nominate_email.NominateEmailFragment
import com.unionbankph.corporate.auth.presentation.migration.nominate_merge.NominateMergeEmailFragment
import com.unionbankph.corporate.auth.presentation.migration.nominate_merge.NominateMergeSuccessFragment
import com.unionbankph.corporate.auth.presentation.migration.nominate_merge_verify.NominateMergeVerifyFragment
import com.unionbankph.corporate.auth.presentation.migration.nominate_mobile_number.NominateMobileNumberFragment
import com.unionbankph.corporate.auth.presentation.migration.nominate_password.NominatePasswordFragment
import com.unionbankph.corporate.auth.presentation.migration.nominate_verify.NominateVerifyAccountFragment
import com.unionbankph.corporate.auth.presentation.migration.nominate_verify.NominateVerifyAccountResultFragment
import com.unionbankph.corporate.auth.presentation.migration.nominate_welcome.NominateWelcomeFragment
import com.unionbankph.corporate.auth.presentation.policy.PrivacyPolicyFragment
import com.unionbankph.corporate.auth.presentation.policy.TermsAndConditionsFragment
import com.unionbankph.corporate.auth.presentation.totp.TOTPBottomSheet
import com.unionbankph.corporate.bills_payment.presentation.biller.biller_all.AllBillerFragment
import com.unionbankph.corporate.bills_payment.presentation.biller.frequent_biller.FrequentBillerFragment
import com.unionbankph.corporate.dao.presentation.business_registration_papers.DaoBusinessRegistrationPapersFragment
import com.unionbankph.corporate.dao.presentation.checking_account.DaoCheckingAccountTypeFragment
import com.unionbankph.corporate.dao.presentation.company_info_1.DaoCompanyInformationStepOneFragment
import com.unionbankph.corporate.dao.presentation.company_info_2.DaoCompanyInformationStepTwoFragment
import com.unionbankph.corporate.dao.presentation.confirmation.DaoConfirmationFragment
import com.unionbankph.corporate.dao.presentation.home_default.DaoDefaultFragment
import com.unionbankph.corporate.dao.presentation.jumio_verification.DaoJumioVerificationFragment
import com.unionbankph.corporate.dao.presentation.online_banking_products.DaoOnlineBankingProductsFragment
import com.unionbankph.corporate.dao.presentation.personal_info_1.DaoPersonalInformationStepOneFragment
import com.unionbankph.corporate.dao.presentation.personal_info_2.DaoPersonalInformationStepTwoFragment
import com.unionbankph.corporate.dao.presentation.personal_info_3.DaoPersonalInformationStepThreeFragment
import com.unionbankph.corporate.dao.presentation.personal_info_4.DaoPersonalInformationStepFourFragment
import com.unionbankph.corporate.dao.presentation.preferred_branch.DaoPreferredBranchFragment
import com.unionbankph.corporate.dao.presentation.reminders.DaoRemindersFragment
import com.unionbankph.corporate.dao.presentation.result.DaoResultFragment
import com.unionbankph.corporate.dao.presentation.signature.DaoSignatureFragment
import com.unionbankph.corporate.dao.presentation.type_of_business.DaoTypeOfBusinessFragment
import com.unionbankph.corporate.dao.presentation.welcome.DaoWelcomeFragment
import com.unionbankph.corporate.dao.presentation.welcome_enter.DaoWelcomeEnterFragment
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_selection.BeneficiaryFragment
import com.unionbankph.corporate.fund_transfer.presentation.scheduled.scheduled_transfer_done.ManageScheduledTransferDoneFragment
import com.unionbankph.corporate.fund_transfer.presentation.scheduled.scheduled_transfer_ongoing.ManageScheduledTransferOngoingFragment
import com.unionbankph.corporate.instapay_qr.presentation.instapay_qr_splash.AllowPhoneCamera
import com.unionbankph.corporate.loan.address.AddressFragment
import com.unionbankph.corporate.loan.applyloan.LoansFragment
import com.unionbankph.corporate.loan.business_address.BusinessAddressFragment
import com.unionbankph.corporate.loan.business_information.BusinessInformationFragment
import com.unionbankph.corporate.loan.businesstype.BusinessTypeFragment
import com.unionbankph.corporate.loan.calculator.LoansCalculatorFragment
import com.unionbankph.corporate.loan.citizen.CitizenFragment
import com.unionbankph.corporate.loan.contactinformation.ContactInformationFragment
import com.unionbankph.corporate.loan.financial_information.FinancialInformationFragment
import com.unionbankph.corporate.loan.nonfilipino.NonFilipinoFragment
import com.unionbankph.corporate.loan.personal_information.PersonalInformationFragment
import com.unionbankph.corporate.loan.products.ProductsCameraDataFragment
import com.unionbankph.corporate.loan.products.ProductsCameraFragment
import com.unionbankph.corporate.loan.products.ProductsCameraPreviewFragment
import com.unionbankph.corporate.loan.products.ProductsFragment
import com.unionbankph.corporate.loan.reminders.FewRemindersFragment
import com.unionbankph.corporate.mcd.presentation.onboarding.CheckDepositOnBoardingRemindersFragment
import com.unionbankph.corporate.mcd.presentation.onboarding.CheckDepositOnBoardingScreenFragment
import com.unionbankph.corporate.notification.presentation.notification_log.NotificationLogTabFragment
import com.unionbankph.corporate.notification.presentation.notification_log.notification_log_detail.NotificationLogDetailFragment
import com.unionbankph.corporate.notification.presentation.notification_log.notification_log_list.NotificationLogFragment
import com.unionbankph.corporate.payment_link.presentation.onboarding.upload_photos.OnboardingUploadPhotosFragment
import com.unionbankph.corporate.payment_link.presentation.onboarding.upload_photos.OnboardingDeletePhotosFragment
import com.unionbankph.corporate.user_creation.presentation.confirmation_message.UcConfirmationMessageFragment
import com.unionbankph.corporate.user_creation.presentation.enter_contact_info.UcEnterContactInfoFragment
import com.unionbankph.corporate.payment_link.presentation.payment_link_list.PaymentLinkListFragment
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.upload_documents.CardAcceptanceUploadDocumentFragment
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account.NominateSettlementAccountFragment
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_channels.FeesAndChargesFragment
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_channels.PaymentMethodsFragment
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.terms_of_service.FeeCharges
import com.unionbankph.corporate.settings.presentation.SettingsFragment
import com.unionbankph.corporate.settings.presentation.display.SettingsDisplayFragment
import com.unionbankph.corporate.settings.presentation.fingerprint.FingerprintBottomSheet
import com.unionbankph.corporate.settings.presentation.general.GeneralSettingsFragment
import com.unionbankph.corporate.settings.presentation.notification.NotificationDetailFragment
import com.unionbankph.corporate.settings.presentation.notification.NotificationFragment
import com.unionbankph.corporate.settings.presentation.profile.ProfileSettingsFragment
import com.unionbankph.corporate.settings.presentation.security.SecurityFragment
import com.unionbankph.corporate.settings.presentation.security.device.ManageDeviceDetailFragment
import com.unionbankph.corporate.settings.presentation.security.device.ManageDevicesFragment
import com.unionbankph.corporate.settings.presentation.security.otp.SecurityEnableOTPFragment
import com.unionbankph.corporate.settings.presentation.security.otp.SecurityManageOTPFragment
import com.unionbankph.corporate.settings.presentation.security.otp.SecurityReceiveOTPFragment
import com.unionbankph.corporate.settings.presentation.splash.SplashEndFragment
import com.unionbankph.corporate.settings.presentation.splash.SplashFragment
import com.unionbankph.corporate.settings.presentation.splash.SplashOnboardingFragment
import com.unionbankph.corporate.transact.presentation.transact.TransactFragment
import com.unionbankph.corporate.user_creation.presentation.enter_name.UcEnterNameFragment
import com.unionbankph.corporate.user_creation.presentation.nominate_password.UcNominatePasswordFragment
import com.unionbankph.corporate.user_creation.presentation.reminder.UcReminderFragment
import com.unionbankph.corporate.user_creation.presentation.tnc.UcTNCFragment
import com.unionbankph.corporate.user_creation.presentation.tnc_reminder.UcTNCReminderFragment
import com.unionbankph.corporate.user_creation.presentation.personalise_settings.UcPersonaliseSettingsFragment
import com.unionbankph.corporate.user_creation.presentation.select_account.UcAccountSelectionFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBindingModule {

    @PerActivity
    @ContributesAndroidInjector
    abstract fun fingerprintBottomSheet(): FingerprintBottomSheet

    @PerActivity
    @ContributesAndroidInjector
    abstract fun sessionTimeOutBottomSheet(): SessionTimeOutBottomSheet

    @PerActivity
    @ContributesAndroidInjector
    abstract fun confirmationBottomSheet(): ConfirmationBottomSheet

    @PerActivity
    @ContributesAndroidInjector
    abstract fun fileManagerBottomSheet(): FileManagerBottomSheet

    @PerActivity
    @ContributesAndroidInjector
    abstract fun splashFragment(): SplashFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun splashEndFragment(): SplashEndFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun accountFragment(): AccountFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun approvalFragment(): ApprovalFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun approvalOngoingFragment(): ApprovalOngoingFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun approvalDoneFragment(): ApprovalDoneFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun sendRequestFragment(): TransactFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun notificationLogFragment(): NotificationLogFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun notificationLogDetailFragment(): NotificationLogDetailFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun settingFragment(): SettingsFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun generalSettingsFragment(): GeneralSettingsFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun profileSettingsFragment(): ProfileSettingsFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun frequentBillerFragment(): FrequentBillerFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun allBillerFragment(): AllBillerFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun securityEnableOTPFragment(): SecurityEnableOTPFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun securityFragment(): SecurityFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun securityManageOTPFragment(): SecurityManageOTPFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun settingsDisplayFragment(): SettingsDisplayFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun notificationFragment(): NotificationFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun notificationDetailFragment(): NotificationDetailFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun privacyPolicyFragment(): PrivacyPolicyFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun termsAndConditionsFragment(): TermsAndConditionsFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun beneficiaryFragment(): BeneficiaryFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun ownAccountFragment(): OwnAccountFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun manageScheduledTransferOngoingFragment(): ManageScheduledTransferOngoingFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun manageScheduledTransferDoneFragment(): ManageScheduledTransferDoneFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun nominateWelcomeFragment(): NominateWelcomeFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun nominateEmailFragment(): NominateEmailFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun nominatePasswordFragment(): NominatePasswordFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun nominateMobileNumberFragment(): NominateMobileNumberFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun nominateVerifyAccountFragment(): NominateVerifyAccountFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun nominateVerifyAccountResultFragment(): NominateVerifyAccountResultFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun nominateMergeEmailFragment(): NominateMergeEmailFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun nominateMergeVerifyFragment(): NominateMergeVerifyFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun nominateMergeVerifyAccountFragment(): NominateMergeVerifyAccountFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun nominateMergeSuccessFragment(): NominateMergeSuccessFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun securityReceiveOTPFragment(): SecurityReceiveOTPFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun manageDevicesFragment(): ManageDevicesFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun manageDeviceDetailFragment(): ManageDeviceDetailFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun notificationTabFragment(): NotificationLogTabFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun checkDepositOnBoardingScreenFragment(): CheckDepositOnBoardingScreenFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun checkDepositOnBoardingRemindersFragment(): CheckDepositOnBoardingRemindersFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoCheckingAccountTypeFragment(): DaoCheckingAccountTypeFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoRemindersFragment(): DaoRemindersFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoWelcomeFragment(): DaoWelcomeFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoWelcomeEnterFragment(): DaoWelcomeEnterFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoTypeOfBusinessFragment(): DaoTypeOfBusinessFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoCompanyInformationStepOneFragment(): DaoCompanyInformationStepOneFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoCompanyInformationStepTwoFragment(): DaoCompanyInformationStepTwoFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoBusinessRegistrationPapersFragment(): DaoBusinessRegistrationPapersFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoPreferredBranchFragment(): DaoPreferredBranchFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoPersonalInformationStepOneFragment(): DaoPersonalInformationStepOneFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoPersonalInformationStepTwoFragment(): DaoPersonalInformationStepTwoFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoPersonalInformationSteThreeFragment(): DaoPersonalInformationStepThreeFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoPersonalInformationStepFourFragment(): DaoPersonalInformationStepFourFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoOnlineBankingProductsFragment(): DaoOnlineBankingProductsFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoJumioVerificationFragment(): DaoJumioVerificationFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoSignatureFragment(): DaoSignatureFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoConfirmationFragment(): DaoConfirmationFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoResultFragment(): DaoResultFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun daoDefaultFragment(): DaoDefaultFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun loginFragment(): LoginFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun paymentLinkListFragment(): PaymentLinkListFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun paymentLinkChannelsPaymentMethodsFragment(): PaymentMethodsFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun paymentLinkChannelsFeesAndChargesFragment(): FeesAndChargesFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun nominateSettlementAccountFragment(): NominateSettlementAccountFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun allowPhoneCamera(): AllowPhoneCamera

    @PerActivity
    @ContributesAndroidInjector
    abstract fun splashOnboardingFragment(): SplashOnboardingFragment


    @PerActivity
    @ContributesAndroidInjector
    abstract fun newConfirmationBottomSheet(): NewConfirmationBottomSheet

    @PerActivity
    @ContributesAndroidInjector
    abstract fun ucReminderFragment(): UcReminderFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun ucEnterNameFragment(): UcEnterNameFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun accountSelectionFragment(): UcAccountSelectionFragment //UcAccountSelection

    @PerActivity
    @ContributesAndroidInjector
    abstract fun tncReminderFragment(): UcTNCReminderFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun tncFragment(): UcTNCFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun dashboardFragment(): DashboardFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun ucEnterContactInfoFragment(): UcEnterContactInfoFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun ucNominatePasswordFragment(): UcNominatePasswordFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun ucPersonaliseSettingsFragment(): UcPersonaliseSettingsFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun ucConfirmationMessageFragment(): UcConfirmationMessageFragment

 /*   @PerActivity
    @ContributesAndroidInjector
    abstract fun bottomSheetUploadPhotos(): OnboardingUploadPhotosFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun bottomSheetUploadBir(): CardAcceptanceUploadDocumentFragment*/

    @PerActivity
    @ContributesAndroidInjector
    abstract fun totpBottomSheet(): TOTPBottomSheet

    @PerActivity
    @ContributesAndroidInjector
    abstract fun bottomDashboardMore(): MegaMenuBottomSheet

    @PerActivity
    @ContributesAndroidInjector
    abstract fun asAccountSelectionFragment(): AsBusinessTypeFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun asBusinessAccountTypeFragment(): AsBusinessAccountTypeFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun asRemindersFragment(): AsRemindersFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun asTermsOfServiceFragment(): AsTermsOfServiceFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun asPersonalInfoFragment(): AsPersonalInformationFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun asAddressFragment(): AsAddressFragment

   /* @PerActivity
    @ContributesAndroidInjector
    abstract fun asTermsOfServiceViewPager(): AsTermsOfServiceViewPager

    @PerActivity
    @ContributesAndroidInjector
    abstract fun asPrivacyPolicyViewPager(): AsPrivacyPolicyViewPager*/

    @PerActivity
    @ContributesAndroidInjector
    abstract fun asDebitCardTypeFragment(): AsDebitCardTypeFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun loansFragment(): LoansFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun loansCalculatorFragment(): LoansCalculatorFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun fewRemindersFragment(): FewRemindersFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun citizenFragment(): CitizenFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun nonFilipinoFragment(): NonFilipinoFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun businessTypeFragment(): BusinessTypeFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun contactInformationFragment(): ContactInformationFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun personalInformationFragment(): PersonalInformationFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun asCitizenshipFragment(): AsCitizenshipFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun asNonFilipinoCitizenFragment(): AsNonFilipinoCitizenFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun feeCharges(): FeeCharges

    @PerActivity
    @ContributesAndroidInjector
    abstract fun onboardingDeletePhotosFragment(): OnboardingDeletePhotosFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun onboardingUploadPhotosFragment(): OnboardingUploadPhotosFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun cardAcceptanceUploadDocumentFragment(): CardAcceptanceUploadDocumentFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun financialInformationFragment(): FinancialInformationFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun addressFragment(): AddressFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun businessInformationFragment(): BusinessInformationFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun businessAddressFragment(): BusinessAddressFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun asBusinessInformationFragment(): AsBusinessInformationFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun productsFragment(): ProductsFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun productsCameraFragment(): ProductsCameraFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun productsCameraPreviewFragment(): ProductsCameraPreviewFragment

    @PerActivity
    @ContributesAndroidInjector
    abstract fun productsCameraDataFragment(): ProductsCameraDataFragment
}
