package com.unionbankph.corporate.payment_link.presentation.onboarding

import android.net.Uri
import com.unionbankph.corporate.app.base.BaseViewModel
import io.reactivex.subjects.BehaviorSubject
import java.io.File
import javax.inject.Inject


class RequestPaymentSplashViewModel
@Inject constructor(
) : BaseViewModel() {
    val currentFile = BehaviorSubject.create<File>()
    val originalApplicationFileInput = BehaviorSubject.create<File>()
    val originalApplicationUriInput = BehaviorSubject.create<Uri>()

}