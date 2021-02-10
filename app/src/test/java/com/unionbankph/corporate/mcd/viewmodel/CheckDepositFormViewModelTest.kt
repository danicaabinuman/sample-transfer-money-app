package com.unionbankph.corporate.mcd.viewmodel

import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.auth.data.model.Permissions
import com.unionbankph.corporate.common.BaseTest
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.fund_transfer.data.model.Bank
import com.unionbankph.corporate.mcd.data.form.CheckDepositForm
import com.unionbankph.corporate.mcd.data.model.CheckDepositUpload
import com.unionbankph.corporate.mcd.presentation.form.CheckDepositFormViewModel
import com.unionbankph.corporate.settings.data.constant.PermissionNameEnum
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

/**
 * Created by herald25santos on 3/6/20
 */
class CheckDepositFormViewModelTest : BaseTest() {

    @RelaxedMockK
    lateinit var schedulerProvider: SchedulerProvider

    @RelaxedMockK
    lateinit var cacheManager: CacheManager

    @RelaxedMockK
    lateinit var viewUtil: ViewUtil

    @RelaxedMockK
    lateinit var settingsGateway: SettingsGateway

    lateinit var viewModel: CheckDepositFormViewModel

    override fun setupBeforeTest() {
        super.setupBeforeTest()
        viewModel =
            CheckDepositFormViewModel(schedulerProvider, cacheManager, viewUtil, settingsGateway)
    }

    override fun isMockServerEnabled(): Boolean = false

    @Test
    fun `get mcd accounts permission, then it should id if I have permission`() {
        val expectedPermissions = Permissions(1, "MCD")

        every {
            settingsGateway.hasPermissionChannel(
                PermissionNameEnum.CHECK_DEPOSIT.value,
                Constant.Permissions.CODE_RCD_MOBILE_CHECK
            )
        } returns Single.just(expectedPermissions)

        viewModel.getAccountMCDPermission(
            PermissionNameEnum.CHECK_DEPOSIT.value,
            Constant.Permissions.CODE_RCD_MOBILE_CHECK
        )

        verify {
            settingsGateway.hasPermissionChannel(
                PermissionNameEnum.CHECK_DEPOSIT.value,
                Constant.Permissions.CODE_RCD_MOBILE_CHECK
            )
        }

        val result = viewModel.permissionId.value
        assertEquals(expectedPermissions.id, result?.toInt())
    }

    @Test
    fun `set check deposit form, then it should mcd model from json string`() {
        val expectedStringResult = getJson("check_deposit_form")
        val expectedResult = JsonHelper.fromJson<CheckDepositUpload>(expectedStringResult)

        viewModel.setCheckDepositUpload(expectedStringResult)

        val bankInput = viewModel.bankInput.value
        val checkDepositForm = viewModel.checkDepositForm.value
        assertEquals(expectedResult.issuer, bankInput?.bank)
        assertEquals(expectedResult.brstn, bankInput?.pesonetBrstn)
        assertEquals(expectedResult.id, checkDepositForm?.id)
        assertEquals(expectedResult.checkNumber, checkDepositForm?.checkNumber)
    }

    @Test
    fun `submit check deposit form, then it should check deposit form json string`() {
        val expectedStringResult = getJson("check_deposit_submit")

        viewModel.checkDepositForm.onNext(CheckDepositForm())
        viewModel.depositToAccount.onNext(
            Account(
                currency = "PHP",
                name = "Test",
                accountNumber = "123132132321",
                productCodeDesc = "Test"
            )
        )
        viewModel.bankInput.onNext(Bank(bank = "Test", pesonetBrstn = "321321"))
        viewModel.dateOnCheckInput.onNext(Calendar.getInstance())

        viewModel.submitForm()

        val actualResult = viewModel.checkDepositFormOutput.value
        assertEquals(expectedStringResult, actualResult)
    }

}