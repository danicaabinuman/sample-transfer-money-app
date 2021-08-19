package com.unionbankph.corporate.payment_link.presentation.onboarding.camera

import android.Manifest
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.otaliastudios.cameraview.CameraException
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraLogger
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Flash
import com.tbruyelle.rxpermissions2.RxPermissions
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.util.BitmapUtil
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.presentation.onboarding.upload_photos.OnboardingUploadPhotosActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_onboarding_camera.*
import kotlinx.android.synthetic.main.activity_onboarding_camera.viewToolbar
import kotlinx.android.synthetic.main.activity_onboarding_upload_photos.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.toolbar
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class OnboardingCameraActivity :
    BaseActivity<OnboardingCameraViewModel>(R.layout.activity_onboarding_camera) {

    @Inject
    lateinit var bitmapUtil: BitmapUtil

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setDrawableBackButton(
            R.drawable.ic_msme_back_button_orange,
            R.color.colorSMEMediumOrange,
            true
        )
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[OnboardingCameraViewModel::class.java]
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
            navigateImageScreen(it.path)
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initCameraView()
        initCheckPermission()
        initBinding()
    }

    private fun initBinding() {
        viewModel.isFlashOn
            .subscribe {
                if (it) {
                    cameraView.flash = Flash.TORCH
                } else {
                    cameraView.flash = Flash.OFF
                }
                invalidateOptionsMenu()
            }.addTo(disposables)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        imageViewCapture.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return@setOnClickListener
            cameraView.takePicture()
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
        super.onBackPressed()
        val bundle = Bundle().apply {
            putParcelableArrayList(
                OnboardingUploadPhotosActivity.LIST_OF_IMAGES_URI,
                intent.getParcelableArrayListExtra<Uri>(OnboardingUploadPhotosActivity.LIST_OF_IMAGES_URI)
            )
        }
        navigator.navigate(
            this,
            OnboardingUploadPhotosActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = true
        )
    }


    private fun initCameraView() {
        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE)
        with(cameraView) {
            setLifecycleOwner(this@OnboardingCameraActivity)
            addCameraListener(cameraViewListener)
        }
    }

    private fun initCheckPermission() {
        RxPermissions(this)
            .request(Manifest.permission.CAMERA)
            .subscribe { granted ->
                if (granted) {
                    cameraView.open()
                } else {
                    initCheckPermission()
                }
            }.addTo(disposables)
    }

    private var cameraViewListener = object : CameraListener() {

        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            val timeStamp: String = SimpleDateFormat("HHmmss").format(Date())
            result.toFile(File(filesDir, "${timeStamp}.jpg")) {
                it?.let { viewModel.onPictureTaken(it) }
            }
        }

        override fun onCameraError(exception: CameraException) {
            super.onCameraError(exception)
            Timber.e(exception, "onCameraError")
            handleOnError(exception)
        }
    }

    private fun navigateImageScreen(filePath: String?) {
        val bundle = Bundle().apply {
            putString(
                OnboardingImagePreviewActivity.EXTRA_FILE_PATH,
                filePath
            )
            putParcelableArrayList(
                OnboardingUploadPhotosActivity.LIST_OF_IMAGES_URI,
                intent.getParcelableArrayListExtra<Uri>(OnboardingUploadPhotosActivity.LIST_OF_IMAGES_URI)
            )

        }
        navigator.navigate(
            this,
            OnboardingImagePreviewActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }
}