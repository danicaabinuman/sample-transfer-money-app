package com.unionbankph.corporate.loan.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentProductsBinding
import com.unionbankph.corporate.feature.definition.CameraMode
import com.unionbankph.corporate.loan.LoanActivity

class ProductsFragment : BaseFragment<FragmentProductsBinding,
        ProductsViewModel>(), ProductsHandler
{

    private val activity by lazyFast { getAppCompatActivity() as LoanActivity }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentProductsBinding
        get() = FragmentProductsBinding::inflate

    override val viewModelClassType: Class<ProductsViewModel>
        get() = ProductsViewModel::class.java

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

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
        initViews()
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

    override fun addPhotos() {
        findNavController().navigate(
            R.id.nav_to_productsCameraActivity/*,
            bundleOf(ProductsCameraFragment.ARGS_CAMERA_MODE to CameraMode.NEW_PHOTO)*/
        )
    }

}
