package com.unionbankph.corporate.mcd.presentation.preview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.util.BitmapUtil
import com.unionbankph.corporate.app.util.FileUtil
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.ActivityCheckDepositPreviewBinding
import com.unionbankph.corporate.mcd.data.model.CheckDepositUpload
import com.unionbankph.corporate.mcd.presentation.camera.CheckDepositCameraActivity
import com.unionbankph.corporate.mcd.presentation.confirmation.CheckDepositConfirmationActivity
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositScreenEnum
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositTypeEnum
import com.unionbankph.corporate.mcd.presentation.form.CheckDepositFormActivity
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by herald25santos on 2019-10-23
 */
class CheckDepositPreviewActivity :
    BaseActivity<ActivityCheckDepositPreviewBinding, CheckDepositPreviewViewModel>() {

    @Inject
    lateinit var fileUtil: FileUtil

    @Inject
    lateinit var bitmapUtil: BitmapUtil

    private lateinit var filePath: String

    private lateinit var currentCroppedBitmap: Bitmap

    private var isCroppedMode = false

    private val checkDepositType by lazyFast { intent.getStringExtra(EXTRA_CHECK_DEPOSIT_TYPE) }

    private val screen by lazyFast { intent.getStringExtra(EXTRA_SCREEN) }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        updateViews()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initBinding()
        init()
        initCheckDepositPreview()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initCheckDepositViewModel()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initListener()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        val menuCrop = menu.findItem(R.id.menu_crop)
        menuCrop.isVisible = screen != CheckDepositScreenEnum.SUMMARY.name &&
                screen != CheckDepositScreenEnum.DETAIL.name &&
                screen != CheckDepositScreenEnum.CONFIRMATION.name && !isCroppedMode
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_crop -> {
                onCroppingMode()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
            (screen == CheckDepositScreenEnum.CONFIRMATION.name ||
                    screen == CheckDepositScreenEnum.SUMMARY.name)
        ) {
            supportFinishAfterTransition()
        } else {
            super.onBackPressed()
        }
    }

    private fun initCheckDepositViewModel() {
        viewModel.uiState.observe(this, Observer {
            it.getContentIfNotHandled().let { event ->
                when (event) {
                    is UiState.Loading -> {
                        showProgressAlertDialog(CheckDepositConfirmationActivity::class.java.simpleName)
                    }
                    is UiState.Complete -> {
                        dismissProgressAlertDialog()
                    }
                    is UiState.Error -> {
                        handleOnError(event.throwable)
                    }
                }
            }
        })
        viewModel.checkDepositUpload.observe(this, Observer {
            uploadedFile(it)
        })
    }

    private fun uploadedFile(checkDepositUpload: CheckDepositUpload) {
        when {
            screen.equals(CheckDepositScreenEnum.FRONT_OF_CHECK.name, true) -> {
                navigator.navigate(
                    this,
                    CheckDepositCameraActivity::class.java,
                    Bundle().apply {
                        putString(
                            CheckDepositCameraActivity.EXTRA_SCREEN,
                            CheckDepositScreenEnum.BACK_OF_CHECK.name
                        )
                        putString(
                            CheckDepositCameraActivity.EXTRA_CHECK_DEPOSIT_TYPE,
                            CheckDepositTypeEnum.BACK_OF_CHECK.name
                        )
                        putString(
                            CheckDepositCameraActivity.EXTRA_CHECK_DEPOSIT_UPLOAD,
                            JsonHelper.toJson(checkDepositUpload)
                        )
                    },
                    isClear = false,
                    isAnimated = true,
                    transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
                )
            }
            screen.equals(CheckDepositScreenEnum.BACK_OF_CHECK.name, true) -> {
                navigator.navigate(
                    this,
                    CheckDepositFormActivity::class.java,
                    bundle = Bundle().apply {
                        putString(
                            CheckDepositFormActivity.EXTRA_CHECK_DEPOSIT_UPLOAD,
                            JsonHelper.toJson(checkDepositUpload)
                        )
                    },
                    isClear = false,
                    isAnimated = true,
                    transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
                )
            }
            else -> {
                eventBus.actionSyncEvent.emmit(
                    BaseEvent(
                        ActionSyncEvent.ACTION_UPDATE_CHECK_DEPOSIT_PHOTO,
                        checkDepositType.notNullable()
                    )
                )
                onBackPressed()
            }
        }
    }

    private fun initListener() {
        RxView.clicks(binding.buttonUseThis)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                when (screen) {
                    CheckDepositScreenEnum.FRONT_OF_CHECK.name -> {
                        uploadFile(FRONT_CHECK)
                    }
                    CheckDepositScreenEnum.BACK_OF_CHECK.name -> {
                        uploadFile(BACK_CHECK)
                    }
                }
            }.addTo(disposables)
        binding.buttonRetake.setOnClickListener {
            onBackPressed()
        }
        binding.tvDone.setOnClickListener {
            onClickedDone()
        }
        binding.tvCancel.setOnClickListener {
            onClickedCancel()
        }
    }

    private fun onClickedCancel() {
        exitCroppingMode()
    }

    private fun onClickedDone() {
        if (binding.imageViewCrop.canRightCrop()) {
            currentCroppedBitmap = binding.imageViewCrop.crop()
            exitCroppingMode()
        }
    }

    private fun onCroppingMode() {
        isCroppedMode = true
        invalidateOptionsMenu()
        viewUtil.startAnimateView(true, binding.tvCancel, android.R.anim.fade_in)
        viewUtil.startAnimateView(true, binding.tvDone, android.R.anim.fade_in)
        viewUtil.startAnimateView(true, binding.imageViewCrop, android.R.anim.fade_in)
        viewUtil.startAnimateView(false, binding.buttonUseThis, android.R.anim.fade_out)
        viewUtil.startAnimateView(false, binding.buttonRetake, android.R.anim.fade_out)
        viewUtil.startAnimateView(false, binding.ivPreview, android.R.anim.fade_out)
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        options.inJustDecodeBounds = false
        currentCroppedBitmap = BitmapFactory.decodeFile(filePath, options)
        binding.imageViewCrop.setImageToCrop(currentCroppedBitmap)
    }

    private fun exitCroppingMode() {
        isCroppedMode = false
        invalidateOptionsMenu()
        viewUtil.startAnimateView(false, binding.tvCancel, android.R.anim.fade_out)
        viewUtil.startAnimateView(false, binding.tvDone, android.R.anim.fade_out)
        viewUtil.startAnimateView(false, binding.imageViewCrop, android.R.anim.fade_out)
        viewUtil.startAnimateView(true, binding.buttonUseThis, android.R.anim.fade_in)
        viewUtil.startAnimateView(true, binding.buttonRetake, android.R.anim.fade_in)
        viewUtil.startAnimateView(true, binding.ivPreview, android.R.anim.fade_in)
        binding.ivPreview.loadImage(currentCroppedBitmap)
        binding.ivPreview.postDelayed(
            {
                binding.ivPreview.zoomOut()
            }, 100
        )
    }

    private fun init() {
        filePath = intent.getStringExtra(EXTRA_FILE_PATH).notNullable()
    }

    private fun initBinding() {
        intent.getStringExtra(EXTRA_CHECK_DEPOSIT_UPLOAD)?.let {
            viewModel.setCheckDepositUpload(it)
        }
    }

    private fun initCheckDepositPreview() {
        if (screen == CheckDepositScreenEnum.FRONT_OF_CHECK.name ||
            screen == CheckDepositScreenEnum.BACK_OF_CHECK.name
        ) {
            onCroppingMode()
        } else if (screen == CheckDepositScreenEnum.DETAIL.name) {
            binding.ivPreview.loaderImageByUrl(filePath, binding.progressBar)
        } else {
            binding.ivPreview.loadImageByPath(filePath)
        }
        when (screen) {
            CheckDepositScreenEnum.CONFIRMATION.name,
            CheckDepositScreenEnum.SUMMARY.name,
            CheckDepositScreenEnum.DETAIL.name -> {
                binding.buttonUseThis.visibility(false)
                binding.buttonRetake.visibility(false)
                binding.tvDone.visibility(false)
                binding.tvCancel.visibility(false)
            }
        }
    }

    private fun updateViews() {
        when (checkDepositType) {
            CheckDepositTypeEnum.FRONT_OF_CHECK.name -> {
                setToolbarTitle(binding.viewToolbar.tvToolbar, formatString(R.string.title_front_of_check))
            }
            CheckDepositTypeEnum.BACK_OF_CHECK.name -> {
                setToolbarTitle(binding.viewToolbar.tvToolbar, formatString(R.string.title_back_of_check))
            }
        }
    }

    private fun saveFile(bitmap: Bitmap): String? {
        val file = bitmapUtil.saveBitmap(
            bitmap,
            filename = "${checkDepositType}_CROPPED",
            format = Bitmap.CompressFormat.JPEG
        )
        return file?.path
    }

    private fun rotateImage(bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(-90f)
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    private fun uploadFile(fileKey: String) {
        val bitmap = rotateImage(currentCroppedBitmap)
        val filePath = saveFile(bitmap)
        val file = File(filePath.notNullable())
        Timber.d("filePath: $filePath")
        Timber.d("fileName: ${file.name}")
        viewModel.uploadCheckDeposit(file, fileKey)
    }

    companion object {
        const val EXTRA_SCREEN = "screen"
        const val EXTRA_CHECK_DEPOSIT_TYPE = "check_deposit_type"
        const val EXTRA_FILE_PATH = "file_path"
        const val FRONT_CHECK = "front"
        const val BACK_CHECK = "back"
        const val EXTRA_CHECK_DEPOSIT_UPLOAD = "check_deposit_upload"
    }

    override val layoutId: Int
        get() = R.layout.activity_check_deposit_preview

    override val viewModelClassType: Class<CheckDepositPreviewViewModel>
        get() = CheckDepositPreviewViewModel::class.java
}
