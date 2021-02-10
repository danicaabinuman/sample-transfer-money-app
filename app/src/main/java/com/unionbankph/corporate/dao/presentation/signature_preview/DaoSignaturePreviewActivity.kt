package com.unionbankph.corporate.dao.presentation.signature_preview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.SystemClock
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.util.BitmapUtil
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import kotlinx.android.synthetic.main.activity_dao_signature_result.*
import kotlinx.android.synthetic.main.activity_organization_transfer.viewToolbar
import kotlinx.android.synthetic.main.widget_transparent_appbar.*
import java.io.File
import javax.annotation.concurrent.ThreadSafe
import javax.inject.Inject

class DaoSignaturePreviewActivity :
    BaseActivity<DaoSignaturePreviewViewModel>(R.layout.activity_dao_signature_result) {

    @Inject
    lateinit var bitmapUtil: BitmapUtil

    private lateinit var croppedBitmap: Bitmap

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setToolbarTitle(tvToolbar, formatString(R.string.title_preview))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        buttonUseThis.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return@setOnClickListener
            val path = saveCroppedFile()
            viewModel.onSubmitSignatureFile(File(path))
        }
        buttonRetake.setOnClickListener {
            onBackPressed()
        }
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

    override fun onDestroy() {
        super.onDestroy()
        if (viewModel.isSuccessUpload.value == true) {
            val daoHit = viewModel.navigateResult.value?.peekContent()
            if (daoHit?.isHit == true) {
                eventBus.actionSyncEvent.emmit(
                    BaseEvent(
                        ActionSyncEvent.ACTION_SIGNATURE_USE_THIS_SKIP,
                        JsonHelper.toJson(daoHit)
                    )
                )
            } else {
                eventBus.actionSyncEvent.emmit(
                    BaseEvent(
                        ActionSyncEvent.ACTION_SIGNATURE_USE_THIS,
                        viewModel.navigateNextStep.value?.peekContent()?.path.notNullable()
                    )
                )
            }
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[DaoSignaturePreviewViewModel::class.java]
        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    showProgressAlertDialog(this::class.java.simpleName)
                }
                is UiState.Complete -> {
                    dismissProgressAlertDialog()
                }
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.navigateNextStep.observe(this, EventObserver {
            onBackPressed()
        })
    }

    private fun cropImage(bitmap: Bitmap): Bitmap {
        return Bitmap.createBitmap(
            bitmap,
            getPercentage(bitmap.width, 3.0f),
            getPercentage(bitmap.height, 30.0f),
            bitmap.width - getPercentage(bitmap.width, 6.0f),
            bitmap.height - getPercentage(bitmap.height, 60.0f),
            null,
            true
        )
    }

    private fun getPercentage(value: Int, percentage: Float): Int {
        return (value * (percentage / 100.0f)).toInt()
    }

    private fun init() {
        viewModel.loadDaoForm(intent.getStringExtra(EXTRA_DAO_FORM))
        croppedBitmap =
            cropImage(BitmapFactory.decodeFile(intent.getStringExtra(EXTRA_PATH).notNullable()))
        iv_preview.setImageBitmap(croppedBitmap)
    }

    private fun saveCroppedFile(): String {
        val croppedFile = bitmapUtil.saveBitmap(
            croppedBitmap,
            filename = "signature",
            format = Bitmap.CompressFormat.JPEG
        )
        return croppedFile?.path.notNullable()
    }

    @ThreadSafe
    companion object {
        const val EXTRA_PATH = "path"
        const val EXTRA_DAO_FORM = "daoForm"
    }
}
