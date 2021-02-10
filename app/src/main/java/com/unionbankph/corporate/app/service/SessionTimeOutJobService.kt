package com.unionbankph.corporate.app.service

import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.receiver.ForceLogoutReceiver
import com.unionbankph.corporate.app.util.SettingsUtil
import com.unionbankph.corporate.common.presentation.constant.Constant
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class SessionTimeOutJobService : JobService() {

    @Inject
    lateinit var settingsUtil: SettingsUtil

    private val timeSessionDialog by lazyFast {
        resources.getInteger(R.integer.time_session_out_dialog).toLong()
    }

    private var disposables = CompositeDisposable()

    private var sessionTimeOutDisposable: Disposable? = null

    override fun onCreate() {
        Timber.d("onCreate")
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onStartJob(jobParameters: JobParameters): Boolean {
        Timber.d("onStartJob")
        startSessionTimeOut(jobParameters)
        return true
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean {
        Timber.d("onStopJob")
        disposables.clear()
        disposables.dispose()
        return true
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Timber.d("onTaskRemoved")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        disposables.clear()
        disposables.dispose()
    }

    private fun initObservableSessionTimeOut(
        jobParameters: JobParameters,
        period: Long,
        time: Long
    ) {
        sessionTimeOutDisposable?.dispose()
        sessionTimeOutDisposable = Observable.interval(period, TimeUnit.SECONDS)
            .doOnNext {
                val timer = time - it.toInt()
                Timber.d("session timer: %s", timer)
                if (timer <= timeSessionDialog) {
                    val intent = Intent(Constant.ACTION_SESSION_TIMEOUT).apply {
                        putExtra(EXTRA_SESSION_TYPE, TYPE_SESSION_TIMER)
                        putExtra(EXTRA_SESSION_TIMER, it)
                        putExtra(EXTRA_SESSION_TIMER_COUNTDOWN, timer)
                    }
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                }
            }
            .takeUntil { it == time }
            .subscribeOn(Schedulers.newThread())
            .doOnComplete {
                Timber.d("onTimeOutSuccess")
                if (App.isActivityVisible()) {
                    val intent = Intent(Constant.ACTION_SESSION_TIMEOUT).apply {
                        putExtra(EXTRA_SESSION_TYPE, TYPE_SESSION_TIMEOUT)
                    }
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                } else {
                    initForceLogoutReceiver()
                }
                jobFinished(jobParameters, false)
            }.subscribe()
        sessionTimeOutDisposable?.addTo(disposables)
    }

    private fun startSessionTimeOut(jobParameters: JobParameters) {
        if (settingsUtil.isLoggedIn()) {
            Timber.d("startSessionTimeOut")
            initObservableSessionTimeOut(
                jobParameters,
                resources.getInteger(R.integer.time_session_out_start).toLong(),
                resources.getInteger(R.integer.time_session_out_logout).toLong()
            )
        }
    }

    private fun initForceLogoutReceiver() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ForceLogoutReceiver::class.java)
        intent.action = ForceLogoutReceiver.ACTION_FORCE_LOGOUT
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            REQUEST_CODE_SESSION,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val calendar = Calendar.getInstance()
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    companion object {
        const val EXTRA_SESSION_TIMER = "session_timer"
        const val EXTRA_SESSION_TIMER_COUNTDOWN = "session_timer_countdown"
        const val EXTRA_SESSION_TYPE = "session_type"
        const val TYPE_SESSION_TIMER = "type_session_timer"
        const val TYPE_SESSION_TIMEOUT = "type_session_timeout"
        const val REQUEST_CODE_SESSION = 1
    }
} 
