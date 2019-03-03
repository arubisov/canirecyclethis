package com.example.tnaworks.canirecyclethis

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View


class AndroidUtils {

    companion object {
        /**
         * @param view         View to animate
         * @param toVisibility Visibility at the end of animation
         * @param toAlpha      Alpha at the end of animation
         * @param duration     Animation duration in ms
         */
        fun animateView(view: View, toVisibility: Int, toAlpha: Float, duration: Long) {
            val show = toVisibility == View.VISIBLE
            if (show) {
                view.alpha = 0F
            }
            view.visibility = View.VISIBLE
            view.animate()
                .setDuration(duration)
                .alpha(if (show) toAlpha else 0F)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = toVisibility
                    }
                })
        }
    }
}

