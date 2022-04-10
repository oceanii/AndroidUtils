package com.oceanii.androidutils.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by oceanii on 2021/11/26.
 */

public class AssetsUtils {
    private static final String TAG = "AssetsUtils";

    public static boolean copyAssetsFilesToSdDir(Context context, String assetInPath, String sdOutPath, boolean isCoverFileMode){
        LogUtils.d(TAG, "copyAssetsFilesToSdDir begin");
        boolean res = false;
        try {
            AssetManager assets = context.getAssets();
            String fileNames[] = assets.list(assetInPath);
            if(fileNames != null && fileNames.length > 0){
                res = copyAssets(assets, assetInPath, sdOutPath, isCoverFileMode);
            }else{
                LogUtils.e(TAG, "asset path is no exist dir or is single file, no supported");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogUtils.d(TAG, "copyAssetsFilesToSdDir end res: " + res);
        return res;
    }

    /**
     * Assets目录或文件拷贝到SD卡目录
     * @param assets
     * @param assetInPath  //必须是目录，带或者不带/都可以
     * @param sdOutPath    最后带或者不带/都默认为是文件夹，文件夹会自动创建
     * @return
     */
    private static boolean copyAssets(AssetManager assets, String assetInPath, String sdOutPath, boolean isCoverFileMode) {
        //LogUtils.d(TAG, "copyAssetsFilesToSd: inPath:" + assetInPath + " outPath:" + sdOutPath);
        try {
            String fileNames[] = assets.list(assetInPath);// 获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {// 如果是目录
                File outDir = new File(sdOutPath);
                if(outDir.isFile()){
                    outDir.delete();
                }
                if(!outDir.exists()){
                    outDir.mkdirs();
                }
                for (String fileName : fileNames) {
                    String inPath = FileUtils.cutDirPathEndFileSeparator(assetInPath) + File.separator + fileName;
                    String outPath = FileUtils.cutDirPathEndFileSeparator(sdOutPath) + File.separator + fileName;
                    copyAssets(assets, inPath, outPath, isCoverFileMode);
                }
                return true;
            } else {// 如果是文件
                InputStream is = assets.open(assetInPath);
                File outFile = new File(sdOutPath);

                boolean isNeedCopyFile = false;
                if(isCoverFileMode){
                    if(outFile.exists()){
                        outFile.delete();
                    }
                    outFile.createNewFile();
                    isNeedCopyFile = true;
                }else{
                    if(outFile.exists()){
                        isNeedCopyFile = false;
                    }else{
                        outFile.createNewFile();
                        isNeedCopyFile = true;
                    }
                }

                if(isNeedCopyFile){
                    FileOutputStream fos = new FileOutputStream(outFile);
                    byte[] buffer = new byte[1024];
                    int byteCount = 0;
                    while ((byteCount = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, byteCount);
                    }
                    fos.flush();
                    fos.close();
                }
                is.close();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 从assets的某一个文件夹获取里面所有的文件名
     * return assets: lookup/table.png
     * 例如：folderName + File.separator + imageName
     * @param context
     * @param folderName
     * @return
     */
    public static List<String> getFileNameListFromAssetsFolder(Context context, String folderName){
        String[] imageNameArray = new String[0];
        List<String> imagePathList = new ArrayList<>();
        try {
            imageNameArray = context.getAssets().list(folderName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (imageNameArray != null && imageNameArray.length > 0) {

            for (String imageName : imageNameArray) {
                imagePathList.add(folderName + File.separator + imageName);
            }
        }
        return imagePathList;
    }
}
