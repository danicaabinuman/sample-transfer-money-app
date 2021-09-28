package com.unionbankph.corporate.user_creation.presentation.personalise_settings

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.jakewharton.rxbinding2.view.RxView
import com.mtramin.rxfingerprint.RxFingerprint
import com.tbruyelle.rxpermissions2.RxPermissions
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.setVisible
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.bills_payment.data.model.Transaction
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialHasTutorial
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.FragmentUcPersonaliseSettingsBinding
import com.unionbankph.corporate.settings.data.form.ManageDeviceForm
import com.unionbankph.corporate.settings.presentation.*
import com.unionbankph.corporate.settings.presentation.general.GeneralSettingsViewModel
import com.unionbankph.corporate.settings.presentation.profile.ProfileSettingsFragment
import com.unionbankph.corporate.user_creation.presentation.UserCreationActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit


class UcPersonaliseSettingsFragment :
    BaseFragment<FragmentUcPersonaliseSettingsBinding, UcPersonaliseSettingsViewModel>()  {
    private val formDisposable = CompositeDisposable()

    private var isCheckedTrustDevice :Boolean = true
    private var isCheckedOTP :Boolean = true
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
        initClickListener()
    }

    private fun initViewModel(){
        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    showProgressAlertDialog(this::class.java.simpleName)
                }
                is UiState.Complete -> {
                    initRxPermission()
                }
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    private fun initClickListener(){
        binding.scTrustDevice.setOnCheckedChangeListener { buttonView, isChecked ->
            isCheckedTrustDevice = isChecked
            binding.scOTP.isChecked = isChecked
        }
        binding.scOTP.setOnCheckedChangeListener { buttonView, isChecked ->
            isCheckedOTP = isChecked
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

        RxView.clicks(binding.buttonNext)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                initOnPermissions()

                findNavController().navigate(R.id.action_permission_settings_to_confirmation_message)
            }.addTo(formDisposable)
    }

    private fun initOnPermissions(){
        when(isCheckedTrustDevice){
            true -> {
                viewModel.totpSubscribe(ManageDeviceForm(""))
            }
        }
        when(isCheckedOTP){
            true -> {

            }
        }
        when(isCheckedNotif){
            true -> {

            }
        }
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

    private fun initRxPermission(){
        when{
            listPermissions.size > 0 -> {
                RxPermissions(this)
                    .request(
                        *listPermissions.map { it }.toTypedArray()
                    )
                    .subscribe { granted ->
                        if (granted) {

                        }
                    }.addTo(disposables)
            }
            else -> {}
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUcPersonaliseSettingsBinding
        get() = FragmentUcPersonaliseSettingsBinding::inflate

    override val viewModelClassType: Class<UcPersonaliseSettingsViewModel>
        get() = UcPersonaliseSettingsViewModel::class.java
}