package com.unionbankph.corporate.payment_link.presentation.onboarding.upload_photos

import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.BaseAdapter
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.DialogFactory
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.payment_link.presentation.onboarding.camera.OnboardingCameraActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.payment_link_channels.PaymentLinkChannelsActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_onboarding_upload_photos.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.toolbar
import kotlinx.android.synthetic.main.widget_transparent_rmo_appbar.*
import java.util.*
import javax.annotation.concurrent.ThreadSafe
import kotlin.concurrent.timerTask

class OnboardingUploadPhotosActivity :
    BaseActivity<OnboardingUploadPhotosViewModel>(R.layout.activity_onboarding_upload_photos),
    OnboardingUploadPhotosFragment.OnOnboardingUploadPhotosFragmentInteraction,
    OnboardingDeletePhotosFragment.OnboardingDeletePhotosInteraction {

    val REQUEST_CODE = 200
    private var uriArrayList = arrayListOf<Uri>()
    var adapter: BaseAdapter? = null
    private var onboardingUploadFragment: OnboardingUploadPhotosFragment? = null
    private var itemUri: Uri? = null

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
            )[OnboardingUploadPhotosViewModel::class.java]
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

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        initBinding()
        initOnClicks()

    }

    private fun initOnClicks() {
        btnAddPhotos.setOnClickListener {
//            showUploadPhotoDialog()
            captureImageViaCamera()
        }
        btnAddPhotos2.setOnClickListener {
//            showUploadPhotoDialog()
            captureImageViaCamera()
        }
        btnNext.setOnClickListener {
            showSnackbar()
        }
    }

    private fun init() {
        uriArrayList = intent.getParcelableArrayListExtra<Uri>(LIST_OF_IMAGES_URI)
            .notNullable() as ArrayList<Uri>
        if (uriArrayList.size != 0) {
            buttonsVisibility()
            initListener()
            btnNext?.isEnabled = true
        }

    }

    private fun initBinding() {
        viewModel.originalApplicationUriInput
            .filter {
                viewModel.originalApplicationUriInput.hasValue()
            }
            .subscribe {
                uriArrayList.add(it)
                buttonsVisibility()
                initListener()
            }.addTo(disposables)
    }

    private fun initListener() {
        adapter = UploadPhotosCustomAdapter(this, uriArrayList)
        gv.adapter = adapter

        gv.setOnItemClickListener { parent, view, position, id ->
            itemUri = uriArrayList[position]
            ivFullscreenImage.setImageURI(itemUri)
            clUploadPhotosIntro.visibility(false)
            clSelectedPhotos.visibility(false)
            btnSaveAndExit.visibility(false)
            btnNext.visibility(false)
            clDeleteSelectedPhoto.visibility(true)
            btnDelete.visibility(true)
        }
        btnDelete.setOnClickListener {
            showDeletePhotoDialog()
        }
        if (uriArrayList.size == 6) {
            btnAddPhotos2.visibility(false)
        }
    }

    private fun onClickDelete() {
        uriArrayList.remove(itemUri!!)
        adapter?.notifyDataSetChanged()
        clDeleteSelectedPhoto.visibility(false)
        btnDelete.visibility(false)
        clSelectedPhotos.visibility(true)
        btnNext.visibility(true)
        btnSaveAndExit.visibility(true)
        btnAddPhotos2.visibility(true)
        btnNext?.isEnabled = false
    }

    private fun buttonsVisibility() {
        clUploadPhotosIntro.visibility(false)
        clSelectedPhotos.visibility(true)
        btnNext.visibility(true)
        btnSaveAndExit.visibility(true)
        btnNext?.isEnabled = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE -> {
                    if (data?.clipData != null) {
                        val uri = data.clipData
                        val count = uri!!.itemCount
                        if (count + uriArrayList.size <= 6) {
                            buttonsVisibility()
                            for (i in 0 until count) {
                                val imageUri = data.clipData!!.getItemAt(i).uri
                                val fileDescriptor: AssetFileDescriptor = applicationContext.contentResolver.openAssetFileDescriptor(imageUri, "r")!!
                                val fileType: String? = applicationContext.contentResolver.getType(imageUri)
                                val fileSize: Long = fileDescriptor.length
                                if (fileSize > MAX_FILESIZE_2MB){
                                    DialogFactory().createSMEDialog(
                                        this,
                                        isNewDesign = false,
                                        title = "Item wasn't uploaded",
                                        description = "File size exceeds the maximum allowed size. Maximum file size is 2 MB",
                                        positiveButtonText = "TRY AGAIN",
                                        onPositiveButtonClicked = {
                                            uriArrayList.remove(imageUri)
                                            adapter?.notifyDataSetChanged()
                                        }
                                    ).show()
                                }
                                if (fileType != IMAGE_JPEG && fileType != IMAGE_PNG){
                                    DialogFactory().createSMEDialog(
                                        this,
                                        isNewDesign = false,
                                        title = getString(R.string.item_not_uploaded),
                                        description = getString(R.string.invalid_filetype_desc),
                                        positiveButtonText = getString(R.string.action_try_again),
                                        onPositiveButtonClicked = {
                                            uriArrayList.remove(imageUri)
                                            adapter?.notifyDataSetChanged()
                                        }
                                    ).show()
                                }
                                uriArrayList.add(imageUri)
                                initListener()
                            }
                        } else {
                            showMaterialDialogError(message = getString(R.string.msg_maximum_of_6_photos_only))
                        }

                    } else if (data?.data != null) {
                        viewModel.originalApplicationUriInput.onNext(data.data!!)
                        val imageUri = data.data!!
                        val fileDescriptor: AssetFileDescriptor = applicationContext.contentResolver.openAssetFileDescriptor(imageUri, "r")!!
                        val fileType: String? = applicationContext.contentResolver.getType(imageUri)
                        val fileSize: Long = fileDescriptor.length
                        if (fileSize > MAX_FILESIZE_2MB){
                            DialogFactory().createSMEDialog(
                                this,
                                isNewDesign = false,
                                title = getString(R.string.item_not_uploaded),
                                description = getString(R.string.invalid_filesize_desc),
                                positiveButtonText = getString(R.string.action_try_again),
                                onPositiveButtonClicked = {
                                    uriArrayList.remove(imageUri)
                                    adapter?.notifyDataSetChanged()
                                }
                            ).show()
                        }
                        if (fileType != IMAGE_JPEG && fileType != IMAGE_PNG){
                            DialogFactory().createSMEDialog(
                                this,
                                isNewDesign = false,
                                title = getString(R.string.item_not_uploaded),
                                description = getString(R.string.invalid_filetype_desc),
                                positiveButtonText = getString(R.string.action_try_again),
                                onPositiveButtonClicked = {
                                    uriArrayList.remove(imageUri)
                                    adapter?.notifyDataSetChanged()
                                }
                            ).show()
                        }

                    }
                }
            }
        }
    }


    private fun showUploadPhotoDialog() {
        val onboardingUploadFragment = OnboardingUploadPhotosFragment.newInstance()
        onboardingUploadFragment.show(
            supportFragmentManager,
            OnboardingUploadPhotosFragment.TAG
        )
    }

    private fun showDeletePhotoDialog() {
        val onboardingDeletePhotosFragment = OnboardingDeletePhotosFragment.newInstance()
        onboardingDeletePhotosFragment.show(
            supportFragmentManager,
            OnboardingDeletePhotosFragment.TAG
        )
    }

    private fun showSnackbar() {
        Snackbar.make(snackbar, "Uploading photo...", Snackbar.LENGTH_LONG)
            .setAnchorView(R.id.btnNext).show()
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
            startActivityForResult(
                Intent.createChooser(intent, "Choose pictures"),
                REQUEST_CODE
            )
        } else {
            var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE)
        }
    }

    private fun captureImageViaCamera(){
        val bundle = Bundle().apply {
            putParcelableArrayList(
                LIST_OF_IMAGES_URI,
                uriArrayList
            )
        }
        navigator.navigate(
            this,
            OnboardingCameraActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }
    override fun openGallery() {
        openGalleryForImages()
    }

    override fun openCamera() {
//        val bundle = Bundle().apply {
//            putParcelableArrayList(
//                LIST_OF_IMAGES_URI,
//                uriArrayList
//            )
//        }
//        navigator.navigate(
//            this,
//            OnboardingCameraActivity::class.java,
//            bundle,
//            isClear = true,
//            isAnimated = true,
//            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
//        )
    }

    override fun deletePhoto() {
        onClickDelete()

    }

    override fun onBackPressed() {
        when {
            clDeleteSelectedPhoto.isShown -> {
                clDeleteSelectedPhoto.visibility = View.GONE
                clSelectedPhotos.visibility = View.VISIBLE
                btnNext.visibility = View.VISIBLE
            }
            clSelectedPhotos.isShown -> {
                clSelectedPhotos.visibility = View.GONE
                btnNext.visibility = View.GONE
                clUploadPhotosIntro.visibility = View.VISIBLE
            }
            clUploadPhotosIntro.isShown -> {
                super.onBackPressed()

            }
        }

    }

    @ThreadSafe
    companion object {
        const val REQUEST_CODE = 1209
        const val LIST_OF_IMAGES_URI = "list_of_image_uri"
        const val EXTRA_SETUP_MERCHANT_DETAILS = "extra_setup_merchant_details"
        const val IMAGE_JPEG = "image/jpeg"
        const val IMAGE_PNG = "image/png"
        const val DOCU_PDF = "image/jpeg"
        const val MAX_FILESIZE_2MB = 2097152
    }

}
