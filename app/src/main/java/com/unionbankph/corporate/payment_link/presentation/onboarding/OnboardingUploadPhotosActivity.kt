package com.unionbankph.corporate.payment_link.presentation.onboarding

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.tbruyelle.rxpermissions2.RxPermissions
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.util.FileUtil
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_channels.PaymentLinkChannelsActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_onboarding_upload_photos.*
import kotlinx.android.synthetic.main.activity_onboarding_upload_photos.viewToolbar
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.toolbar
import kotlinx.android.synthetic.main.widget_transparent_rmo_appbar.*
import java.io.File
import java.io.IOException
import java.util.*
import javax.annotation.concurrent.ThreadSafe
import javax.inject.Inject
import kotlin.concurrent.timerTask

class OnboardingUploadPhotosActivity :
    BaseActivity<RequestPaymentSplashViewModel>(R.layout.activity_onboarding_upload_photos),
    OnboardingUploadPhotosFragment.OnOnboardingUploadPhotosFragmentInteraction {

    @Inject
    lateinit var fileUtil: FileUtil
    val REQUEST_CODE = 200
    val uriArrayList = arrayListOf<Uri>()
    var adapter : BaseAdapter? = null
        private var onboardingUploadFragment: OnboardingUploadPhotosFragment? = null
    private var onboardingDeletePhotosFragment: OnboardingDeletePhotosFragment? = null

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
        viewModel =
            ViewModelProviders.of(
                this,
                viewModelFactory
            )[RequestPaymentSplashViewModel::class.java]
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initBinding()
        initOnClicks()

    }
    
    private fun initOnClicks(){
        btnAddPhotos.setOnClickListener {
            showbottomSheetDialog()
        }
        btnAddPhotos2.setOnClickListener {
            showbottomSheetDialog()
        }
        btnNext.setOnClickListener {
            showSnackbar()
        }
    }

    private fun initBinding(){
        viewModel.originalApplicationFileInput
            .filter {
                        viewModel.originalApplicationFileInput.hasValue()
            }
            .subscribe {
                uriArrayList.add(Uri.parse(it.absolutePath))
                buttonsVisibility()
                imageBtnsListener()
            }.addTo(disposables)
        viewModel.originalApplicationUriInput
            .filter {
                viewModel.originalApplicationUriInput.hasValue()
            }
            .subscribe {
                uriArrayList.add(it)
                buttonsVisibility()
                imageBtnsListener()
            }.addTo(disposables)
    }

    private fun imageBtnsListener(){
        adapter = UploadPhotosCustomAdapter(this, uriArrayList)
        gv.adapter = adapter
        if (uriArrayList.size < 6) {
            gv.setOnItemClickListener { parent, view, position, id ->
                val itemUri = uriArrayList[position]
                ivFullscreenImage.setImageURI(itemUri)
                clUploadPhotosIntro.visibility(false)
                clSelectedPhotos.visibility(false)
                btnSaveAndExit.visibility(false)
                btnNext.visibility(false)
                clDeleteSelectedPhoto.visibility(true)
                btnDelete.visibility(true)

                btnDelete.setOnClickListener {
                    uriArrayList.remove(itemUri)
                    adapter?.notifyDataSetChanged()
                    clDeleteSelectedPhoto.visibility(false)
                    btnDelete.visibility(false)
                    clSelectedPhotos.visibility(true)
                    btnNext.visibility(true)
                    btnSaveAndExit.visibility(true)
                    btnAddPhotos2.visibility(true)
                }

                if (uriArrayList.size == 6) {
                    btnAddPhotos2.visibility(false)
                }
            }
        }else{
            btnAddPhotos2.visibility(false)
        }
    }

    private fun buttonsVisibility(){
        clUploadPhotosIntro.visibility(false)
        clSelectedPhotos.visibility(true)
        btnNext.visibility(true)
        btnSaveAndExit.visibility(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE -> {
                    if (data?.clipData != null) {
                        val uri = data.clipData
                        val count = uri!!.itemCount
                        if(count + uriArrayList.size <= 6 ){
                            buttonsVisibility()
                            for (i in 0 until count) {
                                val imageUri = data.clipData!!.getItemAt(i).uri
                                uriArrayList.add(imageUri)
                                imageBtnsListener()
                            }
                        }else{
                            showMaterialDialogError(message = getString(R.string.msg_maximum_of_6_photos_only))
                            }

                    } else if (data?.data != null) {
                        viewModel.originalApplicationUriInput.onNext(data.data!!)
                    }
                }
                CAPTURE_PHOTO -> {
                    viewModel.currentFile.value?.let {
                        viewModel.originalApplicationFileInput.onNext(it)
                    }
                }

            }
        }
    }

    private fun showbottomSheetDialog() {
        if (onboardingUploadFragment == null) {
            onboardingUploadFragment = OnboardingUploadPhotosFragment.newInstance()
        }
        onboardingUploadFragment!!.show(supportFragmentManager, OnboardingUploadPhotosFragment.TAG)
    }

    private fun showSnackbar(){
        Snackbar.make(snackbar, "Uploading photo...", Snackbar.LENGTH_LONG).setAnchorView(R.id.btnNext).show()
        val intent = Intent(this, PaymentLinkChannelsActivity::class.java)
        Timer().schedule(timerTask {
            startActivity(intent)
        }, 1000)
    }

    private fun openGalleryForImages() {
        if (Build.VERSION.SDK_INT < 19) {
            var intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Choose pictures"), REQUEST_CODE)
        } else {
            var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE)
        }
    }

    @ThreadSafe
    companion object {
        const val CAPTURE_PHOTO = 1
        const val REQUEST_CODE = 1209
        const val RESULT_SHOULD_GENERATE_NEW_LINK = "result_should_generate_new_link"
        const val EXTRA_SETUP_MERCHANT_DETAILS = "extra_setup_merchant_details"
    }

    override fun openGallery() {
        openGalleryForImages()
        onboardingUploadFragment?.dismiss()
    }

    override fun openCamera() {
        initPermission()
        onboardingUploadFragment?.dismiss()
    }

    private fun initPermission() {
        RxPermissions(this)
            .request(Manifest.permission.CAMERA)
            .subscribe { granted ->
                if (granted) {
//                    cameraView.open()
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                        takePictureIntent.resolveActivity(packageManager)?.also {
                            val photoFile: File? = try {
                                fileUtil.createCaptureImageFile()
                            } catch (ex: IOException) {
                                showMaterialDialogError(message = ex.message.notNullable())
                                null
                            }
                            photoFile?.let {
                                viewModel.currentFile.onNext(it)
                                it.also {
                                    val photoURI: Uri = FileProvider.getUriForFile(
                                        this,
                                        "${BuildConfig.APPLICATION_ID}.fileprovider",
                                        it
                                    )
                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                                    startActivityForResult(takePictureIntent, CAPTURE_PHOTO)
                                }
                            }
                        }
                    }
                } else {
                    initPermission()
                }
            }.addTo(disposables)
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        if (clDeleteSelectedPhoto.isShown){
            clDeleteSelectedPhoto.visibility = View.GONE
            clSelectedPhotos.visibility = View.VISIBLE
            btnNext.visibility = View.VISIBLE
        } else if (clSelectedPhotos.isShown || clUploadPhotosIntro.isShown){
            super.onBackPressed()
        }
    }
}