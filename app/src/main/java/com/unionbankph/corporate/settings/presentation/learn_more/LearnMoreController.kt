package com.unionbankph.corporate.settings.presentation.learn_more

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.TypedEpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.makeLinks
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import kotlinx.android.synthetic.main.item_learn_more.view.*

class LearnMoreController : TypedEpoxyController<MutableList<LearnMoreData>>() {

    private lateinit var callbacks: EpoxyAdapterCallback<LearnMoreData>

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(data: MutableList<LearnMoreData>) {
        data.forEachIndexed { index, learnMoreData ->
            learnMoreItem {
                id(learnMoreData.id)
                learnMoreData(learnMoreData)
                position(index)
                callbacks(callbacks)
            }
        }
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    fun setAdapterCallbacks(callbacks: EpoxyAdapterCallback<LearnMoreData>) {
        this.callbacks = callbacks
    }
}

@EpoxyModelClass(layout = R.layout.item_learn_more)
abstract class LearnMoreItemModel : EpoxyModelWithHolder<LearnMoreItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var learnMoreData: LearnMoreData

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<LearnMoreData>

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        val context = holder.clItem.context
        holder.tvTitle.text = learnMoreData.title
        holder.tvDesc.text = learnMoreData.desc
        if (learnMoreData.desc.contains(context.getString(R.string.action_here))) {
            holder.tvDesc.makeLinks(
                Pair(context.formatString(R.string.action_here), View.OnClickListener {
                    callbacks.onClickItem(it, learnMoreData, position)
                })
            )
        }
        holder.clItem.setOnClickListener {
            callbacks.onClickItem(it, learnMoreData, position)
        }
        holder.tvDesc.setOnClickListener {
            callbacks.onClickItem(it, learnMoreData, position)
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var clItem: ConstraintLayout
        lateinit var tvTitle: TextView
        lateinit var tvDesc: TextView

        override fun bindView(itemView: View) {
            clItem = itemView.cl_item
            tvTitle = itemView.tv_title
            tvDesc = itemView.tv_desc
        }
    }
}
