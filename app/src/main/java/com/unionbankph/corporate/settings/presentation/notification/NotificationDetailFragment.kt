package com.unionbankph.corporate.settings.presentation.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.unionbankph.corporate.databinding.FragmentNotificationDetailBinding
import com.unionbankph.corporate.notification.data.form.NotificationForm
import com.unionbankph.corporate.notification.data.model.Notification
import io.reactivex.rxkotlin.addTo

class NotificationDetailFragment :
    BaseFragment<FragmentNotificationDetailBinding, NotificationViewModel>() {

    private lateinit var notification: Notification

    private var currentSwitch: SwitchMaterial? = null

    override fun onViewsBound() {
        super.onViewsBound()
        (activity as DashboardActivity).btnHelp().visibility = View.GONE
        (activity as DashboardActivity).setToolbarTitle(
            getString(R.string.title_notifications),
            hasBackButton = true,
            hasMenuItem = false
        )
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowNotificationLoading -> {
                    binding.viewLoadingState.viewLoadingLayout.visibility(true)
                    binding.constraintLayout.visibility(false)
                }
                is ShowNotificationDismissLoading -> {
                    binding.viewLoadingState.viewLoadingLayout.visibility(false)
                }
                is ShowNotificationLoadingSubmit -> {
                    showProgressAlertDialog(NotificationDetailFragment::class.java.simpleName)
                }
                is ShowNotificationDismissLoadingSubmit -> {
                    dismissProgressAlertDialog()
                }
                is ShowNotificationDetailData -> {
                    binding.constraintLayout.visibility(true)
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

        binding.switchCompat.setOnCheckedChangeListener { buttonView, isChecked ->
            if (binding.switchCompat.tag != null) {
                binding. switchCompat.tag = null
                return@setOnCheckedChangeListener
            }
            currentSwitch = binding.switchCompat
            notification.receiveNotifications = isChecked
            viewModel.updateNotificationSettings(setNotificationForm())
        }

        binding.switchCompatPush.setOnCheckedChangeListener { buttonView, isChecked ->
            if (binding.switchCompatPush.tag != null) {
                binding.switchCompatPush.tag = null
                return@setOnCheckedChangeListener
            }
            currentSwitch = binding.switchCompatPush
            notification.receivePush = isChecked
            viewModel.updateNotificationSettings(setNotificationForm())
        }

        binding.switchCompatSMS.setOnCheckedChangeListener { buttonView, isChecked ->
            if (binding.switchCompatSMS.tag != null) {
                binding.switchCompatSMS.tag = null
                return@setOnCheckedChangeListener
            }
            currentSwitch = binding.switchCompatSMS
            notification.receiveSms = isChecked
            viewModel.updateNotificationSettings(setNotificationForm())
        }

        binding.switchCompatEmail.setOnCheckedChangeListener { buttonView, isChecked ->
            if (binding.switchCompatEmail.tag != null) {
                binding.switchCompatEmail.tag = null
                return@setOnCheckedChangeListener
            }
            currentSwitch = binding.switchCompatEmail
            notification.receiveEmail = isChecked
            viewModel.updateNotificationSettings(setNotificationForm())
        }

        binding.switchCompat.setOnTouchListener { view, motionEvent ->
            binding.switchCompat.tag = null
            return@setOnTouchListener false
        }

        binding.switchCompatPush.setOnTouchListener { view, motionEvent ->
            binding.switchCompatPush.tag = null
            return@setOnTouchListener false
        }

        binding.switchCompatSMS.setOnTouchListener { view, motionEvent ->
            binding.switchCompatSMS.tag = null
            return@setOnTouchListener false
        }

        binding.switchCompatEmail.setOnTouchListener { view, motionEvent ->
            binding.switchCompatEmail.tag = null
            return@setOnTouchListener false
        }
    }

    private fun revertSwitch() {
        when (currentSwitch) {
            binding.switchCompat -> {
                binding.switchCompat.tag = NotificationDetailFragment::class.java.simpleName
                binding.switchCompat.isChecked = !binding.switchCompat.isChecked
            }
            binding.switchCompatPush -> {
                binding.switchCompatPush.tag = NotificationDetailFragment::class.java.simpleName
                binding.switchCompatPush.isChecked = !binding.switchCompatPush.isChecked
            }
            binding.switchCompatSMS -> {
                binding.switchCompatSMS.tag = NotificationDetailFragment::class.java.simpleName
                binding.switchCompatSMS.isChecked = !binding.switchCompatSMS.isChecked
            }
            binding.switchCompatEmail -> {
                binding.switchCompatEmail.tag = NotificationDetailFragment::class.java.simpleName
                binding.switchCompatEmail.isChecked = !binding.switchCompatEmail.isChecked
            }
        }
    }

    private fun setSwitchCompatData(data: Notification) {
        notification = data
        binding.switchCompat.tag = NotificationDetailFragment::class.java.simpleName
        binding.switchCompatPush.tag = NotificationDetailFragment::class.java.simpleName
        binding.switchCompatSMS.tag = NotificationDetailFragment::class.java.simpleName
        binding.switchCompatEmail.tag = NotificationDetailFragment::class.java.simpleName
        binding.textViewSwitch.text = when (notification.notificationId) {
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
        binding.constraintLayoutPush.setEnableView(notification.receiveNotifications)
        binding.constraintLayoutSMS.setEnableView(notification.receiveNotifications)
        binding.constraintLayoutEmail.setEnableView(notification.receiveNotifications)
        binding.switchCompat.isChecked = notification.receiveNotifications
        binding.switchCompatPush.isEnabled = notification.receiveNotifications
        binding.switchCompatPush.isChecked = notification.receivePush
        binding.switchCompatSMS.isEnabled = notification.receiveNotifications
        binding.switchCompatSMS.isChecked = notification.receiveSms
        binding.switchCompatEmail.isEnabled = notification.receiveNotifications
        binding.switchCompatEmail.isChecked = notification.receiveEmail
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

    override val viewModelClassType: Class<NotificationViewModel>
        get() = NotificationViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentNotificationDetailBinding
        get() = FragmentNotificationDetailBinding::inflate
}
