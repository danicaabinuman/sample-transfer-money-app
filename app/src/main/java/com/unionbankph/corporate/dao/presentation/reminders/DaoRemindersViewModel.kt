package com.unionbankph.corporate.dao.presentation.reminders

import android.content.Context
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * Created by herald25santos on 3/17/20
 */
class DaoRemindersViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val context: Context
) : BaseViewModel() {

    val isValidFormInput = BehaviorSubject.create<Boolean>()
}
