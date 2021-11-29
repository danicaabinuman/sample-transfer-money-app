package com.unionbankph.corporate.loan.products.camera

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.databinding.ActivityProductCameraBinding


class ProductsCameraActivity : BaseActivity<ActivityProductCameraBinding, ProductsCameraMainViewModel>() {

    private lateinit var navHostFragment: NavHostFragment

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.toolbar, binding.appBarLayout)
        setDrawableBackButton(R.drawable.ic_msme_back_button_orange, R.color.colorDarkOrange, true)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initNavHost()
    }

    fun setProgressValue(step: Int) {
        val progressValue = step * 100 / 7
        ObjectAnimator.ofInt(binding.progressAction, "progress", progressValue)
            .setDuration(150)
            .start()
    }

    fun showProgress(isShown: Boolean) {
        binding.progressAction.visibility(isShown)
    }

    private fun initNavHost() {
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_product_camera_host_fragment) as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_camera)
        navHostFragment.navController.graph = graph
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_product_camera_host_fragment)
        return when(navController.currentDestination?.id) {
            R.id.nav_to_productsCameraDataFragment -> {
                finish()
                true
            }
            else -> navController.navigateUp()
        }
    }

    override val viewModelClassType: Class<ProductsCameraMainViewModel>
        get() = ProductsCameraMainViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityProductCameraBinding
        get() = ActivityProductCameraBinding::inflate

}