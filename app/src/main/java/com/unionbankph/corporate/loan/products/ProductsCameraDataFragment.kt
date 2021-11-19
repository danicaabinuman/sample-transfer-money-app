package com.unionbankph.corporate.loan.products

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import com.jumio.analytics.JumioAnalytics.finish
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentProductsCameraDataBinding
import com.unionbankph.corporate.loan.products.camera.ProductsCameraActivity

class ProductsCameraDataFragment : BaseFragment<FragmentProductsCameraDataBinding,
        ProductsCameraDataViewModel>(), ProductsCameraDataHandler
{

    private val activity by lazyFast { getAppCompatActivity() as ProductsCameraActivity }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentProductsCameraDataBinding
        get() = FragmentProductsCameraDataBinding::inflate

    override val viewModelClassType: Class<ProductsCameraDataViewModel>
        get() = ProductsCameraDataViewModel::class.java

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
    }

    private fun checkArgs() {
        arguments?.let {
            if (it.containsKey(ARGS_IMAGE_FILE)) {
                //viewModel.setImage(it.getString(ProductsCameraPreviewFragment.ARGS_IMAGE_FILE).toString())
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_toolbar, menu)
        this@ProductsCameraDataFragment.menu = menu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity.finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initObservers()
    }

    private fun initObservers() {
        viewModel.apply {

        }
        binding.lifecycleOwner = this
        binding.handler = this
    }

    private fun initViews() {

        activity.apply {
            showProgress(true)
            setProgressValue(6)
        }
    }

    companion object {
        const val ARGS_IMAGE_FILE = "_image_file"
    }

    override fun onAddPhotos() {
//        findNavController().navigate(R.id.nav_to_productsCameraFragment)
    }

}
