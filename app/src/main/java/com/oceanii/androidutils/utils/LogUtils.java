package com.oceanii.androidutils.utils;

import android.util.Log;

/**
 * Created by oceanii on 2021/11/26.
 */

public class LogUtils {
    public static final String TAG_TITLE = "OceanII_";

    private static final boolean DEBUG_ENABLE = true;
    private static final boolean INFO_ENABLE = true;
    private static final boolean ERROR_ENABLE = true;

    public static void d(String tag, String msg){
        if(DEBUG_ENABLE){
            Log.d(TAG_TITLE + tag, msg);
        }
    }

    public static void i(String tag, String msg){
        if(INFO_ENABLE){
            Log.i(TAG_TITLE + tag, msg);
        }
    }

    public static void e(String tag, String msg){
        if(ERROR_ENABLE){
            Log.e(TAG_TITLE + tag, msg);
        }
    }
}
