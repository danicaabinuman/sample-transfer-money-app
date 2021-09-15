package com.unionbankph.corporate.app.dashboard.fragment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseBottomSheetDialog
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.chip.ChipItemModel
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.generic_item_1.GenericItemModel
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.BottomSheetDashboardMoreBinding

class DashboardMoreBottomSheet :
    BaseBottomSheetDialog<BottomSheetDashboardMoreBinding, DashboardFragmentViewModel>() {

    private var moreBottomSheetState = MoreBottomSheetState()

    private val controller by lazyFast {
        DashboardMoreController(requireContext(), viewUtil)
    }

    override fun onViewsBound() {
        super.onViewsBound()

        initDefaultActionFilter()
        initDefaultActions()
        initRecyclerView()

    }

    private fun initRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMoreOptions.layoutManager = linearLayoutManager

        binding.recyclerViewMoreOptions.setController(controller)

        controller.setData(moreBottomSheetState)
//        controller.setDashboardAdapterCallbacks(this)
//        controller.setAccountAdapterCallbacks(this)
    }

    private fun initDefaultActionFilter() {

        val parseFilterListFromJson = viewUtil.loadJSONFromAsset(
            requireActivity(),
            MORE_BOTTOM_SHEET_FILTER_ITEMS
        )
        val filterItems = JsonHelper.fromListJson<ChipItemModel>(parseFilterListFromJson)

        moreBottomSheetState.apply {
            this.filters = filterItems
        }
    }

    private fun initDefaultActions() {
        moreBottomSheetState.apply {
            this.actions = getActionItems()
        }
    }

    private fun getActionItems(): MutableList<GenericItemModel> {
        val actionList = arguments?.getParcelableArrayList<ActionItem>(
            EXTRA_ACTION_LIST
        )?.toMutableList() ?: mutableListOf()

        val genericItemList = mutableListOf<GenericItemModel>()

        actionList.forEach {
            genericItemList.add(
                GenericItemModel().apply {
                    this.id = it.id
                    this.label = it.label?.replace("\r\n", " ")?.replace("\n", " ")
                    this.caption = it.caption
                    this.action = it.action
                    this.src = "ic_dashboard_" + it.src
                    this.isEnabled = it.isEnabled
                }
            )
        }

        genericItemList.removeAll { it.id == Constant.DASHBOARD_ACTION_MORE } // Remove [More] Item

        return genericItemList
    }

    companion object {

        const val MORE_BOTTOM_SHEET_FILTER_ITEMS = "more_bottom_sheet_filter_items"

        const val EXTRA_ACTION_LIST = "action_list"

        fun newInstance(
            actionList: ArrayList<ActionItem>
        ) = DashboardMoreBottomSheet().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(EXTRA_ACTION_LIST, actionList)
            }
        }
    }

    override val bindingBinder: (View) -> BottomSheetDashboardMoreBinding
        get() = BottomSheetDashboardMoreBinding::bind

    override val layoutId: Int
        get() = R.layout.bottom_sheet_dashboard_more

    override val viewModelClassType: Class<DashboardFragmentViewModel>
        get() = DashboardFragmentViewModel::class.java
}