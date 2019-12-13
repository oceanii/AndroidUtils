package com.oceanii.androidutils.baseactivity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by oceanii on 2019/12/1.
 */

public abstract class BaseActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //将当前初始化的Activity加入栈中
        //ActivityManager.getInstance().addActivity(this);
        setContentView(initRootView());
        initViews();
        initTitle();
        initData();
        initListener();
    }


    //初始化根视图
    protected abstract int initRootView();
    //初始化视图组件
    protected abstract void initViews();
    //初始化顶部标题布局
    protected abstract void initTitle();
    //初始化数据
    protected abstract void initData();
    //视图组件监听事件处理
    protected abstract void initListener();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //结束Activity, 从栈中移除
        //Activity.getInstance().finishActivity(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
