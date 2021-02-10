package com.unionbankph.corporate.app.di.module

import com.unionbankph.corporate.app.di.scope.PerActivity
import com.unionbankph.corporate.app.service.SessionTimeOutJobService
import com.unionbankph.corporate.app.service.fcm.AutobahnFirebaseMessagingService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBindingModule {

    @PerActivity
    @ContributesAndroidInjector
    abstract fun autobahnFirebaseMessagingService(): AutobahnFirebaseMessagingService

    @PerActivity
    @ContributesAndroidInjector
    abstract fun sessionTimeOutJobService(): SessionTimeOutJobService
}
