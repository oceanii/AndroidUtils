package com.oceanii.androidutils.utils;

/**
 * Created by oceanii on 2019/1/13.
 */

public class MathUtils {

    /*线性映射*/
    public static float linearMap(float input, float src, float dst){
        return input * dst / src ;
    }

    /*夹板函数*/
    public static float clamp(float srcData, float minData, float maxData){
        return Math.max(minData, Math.min(srcData, maxData));
    }
}
