package com.unionbankph.corporate.common.presentation.constant

/**
 * Created by herald25santos on 31/01/2019
 */
enum class RegexFormatEnum(val value: String) {
    REGEX_FORMAT_HAS_NUMBER(".*\\d+.*"),
    REGEX_FORMAT_HAS_ALPHA(".*[A-Za-z].*"),
    REGEX_FORMAT_HAS_SYMBOL(".*[^A-Za-z0-9].*");
}
