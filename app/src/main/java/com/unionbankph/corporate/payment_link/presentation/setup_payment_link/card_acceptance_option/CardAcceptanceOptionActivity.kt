package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_card_acceptance_option.viewToolbar
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*

class CardAcceptanceOptionActivity :
    BaseActivity<CardAcceptanceOptionViewModel>(R.layout.activity_card_acceptance_option) {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setDrawableBackButton(R.drawable.ic_msme_back_button_orange, R.color.colorSMEMediumOrange, true)
    }

    override fun onViewsBound() {
        super.onViewsBound()
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
}