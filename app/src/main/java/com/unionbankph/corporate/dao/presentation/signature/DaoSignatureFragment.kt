package com.unionbankph.corporate.dao.presentation.signature

import android.Manifest
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.otaliastudios.cameraview.CameraException
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraLogger
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Flash
import com.tbruyelle.rxpermissions2.RxPermissions
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.setColor
import com.unionbankph.corporate.app.common.extension.showToast
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.domain.model.DaoHit
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.dao.presentation.result.DaoResultFragment
import com.unionbankph.corporate.databinding.FragmentDaoSignatureBinding
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.io.File
import javax.annotation.concurrent.ThreadSafe

class DaoSignatureFragment :
    BaseFragment<FragmentDaoSignatureBinding, DaoSignatureViewModel>() {

    private val daoActivity by lazyFast { getAppCompatActivity() as DaoActivity }

    override fun beforeLayout(savedInstanceState: Bundle?) {
        super.beforeLayout(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        initCameraView()
        initPermission()
        initBinding()
    }

    private fun initBinding() {
        viewModel.loadDaoForm(daoActivity.viewModel.defaultDaoForm())
        viewModel.isFlashOn
            .subscribe {
                if (it) {
                    binding.cameraView.flash = Flash.TORCH
                } else {
                    binding.cameraView.flash = Flash.OFF
                }
                getAppCompatActivity().invalidateOptionsMenu()
            }.addTo(disposables)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        initClickListener()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_toolbar, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
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
        menuFlash.icon?.let {
            it.mutate()
            it.setColor(requireActivity(), R.color.colorInfo)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_flash -> {
                viewModel.isFlashOn.value?.let {
                    viewModel.isFlashOn.onNext(!it)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    showProgressAlertDialog(this::class.java.simpleName)
                }
                is UiState.Complete -> {
                    dismissProgressAlertDialog()
                }
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.navigateSignaturePreview.observe(viewLifecycleOwner, EventObserver {
            navigateSignaturePreview(it.path)
        })
        viewModel.navigateNextStep.observe(viewLifecycleOwner, EventObserver {
            daoActivity.setSignatureInput(it)
            if (viewModel.isEditMode.value == true) {
                requireActivity().onBackPressed()
            } else {
                findNavController().navigate(R.id.action_dao_confirmation_fragment)
            }
        })
        if (daoActivity.viewModel.hasSignatureInput.hasValue() &&
            !viewModel.isLoadedScreen.hasValue()
        ) {
            daoActivity.viewModel.input6.let {
                viewModel.setExistingSignature(it)
            }
        }
        arguments?.getBoolean(EXTRA_IS_EDIT, false)?.let {
            viewModel.isEditMode.onNext(it)
        }
    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable
            .subscribe {
                if (it.eventType == ActionSyncEvent.ACTION_SIGNATURE_USE_THIS) {
                    viewModel.onSubmitSignatureFile(File(it.payload.notNullable()))
                } else if (it.eventType == ActionSyncEvent.ACTION_SIGNATURE_USE_THIS_SKIP) {
                    val daoHit = JsonHelper.fromJson<DaoHit>(it.payload)
                    navigationDaoResult(daoHit)
                }
            }.addTo(disposables)
    }

    private fun initClickListener() {
        binding.imageViewCapture.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return@setOnClickListener
            binding.cameraView.takePicture()
        }
    }

    private fun init() {
        daoActivity.setToolBarDesc(formatString(R.string.title_scan_signature))
        daoActivity.showToolBarDetails()
        daoActivity.showButton(false)
        daoActivity.showProgress(true)
        daoActivity.setProgressValue(6)
    }

    private fun initCameraView() {
        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE)
        with(binding.cameraView) {
            setLifecycleOwner(this@DaoSignatureFragment)
            addCameraListener(cameraViewListener)
            frameProcessingMaxHeight = binding.cameraView.height
            frameProcessingMaxWidth = binding.cameraView.width
        }
        binding.cameraView.frameProcessingMaxHeight
    }

    private fun initPermission() {
        RxPermissions(this)
            .request(Manifest.permission.CAMERA)
            .subscribe { granted ->
                if (granted) {
                    binding.cameraView.open()
                } else {
                    initPermission()
                }
            }.addTo(disposables)
    }

    private var cameraViewListener = object : CameraListener() {

        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            result.toFile(File(context?.filesDir, "signature.jpg")) { file ->
                file?.let {
                    viewModel.onTakenSignature(file)
                }
            }
        }

        override fun onCameraError(exception: CameraException) {
            super.onCameraError(exception)
            Timber.e(exception, "onCameraError")
            handleOnError(exception)
        }
    }

    private fun navigateSignaturePreview(path: String) {
        val action = DaoSignatureFragmentDirections.actionSignaturePreviewActivity(
            path,
            JsonHelper.toJson(viewModel.daoForm)
        )
        findNavController().navigate(action)
    }

    private fun navigationDaoResult(daoHit: DaoHit) {
        val action =
            DaoSignatureFragmentDirections.actionDaoResultFragment(
                daoHit.referenceNumber,
                DaoResultFragment.TYPE_REACH_OUT_HIT,
                daoHit.businessName,
                daoHit.preferredBranch,
                daoHit.preferredBranchEmail
            )
        findNavController().navigate(action)
    }

    @ThreadSafe
    companion object {
        const val EXTRA_IS_EDIT = "isEdit"
    }

    override val viewModelClassType: Class<DaoSignatureViewModel>
        get() = DaoSignatureViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentDaoSignatureBinding
        get() = FragmentDaoSignatureBinding::inflate
}
