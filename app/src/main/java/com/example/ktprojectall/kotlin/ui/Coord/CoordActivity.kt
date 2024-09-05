package com.example.ktprojectall.kotlin.ui.Coord

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.ktprojectall.R
import com.google.android.material.tabs.TabLayout

class CoordActivity : AppCompatActivity() {
    var tabLayout: TabLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
//        setSystemBarColor(this,false);
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_coord)

        tabLayout = findViewById(R.id.tab_layout)
        val viewPager = findViewById<ViewPager>(R.id.vp2)
        tabLayout?.setupWithViewPager(viewPager)
        viewPager.adapter = TabsAdapter(supportFragmentManager)
        viewPager.offscreenPageLimit = 1
        viewPager.adapter?.notifyDataSetChanged()

    }


    private class TabsAdapter internal constructor(fm: FragmentManager?) :
        FragmentPagerAdapter(fm!!) {
        override fun getCount(): Int {
            return TAB_COUNT
        }

        override fun getItem(i: Int): Fragment {
            return OutFragment.newInstance()
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return "Tab $position"
        }

        companion object {
            private const val TAB_COUNT = 2
        }
    }

    override fun onPause() {
        super.onPause()
        Log.e("ajiang", "onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("ajiang", "onDestroy")

    }
    fun setSystemBarColor(activity: Activity?, isWebActivity: Boolean) {
        try {


            // 如果有虚拟按键，将虚拟按键设置为显示状态
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                //                //低版本sdk
                val v = activity!!.window.decorView
                if (null != v) {
                    v.systemUiVisibility = View.VISIBLE
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val decorView = activity!!.window.decorView
                if (null != decorView && !isWebActivity) {
                    val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                    decorView.systemUiVisibility = uiOptions
                }
            }
            // 设置手机顶部状态栏的颜色为透明
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && null != activity) {
                val window = activity.window
                //添加Flag把状态栏设为可绘制模式
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                //如果为全透明模式，取消设置Window半透明的Flag
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                //设置状态栏为透明
                if (isWebActivity) {
                    window.statusBarColor = Color.WHITE
                    //设置window的状态栏不可见
                    window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    window.statusBarColor = Color.TRANSPARENT
                    //设置window的状态栏不可见
                    window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
                //view不根据系统窗口来调整自己的布局
                val mContentView = window.findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup
                if (null != mContentView) {
                    val mChildView = mContentView.getChildAt(0)
                    if (mChildView != null) {
                        ViewCompat.setFitsSystemWindows(mChildView, false)
                        ViewCompat.requestApplyInsets(mChildView)
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && null != activity) {
                val window = activity.window
                //设置Window为透明
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                val mContentView = activity.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
            }
        } catch (e: Exception) {
        }
    }
}