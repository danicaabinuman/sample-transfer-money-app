package com.unionbankph.corporate.auth.presentation.migration

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.widget.recyclerview.viewpager.ViewPagerAdapter
import com.unionbankph.corporate.auth.data.model.ECredLoginOTPDto
import com.unionbankph.corporate.auth.data.model.LoginMigrationDto
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.auth.presentation.migration.nominate_email.NominateEmailFragment
import com.unionbankph.corporate.auth.presentation.migration.nominate_mobile_number.NominateMobileNumberFragment
import com.unionbankph.corporate.auth.presentation.migration.nominate_password.NominatePasswordFragment
import com.unionbankph.corporate.auth.presentation.migration.nominate_verify.NominateVerifyAccountFragment
import com.unionbankph.corporate.auth.presentation.migration.nominate_verify.NominateVerifyAccountResultFragment
import com.unionbankph.corporate.auth.presentation.migration.nominate_welcome.NominateWelcomeFragment
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import com.unionbankph.corporate.databinding.ActivityMigrationMainBinding

class MigrationMainActivity :
    BaseActivity<ActivityMigrationMainBinding, GeneralViewModel>() {

    private lateinit var adapter: ViewPagerAdapter

    private var onBackPressedEvent: OnBackPressedEvent? = null

    private var loginMigrationDto = LoginMigrationDto()

    private var eCredLoginOTPDto = ECredLoginOTPDto()

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        if (getType() == TYPE_ECREDITING) {
            eCredLoginOTPDto = JsonHelper.fromJson(intent.getStringExtra(EXTRA_DATA))
        } else {
            loginMigrationDto = JsonHelper.fromJson(intent.getStringExtra(EXTRA_DATA))
        }

        binding.textViewStep.text = String.format(
            getString(R.string.param_steps), 1, 5
        )
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
        when (binding.viewPagerMigration.currentItem) {
            0 -> {
                super.onBackPressed()
            }
            5 -> {
                navigator.navigateClearUpStack(
                    this,
                    LoginActivity::class.java,
                    Bundle().apply {
                        putBoolean(LoginActivity.EXTRA_SPLASH_SCREEN, false)
                    },
                    isClear = true,
                    isAnimated = true
                )
            }
            else -> {
                if (viewUtil.isSoftKeyboardShown(binding.parentLayout)) {
                    viewUtil.dismissKeyboard(this)
                }
                binding.viewPagerMigration.currentItem = binding.viewPagerMigration.currentItem.minus(1)
            }
        }
    }

    private fun setupViewPager() {
        adapter = ViewPagerAdapter(
            supportFragmentManager
        )
        adapter.addFragment(
            NominateWelcomeFragment.newInstance(),
            FRAGMENT_NOMINATE_WELCOME
        )
        adapter.addFragment(
            NominateEmailFragment.newInstance(),
            FRAGMENT_NOMINATE_EMAIL
        )
        adapter.addFragment(
            NominatePasswordFragment.newInstance(),
            FRAGMENT_NOMINATE_PASSWORD
        )
        adapter.addFragment(
            NominateMobileNumberFragment.newInstance(),
            FRAGMENT_NOMINATE_MOBILE_NUMBER
        )
        adapter.addFragment(
            NominateVerifyAccountFragment.newInstance(),
            FRAGMENT_NOMINATE_VERIFY_ACCOUNT
        )
        adapter.addFragment(
            NominateVerifyAccountResultFragment.newInstance(),
            FRAGMENT_NOMINATE_VERIFY_ACCOUNT_RESULT
        )
        binding.viewPagerMigration.offscreenPageLimit = 5
        binding.viewPagerMigration.currentItem = 1
        binding.viewPagerMigration.adapter = adapter
        binding.viewPagerMigration.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0,
                    5 -> {
                        setDrawableBackButton(R.drawable.ic_close_white_24dp)
                    }
                    else -> {
                        setDrawableBackButton(R.drawable.ic_arrow_back_white_24dp)
                    }
                }
                binding.textViewStep.visibility = if (position == 0) View.GONE else View.VISIBLE
                binding.textViewStep.text = String.format(
                    getString(R.string.param_steps),
                    position,
                    5
                )
            }
        })
    }

    interface OnBackPressedEvent {
        fun onBackPressed() = Unit
    }

    fun setOnBackPressedEvent(onBackPressedEvent: OnBackPressedEvent) {
        this.onBackPressedEvent = onBackPressedEvent
    }

    fun getType() = intent.getStringExtra(EXTRA_TYPE)

    fun setCurrentViewPager(currentItem: Int) {
        binding.viewPagerMigration.currentItem = currentItem
    }

    fun getViewPager(): ViewPager = binding.viewPagerMigration

    fun getLoginMigrationInfo(): LoginMigrationDto = loginMigrationDto

    fun getECredLoginOTPDto(): ECredLoginOTPDto = eCredLoginOTPDto

    fun getAccessToken(): String = eCredLoginOTPDto.accessToken.notNullable()

    companion object {
        const val EXTRA_DATA = "data"
        const val EXTRA_TYPE = "type"
        const val EXTRA_USER_ID = "user_id"
        const val TYPE_ECREDITING = "e_crediting"
        const val TYPE_EBANKING = "e_banking"
        const val FRAGMENT_NOMINATE_WELCOME = "nominate_welcome"
        const val FRAGMENT_NOMINATE_EMAIL = "nominate_email"
        const val FRAGMENT_NOMINATE_PASSWORD = "nominate_password"
        const val FRAGMENT_NOMINATE_MOBILE_NUMBER = "nominate_mobile_number"
        const val FRAGMENT_NOMINATE_VERIFY_ACCOUNT = "nominate_verify_account"
        const val FRAGMENT_NOMINATE_VERIFY_ACCOUNT_RESULT = "nominate_verify_account_result"
    }

    override val layoutId: Int
        get() = R.layout.activity_migration_main

    override val viewModelClassType: Class<GeneralViewModel>
        get() = GeneralViewModel::class.java
}
