package com.unionbankph.corporate.dao.presentation.business_registration_papers

import android.content.Context
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import io.reactivex.subjects.BehaviorSubject
import java.io.File
import javax.inject.Inject

/**
 * Created by herald25santos on 3/17/20
 */
class DaoBusinessRegistrationPapersViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val context: Context
) : BaseViewModel() {

    val isValidFormInput = BehaviorSubject.create<Boolean>()
    val currentFile = BehaviorSubject.create<File>()
    val originalApplicationFileInput = BehaviorSubject.create<File>()
    val certificateOfDtiFileInput = BehaviorSubject.create<File>()
    val mayorsPermitFileInput = BehaviorSubject.create<File>()

    val originalApplicationHasFileInput = BehaviorSubject.create<Boolean>()
    val certificateOfDtiHasFileInput = BehaviorSubject.create<Boolean>()
    val mayorsPermitHasFileInput = BehaviorSubject.create<Boolean>()

    fun hasValidForm() = isValidFormInput.value ?: false
}
