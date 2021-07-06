package com.unionbankph.corporate.app.common.platform.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.presentation.constant.URLDataEnum
import com.unionbankph.corporate.mcd.presentation.camera.CheckDepositCameraActivity

class Navigator {

    enum class TransitionActivity {
        TRANSITION_SLIDE_UP,
        TRANSITION_SLIDE_DOWN,
        TRANSITION_SLIDE_RIGHT,
        TRANSITION_SLIDE_LEFT,
        TRANSITION_FADE_IN,
        TRANSITION_FADE_OUT,
        TRANSITION_FLIP
    }

    fun navigate(
        source: AppCompatActivity,
        target: Class<out AppCompatActivity>,
        bundle: Bundle? = null,
        isClear: Boolean,
        isAnimated: Boolean,
        transitionActivity: TransitionActivity? = null
    ) {
        val intent = Intent(source, target).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            bundle?.let { putExtras(it) }
        }
        source.startActivity(intent)
        if (isClear) source.finish()
        if (isAnimated) {
            when (transitionActivity) {
                TransitionActivity.TRANSITION_SLIDE_UP -> source.overridePendingTransition(
                    R.anim.anim_slide_up, R.anim.anim_no_change
                )
                TransitionActivity.TRANSITION_SLIDE_DOWN -> source.overridePendingTransition(
                    R.anim.anim_no_change, R.anim.anim_slide_down
                )
                TransitionActivity.TRANSITION_SLIDE_LEFT -> source.overridePendingTransition(
                    R.anim.anim_forward_left_to_right, R.anim.anim_forward_right_to_left
                )
                TransitionActivity.TRANSITION_SLIDE_RIGHT -> source.overridePendingTransition(
                    R.anim.anim_backward_left_to_right, R.anim.anim_backward_right_to_left
                )
                TransitionActivity.TRANSITION_FADE_IN -> source.overridePendingTransition(
                    R.anim.anim_fade_in, R.anim.anim_no_change
                )
                TransitionActivity.TRANSITION_FADE_OUT -> source.overridePendingTransition(
                    R.anim.anim_fade_out, R.anim.anim_no_change
                )
            }
        }
    }

    fun navigateForResult(
        source: AppCompatActivity,
        target: Class<out AppCompatActivity>,
        bundle: Bundle? = null,
        isClear: Boolean,
        isAnimated: Boolean,
        resultCode: Int,
        transitionActivity: TransitionActivity? = null
    ) {
        val intent = Intent(source, target).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            bundle?.let { putExtras(it) }
        }
        source.startActivityForResult(intent, resultCode)
        if (isClear) source.finish()
        if (isAnimated) {
            when (transitionActivity) {
                TransitionActivity.TRANSITION_SLIDE_UP -> source.overridePendingTransition(
                    R.anim.anim_slide_up, R.anim.anim_no_change
                )
                TransitionActivity.TRANSITION_SLIDE_DOWN -> source.overridePendingTransition(
                    R.anim.anim_no_change, R.anim.anim_slide_down
                )
                TransitionActivity.TRANSITION_SLIDE_LEFT -> source.overridePendingTransition(
                    R.anim.anim_forward_left_to_right, R.anim.anim_forward_right_to_left
                )
                TransitionActivity.TRANSITION_SLIDE_RIGHT -> source.overridePendingTransition(
                    R.anim.anim_backward_left_to_right, R.anim.anim_backward_right_to_left
                )
                TransitionActivity.TRANSITION_FADE_IN -> source.overridePendingTransition(
                    R.anim.anim_fade_in, R.anim.anim_no_change
                )
                TransitionActivity.TRANSITION_FADE_OUT -> source.overridePendingTransition(
                    R.anim.anim_fade_out, R.anim.anim_no_change
                )
            }
        }
    }

    fun navigateImageTransition(
        source: AppCompatActivity,
        target: Class<out AppCompatActivity>,
        bundle: Bundle? = null,
        imageView: View
    ) {
        val intent = Intent(source, target).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            bundle?.let { putExtras(it) }
        }
        val pair1 = Pair.create(imageView, source.formatString(R.string.image_animation_tag))
        val activityOptionsCompat =
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                source,
                pair1
            )
        source.startActivity(intent, activityOptionsCompat.toBundle())
    }

    fun navigateClearUpStack(
        source: AppCompatActivity,
        target: Class<out AppCompatActivity>,
        bundle: Bundle? = null,
        isClear: Boolean,
        isAnimated: Boolean
    ) {
        val intent = Intent(source, target).apply {
            if (isClear) addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            bundle?.let { putExtras(it) }
        }
        source.startActivity(intent)
        if (target == DashboardActivity::class.java || target == CheckDepositCameraActivity::class.java) {
            if (isAnimated) source.overridePendingTransition(
                R.anim.anim_no_change, R.anim.anim_slide_down
            )
        } else {
            if (isAnimated) source.overridePendingTransition(
                R.anim.anim_backward_left_to_right, R.anim.anim_backward_right_to_left
            )
        }
    }

    fun navigateClearStacks(
        source: AppCompatActivity,
        target: Class<out AppCompatActivity>,
        bundle: Bundle? = null,
        isAnimated: Boolean,
        transitionActivity: TransitionActivity? = null
    ) {
        val intent = Intent(source, target).apply {
            bundle?.let { putExtras(it) }
        }
        source.startActivity(intent)
        source.finishAffinity()
        if (isAnimated) {
            when (transitionActivity) {
                TransitionActivity.TRANSITION_SLIDE_UP -> {
                    source.overridePendingTransition(
                        R.anim.anim_slide_up, R.anim.anim_no_change
                    )
                }
                TransitionActivity.TRANSITION_SLIDE_DOWN -> {
                    source.overridePendingTransition(
                        R.anim.anim_no_change, R.anim.anim_slide_down
                    )
                }
                TransitionActivity.TRANSITION_SLIDE_LEFT -> {
                    source.overridePendingTransition(
                        R.anim.anim_forward_left_to_right, R.anim.anim_forward_right_to_left
                    )
                }
                TransitionActivity.TRANSITION_FADE_IN -> {
                    source.overridePendingTransition(
                        R.anim.anim_fade_in, R.anim.anim_fade_out
                    )
                }
                TransitionActivity.TRANSITION_FADE_OUT -> {
                    source.overridePendingTransition(R.anim.anim_fade_out, R.anim.anim_fade_in)
                }
                else -> {
                    source.overridePendingTransition(
                        R.anim.anim_backward_left_to_right,
                        R.anim.anim_backward_right_to_left
                    )
                }
            }
        }
    }

    fun navigateClearStacks(
        context: Context,
        target: Class<out AppCompatActivity>,
        bundle: Bundle? = null
    ) {
        val intent = Intent(context, target).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            bundle?.let { putExtras(it) }
        }
        context.startActivity(intent)
    }

    fun reNavigateActivity(
        source: AppCompatActivity,
        target: Class<out AppCompatActivity>,
        bundle: Bundle? = null,
        isClear: Boolean,
        isAnimated: Boolean,
        transitionActivity: TransitionActivity
    ) {
        val intent = Intent(source, target).apply {
            if (isClear) flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            bundle?.let { putExtras(it) }
        }
        if (isClear) source.finish()
        source.startActivity(intent)
        if (isAnimated) {
            when (transitionActivity) {
                TransitionActivity.TRANSITION_FLIP -> {
                    source.overridePendingTransition(
                        R.anim.anim_flip_fade_in, R.anim.anim_flip_fade_out
                    )
                }
                else -> {
                    source.overridePendingTransition(
                        R.anim.anim_no_change, R.anim.anim_fade_out
                    )
                }
            }
        }
    }

    fun navigateBrowser(source: AppCompatActivity, urlDataEnum: URLDataEnum) {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(urlDataEnum.value)
        source.startActivity(i)
    }

    fun replaceFragment(
        container: Int,
        newFragment: Fragment,
        fragmentManager: FragmentManager
    ) {
        fragmentManager.beginTransaction()
            .replace(container, newFragment)
            .setMaxLifecycle(newFragment, Lifecycle.State.RESUMED)
            .commit()
    }

    fun addragment(
        container: Int,
        newFragment: Fragment,
        fragmentManager: FragmentManager
    ) {
        fragmentManager.beginTransaction()
            .add(container, newFragment)
            .setMaxLifecycle(newFragment, Lifecycle.State.RESUMED)
            .commit()
    }

    fun addFragment(
        container: Int,
        newFragment: Fragment,
        fragmentManager: FragmentManager,
        savedInstanceState: Bundle?
    ) {
        if (savedInstanceState == null) {
            fragmentManager
                .beginTransaction()
                .add(container, newFragment)
                .setMaxLifecycle(newFragment, Lifecycle.State.RESUMED)
                .commit()
        }
    }

    fun replaceFragmentSharedElement(
        container: Int,
        newFragment: Fragment,
        fragmentManager: FragmentManager,
        sharedElement: View,
        transitionName: String
    ) {
        val transaction = fragmentManager.beginTransaction()
        transaction.addSharedElement(sharedElement, transitionName)
            .replace(container, newFragment)
            .addToBackStack(null)
            .setMaxLifecycle(newFragment, Lifecycle.State.RESUMED)
            .commit()
    }

    fun addFragment(
        container: Int,
        newFragment: Fragment,
        fragmentManager: FragmentManager,
        sharedElement: View,
        transitionName: String
    ) {
        val transaction = fragmentManager.beginTransaction()
        transaction.addSharedElement(sharedElement, transitionName)
            .add(container, newFragment)
            .addToBackStack(null)
            .setMaxLifecycle(newFragment, Lifecycle.State.RESUMED)
            .commit()
    }

    fun replaceFragment(
        container: Int,
        fragment: Fragment,
        args: Bundle? = null,
        fragmentManager: FragmentManager?,
        tag: String,
        hasAddToStack: Boolean = true
    ) {
        val transaction = fragmentManager?.beginTransaction()
        fragment.arguments = args
        transaction?.replace(container, fragment)
        if (hasAddToStack) {
            transaction?.addToBackStack(tag)
        }
        transaction?.setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
        transaction?.commit()
    }

    fun replaceFragmentWithAnimation(
        container: Int,
        fragment: Fragment,
        args: Bundle? = null,
        fragmentManager: FragmentManager?,
        tag: String
    ) {
        val transaction = fragmentManager?.beginTransaction()
        transaction?.setCustomAnimations(
            R.anim.anim_enter_from_right,
            R.anim.anim_exit_to_left,
            R.anim.anim_enter_from_left,
            R.anim.anim_exit_to_right
        )
        fragment.arguments = args
        transaction?.replace(container, fragment)
        transaction?.addToBackStack(tag)
        transaction?.setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
        transaction?.commit()
    }

    fun addFragmentWithAnimation(
        container: Int,
        fragment: Fragment,
        args: Bundle? = null,
        fragmentManager: FragmentManager?,
        tag: String
    ) {
        val fragmentPopped = fragmentManager?.popBackStackImmediate(tag, 0)
        if (fragmentPopped == false && fragmentManager.findFragmentByTag(tag) == null) {
            val transaction = fragmentManager.beginTransaction()
            transaction.setCustomAnimations(
                R.anim.anim_enter_from_right,
                R.anim.anim_exit_to_left,
                R.anim.anim_enter_from_left,
                R.anim.anim_exit_to_right
            )
            fragment.arguments = args
            transaction.add(container, fragment, tag)
            transaction.addToBackStack(tag)
            transaction.setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
            transaction.commit()
        }
    }

    fun popBackStack(
            fragmentManager: FragmentManager?,
            tag: String
    ){
        val fragmentPopped = fragmentManager?.popBackStackImmediate(tag, 0)
        val fragment = fragmentManager?.findFragmentByTag(tag)
        if (fragmentPopped == false && fragment != null) {
            val transaction = fragmentManager.beginTransaction()

            transaction.remove(fragment)
            transaction.commit()
        }
    }
}
