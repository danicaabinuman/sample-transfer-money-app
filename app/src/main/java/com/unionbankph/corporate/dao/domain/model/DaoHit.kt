package com.unionbankph.corporate.dao.domain.model

/**
 * Created by herald25santos on 6/16/20
 */
data class DaoHit(
    var isHit: Boolean = false,
    var businessName: String? = null,
    var preferredBranch: String? = null,
    var preferredBranchEmail: String? = null,
    var referenceNumber: String
)