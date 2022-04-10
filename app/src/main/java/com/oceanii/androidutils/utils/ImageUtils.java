package com.oceanii.androidutils.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by oceanii on 2018/4/19.
 */

public class ImageUtils {
    private static final String TAG = "ImageUtils";

    /**
     * 从assets的某一个文件绝对路径名获取bitmap
     * fileName: lookup/table.png
     * @param context
     * @param fileName
     * @return
     */
    public static Bitmap getBitmapFromAssets(Context context, String fileName) {
        Bitmap bmp = null;
        AssetManager assetManager = context.getResources().getAssets();
        try {
            InputStream is = assetManager.open(fileName);
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmp;
    }

    /**
     * 解码图片时，最大边小于MaxSize
     * @param filePath
     * @param MaxSize
     * @return
     */
    public static Bitmap decodeImageByResolution(String filePath, int MaxSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; //Options的第一个属性,设置为true的时候，不会完全的对图片进行解码操作,不会为其分配内存，只是获取图片的基本信息
        BitmapFactory.decodeFile(filePath, options);

        int realWidth = options.outWidth;
        int realHeight = options.outHeight;

        int tmpSize = realWidth > realHeight ? realWidth : realHeight;
        int scale = tmpSize > MaxSize ? Math.round(((float) tmpSize / (float) MaxSize)) : 1;

        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

        return bitmap;
    }

    /**
     * 解码图片时，图像的宽高比与最大宽高比进行比较，确定解码后的大小，占用内存比上面的少
     * @param filePath
     * @param MaxWidth
     * @param MaxHeight
     * @return
     */
    public static Bitmap decodeImageByResolution(String filePath, int MaxWidth, int MaxHeight) {
        FileInputStream fileInputStream = null;
        Bitmap resultBitmap = null;
        try {
            fileInputStream = new FileInputStream(filePath);
            BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inPremultiplied = false; //透明通道预乘
            options.inPreferredConfig = Bitmap.Config.ARGB_8888; //指定解码后的颜色格式
            options.inJustDecodeBounds = true; //Options的第一个属性,设置为true的时候，不会完全的对图片进行解码操作,不会为其分配内存，只是获取图片的基本信息

            BitmapFactory.decodeFileDescriptor(fileInputStream.getFD(), null, options);
            //BitmapFactory.decodeFile(filePath, options);

            int realWidth = options.outWidth;
            int realHeight = options.outHeight;

            float maxRatio = Math.round((float)MaxWidth / (float)MaxHeight);
            float realRatio = Math.round((float)realWidth / (float)realHeight);
            int scale = 1;
            if(maxRatio >= realRatio){
                //原图更细长,所以想要填充屏幕，需要以高为基准计算
                scale = realHeight > MaxHeight ? Math.round(((float) realHeight / (float) MaxHeight)) : 1;
            }else{
                //原图更宽扁,所以想要填充屏幕，需要以宽为基准计算
                scale = realWidth > MaxWidth ? Math.round(((float) realWidth / (float) MaxWidth)) : 1;
            }

            options.inSampleSize = scale;
            options.inJustDecodeBounds = false;
            resultBitmap = BitmapFactory.decodeFileDescriptor(fileInputStream.getFD(), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fileInputStream != null){
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //System.gc();
        return resultBitmap;
    }

    /**
     * 将Bitmap保存为png图片
     * @param fileName
     * @param bmp
     * /sdcard/JniOpenGLDemo/lookupTable.jpg = /storage/emulated/0/Download/lookupTable.jpg = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + lookupTable.jpg;
     */
    public static void saveBitmapToFileByAbsolutePath(String fileName, Bitmap bmp) {
        FileUtils.createFile(fileName, true);
        try {
            FileOutputStream out = new FileOutputStream(fileName);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
            //bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*通知媒体库更新*/
    public static void updateMediaScanner(Context context, String fileName){
        File file = new File(fileName);
        if(file.exists()){
            //MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), file.getName(), null); //把文件插入到系统图库，可以省略
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        }
    }

    /*bitmap镜像*/
    public static Bitmap convertBmp(Bitmap bmp) {
        Bitmap convertBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());// 创建一个新的和原图长度宽度一样的位图
        Canvas canvas = new Canvas(convertBmp);
        Matrix matrix = new Matrix();
        // m.postScale(1, -1);   //镜像垂直翻转
        matrix.postScale(-1, 1);   //镜像水平翻转
        // m.postRotate(-90);  //旋转-90度
        Bitmap newBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        canvas.drawBitmap(newBmp, new Rect(0, 0, newBmp.getWidth(), newBmp.getHeight()),new Rect(0, 0, bmp.getWidth(), bmp.getHeight()), null);
        return convertBmp;
    }

    /**
     * 重新设置图片的大小
     * 有4种情况：
     * 前两种--（isRatio不起作用）如果width为0时，则用height计算缩放倍数，宽高同时缩放，反之亦然
     * 第三种--isRatio为true, width和height都不为0, 则仿照格式工厂重置图片大小
     * 第四种--isRatio为false, width和height都不为0, 则不按原图的宽高比例直接缩放图片
     * @param originBitmap
     * @param width
     * @param height
     * @param isRatio
     * @return
     */
    public static Bitmap scaleBitmap(Bitmap originBitmap, int width, int height, boolean isRatio){
        float horizonScale = 1.0f;
        float verticalScale = 1.0f;
        Matrix matrix = new Matrix();
        if(width != 0 || height != 0){
            if(width == 0){
                verticalScale = ((float) height) / originBitmap.getHeight();
                horizonScale = verticalScale;
            }else if(height == 0){
                horizonScale = ((float) width) / originBitmap.getWidth();
                verticalScale = horizonScale;
            }else{
                if(isRatio){
                    verticalScale = ((float) height) / originBitmap.getHeight();
                    horizonScale = ((float) width) / originBitmap.getWidth();
                    float resizeRatio = verticalScale < horizonScale ? verticalScale : horizonScale;
                    verticalScale = resizeRatio;
                    horizonScale = resizeRatio;
                }else{
                    verticalScale = ((float) height) / originBitmap.getHeight();
                    horizonScale = ((float) width) / originBitmap.getWidth();
                }
            }
        }
        matrix.postScale(horizonScale, verticalScale); //matrix.setScale(xScale, yScale);

        Bitmap resizeBitmap = Bitmap.createBitmap(originBitmap, 0, 0, originBitmap.getWidth(), originBitmap.getHeight(), matrix, true);
        return resizeBitmap;
    }

    /*将view转化为bitmap*/
    public static Bitmap getBitmapFromView2(View view){
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //利用bitmap生成画布
        Canvas canvas = new Canvas(bitmap);
        //把view中的内容绘制在画布上
        view.draw(canvas);
        return bitmap;
    }

    /*将view转化为bitmap*/
    public static Bitmap getBitmapFromView(View view){
        view.setBackgroundColor(Color.GRAY);
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0, 0,view.getWidth(), view.getHeight());
        view.destroyDrawingCache();
        view.setBackgroundColor(Color.WHITE);  //此处再将view的颜色设置原来的颜色
        return bitmap;
    }

    /**
     * 动态高斯模糊
     * @param context
     * @param bitmap
     * @param radius
     * @return
     */
    public static Bitmap gaussBlurBitmap(Context context, Bitmap bitmap, float radius){
        Bitmap output = Bitmap.createBitmap(bitmap);  // 创建输出图片
        RenderScript rs = RenderScript.create(context);  // 构建一个RenderScript对象
        ScriptIntrinsicBlur gaussianBlue = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));  // 创建高斯模糊脚本
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);  // 创建用于输入的脚本类型
        Allocation allOut = Allocation.createFromBitmap(rs, output);  // 创建用于输出的脚本类型
        gaussianBlue.setRadius(radius);  // 设置模糊半径，范围0f <radius <= 25f
        gaussianBlue.setInput(allIn);  // 设置输入脚本类型
        gaussianBlue.forEach(allOut);  // 执行高斯模糊算法，并将结果填入输出脚本类型中
        allOut.copyTo(output);  // 将输出内存编码为Bitmap，图片大小必须注意
        if(Build.VERSION.SDK_INT >= 23){
            rs.releaseAllContexts();
        }else{
            rs.destroy();  // 关闭RenderScript对象，API>=23则使用rs.releaseAllContexts()
        }
        return output;
    }
}