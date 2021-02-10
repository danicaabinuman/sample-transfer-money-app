package com.unionbankph.corporate.mcd.viewmodel

import com.unionbankph.corporate.common.BaseTest
import com.unionbankph.corporate.common.getOrAwaitValue
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.corporate.domain.interactor.GetTransactionStatuses
import com.unionbankph.corporate.mcd.domain.model.CheckDepositFilter
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositStatusEnum
import com.unionbankph.corporate.mcd.presentation.filter.CheckDepositFilterViewModel
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import io.mockk.verify
import io.reactivex.observers.DisposableSingleObserver
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by herald25santos on 3/6/20
 */
class CheckDepositFilterViewModelTest : BaseTest() {

    @RelaxedMockK
    lateinit var settingsGateway: SettingsGateway

    @RelaxedMockK
    lateinit var getTransactionStatuses: GetTransactionStatuses

    private lateinit var viewModel: CheckDepositFilterViewModel

    override fun setupBeforeTest() {
        super.setupBeforeTest()
        viewModel = CheckDepositFilterViewModel(getTransactionStatuses, settingsGateway)
    }

    override fun isMockServerEnabled(): Boolean = false

    @Test
    fun `on clicked clear filter, then fields should return empty`() {
        /* Given */


        /* When */
        viewModel.onClickedClearFilter()


        /* Then */
        assertEquals("", viewModel.input.checkNumber.value)
        assertEquals("${viewModel.input.depositAccount.value?.currency ?: "PHP"} 0.00", viewModel.input.amount.value)
        assertEquals("", viewModel.input.checkStartDate.value)
        assertEquals("", viewModel.input.checkEndDate.value)
        assertEquals("", viewModel.input.status.value)
        assertEquals("", viewModel.input.statusToDisplay.value)
        assertEquals("", viewModel.input.startDateCreated.value)
        assertEquals("", viewModel.input.endDateCreated.value)
        assertEquals(false, viewModel.input.hasDepositAccount.value)
    }

    @Test
    fun `on clicked apply filter, then should return count filter`() {
        viewModel.input.checkNumber.onNext("1234")
        viewModel.input.amount.onNext("1.00")
        viewModel.input.hasDepositAccount.onNext(true)
        viewModel.input.checkStartDate.onNext("123132")
        viewModel.input.status.onNext("status")
        viewModel.input.startDateCreated.onNext("12321")

        viewModel.onClickedApplyFilter()


        val checkDepositFilter = JsonHelper.fromJson<CheckDepositFilter>(
            viewModel.applyFilter.getOrAwaitValue().peekContent()
        )
        assertEquals(6, checkDepositFilter.filterCount)
    }

    @Test
    fun `on clicked statuses, then it should match the return mcd statuses`() {
        val expectedStatuses = mutableListOf(
            Selector(id = CheckDepositStatusEnum.FOR_CLEARING.name),
            Selector(id = CheckDepositStatusEnum.REJECTED.name),
            Selector(id = CheckDepositStatusEnum.POSTED.name)
        )

        viewModel.onClickedStatuses()

        val captor = slot<DisposableSingleObserver<MutableList<Selector>>>()
        verify { getTransactionStatuses.execute(capture(captor), any(), any(), any()) }
        captor.captured.onSuccess(expectedStatuses)

        assertEquals(expectedStatuses, viewModel.statuses.getOrAwaitValue().peekContent())
    }

}