package com.oceanii.androidutils.camera;

import android.hardware.camera2.CameraCharacteristics;

/**
 * Created by oceanii on 2022/3/10.
 */

public class CameraConfig {

    /*前后置摄像头ID*/
    public enum CameraID{
        ID_FRONT(CameraCharacteristics.LENS_FACING_FRONT),
        ID_BACK(CameraCharacteristics.LENS_FACING_BACK);

        private int id;
        CameraID(int id){
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    /*相机画面比例*/
    public enum AspectRatio {
        RATIO_FULL_SCREEN("full", 0, 0),
        RATIO_16_9("9:16", 16, 9),
        RATIO_4_3("3:4", 4, 3),
        RATIO_1_1("1:1", 1, 1);

        private String name;
        private int shortSide;
        private int longSide;

        AspectRatio(String name, int longSide, int shortSide) {
            this.name = name;
            this.shortSide = shortSide;
            this.longSide = longSide;
        }

        public String getName() {
            return name;
        }

        public int getLongSide() {
            return longSide;
        }

        public int getShortSide() {
            return shortSide;
        }
    }
}
