package com.oceanii.androidutils.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by oceanii on 2021/11/27.
 */

public class IOUtils {
    /*关闭出错时，抛出异常*/
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                throw new RuntimeException("IOException Occurred", e);
            }
        }
    }

    /*关闭出错时，不做处理*/
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // Ignored
            }
        }
    }

}
