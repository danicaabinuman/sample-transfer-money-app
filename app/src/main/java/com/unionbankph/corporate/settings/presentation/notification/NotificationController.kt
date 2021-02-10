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
import com.unionbankph.corporate.notification.data.model.Notification
import com.unionbankph.corporate.notification.data.model.NotificationDto
import kotlinx.android.synthetic.main.header_switch.view.*
import kotlinx.android.synthetic.main.item_notification.view.*

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
                callbacks(callbacks)
                context(context)
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
        holder.textViewSwitch.text = title
        holder.switchCompat.tag = NotificationController::class.java.simpleName
        holder.switchCompat.isChecked = notificationDto?.receiveAllNotifications ?: false
        holder.switchCompat.setOnCheckedChangeListener { buttonView, isChecked ->
            if (holder.switchCompat.tag != null) {
                holder.switchCompat.tag = null
                return@setOnCheckedChangeListener
            }
            callbacks.onSlideSwitchCompat(holder.switchCompat, isChecked)
        }
        holder.switchCompat.setOnTouchListener { view, motionEvent ->
            holder.switchCompat.tag = null
            return@setOnTouchListener false
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var textViewSwitch: TextView
        lateinit var switchCompat: SwitchMaterial

        override fun bindView(itemView: View) {
            textViewSwitch = itemView.textViewSwitch
            switchCompat = itemView.switchCompat
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
            holder.viewBorder1.visibility = View.VISIBLE
        } else {
            holder.viewBorder1.visibility = View.GONE
        }
        if (receiveAllNotifications == true) {
            holder.constraintLayoutItemNotification.isEnabled = true
            holder.constraintLayoutItemNotification.alpha = 1f
        } else {
            holder.constraintLayoutItemNotification.isEnabled = false
            holder.constraintLayoutItemNotification.alpha = 0.5f
        }

        holder.textViewNotificationTitle.text =
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
        holder.textViewNotification.text = if (stringBuilder.toString().isEmpty() ||
            notification?.receiveNotifications == false) {
            context?.getString(R.string.title_no_notification)
        } else {
            stringBuilder.toString()
        }

        holder.constraintLayoutItemNotification.setOnClickListener {
            callbacks.onClickItem(notification?.notificationId)
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var constraintLayoutItemNotification: ConstraintLayout
        lateinit var viewBorder1: View
        lateinit var textViewNotificationTitle: TextView
        lateinit var textViewNotification: TextView
        override fun bindView(itemView: View) {
            constraintLayoutItemNotification = itemView.constraintLayoutItemNotification
            viewBorder1 = itemView.viewBorder1
            textViewNotificationTitle = itemView.textViewNotificationTitle
            textViewNotification = itemView.textViewNotification
        }
    }
}
