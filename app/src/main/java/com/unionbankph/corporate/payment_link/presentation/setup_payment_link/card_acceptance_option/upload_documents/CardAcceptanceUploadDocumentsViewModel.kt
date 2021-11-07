package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.upload_documents

import android.net.Uri
import com.unionbankph.corporate.app.base.BaseViewModel
import io.reactivex.subjects.BehaviorSubject
import java.io.File
import javax.inject.Inject

class CardAcceptanceUploadDocumentsViewModel @Inject constructor() : BaseViewModel() {
    val currentFile = BehaviorSubject.create<File>()
    val originalApplicationFileInput = BehaviorSubject.create<File>()
    val originalApplicationUriInput = BehaviorSubject.create<Uri>()

}