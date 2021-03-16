package com.unionbankph.corporate.app.di.module

import com.unionbankph.corporate.account.data.source.remote.AccountRemote
import com.unionbankph.corporate.account.data.source.remote.impl.AccountRemoteImpl
import com.unionbankph.corporate.app.di.scope.PerApplication
import com.unionbankph.corporate.approval.data.source.remote.ApprovalRemote
import com.unionbankph.corporate.approval.data.source.remote.impl.ApprovalRemoteImpl
import com.unionbankph.corporate.auth.data.source.remote.AuthRemote
import com.unionbankph.corporate.auth.data.source.remote.impl.AuthRemoteImpl
import com.unionbankph.corporate.bills_payment.data.source.remote.BillsPaymentRemote
import com.unionbankph.corporate.bills_payment.data.source.remote.impl.BillsPaymentRemoteImpl
import com.unionbankph.corporate.branch.data.source.remote.BranchVisitRemote
import com.unionbankph.corporate.branch.data.source.remote.impl.BranchVisitRemoteImpl
import com.unionbankph.corporate.mcd.data.source.remote.CheckDepositRemote
import com.unionbankph.corporate.mcd.data.source.remote.impl.CheckDepositRemoteImpl
import com.unionbankph.corporate.corporate.data.source.remote.CorporateRemote
import com.unionbankph.corporate.corporate.data.source.remote.impl.CorporateRemoteImpl
import com.unionbankph.corporate.dao.data.source.remote.DaoRemote
import com.unionbankph.corporate.dao.data.source.remote.impl.DaoRemoteImpl
import com.unionbankph.corporate.fund_transfer.data.source.remote.FundTransferRemote
import com.unionbankph.corporate.fund_transfer.data.source.remote.impl.FundTransferRemoteImpl
import com.unionbankph.corporate.link_details.data.source.remote.LinkDetailsRemote
import com.unionbankph.corporate.link_details.data.source.remote.impl.LinkDetailsRemoteImpl
import com.unionbankph.corporate.notification.data.source.remote.NotificationRemote
import com.unionbankph.corporate.notification.data.source.remote.impl.NotificationRemoteImpl
import com.unionbankph.corporate.settings.data.source.remote.SettingsRemote
import com.unionbankph.corporate.settings.data.source.remote.impl.SettingsRemoteImpl
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class RemoteDataModule {

    @Provides
    @PerApplication
    fun checkDepositRemote(
        retrofit: Retrofit
    ): CheckDepositRemote =
        CheckDepositRemoteImpl(retrofit)

    @Provides
    @PerApplication
    fun branchVisitRemote(
        retrofit: Retrofit
    ): BranchVisitRemote =
        BranchVisitRemoteImpl(retrofit)

    @Provides
    @PerApplication
    fun accountRemote(retrofit: Retrofit): AccountRemote = AccountRemoteImpl(retrofit)

    @Provides
    @PerApplication
    fun approvalRemote(retrofit: Retrofit): ApprovalRemote = ApprovalRemoteImpl(retrofit)

    @Provides
    @PerApplication
    fun fundTransferRemote(retrofit: Retrofit): FundTransferRemote =
        FundTransferRemoteImpl(retrofit)

    @Provides
    @PerApplication
    fun settingsRemote(retrofit: Retrofit): SettingsRemote = SettingsRemoteImpl(retrofit)

    @Provides
    @PerApplication
    fun corporateRemote(retrofit: Retrofit): CorporateRemote = CorporateRemoteImpl(retrofit)

    @Provides
    @PerApplication
    fun billsPaymentRemote(retrofit: Retrofit): BillsPaymentRemote =
        BillsPaymentRemoteImpl(retrofit)

    @Provides
    @PerApplication
    fun notificationRemote(retrofit: Retrofit): NotificationRemote =
        NotificationRemoteImpl(retrofit)

    @Provides
    @PerApplication
    fun authRemote(retrofit: Retrofit): AuthRemote = AuthRemoteImpl(retrofit)

    @Provides
    @PerApplication
    fun daoRemote(retrofit: Retrofit): DaoRemote = DaoRemoteImpl(retrofit)

    @Provides
    @PerApplication
    fun linkDetailsRemote(retrofit: Retrofit): LinkDetailsRemote = LinkDetailsRemoteImpl(retrofit)
}
