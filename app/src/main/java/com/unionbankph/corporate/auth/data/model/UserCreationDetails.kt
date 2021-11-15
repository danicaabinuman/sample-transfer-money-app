package com.unionbankph.corporate.auth.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserCreationDetails(

    @SerialName("token")
    val token: Token? = null,

    @SerialName("role")
    val role: UserCreationRole? = null,

    @SerialName("corporate_user")
    val corporateUser: UserCreationCorporateUser? = null,

    @SerialName("approval_groups")
    val approvalGroups: MutableList<Int>? = null,

    @SerialName("read_mcd_terms")
    var isPolicyAgreed: Boolean? = null,

    @SerialName("trusted")
    var isTrusted: Boolean? = null,

    @SerialName("readMcdTerms")
    var readMcdTerms: Boolean? = null,

    @SerialName("trial_days_remaining")
    var trialDaysRemaining: Int? = null,

    @SerialName("trial_mode")
    var trialMode: Boolean? = null

)

@Serializable
data class UserCreationRole(

    @SerialName("name")
    var name: String? = null,

    @SerialName("role_id")
    var roleId: String? = null,

    @SerialName("organization_id")
    var organizationId: String? = null,

    @SerialName("organization_name")
    var organizationName: String? = null,

    @SerialName("organization_short_code")
    var organizationShortCode: String? = null,

    @SerialName("role_account_permissions")
    var roleAccountPermissions: MutableList<RoleAccountPermissions>? = null,

    @SerialName("approval_groups")
    var approvalGroups: MutableList<Int>? = null,

    @SerialName("has_approval")
    var hasApproval: Boolean = true,

    @SerialName("approver")
    var isApprover: Boolean = true,

    @SerialName("organization_user_id")
    var organizationUserId: String? = null,
)

@Serializable
data class UserCreationCorporateUser(

    @SerialName("id")
    val id: String? = null,

    @SerialName("salutation")
    val salutation: String? = null,

    @SerialName("email_address")
    val emailAddress: String? = null,

    @SerialName("first_name")
    val firstName: String? = null,

    @SerialName("last_name")
    val lastName: String? = null,

    @SerialName("countryCode")
    var countryCode: UserCreationCountryCode? = null,

    @SerialName("mobile_number")
    var mobileNumber: String? = null,

    @SerialName("activation_id")
    val activationId: String? = null,

    @SerialName("created_date")
    val createdDate: String? = null,

    @SerialName("secret_token")
    val secretToken: String? = null,

    @SerialName("full_name")
    val fullName: String? = null
)

@Parcelize
@Serializable
data class UserCreationCountryCode(

    @SerialName("id")
    var id: Int? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("code")
    val code: String? = null,

    @SerialName("status")
    val status: Int? = null,

    @SerialName("preferred")
    val preferred: Int? = null,

    @SerialName("calling_code")
    val callingCode: String? = null
) : Parcelable