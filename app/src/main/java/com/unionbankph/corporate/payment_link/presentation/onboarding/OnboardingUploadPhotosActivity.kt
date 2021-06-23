package com.unionbankph.corporate.payment_link.presentation.onboarding

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.GridView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_onboarding_upload_photos.*

class OnboardingUploadPhotosActivity :
    BaseActivity<RequestPaymentSplashViewModel>(R.layout.activity_onboarding_upload_photos) {

    private var imageUri: Uri? = null
    val REQUEST_CODE = 200
    lateinit var gridView: GridView

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
                clUploadPhotosIntro.visibility = View.GONE
                clSelectedPhotos.visibility = View.VISIBLE
                btnSaveAndExit.visibility = View.VISIBLE
                btnNext.visibility = View.VISIBLE

                val uri = data.clipData
//                ivPhotoFromGallery.setImageURI(uri!!.getItemAt(0).uri)
//                ivPhotoFromGallery2.setImageURI(uri.getItemAt(1).uri)
//                ivPhotoFromGallery3.setImageURI(uri.getItemAt(2).uri)
//                ivPhotoFromGallery4.setImageURI(uri.getItemAt(3).uri)
//                ivPhotoFromGallery5.setImageURI(uri.getItemAt(4).uri)
//                ivPhotoFromGallery6.setImageURI(uri.getItemAt(5).uri)
//

                gridView = findViewById(R.id.gv)
                var count = data.clipData!!.itemCount
                val uriArrayList = arrayListOf<Uri>()
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    uriArrayList.add(imageUri)
                    val adapter = UploadPhotosCustomAdapter(this, uriArrayList)

                    gridView.adapter = adapter
                }

//                if (count == 2){
//                    ivPhotoFromGallery.setImageURI(uri!!.getItemAt(0).uri)
//                    ivPhotoFromGallery2.setImageURI(uri.getItemAt(1).uri)
//                } else if (count == 3){
//                    ivPhotoFromGallery.setImageURI(uri!!.getItemAt(0).uri)
//                    ivPhotoFromGallery2.setImageURI(uri.getItemAt(1).uri)
//                    ivPhotoFromGallery3.setImageURI(uri.getItemAt(2).uri)
//                } else if (count == 4){
//                    ivPhotoFromGallery.setImageURI(uri!!.getItemAt(0).uri)
//                    ivPhotoFromGallery2.setImageURI(uri.getItemAt(1).uri)
//                    ivPhotoFromGallery3.setImageURI(uri.getItemAt(2).uri)
//                    ivPhotoFromGallery4.setImageURI(uri.getItemAt(3).uri)
//                } else if (count ==5){
//                    ivPhotoFromGallery.setImageURI(uri!!.getItemAt(0).uri)
//                    ivPhotoFromGallery2.setImageURI(uri.getItemAt(1).uri)
//                    ivPhotoFromGallery3.setImageURI(uri.getItemAt(2).uri)
//                    ivPhotoFromGallery4.setImageURI(uri.getItemAt(3).uri)
//                    ivPhotoFromGallery5.setImageURI(uri.getItemAt(4).uri)
//                } else if (count == 6){
//                    ivPhotoFromGallery.setImageURI(uri!!.getItemAt(0).uri)
//                    ivPhotoFromGallery2.setImageURI(uri.getItemAt(1).uri)
//                    ivPhotoFromGallery3.setImageURI(uri.getItemAt(2).uri)
//                    ivPhotoFromGallery4.setImageURI(uri.getItemAt(3).uri)
//                    ivPhotoFromGallery5.setImageURI(uri.getItemAt(4).uri)
//                    ivPhotoFromGallery6.setImageURI(uri.getItemAt(5).uri)
//                }
            }
            else if (data?.data != null) {
                clUploadPhotosIntro.visibility = View.GONE
                clSelectedPhotos.visibility = View.VISIBLE
                btnSaveAndExit.visibility = View.VISIBLE
                btnNext.visibility = View.VISIBLE
                gridView = findViewById(R.id.gv)

                val imageUri = data.data!!
                val uriArrayList = arrayListOf<Uri>()
                uriArrayList.add(imageUri)
                val adapter = UploadPhotosCustomAdapter(this, uriArrayList)
                gridView.adapter = adapter
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