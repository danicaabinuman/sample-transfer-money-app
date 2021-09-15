package com.unionbankph.corporate.app.dashboard.fragment

import android.content.Context
import com.airbnb.epoxy.*
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.chip.SMEChipModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.chip.SMEChipModel
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.generic_item_1.GenericItem1Model_
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback

class DashboardMoreController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil
) : TypedEpoxyController<MoreBottomSheetState>() {

    private lateinit var dashboardAdapterCallback: DashboardAdapterCallback

    private lateinit var accountAdapterCallback: AccountAdapterCallback

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(data: MoreBottomSheetState?) {

        val filterModels = mutableListOf<SMEChipModel>()
        data?.filters?.forEach {
            filterModels.add(
                SMEChipModel_()
                    .id(it.id)
                    .model(it)
            )
        }

        carousel {
            this.id("selection-filters")
            this.models(filterModels)
        }

        data?.actions?.forEach {
            GenericItem1Model_()
                .id(it.id)
                .context(context)
                .model(it)
                .addTo(this)
        }
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }
}
