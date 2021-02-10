package com.unionbankph.corporate.common.data.form

import com.unionbankph.corporate.app.common.extension.notNullable
import javax.annotation.concurrent.ThreadSafe

class Pageable {

    var page: Int = 0
    var size: Int = NORMAL_SIZE
    var filter: String? = null
    var totalPageCount: Int = 0
    var isLastPage: Boolean = false
    var isLoadingPagination: Boolean = false
    var isInitialLoad: Boolean = true
    var isFailed: Boolean = false
    var errorMessage: String = ""

    fun currentPage() = page

    fun increasePage() {
        this.page = page + 1
    }

    fun resetPagination() {
        this.isInitialLoad = true
        this.page = 0
    }

    fun resetLoad() {
        this.isInitialLoad = false
    }

    fun errorPagination(message: String?) {
        isFailed = true
        errorMessage = message.notNullable()
    }
    fun refreshErrorPagination() {
        isFailed = false
        errorMessage = ""
    }

    fun <T> combineList(currentList: MutableList<T>?, newList: MutableList<T>): MutableList<T> {
        if (isInitialLoad) {
            return newList
        }
        val combinedList = mutableListOf<T>()
        combinedList.addAll(currentList ?: mutableListOf())
        combinedList.addAll(newList)
        return combinedList
    }

    @ThreadSafe
    companion object {
        const val NORMAL_SIZE = 10
    }
}
