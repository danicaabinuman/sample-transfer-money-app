package com.unionbankph.corporate.app.di.component

import android.app.Application
import com.unionbankph.corporate.account.presentation.di.AccountModule
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.di.module.ActivityBindingModule
import com.unionbankph.corporate.app.di.module.AppModule
import com.unionbankph.corporate.app.di.module.BroadcastBindingModule
import com.unionbankph.corporate.app.di.module.FragmentBindingModule
import com.unionbankph.corporate.app.di.module.GatewayDataModule
import com.unionbankph.corporate.app.di.module.LocalDataModule
import com.unionbankph.corporate.app.di.module.NetworkModule
import com.unionbankph.corporate.app.di.module.RemoteDataModule
import com.unionbankph.corporate.app.di.module.ServiceBindingModule
import com.unionbankph.corporate.app.di.module.ViewModelModule
import com.unionbankph.corporate.app.di.scope.PerApplication
import com.unionbankph.corporate.approval.presentation.di.ApprovalMapperModule
import com.unionbankph.corporate.approval.presentation.di.ApprovalModule
import com.unionbankph.corporate.corporate.presentation.di.CorporateModule
import com.unionbankph.corporate.dao.presentation.di.DaoMapperModule
import com.unionbankph.corporate.dao.presentation.di.DaoModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.support.AndroidSupportInjectionModule

@PerApplication
@Component(
    modules = [
        AndroidInjectionModule::class,
        AndroidSupportInjectionModule::class,
        AppModule::class,
        NetworkModule::class,
        ViewModelModule::class,
        ActivityBindingModule::class,
        FragmentBindingModule::class,
        BroadcastBindingModule::class,
        ServiceBindingModule::class,
        RemoteDataModule::class,
        LocalDataModule::class,
        GatewayDataModule::class,
        // Mappers
        DaoMapperModule::class,
        ApprovalMapperModule::class,
        // Interactors
        DaoModule::class,
        ApprovalModule::class,
        CorporateModule::class,
        AccountModule::class
    ]
)
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(app: App)
}
