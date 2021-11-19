package com.unionbankph.corporate.loan.products

import android.Manifest
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.otaliastudios.cameraview.CameraException
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Flash
import com.tbruyelle.rxpermissions2.RxPermissions
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.observe
import com.unionbankph.corporate.databinding.FragmentProductsCameraBinding
import com.unionbankph.corporate.loan.products.camera.ProductsCameraActivity
import io.reactivex.rxkotlin.addTo
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class ProductsCameraFragment : BaseFragment<FragmentProductsCameraBinding,
        ProductsCameraViewModel>(), ProductsCameraHandler
{

    private val activity by lazyFast { getAppCompatActivity() as ProductsCameraActivity }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentProductsCameraBinding
        get() = FragmentProductsCameraBinding::inflate

    override val viewModelClassType: Class<ProductsCameraViewModel>
        get() = ProductsCameraViewModel::class.java

    private var menu: Menu? = null

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        setHasOptionsMenu(true)

        activity.apply {
            showProgress(false)
            setToolbarTitle(
                activity.binding.tvToolbar,
                ""
            )
        }
    }

    override fun onViewsBound() {
        super.onViewsBound()
        //checkArgs()
        initViews()
        initCheckPermission()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initObservers()
    }

    private fun checkArgs() {
        arguments?.let {
            if (it.containsKey(ARGS_CAMERA_MODE)) {
                viewModel.setCameraMode(it.getInt(ARGS_CAMERA_MODE))
            }
        }
    }
    private fun initObservers() {

        viewModel.apply {
            observe(image, ::handleImage)
            observe(flash) {
                it?.let {
                    if (it) {
                        binding.cameraView.flash = Flash.TORCH
                    } else {
                        binding.cameraView.flash = Flash.OFF
                    }
                }
            }
        }
        binding.lifecycleOwner = this
        binding.handler = this
        binding.viewModel = viewModel
    }

    private fun handleImage(file: File?) {
        file?.let {
            /*findNavController().navigate(
                R.id.nav_to_productsCameraPreviewFragment,
                bundleOf(ProductsCameraPreviewFragment.ARGS_IMAGE_FILE to it.toString())
            )*/
/*            findNavController().navigate(
                R.id.nav_to_productsCameraPreviewFragment,
                bundleOf(ProductsCameraDataFragment.ARGS_IMAGE_FILE to it.toString())
            )*/
/*            when (viewModel.cameraMode.value) {
                ADD_PHOTO -> {

                }
                NEW_PHOTO -> {

                }
            }*/
            findNavController().navigate(
                R.id.nav_to_productsCameraDataFragment,
                bundleOf(ProductsCameraDataFragment.ARGS_IMAGE_FILE to it.toString())
            )
            viewModel.setImage(null)
        }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_toolbar, menu)
        this@ProductsCameraFragment.menu = menu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            R.id.menu_flash_on_black -> {
                handleFlash(false)
                true
            }
            R.id.menu_flash_off_black -> {
                handleFlash(true)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.menu_flash_off_black).isVisible = true
    }

    private fun handleFlash(status: Boolean?) {
        status?.let {
            viewModel.setFlash(it)
            menu?.findItem(R.id.menu_flash_off_black)?.isVisible = !it
            menu?.findItem(R.id.menu_flash_on_black)?.isVisible = it
        }
    }

    private fun initViews() {

        binding.apply {
            cameraView.setLifecycleOwner(this@ProductsCameraFragment)
            cameraView.addCameraListener(object : CameraListener() {

                override fun onPictureTaken(result: PictureResult) {
                    super.onPictureTaken(result)
                    val timeStamp: String = SimpleDateFormat("HHmmss").format(Date())
                    result.toFile(File(context?.filesDir, "${timeStamp}.jpg")) {
                        it?.let { viewModel?.onPictureTaken(it) }
                    }
                }

                override fun onCameraError(exception: CameraException) {
                    super.onCameraError(exception)
                    handleOnError(exception)
                }
            })
        }
    }

    override fun onNext() {

    }

    override fun onCaptureImage() {
        binding.cameraView.takePicture()
    }

    companion object {
        const val ARGS_CAMERA_MODE = "_camera_mode"
    }

}

