package com.unionbankph.corporate.app.common.widget.canvas

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import com.takusemba.spotlight.shape.Shape
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum

class RoundedRectangle() : Shape {

    private lateinit var animation: OverlayAnimationEnum
    private var height: Float = 0f
    private var width: Float = 0f
    private var radius: Float = 0f
    private var rect: Rect? = null

    constructor(height: Float, width: Float, radius: Float, animation: OverlayAnimationEnum) : this() {
        this.height = height
        this.width = width
        this.radius = radius
        this.animation = animation
    }

    constructor(
        rect: Rect?,
        height: Float,
        width: Float,
        radius: Float,
        animation: OverlayAnimationEnum
    ) : this() {
        this.rect = rect
        this.height = height
        this.width = width
        this.radius = radius
        this.animation = animation
    }

    override fun draw(canvas: Canvas, point: PointF, value: Float, paint: Paint) {
        if (animation == OverlayAnimationEnum.ANIM_EXPLODE) {
            if (rect == null) {
                val halfWidth = width / 2 * value
                val halfHeight = height / 2 * value
                val left = point.x - halfWidth
                val top = point.y - halfHeight
                val right = point.x + halfWidth
                val bottom = point.y + halfHeight
                val rect = RectF(left, top, right, bottom)
                canvas.drawRoundRect(rect, radius, radius, paint)
            } else {
                val halfWidth = width / 2 * value
                val halfHeight = height / 2 * value
                val left = rect!!.centerX() - halfWidth
                val top = rect!!.centerY() - halfHeight
                val right = rect!!.centerX() + halfWidth
                val bottom = rect!!.centerY() + halfHeight
                val rect = RectF(left, top, right, bottom)
                canvas.drawRoundRect(rect, radius, radius, paint)
            }
        } else if (animation == OverlayAnimationEnum.ANIM_SLIDE_DOWN) {
            val left = rect!!.left * value
            val top = rect!!.top * value
            val right = rect!!.right * value
            val bottom = rect!!.bottom * value
            val rect = RectF(left, top, right, bottom)
            canvas.drawRoundRect(rect, radius, radius, paint)
        }
    }

    override fun getHeight(): Int {
        return height.toInt()
    }

    override fun getWidth(): Int {
        return width.toInt()
    }
}
