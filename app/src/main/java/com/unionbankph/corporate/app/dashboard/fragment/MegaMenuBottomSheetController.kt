package com.unionbankph.corporate.app.dashboard.fragment

import android.content.Context
import com.airbnb.epoxy.*
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.chip.SMEChipCallback
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.chip.SMEChipModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.chip.SMEChipModel
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.generic_item_1.GenericItem1Model_
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.presentation.helper.JsonHelper

class MegaMenuBottomSheetController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil
) : TypedEpoxyController<MegaMenuBottomSheetState>() {

    private lateinit var dashboardAdapterCallback: DashboardAdapterCallback

    private lateinit var mChipCallback: SMEChipCallback

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(data: MegaMenuBottomSheetState?) {

        val filterModels = mutableListOf<SMEChipModel>()

        data?.filters?.forEachIndexed { index, item ->
            filterModels.add(
                SMEChipModel_()
                    .id(item.id)
                    .position(index.toString())
                    .callback(mChipCallback)
                    .model(JsonHelper.toJson(item))
            )
        }

        carousel {
            this.id("selection-filters")
            this.models(filterModels)
            this.padding(Carousel.Padding.dp(
                18,12,12,24,8)
            )
        }

        data?.menus?.forEach {
            GenericItem1Model_()
                .id(it.id)
                .context(context)
                .model(it)
                .addTo(this)
        }
    }

    fun setChipCallback(chipCallback: SMEChipCallback) {
        this.mChipCallback = chipCallback
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }
}
