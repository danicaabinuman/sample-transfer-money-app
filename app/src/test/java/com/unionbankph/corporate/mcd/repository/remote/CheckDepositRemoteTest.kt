package com.unionbankph.corporate.mcd.repository.remote

import com.unionbankph.corporate.common.BaseTest
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.mcd.data.form.CheckDepositForm
import com.unionbankph.corporate.mcd.data.model.CheckDepositUpload
import com.unionbankph.corporate.mcd.data.source.remote.impl.CheckDepositRemoteImpl
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositStatusEnum
import org.junit.Test
import java.net.HttpURLConnection

/**
 * Created by herald25santos on 09/22/20
 */
class CheckDepositRemoteTest : BaseTest() {

    private lateinit var checkDepositRemote: CheckDepositRemoteImpl

    override fun isMockServerEnabled(): Boolean = true

    override fun setupBeforeTest() {
        super.setupBeforeTest()
        checkDepositRemote = CheckDepositRemoteImpl(retrofit)
    }

    @Test
    fun `upload check, then it should return check deposit details`() {
        mockHttpResponse("check_deposit_upload", HttpURLConnection.HTTP_OK)
        val expectedResponse =
            JsonHelper.fromJson<CheckDepositUpload>(getJson("check_deposit_upload"))
        val checkFile = getFileFromPath("raw/cheque.jpg")

        val responseObserver = checkDepositRemote.checkDepositUploadFile(
            "accessToken",
            checkFile,
            "front",
            "1234"
        ).test()

        responseObserver.assertValue {
            val checkDepositUpload = it.body()
            expectedResponse.id == checkDepositUpload?.id
        }

        responseObserver.dispose()
    }

    @Test
    fun `submit check, then it should return check deposit details`() {
        mockHttpResponse("check_deposit_success", HttpURLConnection.HTTP_OK)
        val checkDepositForm = CheckDepositForm(id = "0001")

        val responseObserver = checkDepositRemote.checkDeposit(
            "accessToken",
            checkDepositForm
        ).test()

        responseObserver.assertValue {
            val checkDepositUpload = it.body()
            "0001" == checkDepositUpload?.id
        }

        responseObserver.dispose()
    }

    @Test
    fun `get check deposit list, then return with result`() {
        mockHttpResponse("check_deposit_list", HttpURLConnection.HTTP_OK)
        val responseObserver = checkDepositRemote.getCheckDeposits(
            "accessToken",
            Pageable(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        ).test()

        responseObserver.assertValue {
            val response = it.body()
            1 == response?.results?.size
        }
        responseObserver.dispose()
    }

    @Test
    fun `get check deposit by id, then return with the details`() {
        mockHttpResponse("check_deposit_success", HttpURLConnection.HTTP_OK)
        val responseObserver = checkDepositRemote.getCheckDeposit(
            "accessToken",
            "1"
        ).test()

        responseObserver.assertValue {
            "000770008690" == it.body()?.sourceAccount
        }
        responseObserver.assertValue {
            "0001" == it.body()?.id
        }
        responseObserver.dispose()
    }

    @Test
    fun `get check deposit activity logs, then return activity result`() {
        mockHttpResponse("check_deposit_activity_log", HttpURLConnection.HTTP_OK)
        val responseObserver = checkDepositRemote.getCheckDepositActivityLogs(
            "accessToken",
            "1"
        ).test()

        responseObserver.assertValue {
            1 == it.body()?.size
        }
        responseObserver.assertValue {
            CheckDepositStatusEnum.FOR_CLEARING.name == it.body()?.get(0)?.status?.type
        }
        responseObserver.dispose()
    }

}