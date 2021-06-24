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
    val uriArrayList = arrayListOf<Uri>()

    override fun onViewsBound() {
        super.onViewsBound()

        ivBackButton.setOnClickListener {
            clUploadPhotosIntro.visibility = View.VISIBLE
            clSelectedPhotos.visibility = View.GONE
            btnSaveAndExit.visibility = View.GONE
            btnNext.visibility = View.GONE
        }
        btnAddPhotos.setOnClickListener {
//            showbottomSheetDialog()

//            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
//            startActivityForResult(gallery, pickImage)

            openGalleryForImages()
        }

        btnAddPhotos2.setOnClickListener {
            openGalleryForImages()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {

            gridView = findViewById(R.id.gv)

            if (data?.clipData != null) {
                clUploadPhotosIntro.visibility = View.GONE
                clSelectedPhotos.visibility = View.VISIBLE
                btnSaveAndExit.visibility = View.VISIBLE
                btnNext.visibility = View.VISIBLE

                val uri = data.clipData
                var count = uri!!.itemCount
                if (uriArrayList.size < 6) {
                    for (i in 0 until count) {

                        btnAddPhotos2.visibility = View.VISIBLE
                        val imageUri = data.clipData!!.getItemAt(i).uri
                        uriArrayList.add(imageUri)
                        val adapter = UploadPhotosCustomAdapter(this, uriArrayList)

                        gridView.adapter = adapter
                        if (uriArrayList.size == 6) {
                            btnAddPhotos2.visibility = View.GONE
                            return
                        }
                    }
                } else {
                    btnAddPhotos2.visibility = View.GONE
                }

            } else if (data?.data != null) {
                clUploadPhotosIntro.visibility = View.GONE
                clSelectedPhotos.visibility = View.VISIBLE
                btnSaveAndExit.visibility = View.VISIBLE
                btnNext.visibility = View.VISIBLE

                if (uriArrayList.size < 6) {
                    val imageUri = data.data!!
                    uriArrayList.add(imageUri)
                    val adapter = UploadPhotosCustomAdapter(this, uriArrayList)
                    gridView.adapter = adapter
                } else {
                    btnAddPhotos2.visibility = View.GONE
                }

            }

            if (uriArrayList.size < 6) {
                btnAddPhotos2.visibility = View.VISIBLE
            } else {
                btnAddPhotos2.visibility = View.GONE
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