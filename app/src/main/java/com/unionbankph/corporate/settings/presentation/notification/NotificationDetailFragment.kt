package com.unionbankph.corporate.settings.presentation.notification

import android.view.View
import android.widget.Switch
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.switchmaterial.SwitchMaterial
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.setEnableView
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.notification.data.form.NotificationForm
import com.unionbankph.corporate.notification.data.model.Notification
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_notification_detail.*

class NotificationDetailFragment :
    BaseFragment<NotificationViewModel>(R.layout.fragment_notification_detail) {

    private lateinit var notification: Notification

    private var currentSwitch: SwitchMaterial? = null

    override fun onViewsBound() {
        super.onViewsBound()
        (activity as DashboardActivity).imageViewHelp().visibility = View.GONE
        (activity as DashboardActivity).setToolbarTitle(
            getString(R.string.title_notifications),
            hasBackButton = true,
            hasMenuItem = false
        )
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[NotificationViewModel::class.java]

        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowNotificationLoading -> {
                    viewLoadingState.visibility(true)
                    constraintLayout.visibility(false)
                }
                is ShowNotificationDismissLoading -> {
                    viewLoadingState.visibility(false)
                }
                is ShowNotificationLoadingSubmit -> {
                    showProgressAlertDialog(NotificationDetailFragment::class.java.simpleName)
                }
                is ShowNotificationDismissLoadingSubmit -> {
                    dismissProgressAlertDialog()
                }
                is ShowNotificationDetailData -> {
                    constraintLayout.visibility(true)
                    setSwitchCompatData(it.data)
                }
                is ShowNotificationData -> {
                    notification = it.data.notifications.find {
                        it.notificationId == notification.notificationId
                    } ?: notification
                    setSwitchCompatData(notification)
                }
                is ShowNotificationError -> {
                    revertSwitch()
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.getNotification(arguments?.getString(EXTRA_ID)!!)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()

        eventBus.resultSyncEvent.flowable.subscribe {
            //            if (it.eventType == ResultSyncEvent.ACTION_SUCCESS_SECURITY) {
//                setOTPSettingsFormView(it.payload)
//            } else if (it.eventType == ResultSyncEvent.ACTION_BACK_SECURITY) {
//                Timber.d("it.payload: " + it.payload)
//                switchLoginAuth.tag = NotificationDetailFragment::class.java.simpleName
//                switchTransactionAuth.tag = NotificationDetailFragment::class.java.simpleName
//                setOTPSettingsFormView(it.payload)
//            }
        }.addTo(disposables)

        switchCompat.setOnCheckedChangeListener { buttonView, isChecked ->
            if (switchCompat.tag != null) {
                switchCompat.tag = null
                return@setOnCheckedChangeListener
            }
            currentSwitch = switchCompat
            notification.receiveNotifications = isChecked
            viewModel.updateNotificationSettings(setNotificationForm())
        }

        switchCompatPush.setOnCheckedChangeListener { buttonView, isChecked ->
            if (switchCompatPush.tag != null) {
                switchCompatPush.tag = null
                return@setOnCheckedChangeListener
            }
            currentSwitch = switchCompatPush
            notification.receivePush = isChecked
            viewModel.updateNotificationSettings(setNotificationForm())
        }

        switchCompatSMS.setOnCheckedChangeListener { buttonView, isChecked ->
            if (switchCompatSMS.tag != null) {
                switchCompatSMS.tag = null
                return@setOnCheckedChangeListener
            }
            currentSwitch = switchCompatSMS
            notification.receiveSms = isChecked
            viewModel.updateNotificationSettings(setNotificationForm())
        }

        switchCompatEmail.setOnCheckedChangeListener { buttonView, isChecked ->
            if (switchCompatEmail.tag != null) {
                switchCompatEmail.tag = null
                return@setOnCheckedChangeListener
            }
            currentSwitch = switchCompatEmail
            notification.receiveEmail = isChecked
            viewModel.updateNotificationSettings(setNotificationForm())
        }

        switchCompat.setOnTouchListener { view, motionEvent ->
            switchCompat.tag = null
            return@setOnTouchListener false
        }

        switchCompatPush.setOnTouchListener { view, motionEvent ->
            switchCompatPush.tag = null
            return@setOnTouchListener false
        }

        switchCompatSMS.setOnTouchListener { view, motionEvent ->
            switchCompatSMS.tag = null
            return@setOnTouchListener false
        }

        switchCompatEmail.setOnTouchListener { view, motionEvent ->
            switchCompatEmail.tag = null
            return@setOnTouchListener false
        }
    }

    private fun revertSwitch() {
        when (currentSwitch) {
            switchCompat -> {
                switchCompat.tag = NotificationDetailFragment::class.java.simpleName
                switchCompat.isChecked = !switchCompat.isChecked
            }
            switchCompatPush -> {
                switchCompatPush.tag = NotificationDetailFragment::class.java.simpleName
                switchCompatPush.isChecked = !switchCompatPush.isChecked
            }
            switchCompatSMS -> {
                switchCompatSMS.tag = NotificationDetailFragment::class.java.simpleName
                switchCompatSMS.isChecked = !switchCompatSMS.isChecked
            }
            switchCompatEmail -> {
                switchCompatEmail.tag = NotificationDetailFragment::class.java.simpleName
                switchCompatEmail.isChecked = !switchCompatEmail.isChecked
            }
        }
    }

    private fun setSwitchCompatData(data: Notification) {
        notification = data
        switchCompat.tag = NotificationDetailFragment::class.java.simpleName
        switchCompatPush.tag = NotificationDetailFragment::class.java.simpleName
        switchCompatSMS.tag = NotificationDetailFragment::class.java.simpleName
        switchCompatEmail.tag = NotificationDetailFragment::class.java.simpleName
        textViewSwitch.text = when (notification.notificationId) {
            NotificationLogCodeEnum.CREATE_NOTIFICATION.value -> {
                formatString(R.string.desc_notification_1001)
            }
            NotificationLogCodeEnum.CREATE_CHANGES_NOTIFICATION.value -> {
                formatString(R.string.desc_notification_1002)
            }
            NotificationLogCodeEnum.PENDING_NOTIFICATION.value -> {
                formatString(R.string.desc_notification_1003)
            }
            NotificationLogCodeEnum.APPROVED_NOTIFICATION.value -> {
                formatString(R.string.desc_notification_1004)
            }
            NotificationLogCodeEnum.CHANGES_ORGANIZATION.value -> {
                formatString(R.string.desc_notification_1005)
            }
            else -> {
                formatString(R.string.desc_notification_1001)
            }
        }
        constraintLayoutPush.setEnableView(notification.receiveNotifications)
        constraintLayoutSMS.setEnableView(notification.receiveNotifications)
        constraintLayoutEmail.setEnableView(notification.receiveNotifications)
        switchCompat.isChecked = notification.receiveNotifications
        switchCompatPush.isEnabled = notification.receiveNotifications
        switchCompatPush.isChecked = notification.receivePush
        switchCompatSMS.isEnabled = notification.receiveNotifications
        switchCompatSMS.isChecked = notification.receiveSms
        switchCompatEmail.isEnabled = notification.receiveNotifications
        switchCompatEmail.isChecked = notification.receiveEmail
    }

    private fun setNotificationForm(): NotificationForm {
        return NotificationForm(
            mutableListOf(notification),
            true
        )
    }

    companion object {
        const val EXTRA_ID = "id"
    }
}
