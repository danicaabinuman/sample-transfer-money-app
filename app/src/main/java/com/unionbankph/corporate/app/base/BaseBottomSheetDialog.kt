package com.unionbankph.corporate.app.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.common.extension.viewBinding
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.source.local.sharedpref.SharedPreferenceUtil
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Created by herald25santos on 09/05/2019
 */
abstract class BaseBottomSheetDialog<VB : ViewBinding, VM : ViewModel> :
    BottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var viewUtil: ViewUtil

    @Inject
    lateinit var sharedPreferenceUtil: SharedPreferenceUtil

    @Inject
    lateinit var applicationContext: Context

    lateinit var viewModel: VM

    abstract val bindingBinder: (View) -> VB

    @get:LayoutRes
    abstract val layoutId: Int

    val binding by viewBinding(bindingBinder)

    protected abstract val viewModelClassType: Class<VM>

    protected open fun beforeLayout(savedInstanceState: Bundle?) {}
    protected open fun afterLayout(savedInstanceState: Bundle?) {}

    protected open fun onInitializeListener() {}
    protected open fun onViewModelBound() { initViewModel() }

    protected open fun onViewsBound() {}

    val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (App.isSME()) {
            setStyle(STYLE_NORMAL, R.style.SMECustomBottomSheetDialogTheme)
        } else {
            setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        }
        beforeLayout(savedInstanceState)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutId, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        afterLayout(savedInstanceState)
        onViewModelBound()
        onViewsBound()
        onInitializeListener()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[viewModelClassType]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
        disposables.dispose()
    }
}
