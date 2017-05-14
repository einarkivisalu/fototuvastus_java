package com.fototuvastus;


import org.opencv.core.Size;

import java.util.Arrays;

public class FaceFeature {
    public String featureName;
    public int count = 0;
    public Size size;
    public int[] positionXY;
    public int[] positionCenterXY;
    public double[] baseAxisOffsetXY;
    public boolean isFeatureValid = false;

    public FaceFeature(String featureName, int count, Size size, int[] positionXY, int[] positionCenterXY, double[] baseAxisOffsetXY, boolean isFeatureValid) {
        this.featureName = featureName;
        this.count = count;
        this.size = size;
        this.positionXY = positionXY;
        this.positionCenterXY = positionCenterXY;
        this.baseAxisOffsetXY = baseAxisOffsetXY;
        this.isFeatureValid = isFeatureValid;
    }

    @Override
    public String toString() {
        return "FaceFeature{" +
                "isFeatureValid=" + isFeatureValid +
                ", baseAxisOffsetXY=" + Arrays.toString(baseAxisOffsetXY) +
                ", positionCenterXY=" + Arrays.toString(positionCenterXY) +
                ", positionXY=" + Arrays.toString(positionXY) +
                ", size=" + size +
                ", count=" + count +
                ", featureName='" + featureName + '\'' +
                '}';
    }
}
