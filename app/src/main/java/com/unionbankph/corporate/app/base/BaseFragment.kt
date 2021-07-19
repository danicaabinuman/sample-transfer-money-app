package com.unionbankph.corporate.app.base

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewbinding.ViewBinding
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.setColor
import com.unionbankph.corporate.app.common.platform.bus.data.DataBus
import com.unionbankph.corporate.app.common.platform.bus.event.EventBus
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.tutorial.TutorialEngineUtil
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.SettingsUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.data.source.local.sharedpref.SharedPreferenceUtil
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

abstract class BaseFragment<VB : ViewBinding, VM : ViewModel>() : Fragment() {

    lateinit var viewModel: VM
        private set

    lateinit var binding: VB
        private set

    @get:LayoutRes
    abstract val layoutId: Int

    protected abstract val viewModelClassType: Class<VM>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var eventBus: EventBus

    @Inject
    lateinit var dataBus: DataBus

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var viewUtil: ViewUtil

    @Inject
    lateinit var applicationContext: Context

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var sharedPreferenceUtil: SharedPreferenceUtil

    @Inject
    lateinit var cacheManager: CacheManager

    @Inject
    lateinit var tutorialEngineUtil: TutorialEngineUtil

    @Inject
    lateinit var autoFormatUtil: AutoFormatUtil

    @Inject
    lateinit var settingsUtil: SettingsUtil

    lateinit var tutorialViewModel: TutorialViewModel

    var hasInitialLoad: Boolean = true

    var isSkipTutorial: Boolean = false

    var isClickedHelpTutorial = false

    var isValidForm: Boolean = false

    val disposables = CompositeDisposable()

    var mLastClickTime: Long = 0

    val isSME = App.isSME()

    protected open fun beforeLayout(savedInstanceState: Bundle?) = Unit
    protected open fun afterLayout(savedInstanceState: Bundle?) = Unit
    protected open fun onViewsBound() = Unit
    protected open fun onViewModelBound() { initViewModel() }

    protected open fun onInitializeListener() = Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        initDependencyInjection()
        super.onCreate(savedInstanceState)
        beforeLayout(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        afterLayout(savedInstanceState)
        onViewModelBound()
        onViewsBound()
        onInitializeListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (App.isSME()) {
            for (i in 0 until menu.size()) {
                val menuItem = menu.getItem(i)
                menuItem.icon?.let {
                    it.mutate()
                    it.setColor(getAppCompatActivity(), R.color.colorInfo)
                }
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun initDependencyInjection() {
        AndroidSupportInjection.inject(this)
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[viewModelClassType]
    }

    private fun initViewBinding(inflater: LayoutInflater, container: ViewGroup?) {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
    }

    fun getLinearLayoutManager(): LinearLayoutManager {
        return LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )
    }

    fun showProgressAlertDialog(tag: String) {
        (activity as BaseActivity<*,*>).showProgressAlertDialog(tag)
    }

    fun dismissProgressAlertDialog() {
        (activity as BaseActivity<*,*>).dismissProgressAlertDialog()
    }

    fun showLoading(
        viewLoadingState: View,
        swipeRefreshLayout: SwipeRefreshLayout?,
        view: View,
        viewState: View?,
        headerTableView: View? = null
    ) {
        (activity as BaseActivity<*,*>).showLoading(
            viewLoadingState,
            swipeRefreshLayout,
            view,
            viewState,
            headerTableView
        )
    }

    fun updateShortCutBadgeCount() {
        (activity as BaseActivity<*,*>).generalViewModel.updateShortCutBadgeCount()
    }

    fun dismissLoading(
        viewLoadingState: View,
        swipeRefreshLayout: SwipeRefreshLayout?,
        view: View
    ) {
        (activity as BaseActivity<*,*>).dismissLoading(viewLoadingState, swipeRefreshLayout, view)
    }

    fun setMargins(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        (activity as BaseActivity<*,*>).setMargins(view, left, top, right, bottom)
    }

    fun getStatusBarHeight(): Int {
        return (activity as BaseActivity<*,*>).getStatusBarHeight()
    }

    fun getNavHeight(): Int {
        return (activity as BaseActivity<*,*>).getNavHeight()
    }

    fun handleOnError(throwable: Throwable) {
        (activity as BaseActivity<*,*>).handleOnError(throwable)
    }

    fun initSetError(
        textInputEditTextObservable: Observable<RxValidationResult<EditText>>
    ) {
        textInputEditTextObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                Timber.d("initSetError: ${it.isProper}")
                viewUtil.setError(it)
            }.addTo(disposables)
    }

    fun initSetCounterFlowError(
        textInputEditTextObservable: Observable<RxValidationResult<EditText>>
    ) {
        textInputEditTextObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                Timber.d("initSetError: ${it.isProper}")
                viewUtil.setCounterFlowError(it)
            }.addTo(disposables)
    }

    fun showMaterialDialogError(
        title: String = formatString(R.string.title_error),
        message: String
    ) {
        (activity as BaseActivity<*,*>).showMaterialDialogError(title, message)
    }

    fun isTableView() = (activity as BaseActivity<*,*>).isTableView()

    fun getAppCompatActivity(): AppCompatActivity = (activity as AppCompatActivity)
}
