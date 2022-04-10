package com.oceanii.androidutils.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by oceanii on 2018/6/4.
 */

public class ToastUtils {
    private static Toast mToast = null;

    /*Toast显示*/
    public static void showShort(Context context, String message) {
        if (mToast == null) {
            mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(message);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }
}
