package com.unionbankph.corporate.loan.applyloan

import android.content.Context
import android.view.View
import com.airbnb.epoxy.*
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.*

@EpoxyModelClass(layout = R.layout.ub_layout_sub_item_common_questions)
abstract class CommonQuestionsSubItemModel : EpoxyModelWithHolder<CommonQuestionsSubItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var dataFromContainer: CommonQuestionsSubBullet

    @EpoxyAttribute
    var commonQuestionsClickListener: (CommonQuestionsSubBullet) -> Unit = { _ -> }

    override fun bind(holder: Holder) {
        holder.binding.apply {
            /*item = dataFromContainer

            itemCommonQuestionsClParent.setOnClickListener {
                dataFromContainer.expand = !dataFromContainer.expand
                commonQuestionsClickListener(dataFromContainer)
            }*/
            executePendingBindings()
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemCommonQuestionsBinding
        override fun bindView(itemView: View) {
            binding = ItemCommonQuestionsBinding.bind(itemView)
        }
    }
}

