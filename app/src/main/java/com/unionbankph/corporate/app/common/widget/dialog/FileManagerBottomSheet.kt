package com.unionbankph.corporate.app.common.widget.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseBottomSheetDialog
import com.unionbankph.corporate.databinding.BottomSheetFileManagerBinding
import com.unionbankph.corporate.settings.presentation.SettingsViewModel

class FileManagerBottomSheet :
    BaseBottomSheetDialog<BottomSheetFileManagerBinding, SettingsViewModel>() {

    private var callback: FileManagerBottomSheetCallback? = null

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.btnTakePhoto.setOnClickListener {
            callback?.onClickTakePhoto(this)
        }
        binding.btnOpenFileManager.setOnClickListener {
            callback?.onClickOpenFileManager(this)
        }
        binding.btnCancel.setOnClickListener {
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

    override val layoutId: Int
        get() = R.layout.bottom_sheet_file_manager

    override val viewModelClassType: Class<SettingsViewModel>
        get() = SettingsViewModel::class.java

    override val bindingBinder: (View) -> BottomSheetFileManagerBinding
        get() = BottomSheetFileManagerBinding::bind
}
