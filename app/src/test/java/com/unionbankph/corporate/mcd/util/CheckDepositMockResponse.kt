package com.unionbankph.corporate.mcd.util

import android.annotation.SuppressLint
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.bills_payment.data.model.Amount
import com.unionbankph.corporate.common.data.model.ContextualClassStatus
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.mcd.data.model.CheckDeposit
import com.unionbankph.corporate.mcd.data.model.CheckDepositUpload
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositStatusEnum
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by herald25santos on 3/6/20
 */
class CheckDepositMockResponse {

    fun getMockResponseCheckDepositTransactions(): PagedDto<CheckDeposit> {
        return PagedDto(
            results = mutableListOf(
                checkDepositItem()
            ),
            hasNextPage = false,
            totalElements = 3,
            currentPage = 0,
            pageSize = 1,
            totalPages = 1
        )
    }

    fun checkDepositItem(): CheckDeposit {
        return CheckDeposit(
            id = "0001",
            issuer = "UnionBank of the Philippines",
            sourceAccount = "000770008690",
            sourceAccountName = "AAA Corporation",
            targetAccount = "000770008690",
            targetAccountName = "AAA Corporation",
            checkNumber = "123457891",
            currency = "PHP",
            checkDate = "2019-03-28 20:25:00.435",
            status = ContextualClassStatus(
                type = CheckDepositStatusEnum.CLEARED.name,
                description = CheckDepositStatusEnum.CLEARED.value,
                contextualClass = Constant.ContextualClass.SUCCESS
            ),
            checkAmount = Amount(
                "PHP",
                250000.00
            ),
            createdBy = "Andrew Davis",
            createdDate = getTransactionDate(ViewUtil.DATE_FORMAT_ISO_WITHOUT_T)
        )
    }

    fun getUploadFileResponse(): CheckDepositUpload {
        return CheckDepositUpload(
            "1",
            "1234",
            "6789",
            "000010055940",
            "Account Name",
            "136810",
            "China Bank",
            "12345"
        )
    }

    // generate transaction date
    private fun getTransactionDate(format: String): String {
        val calendarPosted = Calendar.getInstance()
        return getDateFormatByTimeMilliSeconds(calendarPosted.timeInMillis, format)
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateFormatByTimeMilliSeconds(timeMilliSeconds: Long?, desireFormat: String): String {
        return if (timeMilliSeconds != null) {
            val simpleDateFormat = SimpleDateFormat(desireFormat)
            simpleDateFormat.format(timeMilliSeconds)
        } else {
            Constant.EMPTY
        }
    }

}