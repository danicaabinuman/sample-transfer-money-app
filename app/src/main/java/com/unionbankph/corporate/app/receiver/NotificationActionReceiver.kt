package com.unionbankph.corporate.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.app.util.SettingsUtil
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.data.model.PushNotificationPayload
import com.unionbankph.corporate.common.presentation.constant.NotificationLogTypeEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingUtil: SettingsUtil

    @Inject
    lateinit var navigator: Navigator

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        Timber.d("onReceive: ${intent.getStringExtra(EXTRA_DATA)}")
        val action = intent.getStringExtra(EXTRA_ACTION)
        if (action == ACTION_CLICK_NOTIFICATION) {
            initClickNotificationAction(context, intent.getStringExtra(EXTRA_DATA))
        }
    }

    private fun initClickNotificationAction(context: Context, data: String?) {
        val pushNotificationPayload = JsonHelper.fromJson<PushNotificationPayload>(data)
        when (pushNotificationPayload.code) {
            NotificationLogTypeEnum.CORP_USER_LOGOUT.name -> {
                val intent = Intent(context, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            }
            NotificationLogTypeEnum.ANNOUNCEMENT.name,
            NotificationLogTypeEnum.NEW_DEVICE_LOGIN.name,
            NotificationLogTypeEnum.FILE_UPLOADED.name,
            NotificationLogTypeEnum.NEW_FEATURES.name,
            NotificationLogTypeEnum.ORGANIZATION_UPDATES.name,
            NotificationLogTypeEnum.UNTRUST_DEVICE.name,
            NotificationLogTypeEnum.TRUST_DEVICE.name,
            NotificationLogTypeEnum.FORGET_DEVICE.name,
            NotificationLogTypeEnum.MOBILE_CHECK_DEPOSIT.name,
            NotificationLogTypeEnum.MAINTENANCE.name,
            NotificationLogTypeEnum.TRANSACTION_STATUS_CHANGED.name -> {
                if (settingUtil.isLoggedIn()) {
                    navigator.navigateClearUpStack(
                        App.currentVisibleActivity as AppCompatActivity,
                        DashboardActivity::class.java,
                        Bundle().apply {
                            putString(EXTRA_DATA, data)
                        },
                        isClear = true,
                        isAnimated = true
                    )
                } else {
                    val intent = Intent(context, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra(EXTRA_DATA, data)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    companion object {
        const val EXTRA_ACTION = "action"
        const val EXTRA_DATA = "data"
        const val ACTION_CLICK_NOTIFICATION = "click_notification"
    }
}
