package com.unionbankph.corporate.user_creation.presentation.personalise_settings

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.tbruyelle.rxpermissions2.RxPermissions
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.presentation.constant.PromptTypeEnum
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.FragmentUcPersonaliseSettingsBinding
import com.unionbankph.corporate.settings.presentation.*
import com.unionbankph.corporate.user_creation.presentation.UserCreationActivity
import io.reactivex.rxkotlin.addTo


class UcPersonaliseSettingsFragment :
    BaseFragment<FragmentUcPersonaliseSettingsBinding, UcPersonaliseSettingsViewModel>()  {

    private var isCheckedTrustDevice :Boolean = true
    private var isCheckedNotif :Boolean = true
    private var isCheckedGallery :Boolean = true
    private var isCheckedLocation :Boolean = true
    private var isCheckedContacts :Boolean = true
    private var listPermissions : MutableList<String> = arrayListOf()
    private val userCreationActivity by lazyFast { getAppCompatActivity() as UserCreationActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        userCreationActivity.setIsScreenScrollable(false)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }


    override fun onInitializeListener() {
        super.onInitializeListener()
        initSwitchListener()
        initClickListener()
    }


    private fun initViewModel(){
        viewModel.apply {
            uiState.observe(viewLifecycleOwner, EventObserver {
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
                    is UiState.Exit -> {
                        navigator.navigateClearStacks(
                            getAppCompatActivity(),
                            LoginActivity::class.java,
                            Bundle().apply { putBoolean(LoginActivity.EXTRA_SPLASH_SCREEN, false) },
                            true
                        )
                    }
                }
            })
            getDemoDetails.observe(viewLifecycleOwner, EventObserver {
                //considerAsRecentUser(PromptTypeEnum.TRUST_DEVICE)
                viewModel.getOrgID()
            })
            getLocalSettings.observe(viewLifecycleOwner, EventObserver{
                viewModel.getDemoOrgDetails(it)
            })

            navigateToLocalSettings.observe(viewLifecycleOwner, EventObserver{
                rxPermission()
            })

        }
    }

    private fun initSwitchListener(){
        binding.scTrustDevice.setOnCheckedChangeListener { buttonView, isChecked ->
            isCheckedTrustDevice = isChecked
            binding.scOTP.isChecked = isChecked
        }
        binding.scNotif.setOnCheckedChangeListener { buttonView, isChecked ->
            isCheckedNotif = isChecked
        }
        binding.scCameraOrGallery.setOnCheckedChangeListener { buttonView, isChecked ->
            isCheckedGallery = isChecked
        }
        binding.scLocation.setOnCheckedChangeListener { buttonView, isChecked ->
            isCheckedLocation = isChecked
        }
        binding.scContacts.setOnCheckedChangeListener { buttonView, isChecked ->
            isCheckedContacts = isChecked
        }
    }

    private fun initClickListener(){
        binding.buttonNext.setOnClickListener {
            initOnPermissions()
        }
    }

    private fun initOnPermissions(){
        viewModel.saveSettings(isCheckedNotif, isCheckedTrustDevice,PromptTypeEnum.TRUST_DEVICE)
        when(isCheckedGallery){
            true -> {
                listPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                listPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                listPermissions.add(Manifest.permission.CAMERA)
            }
        }
        when(isCheckedLocation){
            true -> {
                listPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                listPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
        when(isCheckedContacts){
            true -> {
                listPermissions.add(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun rxPermission(){
        when{
            listPermissions.size > 0 -> {
                RxPermissions(this)
                    .request(
                        *listPermissions.map { it }.toTypedArray()
                    )
                    .subscribe { granted ->
                        if (granted) {
                            findNavController().navigate(R.id.action_permission_settings_to_confirmation_message)
                        }
                    }.addTo(disposables)
            }
            else -> {
                findNavController().navigate(R.id.action_permission_settings_to_confirmation_message)
            }
        }
    }


    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUcPersonaliseSettingsBinding
        get() = FragmentUcPersonaliseSettingsBinding::inflate

    override val viewModelClassType: Class<UcPersonaliseSettingsViewModel>
        get() = UcPersonaliseSettingsViewModel::class.java
}