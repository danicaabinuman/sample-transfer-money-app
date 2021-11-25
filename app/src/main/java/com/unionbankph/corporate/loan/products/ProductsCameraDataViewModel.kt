package com.unionbankph.corporate.loan.products

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import javax.inject.Inject


class ProductsCameraDataViewModel @Inject constructor(
) : BaseViewModel() {


    private val _images = MutableLiveData<List<String>>()
    val images: LiveData<List<String>> = _images


}
