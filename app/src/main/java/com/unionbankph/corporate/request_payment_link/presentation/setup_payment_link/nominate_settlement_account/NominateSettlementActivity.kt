package com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link.nominate_settlement_account

import android.accounts.Account
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.presentation.account_detail.AccountDetailActivity
import com.unionbankph.corporate.account.presentation.account_list.*
import com.unionbankph.corporate.account.presentation.account_list.ShowAccountDetailError
import com.unionbankph.corporate.account.presentation.account_list.ShowAccountDismissLoading
import com.unionbankph.corporate.account.presentation.account_list.ShowAccountDismissLoadingAccountDetail
import com.unionbankph.corporate.account.presentation.account_list.ShowAccountError
import com.unionbankph.corporate.account.presentation.account_list.ShowAccountLoading
import com.unionbankph.corporate.account.presentation.account_list.ShowAccountLoadingAccountDetail
import com.unionbankph.corporate.account.presentation.account_list.ShowAccountsDetailError
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.nullable
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.AccountSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.SettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.data.model.PermissionCollection
import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.constant.TutorialScreenEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialHasTutorial
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_accounts.*
import kotlinx.android.synthetic.main.widget_search_layout.*
import java.util.concurrent.TimeUnit

class NominateSettlementActivity :
        BaseActivity<NominateSettlementViewModel>(R.layout.activity_nominate_settlement),
        AccountAdapterCallback
{
    override fun onViewsBound() {
        super.onViewsBound()
    }
}