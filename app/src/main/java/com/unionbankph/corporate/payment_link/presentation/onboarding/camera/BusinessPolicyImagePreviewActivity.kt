package com.unionbankph.corporate.payment_link.presentation.onboarding.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.util.BitmapUtil
import com.unionbankph.corporate.app.util.FileUtil
import com.unionbankph.corporate.databinding.ActivityOnboardingImagePreviewBinding
import com.unionbankph.corporate.payment_link.presentation.onboarding.upload_photos.OnboardingUploadPhotosActivity
import com.unionbankph.corporate.payment_link.presentation.setup_business_information.business_information_forms.BusinessInformation2ndScreenActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.upload_documents.CardAcceptanceUploadDocumentsActivity
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

class BusinessPolicyImagePreviewActivity :
    BaseActivity<ActivityOnboardingImagePreviewBinding,DocumentCameraViewModel>() {

    @Inject
    lateinit var fileUtil: FileUtil

    @Inject
    lateinit var bitmapUtil: BitmapUtil
    var uriArrayList = arrayListOf<Uri>()

    private lateinit var filePath: String

    private lateinit var currentCroppedBitmap: Bitmap

    private var isCroppedMode = false



    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.root)
        setDrawableBackButton(
            R.drawable.ic_msme_back_button_orange,
            R.color.colorSMEMediumOrange,
            true
        )
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        initImagePreview()
    }


    override fun onInitializeListener() {
        super.onInitializeListener()
        initListener()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        val menuCrop = menu.findItem(R.id.menu_crop)
        menuCrop.isVisible = true
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_crop -> {
                onCroppingMode()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val bundle = Bundle().apply {
            putParcelableArrayList(
                BusinessInformation2ndScreenActivity.LIST_OF_IMAGES_URI,
                uriArrayList
            )
        }
        navigator.navigate(
            this,
            BusinessPolicyCameraActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = true
        )
    }


    private fun initListener() {
        RxView.clicks(binding.buttonUseThis)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                uploadFile()
            }.addTo(disposables)
        binding.buttonRetake.setOnClickListener {
            onBackPressed()
        }
        binding.tvDone.setOnClickListener {
            onClickedDone()
        }
        binding.tvCancel.setOnClickListener {
            onClickedCancel()
        }
    }

    private fun onClickedCancel() {
        exitCroppingMode()
    }
    private fun onClickedDone() {
        if (binding.imageViewCrop.canRightCrop()) {
            currentCroppedBitmap = binding.imageViewCrop.crop()
            exitCroppingMode()
        }
    }

    private fun onCroppingMode() {
        isCroppedMode = false
        invalidateOptionsMenu()
        viewUtil.startAnimateView(true, binding.tvCancel, android.R.anim.fade_in)
        viewUtil.startAnimateView(true, binding.tvDone, android.R.anim.fade_in)
        viewUtil.startAnimateView(true, binding.imageViewCrop, android.R.anim.fade_in)
        viewUtil.startAnimateView(false, binding.buttonUseThis, android.R.anim.fade_out)
        viewUtil.startAnimateView(false, binding.buttonRetake, android.R.anim.fade_out)
        viewUtil.startAnimateView(false, binding.ivPreview, android.R.anim.fade_out)
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        options.inJustDecodeBounds = false
        currentCroppedBitmap = BitmapFactory.decodeFile(filePath, options)
        binding.imageViewCrop.setImageToCrop(currentCroppedBitmap)
    }

    private fun exitCroppingMode() {
        isCroppedMode = true
        invalidateOptionsMenu()
        viewUtil.startAnimateView(false, binding.tvCancel, android.R.anim.fade_out)
        viewUtil.startAnimateView(false, binding.tvDone, android.R.anim.fade_out)
        viewUtil.startAnimateView(false, binding.imageViewCrop, android.R.anim.fade_out)
        viewUtil.startAnimateView(true, binding.buttonUseThis, android.R.anim.fade_in)
        viewUtil.startAnimateView(true, binding.buttonRetake, android.R.anim.fade_in)
        viewUtil.startAnimateView(true, binding.ivPreview, android.R.anim.fade_in)
        binding.ivPreview.loadImage(currentCroppedBitmap)
        binding.ivPreview.postDelayed(
            {
                binding.ivPreview.zoomOut()
            }, 100
        )
    }

    private fun init() {
        filePath = intent.getStringExtra(EXTRA_FILE_PATH).notNullable()
        uriArrayList = intent.getParcelableArrayListExtra<Uri>(BusinessInformation2ndScreenActivity.LIST_OF_IMAGES_URI).notNullable() as ArrayList<Uri>
    }



    private fun initImagePreview() {
        viewUtil.startAnimateView(true, binding.buttonUseThis, android.R.anim.fade_in)
        viewUtil.startAnimateView(true, binding.buttonRetake, android.R.anim.fade_in)
        viewUtil.startAnimateView(true, binding.ivPreview, android.R.anim.fade_in)
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        options.inJustDecodeBounds = false
        currentCroppedBitmap = BitmapFactory.decodeFile(filePath, options)
        binding.ivPreview.loadImage(currentCroppedBitmap)
        binding.ivPreview.postDelayed(
            {
                binding.ivPreview.zoomOut()
            }, 100
        )

    }

    private fun saveFile(bitmap: Bitmap): String? {
        val timeStamp: String = SimpleDateFormat("HHmmss").format(Date())
        val file = bitmapUtil.saveBitmap(
            bitmap,
            filename = "${timeStamp}_CROPPED",
            format = Bitmap.CompressFormat.JPEG
        )
        return file?.path
    }

    private fun rotateImage(bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(-90f)
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    private fun uploadFile() {
        val filePath = saveFile(currentCroppedBitmap)
        val file = File(filePath.notNullable())
        uriArrayList.add(Uri.parse(file.absolutePath))
        exitPreviewImage()

        Timber.d("filePath: $filePath")
        Timber.d("fileName: ${file.name}")

    }

    private fun exitPreviewImage(){
        val bundle = Bundle().apply {
            putParcelableArrayList(
                BusinessInformation2ndScreenActivity.LIST_OF_IMAGES_URI,
                uriArrayList
            )
        }
        navigator.navigate(
            this,
            BusinessInformation2ndScreenActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_RIGHT
        )

    }



    companion object {
        const val EXTRA_FILE_PATH = "file_path"
    }

    override val bindingInflater: (LayoutInflater) -> ActivityOnboardingImagePreviewBinding
        get() = ActivityOnboardingImagePreviewBinding::inflate
    override val viewModelClassType: Class<DocumentCameraViewModel>
        get() = DocumentCameraViewModel::class.java
}
