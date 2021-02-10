package com.unionbankph.corporate.general.presentation.link

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.app.service.fcm.AutobahnFirebaseMessagingService
import com.unionbankph.corporate.app.util.SettingsUtil
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.data.model.PushNotificationPayload
import com.unionbankph.corporate.common.presentation.constant.NotificationLogTypeEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.dao.presentation.DaoActivity
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class DeepLinkLandingActivity : AppCompatActivity() {

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var settingsUtil: SettingsUtil

    val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        initIntent()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        disposables.dispose()
    }

    private fun initIntent() {
        val intent = intent
        val action = intent.action
        val data = intent.data
        if (action != null && data != null) {
            if (data.toString().contains("view-transaction")) {
                Timber.d("data.toString(): $data")
                val id = data.getQueryParameter("id")
                val roleId = data.getQueryParameter("role")
                val channel = data.getQueryParameter("channel")
                val code = if (channel.equals("mobile-check-deposit")) {
                    NotificationLogTypeEnum.MOBILE_CHECK_DEPOSIT.name
                } else {
                    NotificationLogTypeEnum.TRANSACTION_STATUS_CHANGED.name
                }
                val pushNotificationPayload = PushNotificationPayload(
                    id = id,
                    channel = channel,
                    roleId = roleId,
                    code = code
                )
                val bundle = Bundle().apply {
                    putString(
                        AutobahnFirebaseMessagingService.EXTRA_DATA,
                        JsonHelper.toJson(pushNotificationPayload)
                    )
                }
                if (settingsUtil.isLoggedIn()) {
                    navigateDashboard(bundle)
                } else {
                    navigateLogin(bundle)
                }
            } else if (data.toString().contains("accountopening")) {
                val token = data.getQueryParameter("token")
                val bundle = Bundle().apply {
                    putString(DaoActivity.EXTRA_TOKEN_DEEP_LINK, token)
                }
                navigateDaoScreen(bundle)
            }
        }
    }

    private fun navigateDashboard(bundle: Bundle) {
        navigator.navigate(
            this,
            DashboardActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    private fun navigateLogin(bundle: Bundle) {
        navigator.navigate(
            this,
            LoginActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = false
        )
    }

    private fun navigateDaoScreen(bundle: Bundle) {
        navigator.navigate(
            this,
            DaoActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = false
        )
    }

}
