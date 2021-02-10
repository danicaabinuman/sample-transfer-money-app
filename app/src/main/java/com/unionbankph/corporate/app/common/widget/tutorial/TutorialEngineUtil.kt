package com.unionbankph.corporate.app.common.widget.tutorial

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.takusemba.spotlight.OnSpotlightStateChangedListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.shape.Circle
import com.takusemba.spotlight.target.CustomTarget
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.widget.canvas.RoundedRectangle
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import javax.inject.Inject

class TutorialEngineUtil
@Inject
constructor(
    private val viewUtil: ViewUtil
) {
    private lateinit var onTutorialListener: OnTutorialListener

    fun setOnTutorialListener(onTutorialListener: OnTutorialListener) {
        this.onTutorialListener = onTutorialListener
    }

    fun startTutorial(
        activity: AppCompatActivity,
        view: View,
        layout: Int,
        radius: Float,
        isCircle: Boolean,
        desc: String,
        gravity: GravityEnum,
        animation: OverlayAnimationEnum
    ) {
        view.post {
            val inflater = LayoutInflater.from(activity)
            val viewTarget = inflater.inflate(layout, null)
            viewTarget.findViewById<TextView>(R.id.textViewTutorialMessage).text = desc
            val rect = Rect()
            view.getGlobalVisibleRect(rect)
            val target = CustomTarget.Builder(activity).setPoint(view)
                .setShape(
                    if (isCircle) Circle(radius)
                    else {
                        RoundedRectangle(
                            if (animation == OverlayAnimationEnum.ANIM_SLIDE_DOWN) rect else null,
                            view.height.toFloat(),
                            view.width.toFloat(),
                            radius,
                            animation
                        )
                    }
                )
                .setOverlay(viewTarget)
            target.setDuration(activity.resources.getInteger(R.integer.time_enter_tutorial).toLong())
            initSpotLight(activity, target, layout, viewTarget, isCircle, gravity, rect, view)
        }
    }

    fun startTutorial(
        activity: AppCompatActivity,
        view: View,
        totalHeight: Int,
        rect: Rect,
        layout: Int,
        radius: Float,
        isCircle: Boolean,
        desc: String,
        gravity: GravityEnum,
        animation: OverlayAnimationEnum
    ) {
        view.post {
            val inflater = LayoutInflater.from(activity)
            val viewTarget = inflater.inflate(layout, null)
            viewTarget.findViewById<TextView>(R.id.textViewTutorialMessage).text = desc
            val target = CustomTarget.Builder(activity).setPoint(view)
                .setShape(
                    if (isCircle) Circle(radius)
                    else {
                        RoundedRectangle(
                            rect,
                            totalHeight.toFloat(),
                            view.width.toFloat(),
                            radius,
                            animation
                        )
                    }
                )
                .setOverlay(viewTarget)
            target.setDuration(activity.resources.getInteger(R.integer.time_enter_tutorial).toLong())

            initSpotLight(activity, target, layout, viewTarget, isCircle, gravity, rect, view)
        }
    }

    private fun initSpotLight(
        activity: AppCompatActivity,
        target: CustomTarget.Builder,
        layout: Int,
        viewTarget: View,
        isCircle: Boolean,
        gravity: GravityEnum,
        rect: Rect,
        view: View
    ) {
        val spotlight = Spotlight.with(activity)
            .setOverlayColor(R.color.colorBlack75)
            .setDuration(activity.resources.getInteger(R.integer.time_enter_tutorial).toLong())
            .setAnimation(DecelerateInterpolator())
            .setTargets(target.build())
            .setClosedOnTouchedOutside(false)
            .setOnSpotlightStateListener(object : OnSpotlightStateChangedListener {
                override fun onStarted() {
                    val constraintSet = ConstraintSet()
                    val constraintLayoutTutorial =
                        viewTarget.findViewById<ConstraintLayout>(
                            R.id.constraintLayoutTutorial
                        )
                    constraintSet.clone(constraintLayoutTutorial)
                    val imgArrow = viewTarget.findViewById<ImageView>(R.id.imageViewArrow)
                    var paramsSource: ViewGroup.LayoutParams? = imgArrow.layoutParams
                    if (paramsSource == null) paramsSource = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    val widthSpecSource = viewUtil.makeMeasureSpec(paramsSource.width)
                    val heightSpecSource = viewUtil.makeMeasureSpec(paramsSource.height)
                    imgArrow.measure(widthSpecSource, heightSpecSource)
                    if (isCircle) {
                        val marginTop = if (gravity == GravityEnum.TOP) {
                            rect.top - imgArrow.measuredHeight - activity.resources.getDimension(
                                R.dimen.content_group_spacing
                            ).toInt()
                        } else {
                            rect.bottom + activity.resources.getDimension(
                                R.dimen.content_spacing_half
                            ).toInt()
                        }
                        val marginLeft =
                            rect.right - imgArrow.measuredWidth - ((rect.right - rect.left) / 2)
                        constraintSet.connect(
                            imgArrow.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID,
                            ConstraintSet.TOP, marginTop
                        )
                        constraintSet.connect(
                            imgArrow.id, ConstraintSet.START, ConstraintSet.PARENT_ID,
                            ConstraintSet.START, marginLeft
                        )
                    } else {
                        val marginTop = if (gravity == GravityEnum.TOP) {
                            rect.top - imgArrow.measuredHeight - activity.resources.getDimension(
                                R.dimen.content_spacing
                            ).toInt()
                        } else {
                            rect.bottom + activity.resources.getDimension(
                                R.dimen.content_spacing_half
                            ).toInt()
                        }
                        val marginLeft = when (layout) {
                            R.layout.frame_tutorial_upper_right ->
                                rect.right - imgArrow.measuredWidth - ((rect.right - rect.left) / 2)
                            R.layout.frame_tutorial_lower_right ->
                                rect.right - imgArrow.measuredWidth - activity.resources.getDimension(
                                    R.dimen.content_group_spacing
                                ).toInt()
                            else ->
                                rect.left + activity.resources.getDimension(
                                    R.dimen.content_group_spacing
                                ).toInt()
                        }

                        constraintSet.connect(
                            imgArrow.id,
                            ConstraintSet.TOP,
                            ConstraintSet.PARENT_ID,
                            ConstraintSet.TOP,
                            marginTop
                        )
                        constraintSet.connect(
                            imgArrow.id,
                            ConstraintSet.START,
                            ConstraintSet.PARENT_ID,
                            ConstraintSet.START,
                            marginLeft
                        )
                    }
                    constraintSet.applyTo(constraintLayoutTutorial)
                    onTutorialListener.onStartedTutorial(view, viewTarget)
                }

                override fun onEnded() {
                    onTutorialListener.onEndedTutorial(view, viewTarget)
                }
            })
        viewTarget.findViewById<Button>(R.id.buttonOk).run {
            setOnClickListener {
                it.isEnabled = false
                onTutorialListener.onClickOkButtonTutorial(spotlight)
            }
        }
        viewTarget.findViewById<TextView>(R.id.textViewSkiptutorial).run {
            setOnClickListener { button ->
                MaterialDialog(activity).show {
                    lifecycleOwner(activity)
                    title(R.string.title_skip_tutorials)
                    message(R.string.msg_skip_tutorials)
                    positiveButton(
                        res = R.string.action_yes,
                        click = {
                            it.dismiss()
                            button.isEnabled = false
                            onTutorialListener.onClickSkipButtonTutorial(spotlight)
                        }
                    )
                    negativeButton(
                        res = R.string.action_no,
                        click = {
                            it.dismiss()
                        }
                    )
                }
            }
        }
        spotlight.start()
    }

    fun startTutorial(
        activity: AppCompatActivity,
        image: Int?,
        title: String,
        message: String
    ) {
        val inflater = LayoutInflater.from(activity)
        val viewTarget = inflater.inflate(R.layout.frame_tutorial, null)

        val imageViewTutorial = viewTarget.findViewById<ImageView>(R.id.imageViewTutorial)
        val textViewTutorialTitle = viewTarget.findViewById<TextView>(R.id.textViewTutorialTitle)
        val textViewTutorialMessage =
            viewTarget.findViewById<TextView>(R.id.textViewTutorialMessage)

        if (image != null) {
            imageViewTutorial.setImageResource(image)
        } else {
            imageViewTutorial.visibility = View.GONE
        }

        textViewTutorialTitle.text = title
        textViewTutorialMessage.text = message

        val target = CustomTarget.Builder(activity)
            .setOverlay(viewTarget)
            .setPoint(-100f, -100f)
            .setDuration(activity.resources.getInteger(R.integer.time_enter_tutorial).toLong())

        val spotlight = Spotlight.with(activity)
            .setOverlayColor(R.color.colorBlack80)
            .setDuration(activity.resources.getInteger(R.integer.time_enter_tutorial).toLong())
            .setAnimation(DecelerateInterpolator())
            .setTargets(target.build())
            .setClosedOnTouchedOutside(false)
            .setOnSpotlightStateListener(object : OnSpotlightStateChangedListener {
                override fun onStarted() {
                    onTutorialListener.onStartedTutorial(null, viewTarget)
                }

                override fun onEnded() {
                    onTutorialListener.onEndedTutorial(null, viewTarget)
                }
            })
        viewTarget.findViewById<Button>(R.id.buttonOk).run {
            setOnClickListener {
                it.isEnabled = false
                onTutorialListener.onClickOkButtonTutorial(spotlight)
            }
        }
        viewTarget.findViewById<TextView>(R.id.textViewSkiptutorial).run {
            setOnClickListener { button ->
                MaterialDialog(activity).show {
                    lifecycleOwner(activity)
                    title(R.string.title_skip_tutorials)
                    message(R.string.msg_skip_tutorials)
                    positiveButton(
                        res = R.string.action_yes,
                        click = {
                            it.dismiss()
                            button.isEnabled = false
                            onTutorialListener.onClickSkipButtonTutorial(spotlight)
                        }
                    )
                    negativeButton(
                        res = R.string.action_no,
                        click = {
                            it.dismiss()
                        }
                    )
                }
            }
        }
        spotlight.start()
    }
}
