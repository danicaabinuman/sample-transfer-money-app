package com.unionbankph.corporate.user_creation.data.param

import com.unionbankph.corporate.common.presentation.constant.PromptTypeEnum
import com.unionbankph.corporate.notification.data.model.Notification

data class PersonalizeSettings(
    var notification: Boolean? = null,
    var notifications: MutableList<Notification>? = null,
    var totp: Boolean? = null,
    var promptTypeEnum: PromptTypeEnum? = null,
)