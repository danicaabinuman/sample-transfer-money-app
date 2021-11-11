package com.unionbankph.corporate.user_creation.data.param

import com.unionbankph.corporate.common.presentation.constant.PromptTypeEnum

data class PersonalizeSettings(
    var notification: Boolean? = null,
    var totp: Boolean? = null,
    var promptTypeEnum: PromptTypeEnum? = null
)