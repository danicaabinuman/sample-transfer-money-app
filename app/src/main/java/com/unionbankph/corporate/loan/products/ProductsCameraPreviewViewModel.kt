package com.unionbankph.corporate.loan.products

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import id.zelory.compressor.Compressor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import javax.inject.Inject


class ProductsCameraPreviewViewModel @Inject constructor(
    private val context: Context
) : BaseViewModel() {


    private val _flash = MutableLiveData(true)
    val flash: LiveData<Boolean> = _flash

    private val _image = MutableLiveData<String>()
    val image: LiveData<String> = _image

    private val _images = MutableLiveData(listOf<File>())
    val images: LiveData<List<File>> = _images

    fun addImage(file: File) {
        _images.value?.apply {
            val images = this.toMutableList()
            images.add(file)
            _images.value = images
        }
    }


    fun setImage(image: String?) {
        image?.let {
            _image.value = it
        }
    }



}
