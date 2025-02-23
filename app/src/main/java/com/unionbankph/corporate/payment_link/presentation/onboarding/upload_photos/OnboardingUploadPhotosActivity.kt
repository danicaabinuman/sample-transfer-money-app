package com.unionbankph.corporate.payment_link.presentation.onboarding.upload_photos

import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
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
import com.unionbankph.corporate.app.common.widget.dialog.NewConfirmationBottomSheet
import com.unionbankph.corporate.databinding.ActivityOnboardingUploadPhotosBinding
import io.reactivex.rxkotlin.addTo
import java.io.File
import java.util.*
import javax.annotation.concurrent.ThreadSafe
import kotlin.concurrent.timerTask

class OnboardingUploadPhotosActivity :
    BaseActivity<ActivityOnboardingUploadPhotosBinding,OnboardingUploadPhotosViewModel>(),
    OnboardingUploadPhotosFragment.OnOnboardingUploadPhotosFragmentInteraction,
    OnboardingDeletePhotosFragment.OnboardingDeletePhotosInteraction {

    val REQUEST_CODE = 200
    private var uriArrayList = arrayListOf<Uri>()
    var adapter: BaseAdapter? = null
    private var onboardingUploadFragment: OnboardingUploadPhotosFragment? = null
    private var itemUri: Uri? = null

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.root)
        setDrawableBackButton(
            R.drawable.ic_msme_back_button_orange,
            R.color.colorSMEMediumOrange,
            true
        )
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
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

    private fun initOnClicks(){
        binding.btnAddPhotos.setOnClickListener {
//            showUploadPhotoDialog()
            captureImageViaCamera()
        }
        binding.btnAddPhotos2.setOnClickListener {
//            showUploadPhotoDialog()
            captureImageViaCamera()
        }
        binding.btnNext.setOnClickListener {
            showSnackbar()
        }
    }

    private fun init() {
        uriArrayList = intent.getParcelableArrayListExtra<Uri>(LIST_OF_IMAGES_URI)
            .notNullable() as ArrayList<Uri>
        if (uriArrayList.size != 0) {
            buttonsVisibility()
            initListener()
            binding.btnNext?.isEnabled = true
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
        binding.gv.adapter = adapter
        binding.gv.setOnItemClickListener { parent, view, position, id ->
            itemUri = uriArrayList[position]
            binding.ivFullscreenImage.setImageURI(itemUri)
            binding.clUploadPhotosIntro.visibility(false)
            binding.clSelectedPhotos.visibility(false)
            binding.viewToolbar.btnSaveAndExit.visibility(false)
            binding.btnNext.visibility(false)
            binding.clDeleteSelectedPhoto.visibility(true)
            binding.viewToolbar.btnDelete.visibility(true)
        }
        binding.viewToolbar.btnDelete.setOnClickListener {
            showDeletePhotoDialog()
        }
        if (uriArrayList.size == 6) {
            binding.btnAddPhotos2.visibility(false)
        }
    }

    private fun onClickDelete() {
        uriArrayList.remove(itemUri!!)
        adapter?.notifyDataSetChanged()
        binding.clDeleteSelectedPhoto.visibility(false)
        binding.viewToolbar.btnDelete.visibility(false)
        binding.clSelectedPhotos.visibility(true)
        binding.btnNext.visibility(true)
        binding.btnNext.isEnabled = false
        binding.viewToolbar.btnSaveAndExit.visibility(true)
        binding.btnAddPhotos2.visibility(true)
    }

    private fun buttonsVisibility(){
        binding.clUploadPhotosIntro.visibility(false)
        binding.clSelectedPhotos.visibility(true)
        binding.btnNext.visibility(true)
        binding.viewToolbar.btnSaveAndExit.visibility(true)
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
                                    DialogFactory().createColoredSMEDialog(
                                        this,
                                        title = "Item wasn't uploaded",
                                        content = "File size exceeds the maximum allowed size. Maximum file size is 2 MB",
                                        positiveButtonText = "TRY AGAIN",
                                        onPositiveButtonClicked = {
                                            uriArrayList.remove(imageUri)
                                            adapter?.notifyDataSetChanged()
                                        }
                                    ).show()
                                    binding.btnNext.isEnabled = false
                                }
                                if (fileType != IMAGE_JPEG && fileType != IMAGE_PNG){
                                    DialogFactory().createColoredSMEDialog(
                                        this,
                                        title = getString(R.string.item_not_uploaded),
                                        content = getString(R.string.invalid_filetype_desc),
                                        positiveButtonText = getString(R.string.action_try_again),
                                        onPositiveButtonClicked = {
                                            uriArrayList.remove(imageUri)
                                            adapter?.notifyDataSetChanged()
                                        }
                                    ).show()
                                    binding.btnNext.isEnabled = false

                                }
                                uriArrayList.add(imageUri)
                                initListener()
                                binding.btnNext.isEnabled = true

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
                            DialogFactory().createColoredSMEDialog(
                                this,
                                title = getString(R.string.item_not_uploaded),
                                content = getString(R.string.invalid_filesize_desc),
                                positiveButtonText = getString(R.string.action_try_again),
                                onPositiveButtonClicked = {
                                    uriArrayList.remove(imageUri)
                                    adapter?.notifyDataSetChanged()
                                }
                            ).show()
                            binding.btnNext.isEnabled = false
                        }
                        if (fileType != IMAGE_JPEG && fileType != IMAGE_PNG){
                            DialogFactory().createColoredSMEDialog(
                                this,
                                title = getString(R.string.item_not_uploaded),
                                content = getString(R.string.invalid_filetype_desc),
                                positiveButtonText = getString(R.string.action_try_again),
                                onPositiveButtonClicked = {
                                    uriArrayList.remove(imageUri)
                                    adapter?.notifyDataSetChanged()
                                }
                            ).show()
                            binding.btnNext.isEnabled = false
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

    private fun showDeletePhotoDialog(){
        val onboardingDeletePhotosFragment = OnboardingDeletePhotosFragment.newInstance()
        onboardingDeletePhotosFragment.show(
            supportFragmentManager,
            OnboardingDeletePhotosFragment.TAG
        )
//        NewConfirmationBottomSheet.newInstance(
//            description = getString(R.string.title_delete_photo),
//            positiveButtonText = getString(R.string.btn_delete_this_photo)
//        )
    }

    private fun showSnackbar(){
        Snackbar.make(binding.snackbar, "Uploading photo...", Snackbar.LENGTH_LONG).setAnchorView(R.id.btnNext).show()
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
            binding.clDeleteSelectedPhoto.isShown -> {
                binding.clDeleteSelectedPhoto.visibility = View.GONE
                binding.clSelectedPhotos.visibility = View.VISIBLE
                binding.btnNext.visibility = View.VISIBLE
            }
            binding.clSelectedPhotos.isShown -> {
                binding.clSelectedPhotos.visibility = View.GONE
                binding.btnNext.visibility = View.GONE
                binding.clUploadPhotosIntro.visibility = View.VISIBLE
            }
            binding.clUploadPhotosIntro.isShown -> {
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

    override val bindingInflater: (LayoutInflater) -> ActivityOnboardingUploadPhotosBinding
        get() = ActivityOnboardingUploadPhotosBinding::inflate
    override val viewModelClassType: Class<OnboardingUploadPhotosViewModel>
        get() = OnboardingUploadPhotosViewModel::class.java


}
