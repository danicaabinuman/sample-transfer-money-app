package com.unionbankph.corporate.app.common.extension

import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.chip.GenericItem
import com.unionbankph.corporate.app.dashboard.fragment.ActionItem

fun ActionItem.toGenericItem(): GenericItem = GenericItem().apply {
    id = this@toGenericItem.id
    title = this@toGenericItem.label?.replace("\r\n", " ")?.replace("\n", " ")
    subtitle = this@toGenericItem.caption
    action = this@toGenericItem.action
    src = "ic_dashboard_" + this@toGenericItem.src
    isEnabled = this@toGenericItem.isEnabled
}