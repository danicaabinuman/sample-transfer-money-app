package com.unionbankph.corporate.settings.presentation.notification

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.Switch
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.AutoModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.TypedEpoxyController
import com.google.android.material.switchmaterial.SwitchMaterial
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.HeaderTitleModel_
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.databinding.HeaderSwitchBinding
import com.unionbankph.corporate.databinding.ItemNotificationBinding
import com.unionbankph.corporate.notification.data.model.Notification
import com.unionbankph.corporate.notification.data.model.NotificationDto

class NotificationController
constructor(
    private val context: Context
) : TypedEpoxyController<NotificationDto>() {

    @AutoModel
    lateinit var headerTitleModel: HeaderTitleModel_

    @AutoModel
    lateinit var notificationHeaderModel: NotificationHeaderModel_

    private lateinit var callbacks: AdapterCallbacks

    interface AdapterCallbacks {
        fun onClickItem(id: Int?)
        fun onSlideSwitchCompat(switch: SwitchMaterial, isChecked: Boolean)
    }

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(
        notificationDto: NotificationDto
    ) {

        notificationHeaderModel
            .title(context.getString(R.string.title_receive_notifications))
            .notificationDto(notificationDto)
            .callbacks(callbacks)
            .addTo(this)

        headerTitleModel
            .title(context.getString(R.string.title_send_me_notification))
            .addTo(this)

        notificationDto.notifications.forEachIndexed { index, notification ->
            notificationItem {
                id(notification.notificationId)
                hasFirst(index == 0)
                notification(notification)
                receiveAllNotifications(notificationDto.receiveAllNotifications)
                callbacks(this@NotificationController.callbacks)
                context(this@NotificationController.context)
            }
        }
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    fun setAdapterCallbacks(callbacks: AdapterCallbacks) {
        this.callbacks = callbacks
    }
}

@EpoxyModelClass(layout = R.layout.header_switch)
abstract class NotificationHeaderModel : EpoxyModelWithHolder<NotificationHeaderModel.Holder>() {

    @EpoxyAttribute
    var title: String? = null

    @EpoxyAttribute
    var notificationDto: NotificationDto? = null

    @EpoxyAttribute
    lateinit var callbacks: NotificationController.AdapterCallbacks

    @SuppressLint("ClickableViewAccessibility")
    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.textViewSwitch.text = title
        holder.binding.switchCompat.tag = NotificationController::class.java.simpleName
        holder.binding.switchCompat.isChecked = notificationDto?.receiveAllNotifications ?: false
        holder.binding.switchCompat.setOnCheckedChangeListener { buttonView, isChecked ->
            if (holder.binding.switchCompat.tag != null) {
                holder.binding.switchCompat.tag = null
                return@setOnCheckedChangeListener
            }
            callbacks.onSlideSwitchCompat(holder.binding.switchCompat, isChecked)
        }
        holder.binding.switchCompat.setOnTouchListener { view, motionEvent ->
            holder.binding.switchCompat.tag = null
            return@setOnTouchListener false
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding : HeaderSwitchBinding

        override fun bindView(itemView: View) {
            binding = HeaderSwitchBinding.bind(itemView)
        }
    }
}

@EpoxyModelClass(layout = R.layout.item_notification)
abstract class NotificationItemModel : EpoxyModelWithHolder<NotificationItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var callbacks: NotificationController.AdapterCallbacks

    @EpoxyAttribute
    var context: Context? = null

    @EpoxyAttribute
    var hasFirst: Boolean? = null

    @EpoxyAttribute
    var notification: Notification? = null

    @EpoxyAttribute
    var receiveAllNotifications: Boolean? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        if (hasFirst == true) {
            holder.binding.viewBorder1.visibility = View.VISIBLE
        } else {
            holder.binding.viewBorder1.visibility = View.GONE
        }
        if (receiveAllNotifications == true) {
            holder.binding.constraintLayoutItemNotification.isEnabled = true
            holder.binding.constraintLayoutItemNotification.alpha = 1f
        } else {
            holder.binding.constraintLayoutItemNotification.isEnabled = false
            holder.binding.constraintLayoutItemNotification.alpha = 0.5f
        }

        holder.binding.textViewNotificationTitle.text =
            when (notification?.notificationId) {
                NotificationLogCodeEnum.CREATE_NOTIFICATION.value -> {
                    context?.getString(R.string.title_notification_1001)
                }
                NotificationLogCodeEnum.CREATE_CHANGES_NOTIFICATION.value -> {
                    context?.getString(R.string.title_notification_1002)
                }
                NotificationLogCodeEnum.PENDING_NOTIFICATION.value -> {
                    context?.getString(R.string.title_notification_1003)
                }
                NotificationLogCodeEnum.APPROVED_NOTIFICATION.value -> {
                    context?.getString(R.string.title_notification_1004)
                }
                NotificationLogCodeEnum.CHANGES_ORGANIZATION.value -> {
                    context?.getString(R.string.title_notification_1005)
                }
                else -> {
                    context?.getString(R.string.title_notification_1001)
                }
            }

        val stringBuilder = StringBuilder()
        if (notification?.receivePush!!) {
            stringBuilder.append("Push")
        }
        if (notification?.receiveEmail!!) {
            if (stringBuilder.toString().isEmpty()) {
                stringBuilder.append("Email")
            } else {
                stringBuilder.append(", Email")
            }
        }
        holder.binding.textViewNotification.text = if (stringBuilder.toString().isEmpty() ||
            notification?.receiveNotifications == false) {
            context?.getString(R.string.title_no_notification)
        } else {
            stringBuilder.toString()
        }

        holder.binding.constraintLayoutItemNotification.setOnClickListener {
            callbacks.onClickItem(notification?.notificationId)
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding : ItemNotificationBinding

        override fun bindView(itemView: View) {
            binding = ItemNotificationBinding.bind(itemView)
        }
    }
}
