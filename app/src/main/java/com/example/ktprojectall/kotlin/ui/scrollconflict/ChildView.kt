package com.example.ktprojectall.kotlin.ui.scrollconflict

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView

class ChildView : ScrollView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        Log.e("ajiang", "ChildView-dispatchTouchEvent" + ev?.action)
        return super.dispatchTouchEvent(ev)
    }
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        Log.e("ajiang", "ChildView-onTouchEvent" + ev?.action)
        when (ev!!.action) {
            MotionEvent.ACTION_DOWN ,MotionEvent.ACTION_MOVE -> {
                if (scrollY == 0) {
                    Log.e("ajiang", "ChildView-requestDisallowInterceptTouchEvent-false")
                    parent.requestDisallowInterceptTouchEvent(false)
                } else {
                    Log.e("ajiang", "ChildView-requestDisallowInterceptTouchEvent-true")
                    parent.requestDisallowInterceptTouchEvent(true)
                }
            }

        }
        return super.onTouchEvent(ev)
    }
}