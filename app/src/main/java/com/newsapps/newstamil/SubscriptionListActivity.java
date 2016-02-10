package com.newsapps.newstamil;

import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import com.fenjuly.mylibrary.ToggleExpandLayout;
import com.kyleduo.switchbutton.SwitchButton;


public class SubscriptionListActivity extends CommonActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_list);
        final ToggleExpandLayout layout = (ToggleExpandLayout) findViewById(R.id.cat_main);
        final ToggleExpandLayout layout2 = (ToggleExpandLayout) findViewById(R.id.cat_state);
        final ToggleExpandLayout layout3 = (ToggleExpandLayout) findViewById(R.id.cat_india);
        SwitchButton SwitchButton = (SwitchButton) findViewById(R.id.switch_cat_main);

        layout.setOnToggleTouchListener(new ToggleExpandLayout.OnToggleTouchListener() {
            @Override
            public void onStartOpen(int height, int originalHeight) {
            }

            @Override
            public void onOpen() {
                int childCount = layout.getChildCount();
                for(int i = 0; i < childCount; i++) {
                    View view = layout.getChildAt(i);
                    ViewCompat.setElevation(view, dp2px(1));
                }
            }

            @Override
            public void onStartClose(int height, int originalHeight) {
                int childCount = layout.getChildCount();
                for(int i = 0; i < childCount; i++) {
                    View view = layout.getChildAt(i);
                    ViewCompat.setElevation(view, dp2px(i));
                }
            }

            @Override
            public void onClosed() {

            }
        });

        SwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    layout.open();
                } else {
                    layout.close();
                }
            }
        });

        SwitchButton SwitchButton2 = (SwitchButton) findViewById(R.id.switch_cat_state);

        layout2.setOnToggleTouchListener(new ToggleExpandLayout.OnToggleTouchListener() {
            @Override
            public void onStartOpen(int height, int originalHeight) {
            }

            @Override
            public void onOpen() {
                int childCount = layout2.getChildCount();
                for(int i = 0; i < childCount; i++) {
                    View view = layout2.getChildAt(i);
                    ViewCompat.setElevation(view, dp2px(1));
                }
            }

            @Override
            public void onStartClose(int height, int originalHeight) {
                int childCount = layout2.getChildCount();
                for(int i = 0; i < childCount; i++) {
                    View view = layout2.getChildAt(i);
                    ViewCompat.setElevation(view, dp2px(i));
                }
            }

            @Override
            public void onClosed() {

            }
        });

        SwitchButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    layout2.open();
                } else {
                    layout2.close();
                }
            }
        });


        SwitchButton SwitchButton3 = (SwitchButton) findViewById(R.id.switch_cat_india);

        layout3.setOnToggleTouchListener(new ToggleExpandLayout.OnToggleTouchListener() {
            @Override
            public void onStartOpen(int height, int originalHeight) {
            }

            @Override
            public void onOpen() {
                int childCount = layout3.getChildCount();
                for(int i = 0; i < childCount; i++) {
                    View view = layout3.getChildAt(i);
                    ViewCompat.setElevation(view, dp2px(1));
                }
            }

            @Override
            public void onStartClose(int height, int originalHeight) {
                int childCount = layout3.getChildCount();
                for(int i = 0; i < childCount; i++) {
                    View view = layout3.getChildAt(i);
                    ViewCompat.setElevation(view, dp2px(i));
                }
            }

            @Override
            public void onClosed() {

            }
        });

        SwitchButton3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.e("SwitchButton open", "invoked");
                    layout3.open();
                } else {
                    Log.e("SwitchButton close", "invoked");
                    layout3.close();
                }
            }
        });
    }

    public float dp2px(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

}
