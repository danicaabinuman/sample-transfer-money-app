package com.unionbankph.corporate.loan.applyloan

import android.content.Context
import android.view.View
import com.airbnb.epoxy.*
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.*
import com.unionbankph.corporate.subItemCommonQuestions

@EpoxyModelClass(layout = R.layout.item_common_questions)
abstract class CommonQuestionsItemModel : EpoxyModelWithHolder<CommonQuestionsItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var dataFromContainer: CommonQuestions

    @EpoxyAttribute
    var commonQuestionsClickListener: (CommonQuestions) -> Unit = { _ -> }

    @EpoxyAttribute
    var expand: Boolean? = null

    override fun bind(holder: Holder) {
        holder.binding.apply {
            item = dataFromContainer

            //itemCommonQuestionsErvBullet.visibility = if (!dataFromContainer.expand) View.GONE else View.VISIBLE

            itemCommonQuestionsClParent.setOnClickListener {
                dataFromContainer.expand = !dataFromContainer.expand
                commonQuestionsClickListener(dataFromContainer)
            }

            itemCommonQuestionsErvBullet.withModels {
                dataFromContainer.header?.forEachIndexed { index, commonQuestionsHeader ->
                    subItemCommonQuestions {
                        id(dataFromContainer.id)
                        header(commonQuestionsHeader.header)
                    }
                }
                dataFromContainer.bullet?.forEachIndexed { index, commonQuestionsBullet ->
                    subItemCommonQuestions {
                        id(dataFromContainer.id)
                        bullet(commonQuestionsBullet.bullet?.get(index))
                    }
                }
            }

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

