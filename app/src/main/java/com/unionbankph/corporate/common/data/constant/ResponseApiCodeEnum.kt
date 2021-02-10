package com.unionbankph.corporate.common.data.constant

/**
 * Created by herald25santos on 8/6/20
 */
enum class ResponseApiCodeEnum(val value: Int) {
    LOGOUT_USER(401),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500);
}