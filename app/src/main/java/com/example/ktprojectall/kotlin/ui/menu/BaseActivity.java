package com.example.ktprojectall.kotlin.ui.menu;

import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.example.ktprojectall.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by Dajavu on 26/10/2017.
 */

public abstract class BaseActivity<V extends LinearLayoutManager, S extends SettingPopUpWindow>
        extends AppCompatActivity {
    public RecyclerView recyclerView;
    private V viewPagerLayoutManager;
    private S settingPopUpWindow;
    protected DataAdapter dataAdapter;

    protected abstract V createLayoutManager();

    protected abstract S createSettingPopUpWindow();
    private int[] images = {R.mipmap.icon_home_page_rank_1
            , R.mipmap.icon_home_page_rank_2
            , R.mipmap.icon_home_page_rank_3
            ,
            R.mipmap.icon_home_page_rank_4
            , R.mipmap.icon_home_page_rank_5
            , R.mipmap.icon_home_page_rank_6
            , R.mipmap.icon_home_page_rank_7
            ,
            R.mipmap.icon_home_page_rank_8
            , R.mipmap.icon_home_page_rank_9
            , R.mipmap.icon_home_page_rank_10
    };
    protected List<Item> items = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        setTitle(getIntent().getCharSequenceExtra("标题"));
        recyclerView = findViewById(R.id.recycler);
        viewPagerLayoutManager = createLayoutManager();
        for (int i = 0; i < images.length; i++) {
            items.add(new Item(images[i],0));
        }
        dataAdapter = new DataAdapter(items);
        dataAdapter.setOnItemClickListener(new DataAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                Toast.makeText(v.getContext(), "clicked:" + pos, Toast.LENGTH_SHORT).show();
                ScrollHelper.smoothScrollToTargetView(recyclerView, v);
            }
        });
        recyclerView.setAdapter(dataAdapter);
        recyclerView.setLayoutManager(viewPagerLayoutManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        MenuItem settings = menu.findItem(R.id.setting);
        VectorDrawableCompat settingIcon =
                VectorDrawableCompat.create(getResources(), R.drawable.ic_launcher_foreground, null);
        settings.setIcon(settingIcon);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                showDialog();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDialog() {
        if (settingPopUpWindow == null) {
            settingPopUpWindow = createSettingPopUpWindow();
        }
        settingPopUpWindow.showAtLocation(recyclerView, Gravity.CENTER, 0, 0);
    }

    public V getViewPagerLayoutManager() {
        return viewPagerLayoutManager;
    }

    public S getSettingPopUpWindow() {
        return settingPopUpWindow;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (settingPopUpWindow != null && settingPopUpWindow.isShowing())
            settingPopUpWindow.dismiss();
    }
    protected void resetSelect(){
        for (Item item : items) {
            item.select = 0;
        }
    }
}