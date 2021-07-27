package com.unionbankph.corporate.payment_link.presentation.onboarding

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import com.tbruyelle.rxpermissions2.RxPermissions
import com.google.android.material.snackbar.Snackbar
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_channels.PaymentLinkChannelsActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_card_acceptance_option.*
import kotlinx.android.synthetic.main.activity_onboarding_upload_photos.*
import kotlinx.android.synthetic.main.activity_onboarding_upload_photos.viewToolbar
import kotlinx.android.synthetic.main.bottom_sheet_upload_photos.*
import kotlinx.android.synthetic.main.fragment_dao_signature.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.toolbar
import kotlinx.android.synthetic.main.widget_transparent_rmo_appbar.*
import java.util.logging.Handler

class OnboardingUploadPhotosActivity :
    BaseActivity<RequestPaymentSplashViewModel>(R.layout.activity_onboarding_upload_photos),
    OnboardingUploadPhotosFragment.OnOnboardingUploadPhotosFragmentInteraction {

    private var imageUri: Uri? = null
    val REQUEST_CODE = 200
    lateinit var gridView: GridView
    val uriArrayList = arrayListOf<Uri>()

    private val CAPTURE_PHOTO = 1

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

        addPhotos()
        addMorePhotos()
        navigateToPaymentChannels()
    }
    
    private fun addPhotos(){
        btnAddPhotos.setOnClickListener {
            showbottomSheetDialog()
        }
    }

    private fun addMorePhotos(){
        btnAddPhotos2.setOnClickListener {
            showbottomSheetDialog()
        }
    }

    private fun navigateToPaymentChannels(){
        btnNext.setOnClickListener {
            val snackUploading = Snackbar.make(it, "Uploading photo...", Snackbar.LENGTH_LONG)
            snackUploading.show()

            val intent = Intent(this, PaymentLinkChannelsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        gridView = findViewById(R.id.gv)
        when (requestCode) {
            REQUEST_CODE ->
                if (resultCode == RESULT_OK) {

                    if (data?.clipData != null) {
                        clUploadPhotosIntro.visibility = View.GONE
                        clSelectedPhotos.visibility = View.VISIBLE
                        btnNext.visibility = View.VISIBLE
                        btnSaveAndExit.visibility = View.VISIBLE

                        val uri = data.clipData
                        var count = uri!!.itemCount
                        if (uriArrayList.size < 6) {
                            for (i in 0 until count) {

                                btnAddPhotos2.visibility = View.VISIBLE
                                val imageUri = data.clipData!!.getItemAt(i).uri
                                uriArrayList.add(imageUri)
                                val adapter = UploadPhotosCustomAdapter(this, uriArrayList)

                                gridView.adapter = adapter
                                gridView.onItemClickListener =
                                    AdapterView.OnItemClickListener { parent, view, position, id ->
                                        val itemUri = uriArrayList[position]
                                        clUploadPhotosIntro.visibility = View.GONE
                                        clSelectedPhotos.visibility = View.GONE
                                        btnSaveAndExit.visibility = View.GONE
                                        btnNext.visibility = View.GONE
                                        clDeleteSelectedPhoto.visibility = View.VISIBLE
                                        btnDelete.visibility = View.VISIBLE
                                        ivFullscreenImage.setImageURI(itemUri)

                                        btnDelete.setOnClickListener {
                                            uriArrayList.removeAt(position)
                                            adapter.notifyDataSetChanged()
                                        }

                                        if (uriArrayList.size < 6) {
                                            btnAddPhotos2.visibility = View.VISIBLE
                                        }
                                    }

                                if (uriArrayList.size == 6) {
                                    btnAddPhotos2.visibility = View.GONE
                                    return
                                }

                            }
                        }

                    } else if (data?.data != null) {
                        clUploadPhotosIntro.visibility = View.GONE
                        clSelectedPhotos.visibility = View.VISIBLE
                        btnNext.visibility = View.VISIBLE
                        btnSaveAndExit.visibility = View.VISIBLE

                        if (uriArrayList.size < 6) {
                            val imageUri = data.data!!
                            uriArrayList.add(imageUri)
                            val adapter = UploadPhotosCustomAdapter(this, uriArrayList)
                            gridView.adapter = adapter
//                            gridView.onItemClickListener =
//                                AdapterView.OnItemClickListener { parent, view, position, id ->
//                                    adapter.removeItem(position)
//                                    adapter.notifyDataSetChanged()
//                                }
                            if (uriArrayList.size == 6) {
                                btnAddPhotos2.visibility = View.GONE
                                return
                            }
                        }
                    }
                }

            CAPTURE_PHOTO -> {
                    val photo : Bitmap = data?.extras?.get("data") as Bitmap
                    val adapter = UploadPhotosCustomAdapter(this, uriArrayList)
                    gridView.adapter = adapter
                }
        }
    }

    private fun showbottomSheetDialog() {

        if (onboardingUploadFragment == null) {
            onboardingUploadFragment = OnboardingUploadPhotosFragment.newInstance()
        }

        onboardingUploadFragment!!.show(supportFragmentManager, OnboardingUploadPhotosFragment.TAG)
    }

    fun removePhoto(position: Int){
        btnDelete.setOnClickListener {
            val adapter = UploadPhotosCustomAdapter(this, uriArrayList)
            uriArrayList.removeAt(position)
            adapter.notifyDataSetChanged()
        }

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

    private fun openCameraForImages() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAPTURE_PHOTO)

    }

    companion object {
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
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, CAPTURE_PHOTO)
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