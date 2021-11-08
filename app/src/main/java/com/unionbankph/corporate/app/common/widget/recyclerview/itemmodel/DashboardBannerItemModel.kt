package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.dashboard.fragment.BannerCardItem
import com.unionbankph.corporate.app.dashboard.fragment.DashboardAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ItemDashboardBannerBinding

@EpoxyModelClass
abstract class DashboardBannerItemModel : EpoxyModelWithHolder<DashboardBannerItemModel.Holder>()  {

    @EpoxyAttribute
    lateinit var item: String

    @EpoxyAttribute
    lateinit var callbacks: DashboardAdapterCallback

    @EpoxyAttribute
    lateinit var context: Context

    override fun getDefaultLayout(): Int {
        return R.layout.item_dashboard_banner
    }

    override fun bind(holder: Holder) {
        val model = JsonHelper.fromJson<BannerCardItem>(item)

        holder.binding.apply {

            when (model.id) {
                Constant.Banner.LEARN_MORE -> {
                    val constraintSet = ConstraintSet()
                    constraintSet.clone(clBannerCard)
                    constraintSet.connect(
                        R.id.iv_banner_icon, ConstraintSet.TOP,
                        R.id.cl_banner_card, ConstraintSet.TOP, 0)
                    constraintSet.applyTo(clBannerCard)
                }
                else -> {}
            }

            tvBannerHeader.text = model.header
            tvBannerBody.text = model.body
            tvBannerFooter.text = model.footer

            tvBannerHeader.apply {
                text = model.header
                setTextColor(ContextCompat.getColor(context, getTextColor(model.id!!)))
            }

            tvBannerBody.apply {
                text = model.body
                setTextColor(ContextCompat.getColor(context, getTextColor(model.id!!)))
            }

            tvBannerFooter.text = model.footer
            ivBannerIcon.setImageResource(getBannerIcon(model.src!!))
            clBannerCard.setBackgroundResource(getBannerBackground(model.src!!))

            root.setOnClickListener {
                callbacks.onDashboardActionEmit(model.action!!, true)
            }
        }
    }

    private fun getTextColor(id: String) : Int {
        return when(id) {
            Constant.Banner.BUSINESS_PROFILE -> R.color.dsColorBody
            else -> R.color.colorWhite
        }
    }

    private fun getBannerIcon(src: String): Int {
        return when (src) {
            Constant.Banner.BUSINESS_PROFILE -> R.drawable.bg_business_profile_banner
            else -> R.drawable.bg_banner_learn_more
        }
    }

    private fun getBannerBackground(src: String): Int {
        return when (src) {
            Constant.Banner.BUSINESS_PROFILE -> R.color.dsColorLightOrange
            else -> R.drawable.bg_settlement_account_item_button_default_radius4
        }
    }

    class Holder: EpoxyHolder() {

        lateinit var binding: ItemDashboardBannerBinding

        override fun bindView(itemView: View) {
            binding = ItemDashboardBannerBinding.bind(itemView)
        }
    }
}