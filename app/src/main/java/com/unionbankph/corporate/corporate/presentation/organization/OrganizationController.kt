package com.unionbankph.corporate.corporate.presentation.organization

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.AutoModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.Typed3EpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.notEmpty
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.HeaderTitleModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoadingFooterModel_
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.auth.data.model.UserDetails
import com.unionbankph.corporate.corporate.data.model.CorporateUsers
import com.unionbankph.corporate.databinding.HeaderOrganizationBinding
import com.unionbankph.corporate.databinding.ItemOrganizationBinding

class OrganizationController
constructor(
    private val context: Context,
    private val callbacks: AdapterCallbacks,
    private val viewUtil: ViewUtil
) : Typed3EpoxyController<MutableList<CorporateUsers>, UserDetails, Boolean>() {

    @AutoModel
    lateinit var headerActiveTitleModel: HeaderTitleModel_

    @AutoModel
    lateinit var headerSwitchTitleModel: HeaderTitleModel_

    @AutoModel
    lateinit var organizationHeaderModel: OrganizationHeaderModel_

    @AutoModel
    lateinit var loadingFooterModel: LoadingFooterModel_

    interface AdapterCallbacks {
        fun onClickItem(id: String)
    }

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(
        corporateUsers: MutableList<CorporateUsers>,
        userDetails: UserDetails,
        isLoading: Boolean
    ) {
        headerActiveTitleModel
            .title(context.getString(R.string.title_active_organization))
            .addTo(this)

        organizationHeaderModel
            .accountInitial(
                viewUtil.getCorporateOrganizationInitial(
                    userDetails.role?.organizationName.notEmpty()
                )
            )
            .accountName(userDetails.role?.organizationName.notEmpty())
            .name("${userDetails.corporateUser?.firstName} ${userDetails.corporateUser?.lastName}")
            .role(userDetails.role?.name.notEmpty())
            .email(userDetails.corporateUser?.emailAddress.notEmpty())
            .hasLoading(isLoading)
            .hasOrganization(corporateUsers.isNotEmpty() && !isLoading)
            .addTo(this)

        headerSwitchTitleModel
            .title(context.getString(R.string.title_switch_organization))
            .addIf(corporateUsers.isNotEmpty() && !isLoading, this)

        corporateUsers.forEachIndexed { position, corporate ->
            OrganizationItemModel_()
                .id(corporate.roleId)
                .roleId(corporate.roleId.notNullable())
                .accountInitial(
                    viewUtil.getCorporateOrganizationInitial(
                        corporate.organizationName.notEmpty()
                    )
                )
                .accountName(corporate.organizationName.notEmpty())
                .haveBadge(corporate.badgeCount > 0)
                .colored(corporate.colored)
                .position(position)
                .size(corporateUsers.size)
                .badgeCount(corporate.badgeCount)
                .callbacks(callbacks)
                .addIf(corporateUsers.isNotEmpty() && !isLoading, this)
        }

        loadingFooterModel.loading(isLoading).addIf(isLoading, this)
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // pBest practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }
}

@EpoxyModelClass(layout = R.layout.header_organization)
abstract class OrganizationHeaderModel : EpoxyModelWithHolder<OrganizationHeaderModel.Holder>() {

    @EpoxyAttribute
    lateinit var accountInitial: String

    @EpoxyAttribute
    lateinit var accountName: String

    @EpoxyAttribute
    lateinit var name: String

    @EpoxyAttribute
    lateinit var role: String

    @EpoxyAttribute
    lateinit var email: String

    @EpoxyAttribute
    var hasLoading: Boolean = false

    @EpoxyAttribute
    var hasOrganization: Boolean = false

    @EpoxyAttribute
    lateinit var callbacks: OrganizationController.AdapterCallbacks

    override fun bind(holder: Holder) {
        super.bind(holder)

        holder.binding.apply {
            viewBadgeInitial.textViewInitial.text = accountInitial
            textViewCorporateName.text = accountName
            textViewName.text = name
            textViewRole.text = role
            textViewEmail.text = email
            if (!hasOrganization && !hasLoading) {
                constraintLayoutOrgDetails.visibility = View.VISIBLE
                imageViewCollapse.tag = "open"
                imageViewCollapse.setImageResource(R.drawable.ic_arrow_collapse)
            } else {
                constraintLayoutOrgDetails.visibility = View.GONE
                imageViewCollapse.tag = "close"
                imageViewCollapse.setImageResource(R.drawable.ic_arrow_expand)
            }

            constraintLayoutHeaderOrg.setOnClickListener {
                if (hasOrganization) {
                    if (imageViewCollapse.tag == "close") {
                        constraintLayoutOrgDetails.visibility = View.VISIBLE
                        imageViewCollapse.tag = "open"
                        imageViewCollapse.setImageResource(R.drawable.ic_arrow_collapse)
                    } else {
                        constraintLayoutOrgDetails.visibility = View.GONE
                        imageViewCollapse.tag = "close"
                        imageViewCollapse.setImageResource(R.drawable.ic_arrow_expand)
                    }
                }
            }
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: HeaderOrganizationBinding

        override fun bindView(itemView: View) {
            binding = HeaderOrganizationBinding.bind(itemView)
        }
    }
}

@EpoxyModelClass(layout = R.layout.item_organization)
abstract class OrganizationItemModel : EpoxyModelWithHolder<OrganizationItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var roleId: String

    @EpoxyAttribute
    lateinit var accountInitial: String

    @EpoxyAttribute
    lateinit var accountName: String

    @EpoxyAttribute
    var haveBadge: Boolean = false

    @EpoxyAttribute
    var colored: Boolean = false

    @EpoxyAttribute
    var badgeCount: Int = 0

    @EpoxyAttribute
    var position: Int = 0

    @EpoxyAttribute
    var size: Int = 0

    @EpoxyAttribute
    lateinit var callbacks: OrganizationController.AdapterCallbacks

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.apply {
            viewBadge.textViewInitial.text = accountInitial
            textViewOrgCorporateName.text = accountName
            when (position) {
                0 -> {
                    viewOrgBorder1.visibility = View.VISIBLE
                    viewOrgBorder2.visibility = if (size == 1) View.VISIBLE else View.GONE
                }
                (size.minus(1)) -> {
                    viewOrgBorder1.visibility = View.VISIBLE
                    viewOrgBorder2.visibility = View.VISIBLE
                }
                else -> {
                    viewOrgBorder1.visibility = View.VISIBLE
                    viewOrgBorder2.visibility = View.GONE
                }
            }

            if (haveBadge) {
                viewBadgeCount.textViewBadgeCount.setBackgroundResource(
                    if (colored)
                        R.drawable.circle_red_badge
                    else
                        R.drawable.circle_gray_badge
                )
                viewBadgeCount.textViewBadgeCount.text = badgeCount.toString()
                viewBadgeCount.root.visibility = View.VISIBLE
            } else {
                viewBadgeCount.root.visibility = View.GONE
            }

            constraintLayoutOrg.setOnClickListener {
                callbacks.onClickItem(roleId)
            }
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: ItemOrganizationBinding

        override fun bindView(itemView: View) {
            binding = ItemOrganizationBinding.bind(itemView)
        }
    }
}
