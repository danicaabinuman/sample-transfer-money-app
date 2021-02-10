package com.unionbankph.corporate.app.common.widget.dialog

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseBottomSheetDialog
import com.unionbankph.corporate.settings.presentation.SettingsViewModel
import kotlinx.android.synthetic.main.bottom_sheet_file_manager.*

class FileManagerBottomSheet : BaseBottomSheetDialog<SettingsViewModel>(R.layout.bottom_sheet_file_manager) {

    private var callback: FileManagerBottomSheetCallback? = null

    override fun onInitializeListener() {
        super.onInitializeListener()
        btn_take_photo.setOnClickListener {
            callback?.onClickTakePhoto(this)
        }
        btn_open_file_manager.setOnClickListener {
            callback?.onClickOpenFileManager(this)
        }
        btn_cancel.setOnClickListener {
            callback?.onClickCancel(this)
        }
    }

    fun setFileManagerBottomSheetCallback(callback: FileManagerBottomSheetCallback) {
        this.callback = callback
    }

    interface FileManagerBottomSheetCallback {
        fun onClickTakePhoto(dialog: BottomSheetDialogFragment?)
        fun onClickOpenFileManager(dialog: BottomSheetDialogFragment?)
        fun onClickCancel(dialog: BottomSheetDialogFragment?)
    }
}
