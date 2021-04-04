package com.unionbankph.corporate.app.di.module

import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.gateway.impl.AccountGatewayImpl
import com.unionbankph.corporate.account.data.source.local.AccountCache
import com.unionbankph.corporate.account.data.source.remote.AccountRemote
import com.unionbankph.corporate.app.di.scope.PerApplication
import com.unionbankph.corporate.app.util.SettingsUtil
import com.unionbankph.corporate.approval.data.gateway.ApprovalGatewayImpl
import com.unionbankph.corporate.approval.data.source.remote.ApprovalRemote
import com.unionbankph.corporate.approval.domain.gateway.ApprovalGateway
import com.unionbankph.corporate.auth.data.AuthDataGateway
import com.unionbankph.corporate.auth.data.AuthGateway
import com.unionbankph.corporate.auth.data.source.local.AuthCache
import com.unionbankph.corporate.auth.data.source.remote.AuthRemote
import com.unionbankph.corporate.bills_payment.data.gateway.BillsPaymentGateway
import com.unionbankph.corporate.bills_payment.data.gateway.impl.BillsPaymentGatewayImpl
import com.unionbankph.corporate.bills_payment.data.source.local.BillsPaymentCache
import com.unionbankph.corporate.bills_payment.data.source.remote.BillsPaymentRemote
import com.unionbankph.corporate.branch.data.gateway.BranchVisitGateway
import com.unionbankph.corporate.branch.data.gateway.impl.BranchVisitGatewayImpl
import com.unionbankph.corporate.branch.data.source.local.BranchVisitCache
import com.unionbankph.corporate.branch.data.source.local.CorporateUserCache
import com.unionbankph.corporate.branch.data.source.remote.BranchVisitRemote
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.corporate.data.gateway.CorporateGatewayImpl
import com.unionbankph.corporate.corporate.data.source.local.CorporateCache
import com.unionbankph.corporate.corporate.data.source.remote.CorporateRemote
import com.unionbankph.corporate.corporate.domain.gateway.CorporateGateway
import com.unionbankph.corporate.dao.data.gateway.DaoGatewayImpl
import com.unionbankph.corporate.dao.data.source.local.DaoCache
import com.unionbankph.corporate.dao.data.source.remote.DaoRemote
import com.unionbankph.corporate.dao.domain.gateway.DaoGateway
import com.unionbankph.corporate.fund_transfer.data.gateway.FundTransferGateway
import com.unionbankph.corporate.fund_transfer.data.gateway.impl.FundTransferGatewayImpl
import com.unionbankph.corporate.fund_transfer.data.source.local.FundTransferCache
import com.unionbankph.corporate.fund_transfer.data.source.remote.FundTransferRemote
import com.unionbankph.corporate.link_details.data.LinkDetailsGatewayImpl
import com.unionbankph.corporate.link_details.data.source.remote.LinkDetailsRemote
import com.unionbankph.corporate.link_details.data.LinkDetailsGateway
import com.unionbankph.corporate.mcd.data.gateway.CheckDepositGateway
import com.unionbankph.corporate.mcd.data.gateway.impl.CheckDepositGatewayImpl
import com.unionbankph.corporate.mcd.data.source.local.CheckDepositCache
import com.unionbankph.corporate.mcd.data.source.remote.CheckDepositRemote
import com.unionbankph.corporate.notification.data.gateway.NotificationGateway
import com.unionbankph.corporate.notification.data.gateway.impl.NotificationGatewayImpl
import com.unionbankph.corporate.notification.data.source.remote.NotificationRemote
import com.unionbankph.corporate.request_payment_link.data.CreateMerchantRemote
import com.unionbankph.corporate.request_payment_link.domain.CreateMerchantGateway
import com.unionbankph.corporate.request_payment_link.domain.CreateMerchantGatewayImpl
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.settings.data.gateway.impl.SettingsGatewayImpl
import com.unionbankph.corporate.settings.data.source.local.SettingsCache
import com.unionbankph.corporate.settings.data.source.remote.SettingsRemote
import dagger.Module
import dagger.Provides

@Module
class GatewayDataModule {

    @Provides
    @PerApplication
    fun authGateway(
        responseProvider: ResponseProvider,
        authRemote: AuthRemote,
        authCache: AuthCache,
        settingsGateway: SettingsGateway,
        settingsUtil: SettingsUtil
    ): AuthGateway =
        AuthDataGateway(
            responseProvider,
            authRemote,
            authCache,
            settingsGateway,
            settingsUtil
        )

