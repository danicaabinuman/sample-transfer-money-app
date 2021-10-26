package com.unionbankph.corporate.app.dashboard.fragment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseBottomSheetDialog
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.GenericMenuItem
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.chip.SMEChipCallback
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.BottomSheetDashboardMoreBinding

class MegaMenuBottomSheet :
    BaseBottomSheetDialog<BottomSheetDashboardMoreBinding, DashboardFragmentViewModel>(),
    SMEChipCallback {

    private var moreBottomSheetState = MegaMenuBottomSheetState()

    private lateinit var lastSelectedFilter: String

    private val controller by lazyFast {
        MegaMenuBottomSheetController(requireContext(), viewUtil)
    }

    override fun onViewsBound() {
        super.onViewsBound()

        initDefaultMenuFilter()
        initDefaultMenu()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMoreOptions.layoutManager = linearLayoutManager

        binding.recyclerViewMoreOptions.setController(controller)

        controller.setChipCallback(this)
//        controller.setDashboardAdapterCallbacks(this)
//        controller.setAccountAdapterCallbacks(this)
        controller.setData(moreBottomSheetState)
    }

    private fun initDefaultMenuFilter() {
        val parseFilterListFromJson = viewUtil.loadJSONFromAsset(
            requireActivity(),
            MORE_BOTTOM_SHEET_FILTER_ITEMS
        )
        val filterItems = JsonHelper.fromListJson<GenericMenuItem>(parseFilterListFromJson)
        lastSelectedFilter = filterItems[0].id!!

        moreBottomSheetState.apply {
            this.filters = filterItems
        }
    }

    private fun initDefaultMenu() {
        moreBottomSheetState.apply {
            this.menus = getMenuItems()
        }
    }

    private fun getMenuItems(): MutableList<GenericMenuItem> {
        val menuList = arguments?.getParcelableArrayList<GenericMenuItem>(
            EXTRA_DEFAULT_MENU_LIST
        )?.toMutableList() ?: mutableListOf()

        val newList = arrayListOf<GenericMenuItem>()
        newList.removeAll { it.id == Constant.DASHBOARD_ACTION_MORE } // Remove [More] Item

        menuList.forEach {
            newList.add(it.copy().apply {
                title = title?.replace("\r\n", " ")?.replace("\n", " ")
                src = "ic_dashboard_$src"
            })
        }

        return newList
    }

    override fun onChipClicked(genericMenuSelection: GenericMenuItem, position: Int) {
        moreBottomSheetState.apply {
            filters[lastFilterSelected!!].apply { isSelected = false }
            filters[position].apply { isSelected = true }
            lastFilterSelected = position
        }
        controller.setData(moreBottomSheetState)
    }

    companion object {

        const val MORE_BOTTOM_SHEET_FILTER_ITEMS = "menu_bottom_sheet_filter"

        const val EXTRA_DEFAULT_MENU_LIST = "menu_list"

        fun newInstance(
            defaultMenuList: ArrayList<GenericMenuItem>
        ) = MegaMenuBottomSheet().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(EXTRA_DEFAULT_MENU_LIST, defaultMenuList)
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