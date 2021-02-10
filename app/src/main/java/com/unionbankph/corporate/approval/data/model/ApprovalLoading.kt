package com.unionbankph.corporate.approval.data.model

data class ApprovalLoading(
    var isLoadingHierarchy: Boolean = false,
    var isLoadingApprovalDetail: Boolean = false,
    var isLoadingActivityLogs: Boolean = false
)