package com.unionbankph.corporate.app

import android.app.Activity
import android.app.Application
import android.app.Service
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.multidex.MultiDex
import com.facebook.stetho.Stetho
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.app.common.platform.glide.GlideApp
import com.unionbankph.corporate.app.common.platform.logging.ErrorReportingTree
import com.unionbankph.corporate.app.di.component.DaggerAppComponent
import com.unionbankph.corporate.app.service.SessionTimeOutJobService
import com.unionbankph.corporate.common.presentation.constant.Environment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasBroadcastReceiverInjector
import dagger.android.HasServiceInjector
import dagger.android.support.HasSupportFragmentInjector
import timber.log.Timber
import javax.inject.Inject

open class App : Application(),
    HasActivityInjector,
    HasSupportFragmentInjector,
    HasBroadcastReceiverInjector,
    HasServiceInjector, Application.ActivityLifecycleCallbacks {

    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var broadcastInjector: DispatchingAndroidInjector<BroadcastReceiver>

    @Inject
    lateinit var serviceInjector: DispatchingAndroidInjector<Service>

    override fun onCreate() {
        super.onCreate()
        init()
    }

    override fun onActivityPostDestroyed(activity: Activity) {
        super.onActivityPostDestroyed(activity)
        GlideApp.get(this).clearMemory()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        try {
            MultiDex.install(this)
        } catch (multiDexException: RuntimeException) {
            Timber.e(multiDexException, "attachBaseContext")
        }
    }

    private fun init() {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        registerActivityLifecycleCallbacks(this)
        DaggerAppComponent.builder()
            .application(this)
            .build()
            .inject(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())

            // https://developer.android.com/reference/android/os/StrictMode.html
            // StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
            // StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build())
            // StrictMode.enableDefaults()

            // http://facebook.github.io/stetho/
            Stetho.initializeWithDefaults(this)
        } else {
            Timber.plant(ErrorReportingTree())
            // https://medium.com/fuzz/getting-the-most-out-of-crashlytics-380afb703876
            // Crashlytics.setString("git_sha", BuildConfig.GIT_SHA)
        }
    }

    override fun activityInjector(): AndroidInjector<Activity> {
        return activityInjector
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentInjector
    }

    override fun broadcastReceiverInjector(): AndroidInjector<BroadcastReceiver> {
        return broadcastInjector
    }

    override fun serviceInjector(): AndroidInjector<Service> {
        return serviceInjector
    }

    override fun onActivityPaused(activity: Activity) {
        currentVisibleActivity = activity
        isActive = false
    }

    override fun onActivityResumed(activity: Activity) {
        currentVisibleActivity = activity
        isActive = true
    }

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivityCreated(activity: Activity, p1: Bundle?) {}

    companion object {

        private const val JOB_SESSION_TIME_OUT_ID: Int = 1
        private const val REFRESH_INTERVAL: Long = 1000

        val isSupportedInProduction = Environment.QAT2
                || Environment.QAT3
                || Environment.PREPRODUCTION
                || Environment.PRODUCTION

        var isActive = false
        var currentVisibleActivity: Activity? = null

        fun isActivityVisible() = isActive

        fun isSME() = BuildConfig.FLAVOR.contains("SME", true)

        fun startSessionJobService(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val jobScheduler =
                    context.getSystemService(AppCompatActivity.JOB_SCHEDULER_SERVICE) as (JobScheduler)
                val componentName = ComponentName(context, SessionTimeOutJobService::class.java)
                val jobInfo = JobInfo.Builder(JOB_SESSION_TIME_OUT_ID, componentName).apply {
                    setPersisted(true)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        setMinimumLatency(REFRESH_INTERVAL)
                    } else {
                        setPeriodic(REFRESH_INTERVAL)
                    }
                }.build()
                jobScheduler.schedule(jobInfo)
            }
        }

        fun stopSessionJobService(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val jobScheduler =
                    context.getSystemService(AppCompatActivity.JOB_SCHEDULER_SERVICE) as (JobScheduler)
                jobScheduler.cancel(JOB_SESSION_TIME_OUT_ID)
            }
        }
    }
}
