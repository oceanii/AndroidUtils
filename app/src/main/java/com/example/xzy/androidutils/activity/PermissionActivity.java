package com.example.xzy.androidutils.activity;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import com.example.xzy.androidutils.R;
import com.example.xzy.androidutils.baseactivity.BaseActivity;
import com.example.xzy.androidutils.baseactivity.TitleBar;
import com.example.xzy.androidutils.config.PermissionConfig;

import java.util.List;

public class PermissionActivity extends BaseActivity implements PermissionConfig.PermissionCallbacks{
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
        PermissionConfig.requestPermmissions(this, this, PermissionConfig.REQUEST_ALL_PERMISSTIONS_FLAG, PermissionConfig.mPermissions);
    }

    @Override
    protected void initListener() {

    }

    @Override
    public void onPermissionsAllHas(int requestCode) {
        Log.d(TAG, "onPermissionsAllHas: ");
    }

    @Override
    public void onPermissionGranted(int requestCode, @NonNull List<String> permissions) {
        Log.d(TAG, "onPermissionGranted: " + permissions.size());
    }

    @Override
    public void onPermissionDenied(int requestCode, @NonNull List<String> permissions) {
        Log.d(TAG, "onPermissionDenied: " + permissions.size());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionConfig.onRequestPermissionResult(this, requestCode, permissions, grantResults);
    }
}
