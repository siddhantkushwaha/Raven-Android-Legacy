package com.siddhantkushwaha.raven.custom

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.Interpolator
import android.widget.ImageView

/**
 * [android.view.View.OnClickListener] used to translate the product grid upperSheet downward on
 * the Y-axis when the navigation icon in the toolbar is pressed.
 */
open class NavigationIconClickListener @JvmOverloads internal constructor(
        private val context: Context, private val upperSheet: View, private val interpolator: Interpolator? = null,
        private val openIcon: Drawable? = null, private val closeIcon: Drawable? = null, private val bottomSheet: View? = null) : View.OnClickListener {

    private val animatorSet = AnimatorSet()
    private val height: Int
    private var backdropShown = false

    init {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        height = displayMetrics.heightPixels
    }

    override fun onClick(view: View) {
        backdropShown = !backdropShown

        // Cancel the existing animations
        animatorSet.removeAllListeners()
        animatorSet.end()
        animatorSet.cancel()

        updateIcon(view)

        val translateY = bottomSheet?.height ?: (height * 0.4).toInt()

        val animator = ObjectAnimator.ofFloat(upperSheet, "translationY", (if (backdropShown) translateY else 0).toFloat())
        animator.duration = 400
        if (interpolator != null) {
            animator.interpolator = interpolator
        }
        animatorSet.play(animator)
        animator.start()
    }

    private fun updateIcon(view: View) {
        if (openIcon != null && closeIcon != null) {
            if (view !is ImageView) {
                throw IllegalArgumentException("updateIcon() must be called on an ImageView")
            }
            if (backdropShown) {
                view.setImageDrawable(closeIcon)
            } else {
                view.setImageDrawable(openIcon)
            }
        }
    }
}
