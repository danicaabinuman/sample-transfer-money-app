package com.unionbankph.corporate.loan.products

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.loadImage
import com.unionbankph.corporate.app.common.extension.observe
import com.unionbankph.corporate.databinding.FragmentProductsCameraPreviewBinding
import com.unionbankph.corporate.loan.LoanActivity


class ProductsCameraPreviewFragment : BaseFragment<FragmentProductsCameraPreviewBinding,
        ProductsCameraPreviewViewModel>(), ProductsCameraPreviewHandler
{

    private val activity by lazyFast { getAppCompatActivity() as LoanActivity }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentProductsCameraPreviewBinding
        get() = FragmentProductsCameraPreviewBinding::inflate

    override val viewModelClassType: Class<ProductsCameraPreviewViewModel>
        get() = ProductsCameraPreviewViewModel::class.java

    private lateinit var currentCroppedBitmap: Bitmap

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
        checkArgs()
        initViews()
    }

    private fun checkArgs() {
        arguments?.let {
            if (it.containsKey(ARGS_IMAGE_FILE)) {
                viewModel.setImage(it.getString(ARGS_IMAGE_FILE).toString())
            }
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_toolbar, menu)
        this@ProductsCameraPreviewFragment.menu = menu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            R.id.menu_crop -> {
                handleCrop()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleCrop() {

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.menu_crop_black).isVisible = true
    }

    private fun initObservers() {
        viewModel.apply {
            observe(image, ::handleImage)
        }
        binding.lifecycleOwner = this
        binding.handler = this
        binding.viewModel = viewModel
    }

    private fun handleImage(file: String?) {
        file?.let {
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(file, options)
            options.inJustDecodeBounds = false
            currentCroppedBitmap = BitmapFactory.decodeFile(file, options)
            binding.ivPreview.loadImage(currentCroppedBitmap)
            binding.ivPreview.postDelayed(
                {
                    binding.ivPreview.zoomOut()
                }, 100
            )
        }
    }

    private fun initViews() {

        activity.apply {
            showProgress(false)
        }
    }

    override fun usePhoto() {
    }

    override fun onRetake() {
    }

    companion object {
        const val ARGS_IMAGE_FILE = "_image_file"
    }
}