package com.mobdeve.s13.wang.jeremy.mobdevemco.helper

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

open class OnSwipeTouchListener(context: Context) : View.OnTouchListener {

    private val gestureDetector = GestureDetector(context, GestureListener())

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(motionEvent)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        private val SWIPE_THRESHOLD = 100       // Minimum distance in pixels to consider a swipe
        private val SWIPE_VELOCITY_THRESHOLD = 100 // Minimum velocity to consider a swipe
        @Suppress("NOTHING_TO_OVERRIDE", "ACCIDENTAL_OVERRIDE")
        override fun onFling(
            e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float
        ): Boolean {
            if (e1 == null || e2 == null) return false
            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            return when {
                abs(diffY) > abs(diffX) && abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD -> {
                    if (diffY < 0) onSwipeUp() // Swipe up
                    else onSwipeDown()        // Swipe down
                    true
                }
                else -> false
            }
        }
    }

    open fun onSwipeUp() {
        // Override to handle swipe up
    }

    open fun onSwipeDown() {
        // Override to handle swipe down
    }
}
