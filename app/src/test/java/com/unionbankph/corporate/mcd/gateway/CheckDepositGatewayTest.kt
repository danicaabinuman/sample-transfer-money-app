package com.unionbankph.corporate.mcd.gateway

import com.unionbankph.corporate.common.BaseTest
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.domain.provider.impl.ResponseProviderImpl
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.mcd.data.form.CheckDepositForm
import com.unionbankph.corporate.mcd.data.gateway.impl.CheckDepositGatewayImpl
import com.unionbankph.corporate.mcd.data.model.CheckDeposit
import com.unionbankph.corporate.mcd.data.model.CheckDepositActivityLog
import com.unionbankph.corporate.mcd.data.model.CheckDepositUpload
import com.unionbankph.corporate.mcd.data.source.local.CheckDepositCache
import com.unionbankph.corporate.mcd.data.source.remote.CheckDepositRemote
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import io.reactivex.Single
import org.junit.Test
import retrofit2.Response

/**
 * Created by herald25santos on 09/22/20
 */
class CheckDepositGatewayTest : BaseTest() {

    @InjectMockKs
    lateinit var responseProvider: ResponseProviderImpl

    @RelaxedMockK
    lateinit var checkDepositRemote: CheckDepositRemote

    @RelaxedMockK
    lateinit var checkDepositCache: CheckDepositCache

    @RelaxedMockK
    lateinit var settingGateway: SettingsGateway

    private lateinit var checkDepositGatewayImpl: CheckDepositGatewayImpl

    override fun setupBeforeTest() {
        super.setupBeforeTest()
        checkDepositGatewayImpl = CheckDepositGatewayImpl(
            responseProvider,
            checkDepositRemote,
            checkDepositCache,
            settingGateway
        )
    }

    override fun isMockServerEnabled(): Boolean = false

    @Test
    fun `upload check, then it should return check deposit details`() {
        val expectedResponse =
            JsonHelper.fromJson<CheckDepositUpload>(getJson("check_deposit_upload"))
        val checkFile = getFileFromPath("raw/cheque.jpg")

        every {
            checkDepositRemote.checkDepositUploadFile("accessToken", checkFile, "front", "1234")
        } returns Single.just(Response.success(expectedResponse))
        every {
            settingGateway.getAccessToken()
        } returns Single.just("accessToken")

        val responseObserver = checkDepositGatewayImpl.checkDepositUploadFile(
            checkFile, "front", "1234"
        ).test()

        verify {
            checkDepositRemote.checkDepositUploadFile("accessToken", checkFile, "front", "1234")
        }
        verify {
            settingGateway.getAccessToken()
        }

        responseObserver.assertValue(expectedResponse)
        responseObserver.dispose()
    }

    @Test
    fun `submit check details, then it should submitted and success`() {
        val expectedResponse =
            JsonHelper.fromJson<CheckDeposit>(getJson("check_deposit_success"))
        val checkDepositForm = CheckDepositForm()
        every {
            checkDepositRemote.checkDeposit("accessToken", checkDepositForm)
        } returns Single.just(Response.success(expectedResponse))
        every {
            settingGateway.getAccessToken()
        } returns Single.just("accessToken")

        val responseObserver = checkDepositGatewayImpl.checkDeposit(
            checkDepositForm
        ).test()

        verify {
            checkDepositRemote.checkDeposit("accessToken", checkDepositForm)
        }
        verify {
            settingGateway.getAccessToken()
        }

        responseObserver.assertValue { it.referenceNumber == expectedResponse.referenceNumber }
        responseObserver.dispose()
    }

    @Test
    fun `get check deposit list, then it should return results`() {
        val expectedResponse =
            JsonHelper.fromJson<CheckDeposit>(getJson("check_deposit_success"))
        val pageable = Pageable()
        every {
            checkDepositRemote.getCheckDeposits(
                "accessToken",
                pageable,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )
        } returns Single.just(Response.success(PagedDto(results = mutableListOf(expectedResponse))))
        every {
            settingGateway.getAccessToken()
        } returns Single.just("accessToken")

        val responseObserver = checkDepositGatewayImpl.getCheckDeposits(
            pageable,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        ).test()

        verify {
            checkDepositRemote.getCheckDeposits(
                "accessToken",
                pageable,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )
        }
        verify {
            settingGateway.getAccessToken()
        }

        responseObserver.assertValue { it.results.size == 1 }
        responseObserver.dispose()
    }

    @Test
    fun `get check details, then it should return result`() {
        val expectedResponse =
            JsonHelper.fromJson<CheckDeposit>(getJson("check_deposit_success"))
        every {
            checkDepositRemote.getCheckDeposit(
                "accessToken",
                "1"
            )
        } returns Single.just(Response.success(expectedResponse))
        every {
            settingGateway.getAccessToken()
        } returns Single.just("accessToken")

        val responseObserver = checkDepositGatewayImpl.getCheckDeposit(
            "1"
        ).test()

        verify {
            checkDepositRemote.getCheckDeposit(
                "accessToken",
                "1"
            )
        }
        verify {
            settingGateway.getAccessToken()
        }

        responseObserver.assertValue { it.id == expectedResponse.id }
        responseObserver.dispose()
    }

    @Test
    fun `get check details activity logs, then it should return result`() {
        val expectedResponse =
            JsonHelper.fromListJson<CheckDepositActivityLog>(getJson("check_deposit_activity_log"))
        every {
            checkDepositRemote.getCheckDepositActivityLogs(
                "accessToken",
                "1"
            )
        } returns Single.just(Response.success(expectedResponse))
        every {
            settingGateway.getAccessToken()
        } returns Single.just("accessToken")

        val responseObserver = checkDepositGatewayImpl.getCheckDepositActivityLogs(
            "1"
        ).test()

        verify {
            checkDepositRemote.getCheckDepositActivityLogs(
                "accessToken",
                "1"
            )
        }
        verify {
            settingGateway.getAccessToken()
        }

        responseObserver.assertValue { it.size == expectedResponse.size }
        responseObserver.dispose()
    }


}