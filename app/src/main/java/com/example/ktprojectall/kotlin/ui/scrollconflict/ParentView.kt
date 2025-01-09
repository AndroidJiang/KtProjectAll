package com.example.ktprojectall.kotlin.ui.scrollconflict

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.ScrollView

class ParentView : LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        Log.d("ajiang","MyParentView-dispatchTouchEvent"+ev?.action)
        return super.dispatchTouchEvent(ev)
    }
    var downY = 0
    var moveY = 0
    var move = 0
    var i = 0
    var intercept = false
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        Log.d("ajiang","MyParentView-onInterceptTouchEvent"+ev?.action)
        var y = ev!!.y.toInt()
        when(ev?.action){
            MotionEvent.ACTION_DOWN->{
                downY = y
                intercept = false
            }
            MotionEvent.ACTION_MOVE->{
                moveY = y
                Log.d("xxxx","moveY=${moveY},downY=${downY}")
                if(moveY - downY > 0){
//                    if(getChildAt(0) is ScrollView){   //此处如果打开，子布局无需重写
//                        if((getChildAt(0) as ScrollView).scrollY  == 0){
//                            intercept = true
//                        }
//                    }
                    intercept = true
                }else if(moveY - downY < 0){
                    intercept = false
                }
            }

        }
        Log.d("ajiang","MyParentView-intercept-"+intercept)
        return intercept
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        Log.d("ajiang","MyParentView-onTouchEvent"+ev?.action)
            var y = ev!!.y.toInt()
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                downY = y
            }
            MotionEvent.ACTION_MOVE -> {
                moveY = y
                if(moveY - downY > 0) {
                    move = moveY - downY
                    i+=move
                    layout(left, top + move, right, bottom + move)
                }
            }
            MotionEvent.ACTION_UP -> {
                layout(left, top -i, right, bottom -i)
                i = 0
            }
        }
        return true
    }
}