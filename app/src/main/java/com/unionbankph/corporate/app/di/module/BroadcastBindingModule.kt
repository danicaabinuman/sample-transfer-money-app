package com.unionbankph.corporate.app.di.module

import com.unionbankph.corporate.app.di.scope.PerActivity
import com.unionbankph.corporate.app.receiver.ForceLogoutReceiver
import com.unionbankph.corporate.app.receiver.NotificationActionReceiver
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BroadcastBindingModule {

    @PerActivity
    @ContributesAndroidInjector
    abstract fun forceLogoutReceiver(): ForceLogoutReceiver

    @PerActivity
    @ContributesAndroidInjector
    abstract fun notificationActionReceiver(): NotificationActionReceiver
}
