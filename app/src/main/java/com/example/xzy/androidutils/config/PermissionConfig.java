package com.example.xzy.androidutils.config;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态申请权限的配置类
 */

public class PermissionConfig {
    private static final String TAG = "PermissionConfig";

    /*private static Activity mActivity;  //静态变量一般不会释放，占用内存，尽量少使用

    public static void initConfig(Activity activity_splash){
        mActivity = activity_splash;

        checkAllPermission();

    }*/

    // 权限请求码
    private static final int REQUEST_ALL_PERMISSTIONS = 0x0001;

    // Andoird 6.0系统以上，需要动态申请的权限添加到该数组中
    private static String[] mPermissions = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    // 声明一个集合，用来存储用户拒绝授权的权限
    private static List<String> mPermissionDeniedList = new ArrayList<String>();

    // 动态申请权限初始化
    public static void initCheckAllPermission(Activity activity){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            for(int i = 0; i < mPermissions.length; i++){
                if(ContextCompat.checkSelfPermission(activity, mPermissions[i]) != PackageManager.PERMISSION_GRANTED){
                    mPermissionDeniedList.add(mPermissions[i]);
                }
            }

            // 如果List里的个数为0，说明权限都已经授予了
            if(!mPermissionDeniedList.isEmpty()){
                String[] permissions = mPermissionDeniedList.toArray(new String[mPermissionDeniedList.size()]);
                activity.requestPermissions(permissions, REQUEST_ALL_PERMISSTIONS);
            }

        }
    }

    // 权限请求回调函数
    // onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    public static void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return;
        }

        if(grantResults.length <= 0){
            return;
        }

        if(requestCode == REQUEST_ALL_PERMISSTIONS){
            for(int i = 0; i < grantResults.length; i++){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    // 判断被拒绝的权限是否被勾选了禁止后不再询问
                    boolean isShowRequestPermission = activity.shouldShowRequestPermissionRationale(permissions[i]);
                    Log.d(TAG, "onRequestPermissionsResult: " + isShowRequestPermission);
                    if(isShowRequestPermission){

                    }else{
                        // 禁止后不再询问
                    }
                }
            }
        }
    }
}
