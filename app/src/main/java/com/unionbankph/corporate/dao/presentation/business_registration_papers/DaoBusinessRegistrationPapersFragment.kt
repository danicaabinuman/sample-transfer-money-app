package com.unionbankph.corporate.dao.presentation.business_registration_papers

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tbruyelle.rxpermissions2.RxPermissions
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.setContextCompatBackground
import com.unionbankph.corporate.app.common.extension.setContextCompatTextColor
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.widget.dialog.FileManagerBottomSheet
import com.unionbankph.corporate.app.util.FileUtil
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.databinding.FragmentBusinessRegistrationPapersBinding
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import java.io.File
import java.io.IOException
import javax.annotation.concurrent.ThreadSafe
import javax.inject.Inject

class DaoBusinessRegistrationPapersFragment :
    BaseFragment<FragmentBusinessRegistrationPapersBinding, DaoBusinessRegistrationPapersViewModel>(),
    DaoActivity.ActionEvent, FileManagerBottomSheet.FileManagerBottomSheetCallback {

    @Inject
    lateinit var fileUtil: FileUtil

    private val daoActivity by lazyFast { getAppCompatActivity() as DaoActivity }

    private lateinit var clOriginalApplication: ConstraintLayout
    private lateinit var clCertificationOfDti: ConstraintLayout
    private lateinit var clMayorsPermit: ConstraintLayout

    private lateinit var ivOriginalApplication: AppCompatImageView
    private lateinit var ivCertificationOfDti: AppCompatImageView
    private lateinit var ivMayorsPermit: AppCompatImageView

    private lateinit var tvOriginalApplication: AppCompatTextView
    private lateinit var tvCertificationOfDti: AppCompatTextView
    private lateinit var tvMayorsPermit: AppCompatTextView

    private lateinit var ivPlusOriginalApplication: AppCompatImageView
    private lateinit var ivPlusCertificationOfDti: AppCompatImageView
    private lateinit var ivPlusMayorsPermit: AppCompatImageView

    private lateinit var tvOriginalApplicationError: AppCompatTextView
    private lateinit var tvCertificationOfDtiError: AppCompatTextView
    private lateinit var tvMayorsPermitError: AppCompatTextView

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initDaoActivity()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        initBinding()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.viewOriginalApplication.root.setOnClickListener {
            initPermission(TAG_ORIGINAL_APPLICATION_DIALOG)
        }
        binding.viewCertificationOfDti.root.setOnClickListener {
            initPermission(TAG_CERTIFICATION_OF_DTI_DIALOG)
        }
        binding.viewMayorsPermit.root.setOnClickListener {
            initPermission(TAG_MAYORS_PERMIT_DIALOG)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ORIGINAL_APPLICATION_FILE_REQUEST_CODE -> {
                    intent?.data?.let {
                        try {
                            val filePath = fileUtil.getPath(it)
                            val file = File(filePath)
                            if (VALID_EXTENSIONS.contains(file.extension)) {
                                viewModel.originalApplicationFileInput.onNext(file)
                            } else {
                                showMaterialDialogError(
                                    message = formatString(R.string.error_file_not_supported)
                                )
                            }
                        } catch (e: Exception) {
                            showMaterialDialogError(
                                message = formatString(R.string.error_file_not_supported)
                            )
                        }
                    }
                }
                CERTIFICATION_OF_DTI_FILE_REQUEST_CODE -> {
                    intent?.data?.let {
                        try {
                            val filePath = fileUtil.getPath(it)
                            val file = File(filePath)
                            if (VALID_EXTENSIONS.contains(file.extension)) {
                                viewModel.certificateOfDtiFileInput.onNext(file)
                            } else {
                                showMaterialDialogError(
                                    message = formatString(R.string.error_file_not_supported)
                                )
                            }
                        } catch (e: Exception) {
                            showMaterialDialogError(
                                message = formatString(R.string.error_file_not_supported)
                            )
                        }
                    }
                }
                MAYORS_PERMIT_FILE_REQUEST_CODE -> {
                    intent?.data?.let {
                        try {
                            val filePath = fileUtil.getPath(it)
                            val file = File(filePath)
                            if (VALID_EXTENSIONS.contains(file.extension)) {
                                viewModel.mayorsPermitFileInput.onNext(file)
                            } else {
                                showMaterialDialogError(
                                    message = formatString(R.string.error_file_not_supported)
                                )
                            }
                        } catch (e: Exception) {
                            showMaterialDialogError(
                                message = formatString(R.string.error_file_not_supported)
                            )
                        }
                    }
                }
                ORIGINAL_APPLICATION_PHOTO_REQUEST_CODE -> {
                    viewModel.currentFile.value?.let {
                        viewModel.originalApplicationFileInput.onNext(it)
                    }
                }
                CERTIFICATION_OF_DTI_PHOTO_REQUEST_CODE -> {
                    viewModel.currentFile.value?.let {
                        viewModel.certificateOfDtiFileInput.onNext(it)
                    }
                }
                MAYORS_PERMIT_PHOTO_REQUEST_CODE -> {
                    viewModel.currentFile.value?.let {
                        viewModel.mayorsPermitFileInput.onNext(it)
                    }
                }
            }
        }
    }

    private fun setupAttachmentFile(
        file: File,
        iv: AppCompatImageView,
        tv: AppCompatTextView,
        ivPlus: AppCompatImageView
    ) {
        val fileName = fileUtil.getFileName(Uri.fromFile(file))
        tv.setContextCompatTextColor(R.color.colorInfo)
        tv.setTypeface(tv.typeface, Typeface.BOLD)
        tv.text = fileName
        iv.visibility(true)
        ivPlus.setImageResource(R.drawable.ic_close_gray)
        ivPlus.setOnClickListener {
            clearAttachmentFile(iv, tv, ivPlus)
        }
        invokeFileFlagInput(iv.tag, true)
    }

    private fun clearAttachmentFile(
        iv: AppCompatImageView,
        tv: AppCompatTextView,
        ivPlus: AppCompatImageView
    ) {
        tv.setContextCompatTextColor(R.color.colorHint)
        tv.setTypeface(tv.typeface, Typeface.NORMAL)
        tv.text = formatString(R.string.hint_upload_a_file)
        iv.visibility(false)
        ivPlus.setImageResource(R.drawable.ic_plus_orange)
        ivPlus.setOnClickListener(null)
        invokeFileFlagInput(iv.tag, false)
    }

    private fun invokeFileFlagInput(tag: Any, hasInput: Boolean) {
        when (tag) {
            TAG_ORIGINAL_APPLICATION_DIALOG -> {
                viewModel.originalApplicationHasFileInput.onNext(hasInput)
            }
            TAG_CERTIFICATION_OF_DTI_DIALOG -> {
                viewModel.certificateOfDtiHasFileInput.onNext(hasInput)
            }
            TAG_MAYORS_PERMIT_DIALOG -> {
                viewModel.mayorsPermitHasFileInput.onNext(hasInput)
            }
        }
    }

    override fun onClickTakePhoto(dialog: BottomSheetDialogFragment?) {
        when (dialog?.tag) {
            TAG_ORIGINAL_APPLICATION_DIALOG -> {
                takePhoto(ORIGINAL_APPLICATION_PHOTO_REQUEST_CODE)
            }
            TAG_CERTIFICATION_OF_DTI_DIALOG -> {
                takePhoto(CERTIFICATION_OF_DTI_PHOTO_REQUEST_CODE)
            }
            TAG_MAYORS_PERMIT_DIALOG -> {
                takePhoto(MAYORS_PERMIT_PHOTO_REQUEST_CODE)
            }
        }
        dialog?.dismiss()
    }

    override fun onClickOpenFileManager(dialog: BottomSheetDialogFragment?) {
        when (dialog?.tag) {
            TAG_ORIGINAL_APPLICATION_DIALOG -> {
                openFileManager(ORIGINAL_APPLICATION_FILE_REQUEST_CODE)
            }
            TAG_CERTIFICATION_OF_DTI_DIALOG -> {
                openFileManager(CERTIFICATION_OF_DTI_FILE_REQUEST_CODE)
            }
            TAG_MAYORS_PERMIT_DIALOG -> {
                openFileManager(MAYORS_PERMIT_FILE_REQUEST_CODE)
            }
        }
        dialog?.dismiss()
    }

    private fun initBinding() {
        viewModel.originalApplicationFileInput
            .filter {
                viewModel.originalApplicationHasFileInput.value == true ||
                        viewModel.originalApplicationFileInput.hasValue()
            }
            .subscribe {
                setupAttachmentFile(
                    it,
                    ivOriginalApplication,
                    tvOriginalApplication,
                    ivPlusOriginalApplication
                )
            }.addTo(disposables)
        viewModel.certificateOfDtiFileInput
            .filter {
                viewModel.certificateOfDtiHasFileInput.value == true ||
                        viewModel.certificateOfDtiFileInput.hasValue()
            }
            .subscribe {
                setupAttachmentFile(
                    it,
                    ivCertificationOfDti,
                    tvCertificationOfDti,
                    ivPlusCertificationOfDti
                )
            }.addTo(disposables)
        viewModel.mayorsPermitFileInput
            .filter {
                viewModel.mayorsPermitHasFileInput.value == true ||
                        viewModel.mayorsPermitFileInput.hasValue()
            }
            .subscribe {
                setupAttachmentFile(
                    it,
                    ivMayorsPermit,
                    tvMayorsPermit,
                    ivPlusMayorsPermit
                )
            }.addTo(disposables)
        viewModel.originalApplicationHasFileInput
            .subscribe {
                viewUtil.startAnimateView(!it, tvOriginalApplicationError, R.anim.anim_move)
                setEditTextBackground(clOriginalApplication, it)
            }.addTo(disposables)
        viewModel.certificateOfDtiHasFileInput
            .subscribe {
                viewUtil.startAnimateView(!it, tvCertificationOfDtiError, R.anim.anim_move)
                setEditTextBackground(clCertificationOfDti, it)
            }.addTo(disposables)
        viewModel.mayorsPermitHasFileInput
            .subscribe {
                viewUtil.startAnimateView(!it, tvMayorsPermitError, R.anim.anim_move)
                setEditTextBackground(clMayorsPermit, it)
            }.addTo(disposables)
    }

    private fun setEditTextBackground(cl: ConstraintLayout, isValid: Boolean) {
        cl.setContextCompatBackground(
            if (isValid)
                R.drawable.bg_edittext_form
            else
                R.drawable.bg_edittext_form_error
        )
    }

    private fun init() {
        clOriginalApplication =
            binding.viewOriginalApplication.clUploadFile
        clCertificationOfDti =
            binding.viewCertificationOfDti.clUploadFile
        clMayorsPermit =
            binding.viewMayorsPermit.clUploadFile

        ivOriginalApplication =
            binding.viewOriginalApplication.ivUploadFile
        ivCertificationOfDti =
            binding.viewCertificationOfDti.ivUploadFile
        ivMayorsPermit =
            binding.viewMayorsPermit.ivUploadFile

        ivOriginalApplication.tag = TAG_ORIGINAL_APPLICATION_DIALOG
        ivCertificationOfDti.tag = TAG_CERTIFICATION_OF_DTI_DIALOG
        ivMayorsPermit.tag = TAG_MAYORS_PERMIT_DIALOG

        tvOriginalApplication =
            binding.viewOriginalApplication.tvUploadAFile
        tvCertificationOfDti =
            binding.viewCertificationOfDti.tvUploadAFile
        tvMayorsPermit =
            binding.viewMayorsPermit.tvUploadAFile

        ivPlusOriginalApplication =
            binding.viewOriginalApplication.ivPlus
        ivPlusCertificationOfDti =
            binding.viewCertificationOfDti.ivPlus
        ivPlusMayorsPermit =
            binding.viewMayorsPermit.ivPlus

        tvOriginalApplicationError =
            binding.viewOriginalApplication.tvUploadAFileError
        tvCertificationOfDtiError =
            binding.viewCertificationOfDti.tvUploadAFileError
        tvMayorsPermitError =
            binding.viewMayorsPermit.tvUploadAFileError

        tvOriginalApplicationError.text = formatString(R.string.error_this_field)
        tvCertificationOfDtiError.text = formatString(R.string.error_this_field)
        tvMayorsPermitError.text = formatString(R.string.error_this_field)

        validateForm()
    }

    private fun openFileManager(requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, requestCode)
    }

    private fun takePhoto(requestCode: Int) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(getAppCompatActivity().packageManager)?.also {
                val photoFile: File? = try {
                    fileUtil.createImageFile(requestCode)
                } catch (ex: IOException) {
                    showMaterialDialogError(message = ex.message.notNullable())
                    null
                }
                photoFile?.let {
                    viewModel.currentFile.onNext(it)
                    it.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            getAppCompatActivity(),
                            "${BuildConfig.APPLICATION_ID}.fileprovider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, requestCode)
                    }
                }
            }
        }
    }

    override fun onClickCancel(dialog: BottomSheetDialogFragment?) {
        dialog?.dismiss()
    }

    private fun initDaoActivity() {
        daoActivity.setToolBarDesc(formatString(R.string.title_business_registration_papers))
        daoActivity.setActionEvent(this)
        daoActivity.setProgressValue(8)
    }

    private fun showFileManagerBottomSheet(tag: String) {
        val fileManagerBottomSheet = FileManagerBottomSheet()
        fileManagerBottomSheet.setFileManagerBottomSheetCallback(this)
        fileManagerBottomSheet.show(
            childFragmentManager,
            tag
        )
    }

    private fun initPermission(tag: String) {
        RxPermissions(this)
            .request(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .subscribe { granted ->
                if (granted) {
                    showFileManagerBottomSheet(tag)
                } else {
                    MaterialDialog(getAppCompatActivity()).show {
                        lifecycleOwner(getAppCompatActivity())
                        message(R.string.desc_service_permission)
                        positiveButton(
                            res = R.string.action_ok,
                            click = {
                                it.dismiss()
                                initPermission(tag)
                            }
                        )
                        negativeButton(
                            res = R.string.action_cancel,
                            click = {
                                it.dismiss()
                                initPermission(tag)
                            }
                        )
                    }
                }
            }.addTo(disposables)
    }

    private fun validateForm() {
        Observable.combineLatest(
                mutableListOf<Observable<Boolean>>(
                    viewModel.originalApplicationHasFileInput,
                    viewModel.certificateOfDtiHasFileInput,
                    viewModel.mayorsPermitHasFileInput
                )
            ) { arrays ->
                arrays.forEach {
                    if (!(it as Boolean)) {
                        return@combineLatest false
                    }
                }
                return@combineLatest true
            }
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnNext {
                viewModel.isValidFormInput.onNext(it)
            }
            .subscribe()
            .addTo(disposables)
    }

    override fun onClickNext() {
        if (viewModel.hasValidForm()) {
            findNavController().navigate(R.id.action_personal_information_step_one)
        } else {
            refreshFields()
            showMissingFieldDialog()
        }
    }

    private fun showMissingFieldDialog() {
        showMaterialDialogError(
            message = formatString(R.string.msg_error_missing_fields)
        )
    }

    private fun refreshFields() {
        if (!viewModel.originalApplicationHasFileInput.hasValue()) {
            viewModel.originalApplicationHasFileInput.onNext(false)
        }
        if (!viewModel.certificateOfDtiHasFileInput.hasValue()) {
            viewModel.certificateOfDtiHasFileInput.onNext(false)
        }
        if (!viewModel.mayorsPermitHasFileInput.hasValue()) {
            viewModel.mayorsPermitHasFileInput.onNext(false)
        }
    }

    @ThreadSafe
    companion object {
        const val TAG_ORIGINAL_APPLICATION_DIALOG = "original_application_dialog"
        const val TAG_CERTIFICATION_OF_DTI_DIALOG = "certification_of_dti_dialog"
        const val TAG_MAYORS_PERMIT_DIALOG = "mayors_permit_dialog"

        const val ORIGINAL_APPLICATION_FILE_REQUEST_CODE = 4
        const val CERTIFICATION_OF_DTI_FILE_REQUEST_CODE = 5
        const val MAYORS_PERMIT_FILE_REQUEST_CODE = 6

        const val ORIGINAL_APPLICATION_PHOTO_REQUEST_CODE = 1
        const val CERTIFICATION_OF_DTI_PHOTO_REQUEST_CODE = 2
        const val MAYORS_PERMIT_PHOTO_REQUEST_CODE = 3
        val VALID_EXTENSIONS = mutableListOf("jpg", "png", "pdf")
    }

    override val viewModelClassType: Class<DaoBusinessRegistrationPapersViewModel>
        get() = DaoBusinessRegistrationPapersViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBusinessRegistrationPapersBinding
        get() = FragmentBusinessRegistrationPapersBinding::inflate
}
