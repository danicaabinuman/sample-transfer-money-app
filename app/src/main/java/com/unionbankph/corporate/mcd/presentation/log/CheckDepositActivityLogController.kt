package com.unionbankph.corporate.mcd.presentation.log

import android.content.Context
import com.airbnb.epoxy.TypedEpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.mcd.data.model.SectionedCheckDepositLogs

class CheckDepositActivityLogController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil
) : TypedEpoxyController<MutableList<SectionedCheckDepositLogs>>() {

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(sectionedCheckDepositLogs: MutableList<SectionedCheckDepositLogs>) {
        val totalItems = getTotalItems(sectionedCheckDepositLogs)
        var currentItem = 0
        sectionedCheckDepositLogs.forEachIndexed { headerPosition, sectionedCheckDepositLog ->
            currentItem++
            checkDepositActivityLogHeader {
                id(sectionedCheckDepositLog.header)
                position(headerPosition)
                header(sectionedCheckDepositLog.header.notNullable())
                size(sectionedCheckDepositLogs.size)
            }
            sectionedCheckDepositLog.checkDepositActivityLogs
                ?.forEachIndexed { position, checkDepositActivityLog ->
                    currentItem++
                    checkDepositActivityLogItem {
                        id(checkDepositActivityLog.id)
                        checkDepositActivityLog(checkDepositActivityLog)
                        context(context)
                        hasEnd(totalItems == currentItem)
                        hasStart(headerPosition == 0 && position == 0)
                        viewUtil(viewUtil)
                        position(position)
                    }
                }
        }
    }

    private fun getTotalItems(
        sectionedCheckDepositLogs: MutableList<SectionedCheckDepositLogs>
    ): Int {
        var totalItems = 0
        sectionedCheckDepositLogs.forEach {
            totalItems++
            it.checkDepositActivityLogs?.forEach {
                totalItems++
            }
        }
        return totalItems
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }
}
