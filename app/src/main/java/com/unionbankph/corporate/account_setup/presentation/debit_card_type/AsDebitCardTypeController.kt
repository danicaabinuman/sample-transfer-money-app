package com.unionbankph.corporate.account_setup.presentation.debit_card_type

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.*
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account_setup.data.DebitCardState
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.GenericMenuItem
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.SMETextViewModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.chip.SMEChipModel
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.chip.SMEChipModel_
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ItemAdCardBinding
import com.unionbankph.corporate.databinding.ItemDebitCardBinding

class AsDebitCardTypeController constructor(
    private val context: Context
) : TypedEpoxyController<DebitCardState>() {

    private lateinit var asDebitCardTypeCallback: AsDebitCardTypeCallback
    interface AsDebitCardTypeCallback {
        fun onDebitCardType(id: Int) = Unit
    }

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(debitCardState: DebitCardState) {

        SMETextViewModel_()
            .id("title-header")
            .style(Constant.SMETextViewStyle.H2)
            .text(context.getString(R.string.title_open_bank_account))
            .addTo(this)


        AdCardModel_()
            .id("lazada-card")
            .addTo(this)

        val debitCardItems = mutableListOf(
            GenericMenuItem(
                Constant.DebitCardType.GETGO.toString(),
                context.getString(R.string.title_getgo_debit),
                context.getString(R.string.msg_debit_type),
                null,
                context.getString(R.string.drawable_getgo),
                false,
                true,
                true
            ),
            GenericMenuItem(
                Constant.DebitCardType.PLAYEVERYDAY.toString(),
                context.getString(R.string.title_play_everyday),
                context.getString(R.string.msg_debit_type),
                null,
                context.getString(R.string.drawable_play_everyday),
                false,
                true,
                true
            ))
        debitCardState.apply {
            this.cards = debitCardItems
        }
        val cardModels = mutableListOf<DebitCardModel>()
        debitCardState.cards.forEachIndexed { index, item ->
            cardModels.add(
                DebitCardModel_()
                    .id(item.id)
                    .context(context)
                    .model(debitCardItems[index])
                    .callbacks(asDebitCardTypeCallback)
            )
        }

        carousel {
            this.id("carousel-debit-cards")
            this.models(cardModels)
        }


    }

    fun setDebitCardTypeCallback(asDebitCardTypeCallback: AsDebitCardTypeCallback) {
        this.asDebitCardTypeCallback = asDebitCardTypeCallback
    }
}


@EpoxyModelClass
abstract class AdCardModel : EpoxyModelWithHolder<AdCardModel.Holder>()  {

    override fun getDefaultLayout(): Int {
        return R.layout.item_ad_card
    }

    class Holder : EpoxyHolder() {
        lateinit var binding : ItemAdCardBinding

        override fun bindView(itemView: View) {
            binding = ItemAdCardBinding.bind(itemView)
        }
    }
}

@EpoxyModelClass(layout = R.layout.item_debit_card)
abstract class DebitCardModel : EpoxyModelWithHolder<DebitCardModel.Holder>()  {

    @EpoxyAttribute
    lateinit var model: GenericMenuItem

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var callbacks: AsDebitCardTypeController.AsDebitCardTypeCallback


    override fun bind(holder: Holder) {


        Log.e("model.title",""+model.title)
        Log.e("model.subtitle",""+model.subtitle)
        val imageSourceId = context.resources.getIdentifier(
            model.src, "drawable", Constant.PACKAGE_NAME
        )

        holder.binding.apply {
            textViewDebitTitle.text = model.title
            textViewDebitContent.text = model.subtitle
            imageViewIcon.setImageResource(imageSourceId)

            //cardViewDebitCard.isSelected = !model.isSelected!!

            cardViewDebitCard.setOnClickListener {
                callbacks.onDebitCardType(Integer.parseInt(model.id!!))
            }
            cardViewDebitCard.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    v.background = ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_button_with_icon_selected
                    )
                }
                false
            }

        }

    }

    class Holder : EpoxyHolder() {
        lateinit var binding : ItemDebitCardBinding

        override fun bindView(itemView: View) {
            binding = ItemDebitCardBinding.bind(itemView)
        }
    }


}