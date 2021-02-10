package com.unionbankph.corporate.approval.presentation.approval_detail

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.SystemClock
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.otaliastudios.zoom.ZoomLayout
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.setVisible
import com.unionbankph.corporate.app.common.widget.canvas.DrawLineCanvas
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.approval.data.model.*
import com.unionbankph.corporate.auth.data.model.UserDetails
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper

/**
 * Created by herald on 1/6/21
 */
class ApprovalHierarchyManager constructor(
    private val activity: AppCompatActivity,
    private val viewUtil: ViewUtil,
    private val userDetails: UserDetails
) {

    private var constraintLayout: ConstraintLayout
    private var constraintLayoutContent: ConstraintLayout
    private var cardViewApprovalDetail: CardView
    private var imageViewStatus: ImageView
    private var textViewGroupName: TextView
    private var textViewRequiredApprovalsValue: TextView
    private var textViewTitle: TextView
    private var linearLayoutOrgUser: LinearLayout
    private var nestedScrollView: NestedScrollView
    private var viewGroupStatus: View
    private var viewApprovalStatus: View
    private var border: View
    private var zoomLayout: ZoomLayout

    private var totalHierarchyWidth: Int = 0

    private var totalHierarchyHeight: Int = 0

    private var defaultHierarchyMarginVertical: Int = 0

    private var defaultHierarchyMarginHorizontal: Int = 0

    private var mLastClickTime: Long = 0

    private var approvalProcessOrganizationUsers: MutableList<ApprovalProcessOrganizationUser>? =
        null

    private var approvalProcessGroups: MutableList<ApprovalProcessGroup>? = null

    private var approvalProcessNodes: MutableList<ApprovalProcessNode>? = null

    private val nodePaths: MutableList<NodePath> = mutableListOf()

    lateinit var rootView: CardView

    var tutorialApprovalHierarchyDto: ApprovalHierarchyDto? = null

    var approvalHierarchyName: String? = null

    init {
        defaultHierarchyMarginHorizontal =
            activity.resources.getDimension(
                R.dimen.constraint_approval_hierarchy_margin_horizontal
            ).toInt()
        defaultHierarchyMarginVertical =
            activity.resources.getDimension(
                R.dimen.constraint_approval_hierarchy_margin_vertical
            ).toInt()

        constraintLayout = activity.findViewById(R.id.constraintLayout)
        constraintLayoutContent = activity.findViewById(R.id.constraintLayoutContent)
        cardViewApprovalDetail = activity.findViewById(R.id.cardViewApprovalDetail)
        imageViewStatus = activity.findViewById(R.id.imageViewStatus)
        textViewGroupName = activity.findViewById(R.id.textViewGroupName)
        textViewRequiredApprovalsValue = activity.findViewById(R.id.textViewRequiredApprovalsValue)
        textViewTitle = activity.findViewById(R.id.textViewTitle)
        linearLayoutOrgUser = activity.findViewById(R.id.linearLayoutOrgUser)
        nestedScrollView = activity.findViewById(R.id.nestedScrollView)
        viewGroupStatus = activity.findViewById(R.id.viewGroupStatus)
        viewApprovalStatus = activity.findViewById(R.id.viewApprovalStatus)
        border = activity.findViewById(R.id.border)
        zoomLayout = activity.findViewById(R.id.zoomLayout)
    }

    private fun isAndOperator(operator: Operator?) =
        operator?.threshold == operator?.children?.size

    fun startDrawApprovalHierarchy(approvalHierarchyDto: ApprovalHierarchyDto) {
        resetApprovalHierarchy()
        zoomLayout.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN ->
                    v.parent.requestDisallowInterceptTouchEvent(true)

                MotionEvent.ACTION_UP ->
                    v.parent.requestDisallowInterceptTouchEvent(false)
            }
            v.onTouchEvent(event)
        }
        approvalHierarchyName = approvalHierarchyDto.approvalHierarchy?.name.notNullable()
        textViewTitle.text =
            (activity.getString(R.string.title_approval_hierarchy)
                    + " - "
                    + approvalHierarchyName)
        approvalProcessOrganizationUsers = approvalHierarchyDto.approvalProcessOrganizationUsers
        approvalProcessNodes = approvalHierarchyDto.approvalProcessNodes
        approvalProcessGroups = approvalHierarchyDto.approvalProcessGroups
        if ((approvalHierarchyDto.approvalHierarchy?.root?.group != null &&
                    approvalHierarchyDto.approvalHierarchy?.root?.nextStep == null) &&
            approvalHierarchyDto.approvalHierarchy?.root?.operator == null
        ) {
            drawSingleHierarchy(
                approvalHierarchyDto.approvalHierarchy?.root?.numberOfApprovers,
                approvalHierarchyDto.approvalHierarchy?.root?.group,
                approvalHierarchyDto.approvalHierarchy?.root?.nodeStatus?.type
            )
        } else {
            drawHierarchy(approvalHierarchyDto)
        }
        viewApprovalStatus.visibility = View.VISIBLE
    }

    private fun drawHierarchy(approvalHierarchyDto: ApprovalHierarchyDto) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayoutContent)
        constraintSet.connect(
            viewApprovalStatus.id,
            ConstraintSet.TOP,
            zoomLayout.id,
            ConstraintSet.BOTTOM,
            activity.resources.getDimension(R.dimen.content_spacing).toInt()
        )
        constraintSet.applyTo(constraintLayoutContent)
        zoomLayout.setVisible(true)
        cardViewApprovalDetail.visibility = View.GONE
        val rootNode = Node(
            approvalHierarchyDto.approvalHierarchy?.root,
            approvalHierarchyDto.approvalHierarchy?.root?.id!!
        )
        drawNode(rootNode)
        drawPathSourceDestination()
        constraintLayout.layoutParams.width = totalHierarchyWidth +
                defaultHierarchyMarginHorizontal
        constraintLayout.layoutParams.height = totalHierarchyHeight +
                defaultHierarchyMarginVertical

        val defaultMaxHeight =
            activity.resources.getDimension(R.dimen.constraint_approval_hierarchy_max_size)
        when {
            tutorialApprovalHierarchyDto != null -> {
                zoomLayout.layoutParams.height =
                    defaultMaxHeight.toInt() + defaultHierarchyMarginVertical
                zoomLayout.setTransformation(
                    ZoomLayout.TRANSFORMATION_CENTER_INSIDE,
                    Gravity.CENTER
                )
            }
            totalHierarchyHeight > defaultMaxHeight -> {
                zoomLayout.layoutParams.height =
                    defaultMaxHeight.toInt() + defaultHierarchyMarginVertical
                zoomLayout.setTransformation(
                    ZoomLayout.TRANSFORMATION_CENTER_CROP,
                    Gravity.START or Gravity.CENTER
                )
            }
            else -> {
                zoomLayout.layoutParams.height = totalHierarchyHeight +
                        defaultHierarchyMarginVertical
                zoomLayout.setTransformation(
                    ZoomLayout.TRANSFORMATION_CENTER_INSIDE,
                    Gravity.CENTER
                )
            }
        }
    }

    fun resetApprovalHierarchy() {
        constraintLayout.removeAllViews()
        linearLayoutOrgUser.removeAllViews()
        zoomLayout.engine.clear()
        zoomLayout.setMinZoom(0.9f, ZoomLayout.TYPE_REAL_ZOOM)
        totalHierarchyHeight = 0
        totalHierarchyWidth = 0
        nodePaths.clear()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun drawNode(
        node: Node,
        edge: Edge? = null,
        prevTotalChildren: Int = 0
    ) {
        val constraintSet = ConstraintSet()
        val view = activity.layoutInflater.inflate(R.layout.item_node, null)
        val textViewNode = view.findViewById<TextView>(R.id.textViewNode)
        val cardViewNode = view.findViewById<CardView>(R.id.cardViewNode)
        val linearLayoutNode = view.findViewById<LinearLayout>(R.id.linearLayoutNode)
        val borderNode = view.findViewById<View>(R.id.borderNode)
        view.id = node.id
        constraintLayout.addView(view)
        var params: ViewGroup.LayoutParams? = view.layoutParams
        if (params == null) params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val widthSpec = viewUtil.makeMeasureSpec(params.width)
        val heightSpec = viewUtil.makeMeasureSpec(params.height)
        view.measure(widthSpec, heightSpec)
        node.setSize(view.measuredWidth, view.measuredHeight)
        val nodeSource = Node(node, node.id)
        nodeSource.setSize(view.measuredWidth, view.measuredHeight)
        edge?.destination?.setSize(view.measuredWidth, view.measuredHeight)

        cardViewNode.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return@setOnClickListener
            mLastClickTime = SystemClock.elapsedRealtime()
            if (node.data is Root && edge == null) {
                val root = node.data
                if (root.type == ApprovalDetailActivity.TYPE_GROUP) {
                    onItemClickNode(
                        root.numberOfApprovers,
                        root.group,
                        root.group?.organizationUsers,
                        approvalProcessOrganizationUsers,
                        root.nodeStatus?.type
                    )
                }
            } else if (node.data is NextStep && edge != null) {
                val nextStep = node.data
                if (nextStep.type == ApprovalDetailActivity.TYPE_GROUP) {
                    onItemClickNode(
                        nextStep.numberOfApprovers,
                        nextStep.group,
                        nextStep.group?.organizationUsers,
                        approvalProcessOrganizationUsers,
                        nextStep.nodeStatus?.type
                    )
                }
            } else if (node.data is Children && edge != null) {
                val children = node.data
                if (children.type == ApprovalDetailActivity.TYPE_GROUP) {
                    onItemClickNode(
                        children.numberOfApprovers,
                        children.group,
                        children.group?.organizationUsers,
                        approvalProcessOrganizationUsers,
                        children.nodeStatus?.type
                    )
                }
            }
        }

        cardViewNode.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN ->
                    nestedScrollView.requestDisallowInterceptTouchEvent(true)

                MotionEvent.ACTION_UP ->
                    nestedScrollView.requestDisallowInterceptTouchEvent(false)
            }
            v.onTouchEvent(event)
        }

        if (node.data is Root && edge == null) {
            val root = node.data
            rootView = cardViewNode
            cardViewNode.setCardBackgroundColor(
                ContextCompat.getColor(
                    activity,
                    ConstantHelper.Color.getStatusColor(root.nodeStatus?.type)
                )
            )
            if (root.type == ApprovalDetailActivity.TYPE_GROUP) {
                val group = root.group
                textViewNode.text = group?.groupName
                if (setCurrentGroup(
                        group?.organizationUsers ?: mutableListOf(),
                        userDetails.corporateUser?.id
                    )
                ) {
                    borderNode.background =
                        ContextCompat.getDrawable(activity, R.drawable.bg_cardview_border_red)
                }
                initConstraintConnectionRoot(constraintSet, node)
                totalHierarchyWidth += activity.resources.getDimension(
                    R.dimen.card_view_node_group
                ).toInt() + defaultHierarchyMarginHorizontal
                totalHierarchyHeight += activity.resources.getDimension(R.dimen.card_view_node_group)
                    .toInt() +
                        activity.resources.getDimension(R.dimen.content_group_spacing).toInt()
                if (root.nextStep != null) {
                    val nodeDestination = Node(
                        root.nextStep,
                        root.nextStep?.id!!
                    )
                    val edgeNode = Edge(
                        nodeSource,
                        nodeDestination
                    )
                    nodePaths.add(
                        NodePath(
                            node.id,
                            root.nextStep?.id,
                            ConstantHelper.Color.getStatusColor(root.nodeStatus?.type)
                        )
                    )
                    drawNode(nodeDestination, edgeNode)
                }
            } else if (root.type == ApprovalDetailActivity.TYPE_OPERATOR) {
                cardViewNode.layoutParams.height =
                    activity.resources.getDimension(R.dimen.card_view_node_operator).toInt()
                cardViewNode.layoutParams.width =
                    activity.resources.getDimension(R.dimen.card_view_node_operator).toInt()
                linearLayoutNode.background = null
                val operator = root.operator
                textViewNode.text =
                    if (isAndOperator(operator))
                        activity.getString(R.string.title_and)
                    else activity.getString(R.string.title_or)
                textViewNode.isAllCaps = true
                textViewNode.setTypeface(textViewNode.typeface, Typeface.NORMAL)
                val childrens = operator?.children
                initConstraintConnectionRoot(constraintSet, node)
                totalHierarchyWidth += activity.resources.getDimension(
                    R.dimen.card_view_node_operator
                ).toInt() + defaultHierarchyMarginHorizontal
                var topNodeBasement: Node? = null
                var bottomNodeBasement: Node? = null
                var startNodeBasement: Node? = null
                var endNodeBasement: Node? = null
                childrens?.forEachIndexed { index, child ->
                    val nodeDestination =
                        Node(
                            child,
                            child.id!!
                        )
                    if (index == 0) {
                        totalHierarchyWidth += activity.resources.getDimension(
                            R.dimen.card_view_node_group
                        ).toInt() + defaultHierarchyMarginHorizontal
                        startNodeBasement =
                            Node(
                                childrens[index],
                                childrens[index].id!!
                            )
                    }
                    if (index > 0) {
                        topNodeBasement =
                            Node(
                                childrens[index - 1],
                                childrens[index - 1].id!!
                            )
                    }
                    if (childrens.size in (index + 1)..(index - 1)) {
                        bottomNodeBasement =
                            Node(
                                childrens[index + 1],
                                childrens[index + 1].id!!
                            )
                    }
                    if (index + 1 == childrens.size) {
                        endNodeBasement =
                            Node(
                                childrens[index],
                                childrens[index].id!!
                            )
                    }

                    val edgeNode =
                        Edge(
                            nodeSource,
                            nodeDestination,
                            topNodeBasement,
                            bottomNodeBasement
                        )
                    nodePaths.add(
                        NodePath(
                            nodeSource.id,
                            child.id,
                            ConstantHelper.Color.getStatusColor(root.nodeStatus?.type)
                        )
                    )
                    if (root.nextStep != null) {
                        nodePaths.add(
                            NodePath(
                                child.id,
                                root.nextStep?.id,
                                ConstantHelper.Color.getStatusColor(root.nextStep?.nodeStatus?.type)
                            )
                        )
                    }
                    if (index > 0) {
                        drawNode(
                            nodeDestination,
                            edgeNode,
                            getTotalChild(0, childrens[index.minus(1)])
                        )
                    } else {
                        drawNode(nodeDestination, edgeNode)
                    }
                }
                if (root.nextStep != null) {
                    val nodeDestination = Node(
                        root.nextStep,
                        root.nextStep?.id!!
                    )
                    val edgeNode =
                        Edge(
                            nodeSource,
                            nodeDestination,
                            startNodeBasement,
                            endNodeBasement
                        )
                    drawNode(nodeDestination, edgeNode)
                }

            }
        } else if (node.data is NextStep && edge != null) {
            val nextStep = node.data
            cardViewNode.setCardBackgroundColor(
                ContextCompat.getColor(
                    activity,
                    ConstantHelper.Color.getStatusColor(nextStep.nodeStatus?.type)
                )
            )
            if (nextStep.type == ApprovalDetailActivity.TYPE_GROUP) {
                val group = nextStep.group
                val sourceNode = edge.source!!
                textViewNode.text = group?.groupName
                initConstraintConnectionMargin(
                    nextStep,
                    constraintSet,
                    edge,
                    node,
                    sourceNode,
                    prevTotalChildren
                )
                totalHierarchyWidth += activity.resources.getDimension(
                    R.dimen.card_view_node_group
                ).toInt() + defaultHierarchyMarginHorizontal
                if (nextStep.nextStep != null) {
                    val nodeDestination = Node(
                        nextStep.nextStep,
                        nextStep.nextStep?.id!!
                    )
                    val edgeNode = Edge(
                        nodeSource,
                        nodeDestination
                    )
                    nodePaths.add(
                        NodePath(
                            node.id,
                            nextStep.nextStep?.id,
                            ConstantHelper.Color.getStatusColor(nextStep.nodeStatus?.type)
                        )
                    )
                    drawNode(nodeDestination, edgeNode)
                }
                if (setCurrentGroup(
                        group?.organizationUsers ?: mutableListOf(),
                        userDetails.corporateUser?.id
                    )
                ) {
                    borderNode.background =
                        ContextCompat.getDrawable(activity, R.drawable.bg_cardview_border_red)
                }
            } else if (nextStep.type == ApprovalDetailActivity.TYPE_OPERATOR) {
                cardViewNode.layoutParams.height =
                    activity.resources.getDimension(R.dimen.card_view_node_operator).toInt()
                cardViewNode.layoutParams.width =
                    activity.resources.getDimension(R.dimen.card_view_node_operator).toInt()
                linearLayoutNode.background = null
                val operator = nextStep.operator
                val sourceNode = edge.source!!
                textViewNode.text =
                    if (isAndOperator(operator))
                        activity.getString(R.string.title_and)
                    else activity.getString(R.string.title_or)
                textViewNode.isAllCaps = true
                textViewNode.setTypeface(textViewNode.typeface, Typeface.NORMAL)
                val childrens = operator?.children
                initConstraintConnectionMargin(
                    nextStep,
                    constraintSet,
                    edge,
                    node,
                    sourceNode,
                    prevTotalChildren
                )
                totalHierarchyWidth += activity.resources.getDimension(
                    R.dimen.card_view_node_group
                ).toInt() + defaultHierarchyMarginHorizontal
                var topNodeBasement: Node? = null
                var bottomNodeBasement: Node? = null
                var startNodeBasement: Node? = null
                var endNodeBasement: Node? = null
                childrens?.forEachIndexed { index, child ->
                    val nodeDestination =
                        Node(
                            child,
                            child.id!!
                        )
                    if (index == 0) {
                        totalHierarchyWidth += activity.resources.getDimension(
                            R.dimen.card_view_node_group
                        ).toInt() + defaultHierarchyMarginHorizontal
                        startNodeBasement =
                            Node(
                                childrens[index],
                                childrens[index].id!!
                            )
                    }
                    if (index > 0) {
                        topNodeBasement =
                            Node(
                                childrens[index - 1],
                                childrens[index - 1].id!!
                            )
                    }
                    if (childrens.size in (index + 1)..(index - 1)) {
                        bottomNodeBasement =
                            Node(
                                childrens[index + 1],
                                childrens[index + 1].id!!
                            )
                    }
                    if (index + 1 == childrens.size) {
                        endNodeBasement =
                            Node(
                                childrens[index],
                                childrens[index].id!!
                            )
                    }

                    val edgeNode =
                        Edge(
                            nodeSource,
                            nodeDestination,
                            topNodeBasement,
                            bottomNodeBasement
                        )
                    nodePaths.add(
                        NodePath(
                            nodeSource.id,
                            child.id,
                            ConstantHelper.Color.getStatusColor(nextStep.nodeStatus?.type)
                        )
                    )
                    if (nextStep.nextStep != null) {
                        nodePaths.add(
                            NodePath(
                                child.id,
                                nextStep.nextStep?.id,
                                ConstantHelper.Color.getStatusColor(nextStep.nextStep?.nodeStatus?.type)
                            )
                        )
                    }
                    if (index > 0) {
                        drawNode(
                            nodeDestination,
                            edgeNode,
                            getTotalChild(0, childrens[index.minus(1)])
                        )
                    } else {
                        drawNode(nodeDestination, edgeNode)
                    }
                }

                if (nextStep.nextStep != null) {
                    val nodeDestination = Node(
                        nextStep.nextStep,
                        nextStep.nextStep?.id!!
                    )
                    val edgeNode =
                        Edge(
                            nodeSource,
                            nodeDestination,
                            startNodeBasement,
                            endNodeBasement
                        )
                    drawNode(nodeDestination, edgeNode)
                }
            }

        } else if (node.data is Children && edge != null) {
            val children = node.data
            cardViewNode.setCardBackgroundColor(
                ContextCompat.getColor(
                    activity,
                    ConstantHelper.Color.getStatusColor(
                        children.nodeStatus?.type
                    )
                )
            )
            totalHierarchyHeight += activity.resources.getDimension(
                R.dimen.card_view_node_group
            ).toInt() + defaultHierarchyMarginVertical
            if (children.type == ApprovalDetailActivity.TYPE_GROUP) {
                val group = children.group
                val sourceNode = edge.source!!
                textViewNode.text = group?.groupName
                initConstraintConnectionMargin(
                    children,
                    constraintSet,
                    edge,
                    node,
                    sourceNode,
                    prevTotalChildren
                )
                if (children.nextStep != null) {
                    val nodeDestination = Node(
                        children.nextStep,
                        children.nextStep?.id!!
                    )
                    val edgeNode = Edge(
                        nodeSource,
                        nodeDestination
                    )
                    nodePaths.add(
                        NodePath(
                            node.id,
                            children.nextStep?.id,
                            ConstantHelper.Color.getStatusColor(children.nodeStatus?.type)
                        )
                    )
                    drawNode(nodeDestination, edgeNode)
                }

                if (setCurrentGroup(
                        group?.organizationUsers ?: mutableListOf(),
                        userDetails.corporateUser?.id
                    ) || group?.id == 129523
                ) {
                    borderNode.background =
                        ContextCompat.getDrawable(activity, R.drawable.bg_cardview_border_red)
                }
            } else if (children.type == ApprovalDetailActivity.TYPE_OPERATOR) {
                cardViewNode.layoutParams.height =
                    activity.resources.getDimension(R.dimen.card_view_node_operator).toInt()
                cardViewNode.layoutParams.width =
                    activity.resources.getDimension(R.dimen.card_view_node_operator).toInt()
                linearLayoutNode.background = null
                val operator = children.operator
                val sourceNode = edge.source!!
                textViewNode.text =
                    if (isAndOperator(operator))
                        activity.getString(R.string.title_and)
                    else
                        activity.getString(R.string.title_or)
                textViewNode.isAllCaps = true
                textViewNode.setTypeface(textViewNode.typeface, Typeface.NORMAL)
                val childrens = operator?.children!!
                initConstraintConnectionMargin(
                    children,
                    constraintSet,
                    edge,
                    node,
                    sourceNode,
                    prevTotalChildren
                )
                var topNodeBasement: Node? = null
                var bottomNodeBasement: Node? = null
                var startNodeBasement: Node? = null
                var endNodeBasement: Node? = null
                childrens.forEachIndexed { index, child ->
                    val nodeDestination =
                        Node(
                            child,
                            child.id!!
                        )
                    if (index == 0) {
                        totalHierarchyWidth += activity.resources.getDimension(
                            R.dimen.card_view_node_group
                        ).toInt()
                        startNodeBasement =
                            Node(
                                childrens[index],
                                childrens[index].id!!
                            )
                    }
                    if (index > 0) {
                        topNodeBasement =
                            Node(
                                childrens[index - 1],
                                childrens[index - 1].id!!
                            )
                    }
                    if (childrens.size in (index + 1)..(index - 1)) {
                        bottomNodeBasement =
                            Node(
                                childrens[index + 1],
                                childrens[index + 1].id!!
                            )
                    }
                    if (index + 1 == childrens.size) {
                        endNodeBasement =
                            Node(
                                childrens[index],
                                childrens[index].id!!
                            )
                    }

                    val edgeNode =
                        Edge(
                            nodeSource,
                            nodeDestination,
                            topNodeBasement,
                            bottomNodeBasement
                        )
                    nodePaths.add(
                        NodePath(
                            nodeSource.id,
                            child.id,
                            ConstantHelper.Color.getStatusColor(children.nodeStatus?.type)
                        )
                    )
                    if (children.nextStep != null) {
                        nodePaths.add(
                            NodePath(
                                child.id,
                                children.nextStep?.id,
                                ConstantHelper.Color.getStatusColor(children.nextStep?.nodeStatus?.type)
                            )
                        )
                    }
                    if (index > 0) {
                        drawNode(
                            nodeDestination,
                            edgeNode,
                            getTotalChild(0, childrens[index.minus(1)])
                        )
                    } else {
                        drawNode(nodeDestination, edgeNode)
                    }
                }

                if (children.nextStep != null) {
                    val nodeDestination = Node(
                        children.nextStep,
                        children.nextStep?.id!!
                    )
                    val edgeNode =
                        Edge(
                            nodeSource,
                            nodeDestination,
                            startNodeBasement,
                            endNodeBasement
                        )
                    drawNode(nodeDestination, edgeNode)
                }
            }
        }
    }

    private fun drawSingleHierarchy(
        numberOfApprovers: Int?,
        group: Group?,
        nodeStatus: String?
    ) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayoutContent)
        constraintSet.connect(
            viewApprovalStatus.id,
            ConstraintSet.TOP,
            cardViewApprovalDetail.id,
            ConstraintSet.BOTTOM,
            activity.resources.getDimension(R.dimen.content_spacing).toInt()
        )
        constraintSet.applyTo(constraintLayoutContent)
        zoomLayout.setVisible(false)
        imageViewStatus.visibility = View.VISIBLE
        cardViewApprovalDetail.visibility = View.VISIBLE
        textViewGroupName.text = group?.groupName
        textViewRequiredApprovalsValue.text = numberOfApprovers?.toString()
        imageViewStatus.setImageResource(ConstantHelper.Drawable.getGroupIconStatus(nodeStatus))
        viewGroupStatus.background =
            ContextCompat.getDrawable(
                activity,
                ConstantHelper.Drawable.getBackgroundStatusColor(nodeStatus)
            )
        if (setCurrentGroup(
                group?.organizationUsers ?: mutableListOf(),
                userDetails.corporateUser?.id
            )
        ) {
            border.background =
                ContextCompat.getDrawable(
                    activity,
                    ConstantHelper.Drawable.getBackgroundStatusColor(
                        nodeStatus
                    )
                )
        }
        val orgUsers = getCorporateUserByGroup(
            userDetails.corporateUser?.id,
            group?.organizationUsers,
            approvalProcessOrganizationUsers
        )

        linearLayoutOrgUser.removeAllViews()

        val sortedOrgUsers = orgUsers.asSequence().sortedWith(compareBy(
            { it.status == Constant.STATUS_UNTOUCHED },
            { it.status == Constant.STATUS_NOTIFIED },
            { it.status == Constant.STATUS_FOR_APPROVAL },
            { it.status == Constant.STATUS_APPROVED },
            { it.status == Constant.STATUS_REJECTED }
        ))
        if (orgUsers.size > 0) {
            sortedOrgUsers.forEach {
                val corporateUser = it.corporateUser
                val corpUserView =
                    activity.layoutInflater.inflate(R.layout.item_organization_user, null)
                val textViewOrganizationName =
                    corpUserView?.findViewById<TextView>(R.id.textViewOrganizationName)
                val imageViewOrganizationStatus =
                    corpUserView?.findViewById<ImageView>(R.id.imageViewOrganizationStatus)
                val imageViewCurrentOrganization =
                    corpUserView?.findViewById<ImageView>(R.id.imageViewCurrentOrganization)
                imageViewCurrentOrganization?.visibility =
                    if (it.isCurrent) View.VISIBLE else View.INVISIBLE
                textViewOrganizationName?.text =
                    ("${corporateUser?.firstName} ${corporateUser?.lastName}")
                textViewOrganizationName?.setTextColor(
                    ContextCompat.getColor(
                        activity,
                        ConstantHelper.Color.getStatusColor(it.status)
                    )
                )
                imageViewOrganizationStatus?.setImageResource(
                    ConstantHelper.Drawable.getCorporateIconStatus(it.status)
                )
                linearLayoutOrgUser.addView(corpUserView)
            }
        } else {
            val corpUserView =
                activity.layoutInflater.inflate(R.layout.item_organization_user, null)
            val textViewOrganizationName =
                corpUserView?.findViewById<TextView>(R.id.textViewOrganizationName)
            val imageViewOrganizationStatus =
                corpUserView?.findViewById<ImageView>(R.id.imageViewOrganizationStatus)
            val imageViewCurrentOrganization =
                corpUserView?.findViewById<ImageView>(R.id.imageViewCurrentOrganization)
            imageViewOrganizationStatus?.visibility = View.INVISIBLE
            imageViewCurrentOrganization?.visibility = View.INVISIBLE
            textViewOrganizationName?.text = activity.getString(R.string.msg_no_members_group)
            linearLayoutOrgUser.addView(corpUserView)
        }
    }

    private fun initConstraintConnectionRoot(
        constraintSet: ConstraintSet,
        node: Node
    ) {
        constraintSet.clone(constraintLayout)
        constraintSet.connect(
            node.id,
            ConstraintSet.LEFT,
            constraintLayout.id,
            ConstraintSet.LEFT,
            defaultHierarchyMarginHorizontal
        )
        constraintSet.connect(
            node.id,
            ConstraintSet.TOP,
            constraintLayout.id,
            ConstraintSet.TOP
        )
        constraintSet.connect(
            node.id,
            ConstraintSet.BOTTOM,
            constraintLayout.id,
            ConstraintSet.BOTTOM
        )
        constraintSet.applyTo(constraintLayout)
    }

    private fun initConstraintConnectionMargin(
        data: Any,
        constraintSet: ConstraintSet,
        edge: Edge,
        node: Node,
        sourceNode: Node,
        prevTotalChildren: Int
    ) {
        val cardMargin =
            activity.resources.getDimension(R.dimen.card_view_node_operator_margin).toInt() / 2
        val totalChildrenCount = getTotalChild(0, data)
        val nodeHeight = activity.resources.getDimension(
            R.dimen.card_view_node_group
        ).toInt()
        constraintSet.clone(constraintLayout)
        if (data is Root) {
            if (data.type == ApprovalDetailActivity.TYPE_GROUP) {
                if (edge.topBasement == null && edge.bottomBasement == null) {
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.LEFT,
                        sourceNode.id,
                        ConstraintSet.RIGHT,
                        defaultHierarchyMarginHorizontal
                    )
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.TOP,
                        sourceNode.id,
                        ConstraintSet.TOP
                    )
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.BOTTOM,
                        sourceNode.id,
                        ConstraintSet.BOTTOM
                    )
                } else {
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.LEFT,
                        edge.topBasement?.id!!,
                        ConstraintSet.RIGHT,
                        defaultHierarchyMarginHorizontal
                    )
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.TOP,
                        edge.topBasement?.id!!,
                        ConstraintSet.TOP
                    )
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.BOTTOM,
                        edge.bottomBasement?.id!!,
                        ConstraintSet.BOTTOM
                    )
                }
            } else {
                if (edge.topBasement == null) {
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.LEFT,
                        sourceNode.id,
                        ConstraintSet.RIGHT,
                        defaultHierarchyMarginHorizontal + cardMargin
                    )
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.BOTTOM,
                        sourceNode.id,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.TOP,
                        sourceNode.id,
                        ConstraintSet.TOP,
                        0
                    )
                } else if (edge.topBasement != null) {
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.LEFT,
                        sourceNode.id,
                        ConstraintSet.RIGHT,
                        defaultHierarchyMarginHorizontal + cardMargin + cardMargin
                    )
                    if (prevTotalChildren != 0) {
                        val marginPrevChild: Int =
                            (((nodeHeight * prevTotalChildren) + ((prevTotalChildren - 1)
                                    * defaultHierarchyMarginVertical)) / 2) - (nodeHeight / 2)
                        constraintSet.connect(
                            edge.topBasement?.id!!,
                            ConstraintSet.BOTTOM,
                            node.id,
                            ConstraintSet.TOP,
                            marginPrevChild + cardMargin
                        )
                    }
                    if (totalChildrenCount != 0) {
                        val margin =
                            (((nodeHeight * totalChildrenCount) + ((totalChildrenCount - 1)
                                    * defaultHierarchyMarginVertical)) / 2) - (nodeHeight / 2)
                        constraintSet.connect(
                            node.id,
                            ConstraintSet.TOP,
                            edge.topBasement?.id!!,
                            ConstraintSet.BOTTOM,
                            margin + defaultHierarchyMarginVertical + cardMargin
                        )
                    } else {
                        constraintSet.connect(
                            node.id,
                            ConstraintSet.TOP,
                            edge.topBasement?.id!!,
                            ConstraintSet.BOTTOM,
                            defaultHierarchyMarginVertical + cardMargin
                        )
                    }
                    constraintSet.connect(
                        edge.topBasement?.id!!,
                        ConstraintSet.BOTTOM,
                        node.id,
                        ConstraintSet.TOP
                    )
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.BOTTOM,
                        sourceNode.id,
                        ConstraintSet.BOTTOM
                    )
                }
            }
        } else if (data is NextStep) {
            if (data.type == ApprovalDetailActivity.TYPE_GROUP) {
                if (edge.topBasement == null && edge.bottomBasement == null) {
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.LEFT,
                        sourceNode.id,
                        ConstraintSet.RIGHT,
                        defaultHierarchyMarginHorizontal
                    )
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.TOP,
                        sourceNode.id,
                        ConstraintSet.TOP
                    )
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.BOTTOM,
                        sourceNode.id,
                        ConstraintSet.BOTTOM
                    )
                } else {
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.LEFT,
                        edge.topBasement?.id!!,
                        ConstraintSet.RIGHT,
                        defaultHierarchyMarginHorizontal
                    )
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.TOP,
                        edge.topBasement?.id!!,
                        ConstraintSet.TOP
                    )
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.BOTTOM,
                        edge.bottomBasement?.id!!,
                        ConstraintSet.BOTTOM
                    )
                }
            } else {
                if (edge.topBasement == null) {
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.LEFT,
                        sourceNode.id,
                        ConstraintSet.RIGHT,
                        defaultHierarchyMarginHorizontal + cardMargin
                    )
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.BOTTOM,
                        sourceNode.id,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.TOP,
                        sourceNode.id,
                        ConstraintSet.TOP,
                        0
                    )
                } else if (edge.topBasement != null) {
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.LEFT,
                        sourceNode.id,
                        ConstraintSet.RIGHT,
                        defaultHierarchyMarginHorizontal + cardMargin + cardMargin
                    )
                    if (prevTotalChildren != 0) {
                        val marginPrevChild: Int =
                            (((nodeHeight * prevTotalChildren) + ((prevTotalChildren - 1)
                                    * defaultHierarchyMarginVertical)) / 2) - (nodeHeight / 2)
                        constraintSet.connect(
                            edge.topBasement?.id!!,
                            ConstraintSet.BOTTOM,
                            node.id,
                            ConstraintSet.TOP,
                            marginPrevChild + cardMargin
                        )
                    }
                    if (totalChildrenCount != 0) {
                        val margin =
                            (((nodeHeight * totalChildrenCount) + ((totalChildrenCount - 1)
                                    * defaultHierarchyMarginVertical)) / 2) - (nodeHeight / 2)
                        constraintSet.connect(
                            node.id,
                            ConstraintSet.TOP,
                            edge.topBasement?.id!!,
                            ConstraintSet.BOTTOM,
                            margin + defaultHierarchyMarginVertical + cardMargin
                        )
                    } else {
                        constraintSet.connect(
                            node.id,
                            ConstraintSet.TOP,
                            edge.topBasement?.id!!,
                            ConstraintSet.BOTTOM,
                            defaultHierarchyMarginVertical + cardMargin
                        )
                    }
                    constraintSet.connect(
                        edge.topBasement?.id!!,
                        ConstraintSet.BOTTOM,
                        node.id,
                        ConstraintSet.TOP
                    )
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.BOTTOM,
                        sourceNode.id,
                        ConstraintSet.BOTTOM
                    )
                }
            }
        } else if (data is Children) {
            if (edge.topBasement == null) {
                constraintSet.connect(
                    node.id,
                    ConstraintSet.LEFT,
                    sourceNode.id,
                    ConstraintSet.RIGHT,
                    if (data.type == ApprovalDetailActivity.TYPE_OPERATOR) {
                        defaultHierarchyMarginHorizontal + cardMargin + cardMargin
                    } else {
                        defaultHierarchyMarginHorizontal + cardMargin
                    }
                )
                constraintSet.connect(
                    node.id,
                    ConstraintSet.BOTTOM,
                    sourceNode.id,
                    ConstraintSet.BOTTOM
                )
                constraintSet.connect(node.id, ConstraintSet.TOP, sourceNode.id, ConstraintSet.TOP)
            } else if (edge.topBasement != null) {
                if (data.type == ApprovalDetailActivity.TYPE_GROUP) {
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.LEFT,
                        sourceNode.id,
                        ConstraintSet.RIGHT,
                        defaultHierarchyMarginHorizontal + cardMargin
                    )
                } else if (data.type == ApprovalDetailActivity.TYPE_OPERATOR) {
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.LEFT,
                        sourceNode.id,
                        ConstraintSet.RIGHT,
                        defaultHierarchyMarginHorizontal + cardMargin + cardMargin
                    )
                }
                if (prevTotalChildren != 0) {
                    val marginPrevChild: Int =
                        (((nodeHeight * prevTotalChildren) + ((prevTotalChildren - 1)
                                * defaultHierarchyMarginVertical)) / 2) - (nodeHeight / 2)
                    if (data.type == ApprovalDetailActivity.TYPE_GROUP) {
                        constraintSet.connect(
                            edge.topBasement?.id!!,
                            ConstraintSet.BOTTOM,
                            node.id,
                            ConstraintSet.TOP,
                            marginPrevChild + cardMargin
                        )
                    } else if (data.type == ApprovalDetailActivity.TYPE_OPERATOR) {
                        constraintSet.connect(
                            edge.topBasement?.id!!,
                            ConstraintSet.BOTTOM,
                            node.id,
                            ConstraintSet.TOP,
                            marginPrevChild + defaultHierarchyMarginVertical + cardMargin
                        )
                    }
                }
                if (totalChildrenCount != 0) {
                    val margin =
                        (((nodeHeight * totalChildrenCount) + ((totalChildrenCount - 1)
                                * defaultHierarchyMarginVertical)) / 2) - (nodeHeight / 2)
                    if (data.type == ApprovalDetailActivity.TYPE_GROUP) {
                        constraintSet.connect(
                            node.id,
                            ConstraintSet.TOP,
                            edge.topBasement?.id!!,
                            ConstraintSet.BOTTOM,
                            margin + defaultHierarchyMarginVertical
                        )
                    } else if (data.type == ApprovalDetailActivity.TYPE_OPERATOR) {
                        constraintSet.connect(
                            node.id,
                            ConstraintSet.TOP,
                            edge.topBasement?.id!!,
                            ConstraintSet.BOTTOM,
                            margin + defaultHierarchyMarginVertical + cardMargin
                        )
                    }
                } else {
                    constraintSet.connect(
                        node.id,
                        ConstraintSet.TOP,
                        edge.topBasement?.id!!,
                        ConstraintSet.BOTTOM,
                        defaultHierarchyMarginVertical
                    )
                }
                constraintSet.connect(
                    edge.topBasement?.id!!,
                    ConstraintSet.BOTTOM,
                    node.id,
                    ConstraintSet.TOP
                )
                constraintSet.connect(
                    node.id,
                    ConstraintSet.BOTTOM,
                    sourceNode.id,
                    ConstraintSet.BOTTOM
                )
            }
        }
        constraintSet.applyTo(constraintLayout)
    }

    private fun getTotalChild(count: Int, data: Any?): Int {
        if (data is NextStep) {
            if (data.type == ApprovalDetailActivity.TYPE_GROUP && data.nextStep != null) {
                return getTotalChild(count, data.nextStep) + count
            } else if (data.type == ApprovalDetailActivity.TYPE_OPERATOR) {
                data.operator?.children?.forEach {
                    if (it.type == ApprovalDetailActivity.TYPE_GROUP) {
                        return getTotalChild(
                            data.operator?.children?.size ?: 0,
                            it.nextStep
                        ) + count
                    } else if (it.type == ApprovalDetailActivity.TYPE_OPERATOR) {
                        return getTotalChild(data.operator?.children?.size ?: 0, it) + count
                    }
                }
            }
        } else if (data is Children) {
            if (data.type == ApprovalDetailActivity.TYPE_GROUP) {
                return getTotalChild(count, data.nextStep) + count
            } else if (data.type == ApprovalDetailActivity.TYPE_OPERATOR) {
                data.operator?.children?.forEach {
                    if (it.type == ApprovalDetailActivity.TYPE_GROUP) {
                        return getTotalChild(
                            data.operator?.children?.size ?: 0,
                            it.nextStep
                        ) + count
                    } else if (it.type == ApprovalDetailActivity.TYPE_OPERATOR) {
                        return getTotalChild(data.operator?.children?.size ?: 0, it) + count
                    }
                }
            }
        }
        return count
    }

    private fun drawPathSourceDestination() {
        nodePaths.forEach {
            val viewSource = activity.findViewById<View>(it.source!!)
            val viewDestination = activity.findViewById<View>(it.destination!!)
            viewSource.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        viewSource?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                        val viewSourceX = viewSource.x - 2
                        val viewSourceY = viewSource.y + viewSource.height

                        val viewDestinationX = viewDestination.x - 2
                        val viewDestinationY = viewDestination.y + viewDestination.height

                        val nodeSource = Node(
                            null,
                            it.source!!
                        )
                        val nodeDestination =
                            Node(
                                null,
                                it.destination!!
                            )

                        var paramsSource: ViewGroup.LayoutParams? = viewSource.layoutParams
                        if (paramsSource == null) paramsSource = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        val widthSpecSource = viewUtil.makeMeasureSpec(paramsSource.width)
                        val heightSpecSource = viewUtil.makeMeasureSpec(paramsSource.height)
                        viewSource.measure(widthSpecSource, heightSpecSource)

                        var paramsDestination: ViewGroup.LayoutParams? =
                            viewDestination.layoutParams
                        if (paramsDestination == null) paramsDestination = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        val widthSpecDestination = viewUtil.makeMeasureSpec(paramsDestination.width)
                        val heightSpecDestination =
                            viewUtil.makeMeasureSpec(paramsDestination.height)
                        viewDestination.measure(widthSpecDestination, heightSpecDestination)

                        nodeSource.setPos(
                            Vector(
                                viewSourceX,
                                viewSourceY
                            )
                        )
                        nodeDestination.setPos(
                            Vector(
                                viewDestinationX,
                                viewDestinationY
                            )
                        )
                        nodeSource.setSize(viewSource.measuredWidth, viewSource.measuredHeight)
                        nodeDestination.setSize(
                            viewDestination.measuredWidth,
                            viewDestination.measuredHeight
                        )
                        val edge = Edge(
                            nodeSource,
                            nodeDestination,
                            pathColor = it.color
                        )
                        val isOperatorSource =
                            viewSource.width == activity.resources.getDimension(
                                R.dimen.card_view_node_operator
                            ).toInt() && viewSource.height == activity.resources.getDimension(
                                R.dimen.card_view_node_operator
                            ).toInt()
                        val isOperatorDestination =
                            viewDestination.width == activity.resources.getDimension(
                                R.dimen.card_view_node_operator
                            ).toInt() && viewDestination.height == activity.resources.getDimension(
                                R.dimen.card_view_node_operator
                            ).toInt()
                        val drawLine = DrawLineCanvas(
                            activity,
                            edge,
                            defaultHierarchyMarginHorizontal,
                            isOperatorSource,
                            isOperatorDestination
                        )
                        constraintLayout.addView(drawLine)
                    }

                })
        }
    }

    private fun onItemClickNode(
        numberOfApprovers: Int?,
        group: Group?,
        organizationUsers: MutableList<OrganizationUser>?,
        approvalProcessOrganizationUser: MutableList<ApprovalProcessOrganizationUser>?,
        nodeStatus: String?
    ) {
        val dialogBuilder = AlertDialog.Builder(activity)
        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_approval_detail, null)
        dialogBuilder.setView(dialogView)
        val textViewGroupName = dialogView?.findViewById<TextView>(R.id.textViewGroupName)
        val textViewRequiredApprovalsValue =
            dialogView?.findViewById<TextView>(R.id.textViewRequiredApprovalsValue)
        val linearLayoutOrgUser = dialogView?.findViewById<LinearLayout>(R.id.linearLayoutOrgUser)
        val imageViewStatus = dialogView?.findViewById<ImageView>(R.id.imageViewStatus)
        val viewGroupStatus = dialogView?.findViewById<View>(R.id.viewGroupStatus)
        viewGroupStatus?.background =
            ContextCompat.getDrawable(
                activity,
                ConstantHelper.Drawable.getBackgroundStatusColor(nodeStatus)
            )
        imageViewStatus?.setImageResource(ConstantHelper.Drawable.getGroupIconStatus(nodeStatus))
        textViewGroupName?.text = group?.groupName
        textViewRequiredApprovalsValue?.text = numberOfApprovers?.toString()
        val orgUsers = getCorporateUserByGroup(
            userDetails.corporateUser?.id,
            organizationUsers,
            approvalProcessOrganizationUser
        )
        var orgHeight = 0
        val sortedOrgUsers = orgUsers.asSequence().sortedWith(compareBy(
            { it.status == Constant.STATUS_UNTOUCHED },
            { it.status == Constant.STATUS_NOTIFIED },
            { it.status == Constant.STATUS_FOR_APPROVAL },
            { it.status == Constant.STATUS_APPROVED },
            { it.status == Constant.STATUS_REJECTED }
        ))
        if (orgUsers.size > 0) {
            sortedOrgUsers.forEach {
                val corporateUser = it.corporateUser
                val corpUserView =
                    activity.layoutInflater.inflate(R.layout.item_organization_user, null)
                val textViewOrganizationName =
                    corpUserView?.findViewById<TextView>(R.id.textViewOrganizationName)
                val imageViewOrganizationStatus =
                    corpUserView?.findViewById<ImageView>(R.id.imageViewOrganizationStatus)
                val imageViewCurrentOrganization =
                    corpUserView?.findViewById<ImageView>(R.id.imageViewCurrentOrganization)
                imageViewCurrentOrganization?.visibility =
                    if (it.isCurrent) View.VISIBLE else View.INVISIBLE
                textViewOrganizationName?.text =
                    ("${corporateUser?.firstName} ${corporateUser?.lastName}")
                textViewOrganizationName?.setTextColor(
                    ContextCompat.getColor(
                        activity,
                        ConstantHelper.Color.getStatusColor(
                            it.status
                        )
                    )
                )
                imageViewOrganizationStatus?.setImageResource(
                    ConstantHelper.Drawable.getCorporateIconStatus(it.status)
                )
                orgHeight += corpUserView.height
                linearLayoutOrgUser?.addView(corpUserView)
            }
        } else {
            val corpUserView =
                activity.layoutInflater.inflate(R.layout.item_organization_user, null)
            val textViewOrganizationName =
                corpUserView?.findViewById<TextView>(R.id.textViewOrganizationName)
            val imageViewOrganizationStatus =
                corpUserView?.findViewById<ImageView>(R.id.imageViewOrganizationStatus)
            val imageViewCurrentOrganization =
                corpUserView?.findViewById<ImageView>(R.id.imageViewCurrentOrganization)
            imageViewOrganizationStatus?.visibility = View.INVISIBLE
            imageViewCurrentOrganization?.visibility = View.INVISIBLE
            textViewOrganizationName?.text = activity.getString(R.string.msg_no_members_group)
            linearLayoutOrgUser?.addView(corpUserView)
        }
        if (orgUsers.size >= 5) {
            linearLayoutOrgUser?.layoutParams?.height = orgHeight
        }
        showGroupDialog(dialogBuilder)
    }

    private fun showGroupDialog(dialogBuilder: AlertDialog.Builder) {
        val dialog = dialogBuilder.create()
        dialog.show()
        dialog.window?.setLayout(
            activity.resources.getDimension(R.dimen.dialog_width).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.attributes?.windowAnimations = R.style.SlideUpAnimation
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun setCurrentGroup(
        organizationUsers: MutableList<OrganizationUser>,
        orgId: String?
    ): Boolean {
        organizationUsers.forEach {
            if (it.corporateUser?.id == orgId) {
                return true
            }
        }
        return false
    }

    private fun getCorporateUserByGroup(
        id: String?,
        organizationUsers: MutableList<OrganizationUser>?,
        approvalProcessOrganizationUsers: MutableList<ApprovalProcessOrganizationUser>?
    ): MutableList<OrganizationUser> {

        val approvalOrganizationUsers = mutableListOf<OrganizationUser>()

        organizationUsers?.forEach { organizationUser ->

            approvalProcessOrganizationUsers?.forEach { approvalProcessOrganizationUser ->

                if (organizationUser.id == approvalProcessOrganizationUser.organizationUser?.id) {

                    approvalProcessOrganizationUser.organizationUser?.status =
                        approvalProcessOrganizationUser.status
                    approvalProcessOrganizationUser.organizationUser?.isCurrent = id ==
                            approvalProcessOrganizationUser.organizationUser?.corporateUser?.id

                    approvalOrganizationUsers.add(approvalProcessOrganizationUser.organizationUser!!)
                }

            }

        }

        return approvalOrganizationUsers.toMutableList()
    }

}