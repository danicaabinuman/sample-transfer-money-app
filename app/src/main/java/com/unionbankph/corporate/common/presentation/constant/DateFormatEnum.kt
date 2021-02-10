package com.unionbankph.corporate.common.presentation.constant

/**
 * Created by herald25santos on 31/01/2019
 */
enum class DateFormatEnum(val value: String) {
    DATE_FORMAT_ISO("yyyy-MM-dd'T'HH:mm:ss.SSS"),
    DATE_FORMAT_ISO_Z("yyyy-MM-dd'T'HH:mm:ss.SSSZ"),
    DATE_FORMAT_ISO_WITHOUT_T("yyyy-MM-dd HH:mm:ss.SSS"),
    DATE_FORMAT_ISO_DATE("yyyy-MM-dd"),
    DATE_FORMAT_DEFAULT("MMMM dd, yyyy, h:mm a"),
    DATE_FORMAT_DATE("MMMM dd, yyyy"),
    DATE_FORMAT_THREE_DATE("MMM dd, yyyy"),
    DATE_FORMAT_DATE_SLASH("MM/dd/yyyy"),
    DATE_FORMAT_WITHOUT_TIME("MMMM dd, yyyy, EEEE"),
    DATE_FORMAT_TIME("h:mm a");
}
