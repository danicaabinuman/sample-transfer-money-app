package com.unionbankph.corporate.notification.presentation.notification_log.notification_log_list

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.airbnb.epoxy.AutoModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.Typed2EpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.setContextCompatTextColor
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.StateData
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.NotificationLogTypeEnum
import com.unionbankph.corporate.databinding.ItemNotificationLogBinding
import com.unionbankph.corporate.notification.data.model.NotificationLogDto

class NotificationLogController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil
) : Typed2EpoxyController<MutableList<StateData<NotificationLogDto>>, Pageable>() {

    private lateinit var callbacks: EpoxyAdapterCallback<NotificationLogDto>

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(
        data: MutableList<StateData<NotificationLogDto>>,
        pageable: Pageable
    ) {
        data.forEachIndexed { index, stateData ->
            notificationLogItem {
                id(stateData.data.id)
                context(context)
                viewUtil(viewUtil)
                hasRead(stateData.state)
                notificationLogDto(stateData.data)
                callbacks(callbacks)
                position(index)
            }
        }
        loadingFooterModel.loading(pageable.isLoadingPagination)
            .addIf(pageable.isLoadingPagination, this)
        errorFooterModel.title(pageable.errorMessage)
            .callbacks(callbacks)
            .addIf(pageable.isFailed, this)
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    fun setAdapterCallbacks(callbacks: EpoxyAdapterCallback<NotificationLogDto>) {
        this.callbacks = callbacks
    }
}

@EpoxyModelClass(layout = R.layout.item_notification_log)
abstract class NotificationLogItemModel : EpoxyModelWithHolder<NotificationLogItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var notificationLogDto: NotificationLogDto

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<NotificationLogDto>

    @EpoxyAttribute
    var hasRead: Boolean = false

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.apply {
            viewBorderTop.visibility(position == 0)
            textViewNotificationLogTitle.text = notificationLogDto.title
            textViewNotificationLogDesc.text = notificationLogDto.message
            textViewNotificationLogCreatedDate.text =
                viewUtil.getDateTimeAgoByCreatedDate(notificationLogDto.createdDate)
            textViewNotificationLogTitle.setContextCompatTextColor(
                if (!hasRead) {
                    R.color.colorRecent
                } else {
                    R.color.colorInfo
                }
            )
            when (notificationLogDto.notificationLogType) {
                NotificationLogTypeEnum.ANNOUNCEMENT -> {
                    imageViewNotificationLog.setImageResource(R.drawable.ic_notification_announcement)
                }
                NotificationLogTypeEnum.NEW_DEVICE_LOGIN -> {
                    imageViewNotificationLog.setImageResource(R.drawable.ic_notification_new_device)
                }
                NotificationLogTypeEnum.FILE_UPLOADED -> {
                    imageViewNotificationLog.setImageResource(R.drawable.ic_notification_file_uploaded)
                }
                NotificationLogTypeEnum.NEW_FEATURES -> {
                    imageViewNotificationLog.setImageResource(R.drawable.ic_notification_new_feature)
                }
                else -> {
                    imageViewNotificationLog.setImageResource(R.drawable.ic_notification_announcement)
                }
            }
            viewReadStatus.visibility(!hasRead)
            root.setOnClickListener {
                callbacks.onClickItem(
                    root,
                    notificationLogDto,
                    position
                )
            }
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding : ItemNotificationLogBinding

        lateinit var imageViewNotificationLog: AppCompatImageView
        lateinit var textViewNotificationLogTitle: TextView
        lateinit var textViewNotificationLogDesc: TextView
        lateinit var textViewNotificationLogCreatedDate: TextView
        lateinit var viewBorderTop: View
        lateinit var viewReadStatus: View
        lateinit var itemView: View

        override fun bindView(itemView: View) {
            binding = ItemNotificationLogBinding.bind(itemView)
        }
    }
}
