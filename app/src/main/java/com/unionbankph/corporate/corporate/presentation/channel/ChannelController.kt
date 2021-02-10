package com.unionbankph.corporate.corporate.presentation.channel

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.TypedEpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.supportBullets
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.headerTitle
import com.unionbankph.corporate.common.data.model.SectionedData
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.corporate.data.model.Channel
import kotlinx.android.synthetic.main.item_channel.view.*


class ChannelController
constructor(
    private val context: Context
) : TypedEpoxyController<MutableList<SectionedData<Channel>>>() {

    private lateinit var callbacks: EpoxyAdapterCallback<Channel>

    interface AdapterCallbacks {
        fun onClickItem(channel: Channel, id: String?)
    }

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(sectionedData: MutableList<SectionedData<Channel>>) {

        sectionedData.forEach {
            headerTitle {
                id(it.header)
                title(it.header.notNullable())
            }
            it.data.forEachIndexed { position, channel ->
                channelItem {
                    id(channel.id)
                    channel(channel)
                    hasStart(position == 0)
                    context(context)
                    callbacks(callbacks)
                }
            }
        }
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    fun setAdapterCallbacks(callbacks: EpoxyAdapterCallback<Channel>) {
        this.callbacks = callbacks
    }
}

@EpoxyModelClass(layout = R.layout.item_channel)
abstract class ChannelItemModel : EpoxyModelWithHolder<ChannelItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var channel: Channel

    @EpoxyAttribute
    var hasStart: Boolean = false

    @EpoxyAttribute
    var position: Int = 0

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<Channel>

    override fun bind(holder: Holder) {
        super.bind(holder)

        if (!hasStart) {
            holder.viewBorder1.visibility = View.GONE
        }

        holder.textViewChannel.text = channel.contextChannel?.displayName

        val channelDescription = StringBuilder()
        channelDescription.append("<ul>")
        channel.contextChannel?.details?.forEachIndexed { index, channelDesc ->
            channelDescription.append(
                "<li>$channelDesc</li>"
            )
        }
        channelDescription.append("</ul>")
        holder.textViewChannelDesc.text =
            channelDescription
                .toString()
                .trimIndent()
                .supportBullets(context)

        holder.imageViewChannel.setImageResource(
            when (channel.id) {
                ChannelBankEnum.BILLS_PAYMENT.getChannelId() ->
                    R.drawable.ic_channel_bills_payment
                ChannelBankEnum.BIR.getChannelId() ->
                    R.drawable.ic_channel_bir
                ChannelBankEnum.SSS.getChannelId() ->
                    R.drawable.ic_channel_sss
                ChannelBankEnum.UBP_TO_UBP.getChannelId() ->
                    R.drawable.ic_ub_orange
                ChannelBankEnum.PESONET.getChannelId() ->
                    R.drawable.ic_fund_transfer_pesonet
                ChannelBankEnum.SWIFT.getChannelId() ->
                    R.drawable.ic_fund_transfer_swift
                ChannelBankEnum.INSTAPAY.getChannelId() ->
                    R.drawable.ic_fund_transfer_instapay
                ChannelBankEnum.PDDTS.getChannelId() ->
                    R.drawable.ic_fund_transfer_pddts
                else -> R.drawable.ic_fund_transfer_other_banks_orange
            }
        )
        when (channel.id) {
            ChannelBankEnum.INSTAPAY.getChannelId(),
            ChannelBankEnum.PESONET.getChannelId() -> {
                holder.imageViewChannel.setPadding(0, 0, 0, 0)
            }
            else -> {
                holder.imageViewChannel.setPadding(
                    context.resources.getDimension(R.dimen.content_spacing_half).toInt(),
                    context.resources.getDimension(R.dimen.content_spacing_half).toInt(),
                    context.resources.getDimension(R.dimen.content_spacing_half).toInt(),
                    context.resources.getDimension(R.dimen.content_spacing_half).toInt()
                )
            }
        }
        holder.viewChannel.setOnClickListener {
            callbacks.onClickItem(it, channel, position)
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var constraintLayoutItemChannel: ConstraintLayout
        lateinit var imageViewChannel: ImageView
        lateinit var textViewChannel: TextView
        lateinit var textViewChannelDesc: TextView
        lateinit var viewChannel: View
        lateinit var viewBorder1: View
        lateinit var viewBorder2: View

        override fun bindView(itemView: View) {
            constraintLayoutItemChannel = itemView.constraintLayoutItemChannel
            imageViewChannel = itemView.imageViewChannel
            textViewChannel = itemView.textViewChannel
            textViewChannelDesc = itemView.textViewChannelDesc
            viewBorder1 = itemView.viewBorder1
            viewBorder2 = itemView.viewBorder2
            viewChannel = itemView
        }
    }
}
