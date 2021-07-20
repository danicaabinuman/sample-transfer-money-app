package com.unionbankph.corporate.mcd.presentation.camera

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import com.otaliastudios.cameraview.CameraException
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraLogger
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Flash
import com.tbruyelle.rxpermissions2.RxPermissions
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.util.BitmapUtil
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.ActivityCheckDepositCameraBinding
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositScreenEnum
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositTypeEnum
import com.unionbankph.corporate.mcd.presentation.list.CheckDepositActivity
import com.unionbankph.corporate.mcd.presentation.preview.CheckDepositPreviewActivity
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by herald25santos on 2019-10-23
 */
class CheckDepositCameraActivity :
    BaseActivity<ActivityCheckDepositCameraBinding, CheckDepositCameraViewModel>() {

    @Inject lateinit var bitmapUtil: BitmapUtil

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    showProgressAlertDialog(this::class.java.simpleName)
                }
                is UiState.Complete -> {
                    dismissProgressAlertDialog()
                }
            }
        })
        viewModel.navigateNextStep.observe(this, EventObserver {
            navigateCheckDepositScreen(it.path)
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initCameraView()
        initCheckPermission()
        init()
        initBinding()
        setupOutputs()
    }

    private fun initBinding() {
        viewModel.isFlashOn
            .subscribe {
                if (it) {
                    binding.cameraView.flash = Flash.TORCH
                } else {
                    binding.cameraView.flash = Flash.OFF
                }
                invalidateOptionsMenu()
            }.addTo(disposables)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.imageViewCapture.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return@setOnClickListener
            binding.cameraView.takePicture()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        val menuFlash = menu.findItem(R.id.menu_flash)
        menuFlash.isVisible = true
        viewModel.isFlashOn.value?.let {
            menuFlash.setIcon(
                if (!it) {
                    R.drawable.ic_flash_off_white_24dp
                } else {
                    R.drawable.ic_flash_on_white_24dp
                }
            )
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_flash -> {
                viewModel.isFlashOn.value?.let {
                    viewModel.isFlashOn.onNext(!it)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        when (viewModel.screen.value) {
            CheckDepositScreenEnum.FRONT_OF_CHECK.name -> {
                navigator.navigateClearUpStack(
                    this,
                    CheckDepositActivity::class.java,
                    null,
                    isClear = true,
                    isAnimated = true
                )
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    private fun init() {
        intent.getStringExtra(EXTRA_SCREEN)?.let {
            viewModel.screen.onNext(it)
        }
        intent.getStringExtra(EXTRA_CHECK_DEPOSIT_TYPE)?.let {
            viewModel.checkDepositType.onNext(it)
        }
        changeTextMessageTips()
    }

    private fun setupOutputs() {
        viewModel.checkDepositType.subscribe {
            when (it) {
                CheckDepositTypeEnum.FRONT_OF_CHECK.name -> {
                    setToolbarTitle(binding.viewToolbar.tvToolbar, formatString(R.string.title_front_of_check))
                }
                CheckDepositTypeEnum.BACK_OF_CHECK.name -> {
                    setToolbarTitle(binding.viewToolbar.tvToolbar, formatString(R.string.title_back_of_check))
                }
            }
        }.addTo(disposables)
    }

    private fun initCameraView() {
        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE)
        with(binding.cameraView) {
            setLifecycleOwner(this@CheckDepositCameraActivity)
            addCameraListener(cameraViewListener)
        }
    }

    private fun changeTextMessageTips() {
        Observable.interval(1, TimeUnit.SECONDS)
            .takeUntil { it == 7L }
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .doOnComplete {
                when (binding.imageViewTips.text) {
                    formatString(R.string.msg_check_deposit_camera_tips_1) -> {
                        binding.imageViewTips.text = formatString(R.string.msg_check_deposit_camera_tips_2)
                    }
                    formatString(R.string.msg_check_deposit_camera_tips_2) -> {
                        binding.imageViewTips.text = formatString(R.string.msg_check_deposit_camera_tips_3)
                    }
                    else -> {
                        binding.imageViewTips.text = formatString(R.string.msg_check_deposit_camera_tips_1)
                    }
                }
                changeTextMessageTips()
            }.subscribe()
            .addTo(disposables)
    }

    private fun initCheckPermission() {
        RxPermissions(this)
            .request(Manifest.permission.CAMERA)
            .subscribe { granted ->
                if (granted) {
                    binding.cameraView.open()
                } else {
                    initCheckPermission()
                }
            }.addTo(disposables)
    }

    private var cameraViewListener = object : CameraListener() {

        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            result.toFile(File(filesDir, "${viewModel.checkDepositType.value}.jpg")) {
                it?.let { viewModel.onPictureTaken(it) }
            }
        }

        override fun onCameraError(exception: CameraException) {
            super.onCameraError(exception)
            Timber.e(exception, "onCameraError")
            handleOnError(exception)
        }
    }

    private fun navigateCheckDepositScreen(filePath: String?) {
        val bundle = Bundle().apply {
            putString(
                CheckDepositPreviewActivity.EXTRA_SCREEN,
                viewModel.screen.value
            )
            putString(
                CheckDepositPreviewActivity.EXTRA_CHECK_DEPOSIT_TYPE,
                viewModel.checkDepositType.value
            )
            putString(
                CheckDepositPreviewActivity.EXTRA_FILE_PATH,
                filePath
            )
            putString(
                CheckDepositPreviewActivity.EXTRA_CHECK_DEPOSIT_UPLOAD,
                intent.getStringExtra(EXTRA_CHECK_DEPOSIT_UPLOAD)
            )
        }
        navigator.navigate(
            this,
            CheckDepositPreviewActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    companion object {
        const val EXTRA_SCREEN = "screen"
        const val EXTRA_CHECK_DEPOSIT_TYPE = "check_deposit_type"
        const val EXTRA_CHECK_DEPOSIT_UPLOAD = "check_deposit_upload"
    }

    override val layoutId: Int
        get() = R.layout.activity_check_deposit_camera

    override val viewModelClassType: Class<CheckDepositCameraViewModel>
        get() = CheckDepositCameraViewModel::class.java
}
