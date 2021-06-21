package com.unionbankph.corporate.payment_link.presentation.onboarding

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_onboarding_upload_photos.*

class OnboardingUploadPhotosActivity :
    BaseActivity<RequestPaymentSplashViewModel>(R.layout.activity_onboarding_upload_photos) {

    private var imageUri: Uri? = null
    val REQUEST_CODE = 200
    override fun onViewsBound() {
        super.onViewsBound()

        btnAddPhotos.setOnClickListener {
//            showbottomSheetDialog()

//            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
//            startActivityForResult(gallery, pickImage)

            openGalleryForImages()
        }

        btnAddPhotos2.setOnClickListener{
            openGalleryForImages()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {

            if (data?.clipData != null) {
                var count = data.clipData!!.itemCount
                for (i in 0 until count - 1) {
                    imageUri = data.clipData!!.getItemAt(i).uri
                    val uri = data.clipData
                    clUploadPhotosIntro.visibility = View.GONE
                    clSelectedPhotos.visibility = View.VISIBLE
                    btnSaveAndExit.visibility = View.VISIBLE
                    ivPhotoFromGallery.setImageURI(uri!!.getItemAt(0).uri)
                    ivPhotoFromGallery2.setImageURI(uri.getItemAt(1).uri)
                    ivPhotoFromGallery3.setImageURI(uri.getItemAt(2).uri)
                    ivPhotoFromGallery4.setImageURI(uri.getItemAt(3).uri)
                    ivPhotoFromGallery5.setImageURI(uri.getItemAt(4).uri)
                    ivPhotoFromGallery6.setImageURI(uri.getItemAt(5).uri)
                }
            } else if (data?.data != null) {
                imageUri = data.data!!
                clUploadPhotosIntro.visibility = View.GONE
                clSelectedPhotos.visibility = View.VISIBLE
                btnSaveAndExit.visibility = View.VISIBLE
                ivPhotoFromGallery.setImageURI(imageUri)
            }
        }
    }

    private fun showbottomSheetDialog() {
        OnboardingUploadPhotosFragment().apply {
            show(supportFragmentManager, OnboardingUploadPhotosFragment.TAG)
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
}