    @Provides
    @PerApplication
    fun checkDepositGateway(
        responseProvider: ResponseProvider,
        checkDepositRemote: CheckDepositRemote,
        checkDepositCache: CheckDepositCache,
        settingsGateway: SettingsGateway
    ): CheckDepositGateway = CheckDepositGatewayImpl(
        responseProvider,
        checkDepositRemote,
        checkDepositCache,
        settingsGateway
    )

    @Provides
    @PerApplication
    fun settingsGateway(
        responseProvider: ResponseProvider,
        settingsRemote: SettingsRemote,
        settingsCache: SettingsCache
    ): SettingsGateway = SettingsGatewayImpl(
        responseProvider,
        settingsRemote,
        settingsCache
    )

    @Provides
    @PerApplication
    fun branchVisitGateway(
        responseProvider: ResponseProvider,
        branchVisitRemote: BranchVisitRemote,
        branchVisitCache: BranchVisitCache,
        corporateUserCache: CorporateUserCache,
        settingsGateway: SettingsGateway
    ): BranchVisitGateway = BranchVisitGatewayImpl(
        responseProvider,
        branchVisitRemote,
        branchVisitCache,
        corporateUserCache,
        settingsGateway
    )

    @Provides
    @PerApplication
    fun corporateGateway(
        responseProvider: ResponseProvider,
        corporateRemote: CorporateRemote,
        corporateCache: CorporateCache,
        settingsGateway: SettingsGateway
    ): CorporateGateway =
        CorporateGatewayImpl(
            responseProvider,
            corporateRemote,
            corporateCache,
            settingsGateway
        )

    @Provides
    @PerApplication
    fun accountGateway(
        responseProvider: ResponseProvider,
        accountRemote: AccountRemote,
        accountCache: AccountCache,
        settingsGateway: SettingsGateway
    ): AccountGateway = AccountGatewayImpl(
        responseProvider,
        accountRemote,
        accountCache,
        settingsGateway
    )

    @Provides
    @PerApplication
    fun approvalGateway(
        responseProvider: ResponseProvider,
        approvalRemote: ApprovalRemote,
        settingsGateway: SettingsGateway
    ): ApprovalGateway =
        ApprovalGatewayImpl(
            responseProvider,
            approvalRemote,
            settingsGateway
        )

    @Provides
    @PerApplication
    fun fundTransferGateway(
        responseProvider: ResponseProvider,
        fundTransferRemote: FundTransferRemote,
        fundTransferCache: FundTransferCache,
        settingsGateway: SettingsGateway,
        corporateGateway: CorporateGateway
    ): FundTransferGateway = FundTransferGatewayImpl(
        responseProvider,
        fundTransferRemote,
        fundTransferCache,
        settingsGateway,
        corporateGateway
    )

    @Provides
    @PerApplication
    fun billsPaymentGateway(
        responseProvider: ResponseProvider,
        billsPaymentRemote: BillsPaymentRemote,
        billsPaymentCache: BillsPaymentCache,
        settingsGateway: SettingsGateway,
        corporateGateway: CorporateGateway
    ): BillsPaymentGateway = BillsPaymentGatewayImpl(
        responseProvider,
        billsPaymentRemote,
        billsPaymentCache,
        settingsGateway,
        corporateGateway
    )

    @Provides
    @PerApplication
    fun notificationGateway(
        responseProvider: ResponseProvider,
        notificationRemote: NotificationRemote,
        settingsGateway: SettingsGateway
    ): NotificationGateway =
        NotificationGatewayImpl(
            responseProvider,
            notificationRemote,
            settingsGateway
        )

    @Provides
    @PerApplication
    fun daoGateway(
        responseProvider: ResponseProvider,
        daoRemote: DaoRemote,
        daoCache: DaoCache
    ): DaoGateway = DaoGatewayImpl(
        responseProvider,
        daoRemote,
        daoCache
    )

    @Provides
    @PerApplication
    fun linkDetailsGateway(
        responseProvider: ResponseProvider,
        linkDetailsRemote: LinkDetailsRemote,
        settingsCache: SettingsCache,
        cacheManager: CacheManager
    ): LinkDetailsGateway =
        LinkDetailsGatewayImpl(
            responseProvider,
            linkDetailsRemote,
            settingsCache,
            cacheManager
        )

    @Provides
    @PerApplication
    fun createMerchantGateway(
        responseProvider: ResponseProvider,
        createMerchantRemote: CreateMerchantRemote,
        settingsCache: SettingsCache,
        cacheManager: CacheManager
    ): CreateMerchantGateway =
        CreateMerchantGatewayImpl(
            responseProvider,
            createMerchantRemote,
            settingsCache,
            cacheManager
        )
}
