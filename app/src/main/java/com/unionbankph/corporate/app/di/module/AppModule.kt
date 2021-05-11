package com.unionbankph.corporate.app.di.module

import android.app.Application
import android.content.Context
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.securepreferences.SecurePreferences
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.app.common.platform.bus.data.DataBus
import com.unionbankph.corporate.app.common.platform.bus.event.EventBus
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.tutorial.TutorialEngineUtil
import com.unionbankph.corporate.app.di.scope.PerApplication
import com.unionbankph.corporate.app.executor.UiThread
import com.unionbankph.corporate.app.helper.NotificationHelper
import com.unionbankph.corporate.app.util.AnimUtil
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.BitmapUtil
import com.unionbankph.corporate.app.util.FileUtil
import com.unionbankph.corporate.app.util.SettingsUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.executor.JobExecutor
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.data.source.local.sharedpref.SharedPreferenceUtil
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.common.domain.provider.SMEResponseProvider
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.domain.provider.impl.ResponseProviderImpl
import com.unionbankph.corporate.common.domain.provider.impl.SMEResponseProviderImpl
import com.unionbankph.corporate.common.domain.provider.impl.SchedulerProviderImpl
import com.unionbankph.corporate.settings.data.constant.SingleSelectorData
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class AppModule {

    @Provides
    @PerApplication
    fun context(application: Application): Context = application

    @Provides
    @PerApplication
    internal fun eventBus(): EventBus {
        return EventBus()
    }

    @Provides
    @PerApplication
    internal fun dataBus(): DataBus {
        return DataBus()
    }

    @Provides
    @PerApplication
    internal fun compositeDisposable(): CompositeDisposable {
        return CompositeDisposable()
    }

    @Provides
    @PerApplication
    internal fun rxSharedPreferences(context: Context): RxSharedPreferences {
        val preferences = SecurePreferences(context, "", "autobahn_prefs.xml")
        if (BuildConfig.DEBUG) {
            SecurePreferences.setLoggingEnabled(true)
        } else {
            SecurePreferences.setLoggingEnabled(false)
        }
        return RxSharedPreferences.create(preferences)
    }

    @Provides
    @PerApplication
    internal fun schedulerProvider(): SchedulerProvider {
        return SchedulerProviderImpl()
    }

    @Provides
    @PerApplication
    internal fun responseProvider(context: Context): ResponseProvider {
        return ResponseProviderImpl(
            context
        )
    }

    @Provides
    @PerApplication
    internal fun smeResponseProvider(context: Context): SMEResponseProvider {
        return SMEResponseProviderImpl(
                context
        )
    }

    @Provides
    @PerApplication
    internal fun navigator(): Navigator {
        return Navigator()
    }

    @Provides
    internal fun tutorialEngineUtil(viewUtil: ViewUtil): TutorialEngineUtil {
        return TutorialEngineUtil(viewUtil)
    }

    @Provides
    @PerApplication
    internal fun viewUtil(context: Context): ViewUtil {
        return ViewUtil(context)
    }

    @Provides
    @PerApplication
    internal fun settingsUtil(
        context: Context,
        cacheManager: CacheManager,
        sharedPreferenceUtil: SharedPreferenceUtil
    ): SettingsUtil {
        return SettingsUtil(context, cacheManager, sharedPreferenceUtil)
    }

    @Provides
    @PerApplication
    internal fun cacheManager(): CacheManager {
        return CacheManager()
    }

    @Provides
    @PerApplication
    internal fun sharedPreferenceUtil(
        rxSharedPreferences: RxSharedPreferences
    ): SharedPreferenceUtil {
        return SharedPreferenceUtil(rxSharedPreferences)
    }

    @Provides
    @PerApplication
    internal fun autoFormatUtil(): AutoFormatUtil {
        return AutoFormatUtil()
    }

    @Provides
    @PerApplication
    internal fun animUtil(context: Context): AnimUtil {
        return AnimUtil(context)
    }

    @Provides
    @PerApplication
    internal fun notificationHelper(context: Context): NotificationHelper {
        return NotificationHelper(context)
    }

    @Provides
    @PerApplication
    internal fun fileUtil(context: Context): FileUtil {
        return FileUtil(context)
    }

    @Provides
    @PerApplication
    internal fun bitmapUtil(context: Context): BitmapUtil {
        return BitmapUtil(context)
    }

    @Provides
    @PerApplication
    internal fun singleSelectorData(context: Context): SingleSelectorData {
        return SingleSelectorData(context)
    }

    @Provides
    @PerApplication
    internal fun threadExecutor(jobExecutor: JobExecutor): ThreadExecutor {
        return jobExecutor
    }

    @Provides
    @PerApplication
    internal fun postExecutionThread(uiThread: UiThread): PostExecutionThread {
        return uiThread
    }
}
