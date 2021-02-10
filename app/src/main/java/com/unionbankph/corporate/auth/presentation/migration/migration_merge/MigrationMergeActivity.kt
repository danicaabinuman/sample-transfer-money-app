package com.unionbankph.corporate.auth.presentation.migration.migration_merge

import android.os.Bundle
import android.view.MenuItem
import androidx.viewpager.widget.ViewPager
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.widget.recyclerview.viewpager.ViewPagerAdapter
import com.unionbankph.corporate.auth.data.model.LoginMigrationDto
import com.unionbankph.corporate.auth.presentation.migration.MigrationViewModel
import com.unionbankph.corporate.auth.presentation.migration.migration_form.MigrationFormActivity
import com.unionbankph.corporate.auth.presentation.migration.nominate_merge.NominateMergeEmailFragment
import com.unionbankph.corporate.auth.presentation.migration.nominate_merge.NominateMergeSuccessFragment
import com.unionbankph.corporate.auth.presentation.migration.nominate_merge_verify.NominateMergeVerifyFragment
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import kotlinx.android.synthetic.main.activity_migration_merge.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*

class MigrationMergeActivity : BaseActivity<MigrationViewModel>(R.layout.activity_migration_merge) {

    private lateinit var adapter: ViewPagerAdapter

    private var onBackPressedEvent: OnBackPressedEvent? = null

    private val loginMigrationDto by lazyFast {
        JsonHelper.fromJson<LoginMigrationDto>(intent.getStringExtra(EXTRA_LOGIN_MIGRATION_DTO))
    }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        setupViewPager()
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

    override fun onBackPressed() {
        onBackPressedEvent?.onBackPressed()
        when (viewPagerMigrationMerge.currentItem) {
            0 -> {
                onBackPressed(false)
                overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
            }
            if (adapter.count == 4) 3 else 2 -> {
                navigator.navigateClearUpStack(
                    this,
                    MigrationFormActivity::class.java,
                    null,
                    isClear = true,
                    isAnimated = true
                )
            }
            else -> {
                if (viewUtil.isSoftKeyboardShown(parentLayout)) {
                    viewUtil.dismissKeyboard(this)
                }
                viewPagerMigrationMerge.currentItem = viewPagerMigrationMerge.currentItem.minus(1)
            }
        }
    }

    private fun setupViewPager() {
        adapter = ViewPagerAdapter(
            supportFragmentManager
        )
        adapter.addFragment(
            NominateMergeEmailFragment.newInstance(),
            FRAGMENT_MERGE_EMAIL_TAKEN
        )
        adapter.addFragment(
            NominateMergeVerifyFragment.newInstance(),
            FRAGMENT_MERGE_VERIFY
        )
        if (getType() == TYPE_ECREDITING) {
            adapter.addFragment(
                NominateMergeVerifyAccountFragment.newInstance(),
                FRAGMENT_MERGE_VERIFY_OTP
            )
            viewPagerMigrationMerge.offscreenPageLimit = 3
        } else {
            viewPagerMigrationMerge.offscreenPageLimit = 2
        }
        adapter.addFragment(
            NominateMergeSuccessFragment.newInstance(),
            FRAGMENT_MERGE_SUCCESS
        )

        viewPagerMigrationMerge.currentItem = 1
        viewPagerMigrationMerge.adapter = adapter
        viewPagerMigrationMerge.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(position: Int) {
            }
        })
    }

    interface OnBackPressedEvent {
        fun onBackPressed() = Unit
    }

    fun setOnBackPressedEvent(onBackPressedEvent: OnBackPressedEvent) {
        this.onBackPressedEvent = onBackPressedEvent
    }

    fun getViewPager(): ViewPager = viewPagerMigrationMerge

    fun getType(): String = intent.getStringExtra(EXTRA_TYPE).notNullable()

    fun getAccessToken(): String = intent.getStringExtra(EXTRA_ACCESS_TOKEN).notNullable()

    fun getLoginMigrationMergeInfo(): LoginMigrationDto = loginMigrationDto

    companion object {
        const val EXTRA_LOGIN_MIGRATION_DTO = "login_migration_dto"
        const val EXTRA_TYPE = "type"
        const val EXTRA_ACCESS_TOKEN = "access_token"
        const val TYPE_ECREDITING = "e_crediting"
        const val TYPE_EBANKING = "e_banking"
        const val FRAGMENT_MERGE_EMAIL_TAKEN = "merge_email_taken"
        const val FRAGMENT_MERGE_VERIFY = "merge_verify"
        const val FRAGMENT_MERGE_VERIFY_OTP = "merge_verify_otp"
        const val FRAGMENT_MERGE_SUCCESS = "merge_success"
    }
}
