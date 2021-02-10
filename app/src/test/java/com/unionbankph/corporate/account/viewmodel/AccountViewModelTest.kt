package com.unionbankph.corporate.account.viewmodel

import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.domain.form.GetAccountsBalances
import com.unionbankph.corporate.account.presentation.account_list.AccountViewModel
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.bus.data.DataBus
import com.unionbankph.corporate.common.BaseTest
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.getOrAwaitValue
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import io.reactivex.Single
import junit.framework.Assert.assertEquals
import org.junit.Test

/**
 * Created by herald25santos on 3/6/20
 */
class AccountViewModelTest : BaseTest() {

    @RelaxedMockK
    lateinit var accountGateway: AccountGateway

    @RelaxedMockK
    lateinit var settingsGateway: SettingsGateway

    @RelaxedMockK
    lateinit var dataBus: DataBus

    lateinit var viewModel: AccountViewModel

    override fun setupBeforeTest() {
        super.setupBeforeTest()
        viewModel = AccountViewModel(accountGateway, settingsGateway, dataBus)
    }

    override fun isMockServerEnabled(): Boolean = false

    @Test
    fun `get accounts, then return with accounts result`() {
        val stringResponse = getJson("account_list")
        val expectedResponse = JsonHelper.fromListJson<Account>(stringResponse)
        val pagedDto = PagedDto(results = expectedResponse)
        every {
            accountGateway.getAccountsPaginated(
                pageable = viewModel.pageable
            )
        } returns Single.just(pagedDto)

        viewModel.getCorporateUserOrganization(true)

        verify {
            accountGateway.getAccountsPaginated(
                pageable = viewModel.pageable
            )
        }

        val accounts = viewModel.accounts.getOrAwaitValue()
        assertEquals(expectedResponse.size, accounts.size)
    }

    @Test
    fun `update single account, then return updated account full details`() {
        val stringListResponse = getJson("account_list")
        val expectedListResponse = JsonHelper.fromListJson<Account>(stringListResponse)
        val stringResponse = getJson("account_details")
        val expectedResponse = JsonHelper.fromJson<Account>(stringResponse)

        viewModel.updateEachAccount(expectedResponse, expectedListResponse)

        val accounts = viewModel.accounts.getOrAwaitValue()
        val account = accounts.find { expectedResponse.id == it.id }
        assertEquals(expectedResponse.productCodeDesc, account?.productCodeDesc)
    }

    @Test
    fun `update multiple accounts, then return update each account full details`() {
        val stringRequest = getJson("account_list")
        val expectedRequest = JsonHelper.fromListJson<Account>(stringRequest)
        val stringResponse = getJson("account_list_balances")
        val expectedResponse = JsonHelper.fromListJson<Account>(stringResponse)
        val getAccountsBalances =
            GetAccountsBalances(expectedRequest.map { it.id.notNullable() }.toMutableList())
        val pagedDto = PagedDto(results = expectedResponse)

        every {
            accountGateway.getAccountsPaginated(
                pageable = viewModel.pageable
            )
        } returns Single.just(pagedDto)

        every {
            accountGateway.getAccountsBalances(getAccountsBalances)
        } returns Single.just(expectedResponse)

        viewModel.getCorporateUserOrganization(true)

        verify {
            accountGateway.getAccountsPaginated(
                pageable = viewModel.pageable
            )
        }
        verify {
            accountGateway.getAccountsBalances(getAccountsBalances)
        }

        val accounts = viewModel.accounts.getOrAwaitValue()
        val account1 = accounts.find { expectedRequest[0].id == it.id }
        val account2 = accounts.find { expectedRequest[1].id == it.id }
        val account3 = accounts.find { expectedRequest[2].id == it.id }
        assertEquals("USD 971,650,864.43", account1?.headers?.get(0)?.value)
        assertEquals("PHP 1,000,000.00", account2?.headers?.get(0)?.value)
        assertEquals("PHP 2,000,000.00", account3?.headers?.get(0)?.value)
    }

}