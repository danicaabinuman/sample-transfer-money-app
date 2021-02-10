package com.unionbankph.corporate.app.base

import com.airbnb.epoxy.EpoxyAdapter

class BaseAdapter : EpoxyAdapter() {

    override fun onAttachedToRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {

        // This will force all models to be unbound and their views recycled once the RecyclerView is no longer in use. We need this so resources
        // are properly released, listeners are detached, and views can be returned to view pools (if applicable).
        if (recyclerView.layoutManager is androidx.recyclerview.widget.LinearLayoutManager) {
            (recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager).recycleChildrenOnDetach = true
        }
    }
}
