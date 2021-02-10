package com.unionbankph.corporate.mcd.interactor

import com.unionbankph.corporate.common.BaseTest
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.provider.impl.ResponseProviderImpl
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.corporate.data.model.TransactionStatusDto
import com.unionbankph.corporate.corporate.domain.gateway.CorporateGateway
import com.unionbankph.corporate.corporate.domain.interactor.GetTransactionStatuses
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositStatusEnum
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import io.reactivex.Single
import org.junit.Test
import retrofit2.Response

/**
 * Created by herald25santos on 07/23/20
 */
class GetTransactionStatusesTest : BaseTest() {

    @RelaxedMockK
    lateinit var threadExecutor: ThreadExecutor

    @RelaxedMockK
    lateinit var postThreadExecutor: PostExecutionThread

    @InjectMockKs
    lateinit var responseProvider: ResponseProviderImpl

    @RelaxedMockK
    lateinit var settingsGateway: SettingsGateway

    @RelaxedMockK
    lateinit var corporateGateway: CorporateGateway

    private lateinit var getTransactionStatuses: GetTransactionStatuses

    override fun setupBeforeTest() {
        super.setupBeforeTest()
        getTransactionStatuses = GetTransactionStatuses(
            threadExecutor,
            postThreadExecutor,
            responseProvider,
            settingsGateway,
            corporateGateway
        )
    }

    override fun isMockServerEnabled(): Boolean = false

    @Test
    fun `build usecase observable calls repository`() {
        mockRepositories()

        getTransactionStatuses.buildUseCaseObservable("10").subscribe()

        verify { settingsGateway.getAccessToken() }
        verify {
            corporateGateway.getTransactionStatuses("accessToken", "10")
        }
    }

    @Test
    fun `build usecase observable completes`() {
        mockRepositories()

        val responseObserver = getTransactionStatuses.buildUseCaseObservable("10").test()
        responseObserver.assertComplete()
        responseObserver.dispose()
    }

    @Test
    fun `build usecase single return data`() {
        val expectedStatuses = mutableListOf(
            Selector(
                id = CheckDepositStatusEnum.FOR_CLEARING.name,
                value = CheckDepositStatusEnum.FOR_CLEARING.value
            ),
            Selector(
                id = CheckDepositStatusEnum.REJECTED.name,
                value = CheckDepositStatusEnum.REJECTED.value
            ),
            Selector(
                id = CheckDepositStatusEnum.POSTED.name,
                value = CheckDepositStatusEnum.POSTED.value
            )
        )
        mockRepositories()

        val responseObserver = getTransactionStatuses.buildUseCaseObservable("10").test()

        verify { settingsGateway.getAccessToken() }
        verify {
            corporateGateway.getTransactionStatuses("accessToken", "10")
        }

        responseObserver.assertValue(expectedStatuses)
        responseObserver.dispose()
    }

    private fun mockRepositories() {
        val mockStatuses =
            JsonHelper.fromListJson<TransactionStatusDto>(getJson("mcd_statuses"))
        every {
            settingsGateway.getAccessToken()
        } returns Single.just("accessToken")
        every {
            corporateGateway.getTransactionStatuses("accessToken", "10")
        } returns Single.just(Response.success(mockStatuses))
    }

}