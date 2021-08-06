package com.unionbankph.corporate.user_creation.presentation

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.AppBarLayout
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.user_creation.presentation.enter_name.OAEnterNameViewModel
import kotlinx.android.synthetic.main.activity_open_account.*

class OpenAccountActivity :
    BaseActivity<OpenAccountViewModel>(R.layout.activity_open_account) {

    private lateinit var navHostFragment: NavHostFragment

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, appBarLayout)
        setDrawableBackButton(R.drawable.ic_msme_back_button_orange, R.color.colorDarkOrange, true)
        setIsScreenScrollable(false)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[OpenAccountViewModel::class.java]
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    private fun init() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.openAccountNavHostFragment) as NavHostFragment
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return Navigation.findNavController(
            this, R.id.openAccountNavHostFragment).navigateUp()
    }

    fun showGoBackBottomSheet() {
        val cancelBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_go_back),
            formatString(R.string.msg_go_back),
            formatString(R.string.action_confirm),
            formatString(R.string.action_no)
        )
        cancelBottomSheet.setOnConfirmationPageCallBack(object : OnConfirmationPageCallBack {
            override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
                cancelBottomSheet.dismiss()
                popBackStack()
            }

            override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
                cancelBottomSheet.dismiss()
            }
        })
        cancelBottomSheet.show(
            supportFragmentManager,
            TAG_GO_BACK_DAO_DIALOG
        )
    }

    fun popBackStack() {
        openAccountNavHostFragment.findNavController().popBackStack()
    }

    fun setNameInput(input: OAEnterNameViewModel.Input) {
        viewModel.setNameInput(input)
    }

    fun setIsScreenScrollable(isScrollable: Boolean) {
        val elevation = when (isScrollable) {
            true -> TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics)
            else -> 0f
        }

        supportActionBar?.elevation = elevation
        appBarLayout.elevation = elevation
    }

    companion object {

        const val FRAGMENT_ENTER_NAME = "fragment_enter_name"
        const val TAG_GO_BACK_DAO_DIALOG = "user_creation_go_back_dialog"
    }
}