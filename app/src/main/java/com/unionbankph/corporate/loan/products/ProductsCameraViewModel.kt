package com.unionbankph.corporate.loan.products

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.feature.definition.CameraMode
import id.zelory.compressor.Compressor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import javax.inject.Inject


class ProductsCameraViewModel @Inject constructor(
    private val context: Context
) : BaseViewModel() {


    private val _flash = MutableLiveData(true)
    val flash: LiveData<Boolean> = _flash

    private val _image = MutableLiveData<File>()
    val image: LiveData<File> = _image

    private val _images = MutableLiveData(listOf<File>())
    val images: LiveData<List<File>> = _images

    private val _cameraMode = MutableLiveData(CameraMode.DEFAULT)
    val cameraMode: LiveData<Int> = _cameraMode

    fun addImage(file: File) {
        _images.value?.apply {
            val images = this.toMutableList()
            images.add(file)
            _images.value = images
        }
    }

    // TODO
    fun onPictureTaken(file: File?) {
        Compressor(context)
            .setMaxWidth(3112)
            .setMaxHeight(3316)
            .setQuality(100)
            .compressToFileAsFlowable(file)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { _uiState.value = Event(UiState.Loading) }
            .doFinally { _uiState.value = Event(UiState.Complete) }
            .subscribe(
                {
                    _image.value = it
                }, {
                    Timber.e(it, "onPictureTaken")
                }
            ).addTo(disposables)
    }

    fun setFlash(status: Boolean?) {
        status?.let {
            _flash.value = !status
        }
    }

    fun setImage(image: File?) {
        image?.let {
            _image.value = image!!
        }
    }

    fun setCameraMode(@CameraMode mode: Int) {
        _cameraMode.value = mode
    }

}
