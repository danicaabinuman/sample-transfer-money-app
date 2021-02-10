package com.unionbankph.corporate.app.common.widget.canvas

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.unionbankph.corporate.R
import com.unionbankph.corporate.approval.data.model.Edge

class DrawLineCanvas : View {

    private lateinit var paint: Paint
    private lateinit var path: Path
    private lateinit var mainContext: Context

    private lateinit var edge: Edge
    private var margin: Int = 0
    private var cardMargin: Int = 0
    private var isOperatorSource = false
    private var isOperatorDestination = false
    private var divideValue = 2

    constructor(
        context: Context,
        edge: Edge,
        margin: Int,
        isOperatorSource: Boolean,
        isOperatorDestination: Boolean
    ) : super(context) {
        this.mainContext = context
        this.edge = edge
        this.margin = margin
        this.isOperatorSource = isOperatorSource
        this.isOperatorDestination = isOperatorDestination
        initDestination()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initDestination()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
        initDestination()
    }

    private fun initDestination() {
        path = Path()
        paint = Paint()
        paint.color = ContextCompat.getColor(mainContext, edge.pathColor!!)
        paint.style = Paint.Style.FILL_AND_STROKE
        cardMargin = resources.getDimension(R.dimen.card_view_node_operator_margin).toInt()
    }

    private fun drawPath(edge: Edge, margin: Int): Path {
        val nodeSource = edge.source
        val nodeDestination = edge.destination
        val p = Path()
        if (nodeDestination != null && nodeSource != null) {
            val halfMargin = (margin / divideValue)
            p.moveTo(
                nodeDestination.x - 7,
                nodeDestination.y - nodeDestination.height / divideValue
            )
            // Draw half horizontal line from destination to source
            if (isOperatorSource && isOperatorDestination) {
                p.lineTo(
                    nodeDestination.x - ((margin + cardMargin) / divideValue),
                    nodeDestination.y - nodeDestination.height / divideValue
                )
            } else if (isOperatorDestination) {
                p.lineTo(
                    nodeDestination.x - ((margin + cardMargin) / divideValue),
                    nodeDestination.y - nodeDestination.height / divideValue
                )
            } else {
                p.lineTo(
                    nodeDestination.x - halfMargin - 1,
                    nodeDestination.y - nodeDestination.height / divideValue
                )
            }

            // Draw vertical line at the middle of margin seperator
            if (isOperatorSource && isOperatorDestination) {
                p.moveTo(
                    nodeDestination.x - ((margin + cardMargin) / divideValue),
                    nodeDestination.y - nodeDestination.height / divideValue
                )
                p.lineTo(
                    nodeDestination.x - ((margin + cardMargin) / divideValue),
                    nodeSource.y - nodeSource.height / divideValue
                )
            } else {
                p.moveTo(
                    nodeDestination.x - halfMargin,
                    nodeDestination.y - nodeDestination.height / divideValue
                )
                p.lineTo(
                    nodeDestination.x - halfMargin,
                    nodeSource.y - nodeSource.height / divideValue
                )
            }

            // Draw half horizontal line from source to destination
            p.moveTo(
                nodeSource.x + nodeSource.width,
                nodeSource.y - nodeSource.height / divideValue
            )
            if (isOperatorSource && isOperatorDestination) {
                val halfMarginSource =
                    (nodeDestination.x - ((margin + cardMargin) / divideValue)) -
                            (nodeSource.x + nodeSource.width)
                p.lineTo(
                    (nodeSource.x + nodeSource.width) + halfMarginSource,
                    nodeSource.y - nodeSource.height / divideValue
                )
            } else if (isOperatorSource) {
                val halfMarginSource =
                    (nodeDestination.x - halfMargin) - (nodeSource.x + nodeSource.width)
                p.lineTo(
                    (nodeSource.x + nodeSource.width) + halfMarginSource,
                    nodeSource.y - nodeSource.height / divideValue
                )
            } else {
                p.lineTo(
                    (nodeSource.x + nodeSource.width + 2) + halfMargin,
                    nodeSource.y - nodeSource.height / divideValue
                )
            }
        }
        p.close()
        return p
    }

    private fun drawArrow(edge: Edge): Path {
        val nodeSource = edge.source
        val nodeDestination = edge.destination
        val p = Path()
        if (nodeDestination != null && nodeSource != null) {
            p.moveTo(
                nodeDestination.x - mainContext.resources.getDimension(
                    R.dimen.approval_hierarchy_arrow_width_size
                ),
                nodeDestination.y - nodeDestination.height /
                        divideValue - mainContext.resources.getDimension(
                    R.dimen.approval_hierarchy_arrow_height_size
                )
            )
            p.lineTo(
                nodeDestination.x - mainContext.resources.getDimension(
                    R.dimen.approval_hierarchy_arrow_width_size
                ),
                nodeDestination.y - nodeDestination.height /
                        divideValue + mainContext.resources.getDimension(
                    R.dimen.approval_hierarchy_arrow_height_size
                )
            )
            p.lineTo(nodeDestination.x, nodeDestination.y - nodeDestination.height / divideValue)
        }
        p.close()
        return p
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.strokeWidth = mainContext.resources.getDimension(R.dimen.approval_hierarchy_path_size)
        canvas.drawPath(drawPath(edge, margin), paint)

        paint.strokeWidth =
                mainContext.resources.getDimension(R.dimen.approval_hierarchy_path_stroke_size)
        canvas.drawPath(drawArrow(edge), paint)
    }
}
