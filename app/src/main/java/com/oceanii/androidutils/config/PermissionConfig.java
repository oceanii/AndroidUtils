package com.oceanii.androidutils.config;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by oceanii on 2019/12/7.
 */

public class PermissionConfig {

    // Andoird 6.0系统以上，需要动态申请的权限添加到该数组中
    public static String[] mPermissions = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    public static int mRequestCode;
    public static int REQUEST_ALL_PERMISSTIONS_FLAG = 0X0001;
    public static PermissionCallbacks mPermissionCallbacks;

    public interface PermissionCallbacks extends ActivityCompat.OnRequestPermissionsResultCallback{
        void onPermissionsAllHas(int requestCode);
        void onPermissionGranted(int requestCode, @NonNull List<String> permissions);
        void onPermissionDenied(int requestCode, @NonNull List<String> permissions);

        @Override
        void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
    }

    private static boolean hasPermissions(@NonNull Context context, @NonNull String[] permissions){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return true;
        }

        if(context == null){
            throw new IllegalArgumentException("Can't check permissions for null context");
        }

        for(int i = 0; i < permissions.length; i++){
            if(ContextCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }

        return true;
    }

    public static void requestPermmissions(@NonNull Activity activity, @NonNull PermissionConfig.PermissionCallbacks permissionCallbacks, int requestCode, @NonNull String[] permissions){
        mRequestCode = requestCode;
        mPermissionCallbacks = permissionCallbacks;

        if(hasPermissions(activity, permissions)){
            if(mPermissionCallbacks != null){
                mPermissionCallbacks.onPermissionsAllHas(requestCode);
            }
        }else {
            activity.requestPermissions(permissions, REQUEST_ALL_PERMISSTIONS_FLAG);
        }
    }

    public static void onRequestPermissionResult(@NonNull Activity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(mRequestCode == requestCode){
            List<String> deniedPermissions = new ArrayList<>();
            List<String> grantedPermissions = new ArrayList<>();

            for(int i = 0; i < grantResults.length; i++){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    // 判断被拒绝的权限是否被勾选了禁止后不再询问
                    boolean isShowRequestPermission = activity.shouldShowRequestPermissionRationale(permissions[i]);
                    if(isShowRequestPermission){

                    }else{
                        // 禁止后不再询问
                    }
                    deniedPermissions.add(permissions[i]);
                }else {
                    grantedPermissions.add(permissions[i]);
                }
            }

            if(mPermissionCallbacks != null){
                mPermissionCallbacks.onPermissionDenied(mRequestCode, deniedPermissions);
                mPermissionCallbacks.onPermissionGranted(mRequestCode, grantedPermissions);
            }
        }
    }
}
