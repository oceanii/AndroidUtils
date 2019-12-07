package com.example.xzy.androidutils.activity;

import android.util.Log;
import android.view.View;

import com.example.xzy.androidutils.R;
import com.example.xzy.androidutils.baseactivity.BaseActivity;
import com.example.xzy.androidutils.baseactivity.TitleBar;

public class PermissionActivity extends BaseActivity {
    private static final String TAG = "PermissionActivity";
    private TitleBar mTitleBar;
    @Override
    protected int initRootView() {
        return R.layout.activity_permission;
    }

    @Override
    protected void initViews() {
        mTitleBar = findViewById(R.id.v_titlebar);
    }

    @Override
    protected void initTitle() {
        mTitleBar.setCenterText("权限测试");
        mTitleBar.setRightTextVisible(View.VISIBLE);
        mTitleBar.setRightText("完成");
        mTitleBar.setOnTitleBarClickListener(new TitleBar.OnTitleBarClickListener() {
            @Override
            public void onBackImageClick() {
                Log.d(TAG, "onBackImageClick: ");
            }

            @Override
            public void onCenterTextClick() {
                Log.d(TAG, "onCenterTextClick: ");
            }

            @Override
            public void onRightTextClick() {
                Log.d(TAG, "onRightTextClick: ");
            }
        });
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {

    }
}
