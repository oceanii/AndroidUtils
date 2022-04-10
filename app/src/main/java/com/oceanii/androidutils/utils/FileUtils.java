package com.oceanii.androidutils.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by oceanii on 2021/11/26.
 */

public class FileUtils {
    private static final String TAG = "FileUtils";

    /*在文件夹路径后面智能增加"/"*/
    public static String addDirPathEndFileSeparator(String dirPath){
        if(dirPath != null && !dirPath.isEmpty()){
            int index = dirPath.lastIndexOf(File.separator);
            if(index != dirPath.length() - 1){
                return dirPath + File.separator;
            }
        }
        return dirPath;
    }

    /*在文件夹路径后面智能去掉"/"*/
    public static String cutDirPathEndFileSeparator(String dirPath){
        if(dirPath != null && !dirPath.isEmpty()){
            int index = dirPath.lastIndexOf(File.separator);
            if(index == dirPath.length() - 1){
                return dirPath.substring(0, index);
            }
        }
        return dirPath;
    }

    /*判断文件是否存在*/
    public static boolean isFileExist(String fileName){
        return new File(fileName).exists();
    }

    /*创建文件，支持是否自动创建父文件夹*/
    public static void createFile(String filaName, boolean isAutoCreateParentDir){
        try {
            File file = new File(filaName);
            if (isAutoCreateParentDir) {
                File parentDir = file.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }
            }
            //file.delete();
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*从文件读取数据到byte[]*/
    public static byte[] readFileToData(String fileName){
        File file = new File(fileName);
        if(!file.exists()){
            return null;
        }

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            if(fileInputStream.getChannel().size() == 0){
                return null;
            }
            byte[] buffer = new byte[fileInputStream.available()];
            Log.i(TAG, "readFileToData: " + fileInputStream.available());
            fileInputStream.read(buffer);
            return buffer;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            IOUtils.closeQuietly(fileInputStream);
        }
        return null;
    }

    /*将byte[]数据写入文件, 上一级目录必须存在，否则无法保存*/
    public static void writeDataToFile(String fileName, byte[] data){
        if(data == null){
            return;
        }

        FileOutputStream fileOutputStream = null;
        try {
            createFile(fileName, true);

            fileOutputStream = new FileOutputStream(fileName);
            fileOutputStream.write(data, 0, data.length);
            fileOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            IOUtils.closeQuietly(fileOutputStream);
        }
    }
}
