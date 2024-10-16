package com.example.ktprojectall.kotlin.ui.menu;


import android.util.Log;
import android.widget.Toast;

/**
 * Created by Dajavu on 25/10/2017.
 */

public class CircleLayoutActivity extends BaseActivity<CircleLayoutManager, CirclePopUpWindow> {

    @Override
    protected CircleLayoutManager createLayoutManager() {
        CircleLayoutManager circleLayoutManager = new CircleLayoutManager(this);
        circleLayoutManager.setOnPageChangeListener(new ViewPagerLayoutManager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Toast.makeText(CircleLayoutActivity.this, "选中"+position, Toast.LENGTH_SHORT).show();
                resetSelect();
                items.get(position).select = 1;
                dataAdapter.notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        return circleLayoutManager;
    }



    @Override
    protected CirclePopUpWindow createSettingPopUpWindow() {
        return new CirclePopUpWindow(this, getViewPagerLayoutManager(), getRecyclerView());
    }
}