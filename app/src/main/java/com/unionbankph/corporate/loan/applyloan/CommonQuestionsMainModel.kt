package com.unionbankph.corporate.loan.applyloan

import android.content.Context
import android.view.View
import com.airbnb.epoxy.*
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.ItemCommonQuestionsMainBinding
import com.unionbankph.corporate.databinding.ItemFinanceWithUsMainBinding
import com.unionbankph.corporate.databinding.ItemKeyFeaturesMainBinding

@EpoxyModelClass(layout = R.layout.item_common_questions_main)
abstract class CommonQuestionsMainModel : EpoxyModelWithHolder<CommonQuestionsMainModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var dataFromViewModel: List<CommonQuestions>

    @EpoxyAttribute
    var expand: Boolean? = null

    @EpoxyAttribute
    var clickListener: (CommonQuestions) -> Unit = { _ -> }

    @EpoxyAttribute
    lateinit var callbacks: LoansAdapterCallback

    override fun bind(holder: Holder) {
        holder.binding.apply {

            //val commonQuestions = CommonQuestions.generateCommonQuestions(context)
            val data: MutableList<CommonQuestionsItemModel_> = mutableListOf()

            dataFromViewModel.map {
                data.add(
                    CommonQuestionsItemModel_()
                        .id(it.id)
                        .dataFromContainer(it)
                        .commonQuestionsClickListener {
                            clickListener(it)
                        }
                )
            }

            commonQuestionsErvData.setModels(data)
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemCommonQuestionsMainBinding
        override fun bindView(itemView: View) {
            binding = ItemCommonQuestionsMainBinding.bind(itemView)
        }
    }
}