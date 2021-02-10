package com.unionbankph.corporate.mcd.presentation.summary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositTypeEnum
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import java.io.File
import javax.inject.Inject

class CheckDepositSummaryViewModel @Inject constructor(
    private val cacheManager: CacheManager,
    private val schedulerProvider: SchedulerProvider
) : BaseViewModel() {

    private val _checkDepositState = MutableLiveData<CheckDepositSummaryState>()

    val state: LiveData<CheckDepositSummaryState> get() = _checkDepositState

    fun deleteFiles() {
        File(cacheManager.get(CheckDepositTypeEnum.FRONT_OF_CHECK.name)).delete()
        File(cacheManager.get(CheckDepositTypeEnum.BACK_OF_CHECK.name)).delete()
        File(cacheManager.get(CheckDepositTypeEnum.CROPPED_BACK_OF_CHECK.name)).delete()
        File(cacheManager.get(CheckDepositTypeEnum.CROPPED_FRONT_OF_CHECK.name)).delete()
    }
}

sealed class CheckDepositSummaryState

object ShowCheckDepositSummaryLoading : CheckDepositSummaryState()

object ShowCheckDepositSummaryEndlessLoading : CheckDepositSummaryState()

object ShowCheckDepositSummaryDismissLoading : CheckDepositSummaryState()

object ShowCheckDepositSummaryDismissEndlessLoading : CheckDepositSummaryState()

data class ShowCheckDepositSummaryError(val throwable: Throwable) : CheckDepositSummaryState()
