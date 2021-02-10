package com.unionbankph.corporate.app.common.widget.recyclerview

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class PaginationScrollListener(internal var layoutManager: LinearLayoutManager) :
    RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        val findLastCompletelyVisibleItemPosition =
            layoutManager.findLastCompletelyVisibleItemPosition()
        firstItemVisible(
            firstVisibleItemPosition == 0 && findLastCompletelyVisibleItemPosition == 0
        )
        if (!isLoading
            && !isLastPage
            && !isFailed
            && (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0)) {
            loadMoreItems()
        }
    }

    abstract val totalPageCount: Int

    abstract val isLastPage: Boolean

    abstract val isLoading: Boolean

    abstract val isFailed: Boolean

    protected abstract fun loadMoreItems()

    protected open fun firstItemVisible(isFirstItemVisible: Boolean) = Unit
}
