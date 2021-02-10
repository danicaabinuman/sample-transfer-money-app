package com.unionbankph.corporate.app.service.fcm

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.bus.event.EventBus
import com.unionbankph.corporate.app.common.platform.bus.event.NotificationSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.helper.NotificationHelper
import com.unionbankph.corporate.app.receiver.NotificationActionReceiver
import com.unionbankph.corporate.app.util.SettingsUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.model.PushNotificationPayload
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.data.source.local.sharedpref.SharedPreferenceUtil
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.NotificationLogTypeEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.security.SecureRandom
import javax.inject.Inject

class AutobahnFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var sharedPreferenceUtil: SharedPreferenceUtil

    @Inject
    lateinit var cacheManager: CacheManager

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var settingsUtil: SettingsUtil

    @Inject
    lateinit var viewUtil: ViewUtil

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var eventBus: EventBus

    private val disposables = CompositeDisposable()

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        disposables.dispose()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("Generate new tokenId: %s", token)
        sharedPreferenceUtil.notificationTokenPref().set(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Timber.d("Message data payload: %s", remoteMessage.data)
            val title = remoteMessage.data[EXTRA_TITLE]
            val message = remoteMessage.data[EXTRA_MESSAGE]
            val code = remoteMessage.data[EXTRA_CODE]
            val id = remoteMessage.data[EXTRA_ID]
            val channel = remoteMessage.data[EXTRA_CHANNEL]
            val roleId = remoteMessage.data[EXTRA_ROLE]
            val badgeCount = remoteMessage.data[EXTRA_BADGE_COUNT]
            val data = getPushNotificationStringPayload(title, message, code, id, channel, roleId)
            when (code) {
                NotificationLogTypeEnum.CORP_USER_LOGOUT.name -> {
                    if (settingsUtil.isLoggedIn()) {
                        initBroadcastReceiver(
                            Constant.Notification.ACTION_DIALOG,
                            code,
                            title,
                            message,
                            data
                        )
                    }
                }
                NotificationLogTypeEnum.TRANSACTION_STATUS_CHANGED.name -> {
                    setBadgeCountShortCut(badgeCount)
                    eventBus.notificationSyncEvent.emmit(
                        BaseEvent(NotificationSyncEvent.ACTION_UPDATE_NOTIFICATION_APPROVAL)
                    )
                    initNotification(code, title, message, code, data)
                }
                NotificationLogTypeEnum.TRUST_DEVICE.name -> {
                    val totpToken = remoteMessage.data[EXTRA_TOKEN]
                    initTrustDeviceNotification(code, title, message, totpToken, data)
                }
                NotificationLogTypeEnum.UNTRUST_DEVICE.name -> {
                    initUnTrustDeviceNotification(code, title, message, data)
                }
                NotificationLogTypeEnum.FORGET_DEVICE.name -> {
                    initForgetDeviceNotification(code, title, message, data)
                }
                NotificationLogTypeEnum.MOBILE_CHECK_DEPOSIT.name -> {
                    initNotification(code, title, message, code, data)
                }
                NotificationLogTypeEnum.ANNOUNCEMENT.name,
                NotificationLogTypeEnum.NEW_DEVICE_LOGIN.name,
                NotificationLogTypeEnum.FILE_UPLOADED.name,
                NotificationLogTypeEnum.NEW_FEATURES.name,
                NotificationLogTypeEnum.ORGANIZATION_UPDATES.name,
                NotificationLogTypeEnum.MAINTENANCE.name -> {
                    setBadgeCountShortCut(badgeCount)
                    eventBus.notificationSyncEvent.emmit(
                        BaseEvent(NotificationSyncEvent.ACTION_UPDATE_NOTIFICATION_ALERTS)
                    )
                    initNotification(code, title, message, code, data)
                }
            }
        }

        if (remoteMessage.notification != null) {
            Timber.d("Message Notification Body: %s", remoteMessage.notification?.body)
        }
    }

    private fun setBadgeCountShortCut(badgeCountShortCut: String?) {
        badgeCountShortCut?.let {
            viewUtil.setShortCutBadge(context, badgeCountShortCut.toInt())
        }
    }

    private fun initForgetDeviceNotification(
        code: String,
        title: String?,
        message: String?,
        data: String
    ) {
        clearLoginCredential()
        initBroadcastReceiver(
            Constant.Notification.ACTION_DIALOG,
            code,
            title,
            message,
            data
        )
    }

    private fun initUnTrustDeviceNotification(
        code: String,
        title: String?,
        message: String?,
        data: String
    ) {
        clearCredential()
        initBroadcastReceiver(
            Constant.Notification.ACTION_DIALOG,
            code,
            title,
            message,
            data
        )
    }

    private fun initTrustDeviceNotification(
        code: String,
        title: String?,
        message: String?,
        token: String?,
        data: String
    ) {
        refreshCredential(token)
        initBroadcastReceiver(
            Constant.Notification.ACTION_DIALOG,
            code,
            title,
            message,
            data
        )
    }

    private fun clearLoginCredential() {
        cacheManager.clear(CacheManager.ACCESS_TOKEN)
        cacheManager.clear(CacheManager.CORPORATE_USER)
        cacheManager.clear(CacheManager.ROLE)
        sharedPreferenceUtil.isLoggedIn().set(false)
        sharedPreferenceUtil.isTrustedDevice().set(false)
        sharedPreferenceUtil.fullNameSharedPref().delete()
        sharedPreferenceUtil.emailSharedPref().delete()
        sharedPreferenceUtil.fingerPrintTokenSharedPref().delete()
        sharedPreferenceUtil.totpTokenPref().delete()
    }

    private fun clearCredential() {
        cacheManager.clear(CacheManager.ACCESS_TOKEN)
        cacheManager.clear(CacheManager.CORPORATE_USER)
        cacheManager.clear(CacheManager.ROLE)
        sharedPreferenceUtil.isLoggedIn().set(false)
        sharedPreferenceUtil.totpTokenPref().delete()
    }

    private fun refreshCredential(totpToken: String?) {
        cacheManager.clear(CacheManager.ACCESS_TOKEN)
        cacheManager.clear(CacheManager.CORPORATE_USER)
        cacheManager.clear(CacheManager.ROLE)
        sharedPreferenceUtil.isLoggedIn().set(false)
        sharedPreferenceUtil.totpTokenPref().set(totpToken.notNullable())
    }

    private fun initBroadcastReceiver(
        action: String,
        code: String,
        title: String?,
        message: String?,
        data: String
    ) {
        if (!App.isActivityVisible()) {
            initNotification(code, title, message, code, data)
        }
        val intent = Intent(action)
        intent.putExtra(EXTRA_TITLE, title)
        intent.putExtra(EXTRA_MESSAGE, message)
        intent.putExtra(EXTRA_CODE, code)
        intent.putExtra(EXTRA_DATA, data)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun initNotification(
        channelId: String,
        title: String?,
        message: String?,
        code: String?,
        data: String
    ) {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            putExtra(NotificationActionReceiver.EXTRA_DATA, data)
            putExtra(
                NotificationActionReceiver.EXTRA_ACTION,
                NotificationActionReceiver.ACTION_CLICK_NOTIFICATION
            )
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            nextInt(IntRange(0, 10000)),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificationCompatBuilder = notificationHelper.channelNotification(
            channelId,
            title,
            message,
            pendingIntent
        )
        notificationHelper.manager?.notify(
            if (code == NotificationLogTypeEnum.CORP_USER_LOGOUT.name)
                R.id.notificationLogout
            else
                nextInt(IntRange(0, 10000)),
            notificationCompatBuilder.build()
        )
    }

    private fun nextInt(range: IntRange): Int {
        return range.first + SecureRandom().nextInt(range.last - range.first)
    }

    private fun getPushNotificationStringPayload(
        title: String?,
        message: String?,
        code: String?,
        id: String?,
        channel: String?,
        roleId: String? = null
    ): String {
        return JsonHelper.toJson(PushNotificationPayload(title, message, code, id, channel, roleId))
    }

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_CODE = "code"
        const val EXTRA_ID = "id"
        const val EXTRA_CHANNEL = "channel"
        const val EXTRA_ROLE = "role"
        const val EXTRA_BADGE_COUNT = "badgeCount"
        const val EXTRA_TOKEN = "token"

        const val EXTRA_DATA = "data"
    }
}
