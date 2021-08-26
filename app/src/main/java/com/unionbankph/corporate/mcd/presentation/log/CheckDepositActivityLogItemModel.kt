package com.unionbankph.corporate.mcd.presentation.log

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.notEmpty
import com.unionbankph.corporate.app.common.extension.setContextCompatTextColor
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.databinding.ItemActivityLogBinding
import com.unionbankph.corporate.mcd.data.model.CheckDepositActivityLog

@EpoxyModelClass(layout = R.layout.item_activity_log)
abstract class CheckDepositActivityLogItemModel :
    EpoxyModelWithHolder<CheckDepositActivityLogItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var checkDepositActivityLog: CheckDepositActivityLog

    @EpoxyAttribute
    var hasStart: Boolean = false

    @EpoxyAttribute
    var hasEnd: Boolean = false

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)

        if (hasStart) holder.viewBorderItem1.visibility =
            View.INVISIBLE else holder.viewBorderItem1.visibility = View.VISIBLE

        if (hasEnd) {
            holder.viewBorderItem2.visibility = View.INVISIBLE
        }
        holder.textViewTime.text = viewUtil.getStringOrEmpty(
            viewUtil.getDateFormatByDateString(
                checkDepositActivityLog.createdDate,
                ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
                ViewUtil.DATE_FORMAT_TIME
            )
        )
        checkDepositActivityLog.status?.let {
            holder.textViewRemarks.setContextCompatTextColor(
                ConstantHelper.Color.getTextColor(checkDepositActivityLog.status)
            )
            holder.imageViewPresence.setImageResource(
                ConstantHelper.Drawable.getActivityLogBadge(
                    checkDepositActivityLog.status
                )
            )
            holder.textViewRemarks.text =
                viewUtil.getStringOrEmpty(checkDepositActivityLog.status?.description)
        }

        holder.textViewNoteTitle.setContextCompatTextColor(
            ConstantHelper.Color.getTextColor(checkDepositActivityLog.status)
        )
        holder.textViewNote.setContextCompatTextColor(
            ConstantHelper.Color.getTextColor(checkDepositActivityLog.status)
        )

        when (checkDepositActivityLog.status?.type) {
            "APPROVED",
            "FAILED",
            "REJECTED" -> {
                holder.textViewNote.visibility(true)
                holder.textViewNote.text = checkDepositActivityLog.status?.remarks.notEmpty()
            }
            else -> {
                holder.textViewNoteTitle.visibility(false)
                holder.textViewNote.visibility(false)
            }
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding : ItemActivityLogBinding

        lateinit var constraintLayoutItemRecent: ConstraintLayout
        lateinit var textViewTime: TextView
        lateinit var textViewRemarks: TextView
        lateinit var textViewNoteTitle: TextView
        lateinit var textViewNote: TextView
        lateinit var imageViewPresence: ImageView
        lateinit var viewBorderItem1: View
        lateinit var viewBorderItem2: View

        override fun bindView(itemView: View) {
            binding = ItemActivityLogBinding.bind(itemView)
        }
    }
}
