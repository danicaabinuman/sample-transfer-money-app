package com.unionbankph.corporate.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.auth.data.AuthGateway
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by herald25santos on 05/09/2018.
 */

class ForceLogoutReceiver : BroadcastReceiver() {

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var disposables: CompositeDisposable

    @Inject
    lateinit var authGateway: AuthGateway

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        val action = intent.action
        if (ACTION_FORCE_LOGOUT == action) {
            forceLogout(context)
        }
    }

    private fun forceLogout(context: Context) {
        authGateway.clearCredential()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Timber.d("clearCredential")
                    navigator.navigateClearStacks(
                        context,
                        LoginActivity::class.java,
                        Bundle().apply {
                            putBoolean(LoginActivity.EXTRA_SPLASH_SCREEN, false)
                        }
                    )
                }, {
                    Timber.e(it, "logoutOnBackground failed")
                }
            ).addTo(disposables)
    }

    companion object {
        const val ACTION_FORCE_LOGOUT = "action_force_logout"
    }
}
