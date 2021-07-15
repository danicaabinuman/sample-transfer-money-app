package com.unionbankph.corporate.payment_link.presentation.onboarding

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_channels.PaymentLinkChannelsActivity
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
            openGalleryForImages()
        }

        btnAddPhotos2.setOnClickListener {
            openGalleryForImages()
        }

        btnNext.setOnClickListener {
            val intent = Intent(this, PaymentLinkChannelsActivity::class.java)
            startActivity(intent)
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
                        gridView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                            adapter.removeItem(position)
                            adapter.notifyDataSetChanged()
                            if (uriArrayList.size < 6){
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
                btnSaveAndExit.visibility = View.VISIBLE
                btnNext.visibility = View.VISIBLE

                if (uriArrayList.size < 6) {
                    val imageUri = data.data!!
                    uriArrayList.add(imageUri)
                    val adapter = UploadPhotosCustomAdapter(this, uriArrayList)
                    gridView.adapter = adapter
                    gridView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                        adapter.removeItem(position)
                        adapter.notifyDataSetChanged()
                    }

                }
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

    companion object {
        const val REQUEST_CODE = 1209
        const val RESULT_SHOULD_GENERATE_NEW_LINK = "result_should_generate_new_link"
        const val EXTRA_SETUP_MERCHANT_DETAILS = "extra_setup_merchant_details"
    }
}