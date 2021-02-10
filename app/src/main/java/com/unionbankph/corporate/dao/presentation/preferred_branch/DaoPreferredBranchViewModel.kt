package com.unionbankph.corporate.dao.presentation.preferred_branch

import android.content.Context
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.branch.data.model.Branch
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import javax.inject.Inject

/**
 * Created by herald25santos on 3/17/20
 */
class DaoPreferredBranchViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val context: Context
) : BaseViewModel() {

    val isValidFormInput = BehaviorSubject.create<Boolean>()

    val branchInput = BehaviorSubject.create<Branch>()

    val dateInput = BehaviorSubject.create<Calendar>()

    fun hasValidForm() = isValidFormInput.value ?: false
}
