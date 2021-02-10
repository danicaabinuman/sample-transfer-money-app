package com.unionbankph.corporate.approval.presentation.approval_activity_log

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.Typed2EpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.notEmpty
import com.unionbankph.corporate.app.common.extension.setContextCompatTextColor
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.fund_transfer.data.model.ActivityLogDto
import kotlinx.android.synthetic.main.header_activity_log.view.*
import kotlinx.android.synthetic.main.item_activity_log.view.*

class ActivityLogController
constructor(
    private val context: Context,
    private val callbacks: AdapterCallbacks,
    private val viewUtil: ViewUtil
) : Typed2EpoxyController<MutableList<ActivityLogDto>, MutableList<ActivityLogDto>>() {

    interface AdapterCallbacks {
        fun onClickItem(activityLog: ActivityLogDto)
    }

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(
        activityLogsHeader: MutableList<ActivityLogDto>,
        activityLogs: MutableList<ActivityLogDto>
    ) {

        var contentSize = 0
        var isStart: Boolean

        activityLogsHeader.forEachIndexed { index, recordByDate ->
            ActivityLogHeaderModel_()
                .id("date_header_" + recordByDate.createdDate)
                .position(index)
                .activityLogDto(recordByDate)
                .size(activityLogsHeader.size)
                .viewUtil(viewUtil)
                .addTo(this)
            contentSize++
            activityLogs.filter {
                viewUtil.getDateFormatByDateString(
                    it.createdDate,
                    ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
                    ViewUtil.DATE_FORMAT_WITHOUT_TIME
                ) in viewUtil.getDateFormatByDateString(
                    recordByDate.createdDate,
                    ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
                    ViewUtil.DATE_FORMAT_WITHOUT_TIME
                )
            }.forEachIndexed { position, activityLogDto ->
                isStart = position == 0 && index == 0
                contentSize++
                ActivityLogItemModel_()
                    .id(activityLogDto.id)
                    .activityLogDto(activityLogDto)
                    .hasStart(isStart)
                    .hasEnd((activityLogs.size + activityLogsHeader.size) == contentSize)
                    .viewUtil(viewUtil)
                    .callbacks(callbacks)
                    .context(context)
                    .addTo(this)
            }
        }
    }


    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }
}

@EpoxyModelClass(layout = R.layout.header_activity_log)
abstract class ActivityLogHeaderModel : EpoxyModelWithHolder<ActivityLogHeaderModel.Holder>() {

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var activityLogDto: ActivityLogDto

    @EpoxyAttribute
    var position: Int = 0

    @EpoxyAttribute
    var size: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.textViewTitle.text = viewUtil.getStringOrEmpty(
            viewUtil.getDateFormatByDateString(
                activityLogDto.createdDate,
                ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
                ViewUtil.DATE_FORMAT_WITHOUT_TIME
            )
        )
        if (position == 0) holder.viewBorder.visibility = View.INVISIBLE
    }

    class Holder : EpoxyHolder() {
        lateinit var textViewTitle: TextView
        lateinit var viewBorder: View

        override fun bindView(itemView: View) {
            textViewTitle = itemView.textViewTitle
            viewBorder = itemView.viewBorder
        }
    }

}

@EpoxyModelClass(layout = R.layout.item_activity_log)
abstract class ActivityLogItemModel : EpoxyModelWithHolder<ActivityLogItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var activityLogDto: ActivityLogDto

    @EpoxyAttribute
    var hasStart: Boolean = false

    @EpoxyAttribute
    var hasEnd: Boolean = false

    @EpoxyAttribute
    lateinit var callbacks: ActivityLogController.AdapterCallbacks

    override fun bind(holder: Holder) {
        super.bind(holder)

        if (hasStart) holder.viewBorderItem1.visibility =
            View.INVISIBLE else holder.viewBorderItem1.visibility = View.VISIBLE

        if (hasEnd) {
            holder.viewBorderItem2.visibility = View.INVISIBLE
        }
        holder.textViewTime.text = viewUtil.getStringOrEmpty(
            viewUtil.getDateFormatByDateString(
                activityLogDto.createdDate,
                ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
                ViewUtil.DATE_FORMAT_TIME
            )
        )
        if (activityLogDto.status != null) {
            holder.textViewRemarks.setContextCompatTextColor(
                ConstantHelper.Color.getTextColor(activityLogDto.status)
            )
            holder.imageViewPresence.setImageResource(
                ConstantHelper.Drawable.getActivityLogBadge(
                    activityLogDto.status
                )
            )
            holder.textViewRemarks.text =
                viewUtil.getStringOrEmpty(activityLogDto.status?.description)
        } else {
            holder.textViewRemarks.text =
                viewUtil.getStringOrEmpty(activityLogDto.description)
        }

        holder.textViewNoteTitle.setContextCompatTextColor(
            ConstantHelper.Color.getTextColor(activityLogDto.status)
        )
        holder.textViewNote.setContextCompatTextColor(
            ConstantHelper.Color.getTextColor(activityLogDto.status)
        )

        when (activityLogDto.status?.type) {
            Constant.STATUS_REJECTED,
            Constant.STATUS_FAILED,
            Constant.STATUS_CANCELLED,
            Constant.STATUS_REVERSAL_FAILED,
            Constant.STATUS_REVERSED -> {
                holder.textViewNote.visibility(true)
                holder.textViewNote.text = activityLogDto.status?.remarks?.replace("|", "\n").notEmpty()
            }
            Constant.STATUS_RELEASED,
            Constant.STATUS_SUCCESSFUL,
            Constant.STATUS_APPROVED,
            Constant.STATUS_REMOVED -> {
                holder.textViewNote.visibility(activityLogDto.status?.remarks != null)
                holder.textViewNoteTitle.text = activityLogDto.status?.description.notEmpty()
                holder.textViewNote.text = activityLogDto.status?.remarks?.replace("|", "\n").notEmpty()
            }
            else -> {
                holder.textViewNote.visibility(false)
            }
        }

        holder.constraintLayoutItemRecent.setOnClickListener {
            callbacks.onClickItem(activityLogDto)
        }

    }

    class Holder : EpoxyHolder() {
        lateinit var constraintLayoutItemRecent: ConstraintLayout
        lateinit var textViewTime: TextView
        lateinit var textViewRemarks: TextView
        lateinit var textViewNoteTitle: TextView
        lateinit var textViewNote: TextView
        lateinit var imageViewPresence: ImageView
        lateinit var viewBorderItem1: View
        lateinit var viewBorderItem2: View

        override fun bindView(itemView: View) {
            constraintLayoutItemRecent = itemView.constraintLayoutItemRecent
            textViewTime = itemView.textViewTime
            textViewRemarks = itemView.textViewRemarks
            textViewNoteTitle = itemView.textViewNoteTitle
            textViewNote = itemView.textViewNote
            imageViewPresence = itemView.imageViewPresence
            viewBorderItem1 = itemView.viewBorderItem1
            viewBorderItem2 = itemView.viewBorderItem2
        }
    }
}